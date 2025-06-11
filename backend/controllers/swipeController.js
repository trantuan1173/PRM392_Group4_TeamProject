const Swipe = require("../models/Swipe")
const Match = require("../models/Match")
const User = require("../models/User")
const notificationService = require("../services/notificationService")
const { validationResult } = require("express-validator")

class SwipeController {
  // Create a swipe (like or pass)
  async createSwipe(req, res) {
    try {
      const errors = validationResult(req)
      if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() })
      }

      const { toUserId, isLike } = req.body
      const fromUserId = req.userId

      // Check if user is trying to swipe themselves
      if (fromUserId === toUserId) {
        return res.status(400).json({ message: "Cannot swipe on yourself" })
      }

      // Check if target user exists and is not banned
      const targetUser = await User.findById(toUserId)
      if (!targetUser || targetUser.isBanned) {
        return res.status(404).json({ message: "User not found" })
      }

      // Check if swipe already exists
      const existingSwipe = await Swipe.findOne({
        fromUser: fromUserId,
        toUser: toUserId,
      })

      if (existingSwipe) {
        return res.status(400).json({ message: "Already swiped on this user" })
      }

      // Create the swipe
      const swipe = new Swipe({
        fromUser: fromUserId,
        toUser: toUserId,
        isLike,
      })

      await swipe.save()

      let isMatch = false
      let matchId = null

      // If it's a like, check for mutual like (match)
      if (isLike) {
        const mutualSwipe = await Swipe.findOne({
          fromUser: toUserId,
          toUser: fromUserId,
          isLike: true,
        })

        if (mutualSwipe) {
          // Create a match
          const match = new Match({
            user1: fromUserId < toUserId ? fromUserId : toUserId,
            user2: fromUserId < toUserId ? toUserId : fromUserId,
          })

          await match.save()
          isMatch = true
          matchId = match._id

          // Send match notifications to both users
          const currentUser = await User.findById(fromUserId)

          await notificationService.sendNotification(toUserId, {
            title: "New Match! 💕",
            message: `You matched with ${currentUser.name}!`,
            type: "match",
            data: { matchId, userId: fromUserId },
          })

          await notificationService.sendNotification(fromUserId, {
            title: "New Match! 💕",
            message: `You matched with ${targetUser.name}!`,
            type: "match",
            data: { matchId, userId: toUserId },
          })
        } else {
          // Send like notification
          const currentUser = await User.findById(fromUserId)
          await notificationService.sendNotification(toUserId, {
            title: "Someone likes you! 💖",
            message: `${currentUser.name} liked your profile`,
            type: "like",
            data: { userId: fromUserId },
          })
        }
      }

      res.status(201).json({
        message: "Swipe recorded successfully",
        isMatch,
        matchId,
      })
    } catch (error) {
      console.error("Swipe error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get swipe history
  async getSwipeHistory(req, res) {
    try {
      const { page = 1, limit = 20 } = req.query
      const skip = (page - 1) * limit

      const swipes = await Swipe.find({ fromUser: req.userId })
        .populate("toUser", "name avatar age")
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(Number.parseInt(limit))

      const total = await Swipe.countDocuments({ fromUser: req.userId })

      res.json({
        swipes,
        pagination: {
          page: Number.parseInt(page),
          limit: Number.parseInt(limit),
          total,
          pages: Math.ceil(total / limit),
        },
      })
    } catch (error) {
      console.error("Get swipe history error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get users who liked current user
  async getLikes(req, res) {
    try {
      const { page = 1, limit = 20 } = req.query
      const skip = (page - 1) * limit

      const likes = await Swipe.find({
        toUser: req.userId,
        isLike: true,
      })
        .populate("fromUser", "name avatar age bio location")
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(Number.parseInt(limit))

      const total = await Swipe.countDocuments({
        toUser: req.userId,
        isLike: true,
      })

      res.json({
        likes,
        pagination: {
          page: Number.parseInt(page),
          limit: Number.parseInt(limit),
          total,
          pages: Math.ceil(total / limit),
        },
      })
    } catch (error) {
      console.error("Get likes error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }
}

module.exports = new SwipeController()
