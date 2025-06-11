const Match = require("../models/Match")
const Message = require("../models/Message")
const User = require("../models/User")
const { validationResult } = require("express-validator")

class MatchController {
  // Get user's matches
  async getMatches(req, res) {
    try {
      const { page = 1, limit = 20 } = req.query
      const skip = (page - 1) * limit
      const userId = req.userId

      const matches = await Match.find({
        $or: [{ user1: userId }, { user2: userId }],
        isActive: true,
      })
        .populate("user1 user2", "name avatar age isOnline lastSeen")
        .sort({ lastMessageAt: -1 })
        .skip(skip)
        .limit(Number.parseInt(limit))

      // Get last message for each match
      const matchesWithLastMessage = await Promise.all(
        matches.map(async (match) => {
          const lastMessage = await Message.findOne({ matchId: match._id })
            .sort({ createdAt: -1 })
            .populate("fromUser", "name")

          const otherUser = match.user1._id.toString() === userId ? match.user2 : match.user1

          return {
            ...match.toJSON(),
            otherUser,
            lastMessage,
          }
        }),
      )

      const total = await Match.countDocuments({
        $or: [{ user1: userId }, { user2: userId }],
        isActive: true,
      })

      res.json({
        matches: matchesWithLastMessage,
        pagination: {
          page: Number.parseInt(page),
          limit: Number.parseInt(limit),
          total,
          pages: Math.ceil(total / limit),
        },
      })
    } catch (error) {
      console.error("Get matches error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get match details
  async getMatchDetails(req, res) {
    try {
      const { matchId } = req.params
      const userId = req.userId

      const match = await Match.findOne({
        _id: matchId,
        $or: [{ user1: userId }, { user2: userId }],
        isActive: true,
      }).populate("user1 user2", "name avatar age bio photos isOnline lastSeen")

      if (!match) {
        return res.status(404).json({ message: "Match not found" })
      }

      const otherUser = match.user1._id.toString() === userId ? match.user2 : match.user1

      // Get message count
      const messageCount = await Message.countDocuments({ matchId })

      // Get unread message count
      const unreadCount = await Message.countDocuments({
        matchId,
        fromUser: { $ne: userId },
        isRead: false,
      })

      res.json({
        match: {
          ...match.toJSON(),
          otherUser,
          messageCount,
          unreadCount,
        },
      })
    } catch (error) {
      console.error("Get match details error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Unmatch (deactivate match)
  async unmatch(req, res) {
    try {
      const { matchId } = req.params
      const userId = req.userId

      const match = await Match.findOne({
        _id: matchId,
        $or: [{ user1: userId }, { user2: userId }],
        isActive: true,
      })

      if (!match) {
        return res.status(404).json({ message: "Match not found" })
      }

      // Deactivate the match
      match.isActive = false
      await match.save()

      res.json({ message: "Unmatched successfully" })
    } catch (error) {
      console.error("Unmatch error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get match statistics
  async getMatchStats(req, res) {
    try {
      const userId = req.userId

      const totalMatches = await Match.countDocuments({
        $or: [{ user1: userId }, { user2: userId }],
        isActive: true,
      })

      const activeChats = await Match.countDocuments({
        $or: [{ user1: userId }, { user2: userId }],
        isActive: true,
        lastMessageAt: { $gte: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000) }, // Last 7 days
      })

      const totalMessages = await Message.countDocuments({
        matchId: {
          $in: await Match.find({
            $or: [{ user1: userId }, { user2: userId }],
            isActive: true,
          }).distinct("_id"),
        },
        fromUser: userId,
      })

      res.json({
        totalMatches,
        activeChats,
        totalMessages,
      })
    } catch (error) {
      console.error("Get match stats error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }
}

module.exports = new MatchController()
