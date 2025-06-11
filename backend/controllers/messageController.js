const Message = require("../models/Message")
const Match = require("../models/Match")
const User = require("../models/User")
const contentModerationService = require("../services/contentModerationService")
const notificationService = require("../services/notificationService")
const { validationResult } = require("express-validator")

class MessageController {
  // Get messages for a match
  async getMessages(req, res) {
    try {
      const { matchId } = req.params
      const { page = 1, limit = 50 } = req.query
      const skip = (page - 1) * limit
      const userId = req.userId

      // Verify user is part of this match
      const match = await Match.findOne({
        _id: matchId,
        $or: [{ user1: userId }, { user2: userId }],
        isActive: true,
      })

      if (!match) {
        return res.status(404).json({ message: "Match not found" })
      }

      const messages = await Message.find({ matchId })
        .populate("fromUser", "name avatar")
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(Number.parseInt(limit))

      const total = await Message.countDocuments({ matchId })

      res.json({
        messages: messages.reverse(), // Reverse to show oldest first
        pagination: {
          page: Number.parseInt(page),
          limit: Number.parseInt(limit),
          total,
          pages: Math.ceil(total / limit),
        },
      })
    } catch (error) {
      console.error("Get messages error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Send a message
  async sendMessage(req, res) {
    try {
      const errors = validationResult(req)
      if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() })
      }

      const { matchId } = req.params
      const { content, messageType = "text" } = req.body
      const userId = req.userId

      // Verify user is part of this match
      const match = await Match.findOne({
        _id: matchId,
        $or: [{ user1: userId }, { user2: userId }],
        isActive: true,
      }).populate("user1 user2", "name fcmToken")

      if (!match) {
        return res.status(404).json({ message: "Match not found" })
      }

      // Content moderation for text messages
      let moderationResult = { result: "approved" }
      if (messageType === "text") {
        moderationResult = await contentModerationService.moderateText(content, "message", userId)

        if (moderationResult.result === "rejected") {
          return res.status(400).json({
            message: "Message contains inappropriate content",
            moderation: moderationResult,
          })
        }
      }

      // Create message
      const message = new Message({
        matchId,
        fromUser: userId,
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

      // Send push notification to the other user
      const otherUser = match.user1._id.toString() === userId ? match.user2 : match.user1
      const senderName = match.user1._id.toString() === userId ? match.user1.name : match.user2.name

      if (otherUser.fcmToken) {
        const messagePreview =
          messageType === "text"
            ? content.substring(0, 50) + (content.length > 50 ? "..." : "")
            : `Sent a ${messageType}`

        await notificationService.sendNotification(otherUser._id, {
          title: `New message from ${senderName}`,
          message: messagePreview,
          type: "message",
          data: { matchId, messageId: message._id },
        })
      }

      res.status(201).json({
        message: "Message sent successfully",
        data: message,
      })
    } catch (error) {
      console.error("Send message error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Mark messages as read
  async markMessagesAsRead(req, res) {
    try {
      const { matchId } = req.params
      const userId = req.userId

      // Verify user is part of this match
      const match = await Match.findOne({
        _id: matchId,
        $or: [{ user1: userId }, { user2: userId }],
        isActive: true,
      })

      if (!match) {
        return res.status(404).json({ message: "Match not found" })
      }

      // Mark all unread messages from other user as read
      const result = await Message.updateMany(
        {
          matchId,
          fromUser: { $ne: userId },
          isRead: false,
        },
        {
          isRead: true,
          readAt: new Date(),
        },
      )

      res.json({
        message: "Messages marked as read",
        modifiedCount: result.modifiedCount,
      })
    } catch (error) {
      console.error("Mark messages as read error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Delete a message
  async deleteMessage(req, res) {
    try {
      const { messageId } = req.params
      const userId = req.userId

      const message = await Message.findOne({
        _id: messageId,
        fromUser: userId,
      })

      if (!message) {
        return res.status(404).json({ message: "Message not found or not authorized" })
      }

      // Soft delete - mark as deleted instead of removing
      message.content = "This message was deleted"
      message.messageType = "deleted"
      await message.save()

      res.json({ message: "Message deleted successfully" })
    } catch (error) {
      console.error("Delete message error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get message statistics
  async getMessageStats(req, res) {
    try {
      const { matchId } = req.params
      const userId = req.userId

      // Verify user is part of this match
      const match = await Match.findOne({
        _id: matchId,
        $or: [{ user1: userId }, { user2: userId }],
        isActive: true,
      })

      if (!match) {
        return res.status(404).json({ message: "Match not found" })
      }

      const totalMessages = await Message.countDocuments({ matchId })
      const myMessages = await Message.countDocuments({ matchId, fromUser: userId })
      const unreadMessages = await Message.countDocuments({
        matchId,
        fromUser: { $ne: userId },
        isRead: false,
      })

      res.json({
        totalMessages,
        myMessages,
        theirMessages: totalMessages - myMessages,
        unreadMessages,
      })
    } catch (error) {
      console.error("Get message stats error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }
}

module.exports = new MessageController()
