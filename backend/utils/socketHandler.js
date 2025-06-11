const jwt = require("jsonwebtoken")
const User = require("../models/User")
const Message = require("../models/Message")
const Match = require("../models/Match")
const contentModerationService = require("../services/contentModerationService")
const notificationService = require("../services/notificationService")

const socketHandler = (io) => {
  // Socket authentication middleware
  io.use(async (socket, next) => {
    try {
      const token = socket.handshake.auth.token
      if (!token) {
        return next(new Error("Authentication error"))
      }

      const decoded = jwt.verify(token, process.env.JWT_SECRET)
      const user = await User.findById(decoded.userId)

      if (!user || user.isBanned) {
        return next(new Error("Authentication error"))
      }

      socket.userId = decoded.userId
      socket.user = user
      next()
    } catch (error) {
      next(new Error("Authentication error"))
    }
  })

  io.on("connection", (socket) => {
    console.log(`User ${socket.userId} connected`)

    // Update user online status
    User.findByIdAndUpdate(socket.userId, {
      isOnline: true,
      lastSeen: new Date(),
    }).exec()

    // Join user to their personal room for notifications
    socket.join(`user_${socket.userId}`)

    // Handle joining match rooms for chat
    socket.on("join_match", async (matchId) => {
      try {
        // Verify user is part of this match
        const match = await Match.findOne({
          _id: matchId,
          $or: [{ user1: socket.userId }, { user2: socket.userId }],
          isActive: true,
        })

        if (match) {
          socket.join(`match_${matchId}`)
          socket.currentMatch = matchId
          console.log(`User ${socket.userId} joined match ${matchId}`)
        }
      } catch (error) {
        console.error("Error joining match:", error)
      }
    })

    // Handle sending messages
    socket.on("send_message", async (data) => {
      try {
        const { matchId, content, messageType = "text" } = data

        // Verify user is part of this match
        const match = await Match.findOne({
          _id: matchId,
          $or: [{ user1: socket.userId }, { user2: socket.userId }],
          isActive: true,
        })

        if (!match) {
          socket.emit("error", { message: "Match not found or inactive" })
          return
        }

        // Content moderation for text messages
        let moderationResult = { result: "approved" }
        if (messageType === "text") {
          moderationResult = await contentModerationService.moderateText(content, "message", socket.userId)

          if (moderationResult.result === "rejected") {
            socket.emit("message_rejected", {
              reason: "Message contains inappropriate content",
            })
            return
          }
        }

        // Create message
        const message = new Message({
          matchId,
          fromUser: socket.userId,
          content,
          messageType,
          isModerated: moderationResult.result !== "approved",
          moderationResult: moderationResult.result,
        })

        await message.save()

        // Populate sender info
        await message.populate("fromUser", "name avatar")

        // Update match last message time
        match.lastMessageAt = new Date()
        await match.save()

        // Emit message to match room
        io.to(`match_${matchId}`).emit("new_message", message)

        // Send push notification to the other user
        const otherUserId = match.user1.toString() === socket.userId ? match.user2 : match.user1

        const otherUser = await User.findById(otherUserId)
        if (otherUser && !otherUser.isOnline) {
          const messagePreview =
            messageType === "text"
              ? content.substring(0, 50) + (content.length > 50 ? "..." : "")
              : `Sent a ${messageType}`

          await notificationService.sendNotification(otherUserId, {
            title: `New message from ${socket.user.name}`,
            message: messagePreview,
            type: "message",
            data: { matchId, messageId: message._id },
          })
        }
      } catch (error) {
        console.error("Error sending message:", error)
        socket.emit("error", { message: "Failed to send message" })
      }
    })

    // Handle typing indicators
    socket.on("typing_start", (matchId) => {
      socket.to(`match_${matchId}`).emit("user_typing", {
        userId: socket.userId,
        userName: socket.user.name,
      })
    })

    socket.on("typing_stop", (matchId) => {
      socket.to(`match_${matchId}`).emit("user_stopped_typing", {
        userId: socket.userId,
      })
    })

    // Handle message read receipts
    socket.on("mark_messages_read", async (matchId) => {
      try {
        await Message.updateMany(
          {
            matchId,
            fromUser: { $ne: socket.userId },
            isRead: false,
          },
          {
            isRead: true,
            readAt: new Date(),
          },
        )

        socket.to(`match_${matchId}`).emit("messages_read", {
          userId: socket.userId,
          matchId,
        })
      } catch (error) {
        console.error("Error marking messages as read:", error)
      }
    })

    // Handle disconnect
    socket.on("disconnect", () => {
      console.log(`User ${socket.userId} disconnected`)

      // Update user offline status
      User.findByIdAndUpdate(socket.userId, {
        isOnline: false,
        lastSeen: new Date(),
      }).exec()
    })
  })
}

module.exports = socketHandler
