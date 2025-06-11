const ContentModerationLog = require("../models/ContentModerationLog")
const User = require("../models/User")
const contentModerationService = require("../services/contentModerationService")
const { validationResult } = require("express-validator")

class ContentModerationController {
  // Get moderation logs for current user
  async getMyModerationLogs(req, res) {
    try {
      const { page = 1, limit = 20, contentType, result } = req.query
      const skip = (page - 1) * limit
      const userId = req.userId

      const query = { userId }
      if (contentType) {
        query.contentType = contentType
      }
      if (result) {
        query.result = result
      }

      const logs = await ContentModerationLog.find(query)
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(Number.parseInt(limit))

      const total = await ContentModerationLog.countDocuments(query)

      res.json({
        logs,
        pagination: {
          page: Number.parseInt(page),
          limit: Number.parseInt(limit),
          total,
          pages: Math.ceil(total / limit),
        },
      })
    } catch (error) {
      console.error("Get moderation logs error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Moderate text content manually
  async moderateText(req, res) {
    try {
      const errors = validationResult(req)
      if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() })
      }

      const { text, contentType } = req.body
      const userId = req.userId

      const result = await contentModerationService.moderateText(text, contentType, userId)

      res.json({
        message: "Content moderated successfully",
        moderation: result,
      })
    } catch (error) {
      console.error("Moderate text error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Moderate image content manually
  async moderateImage(req, res) {
    try {
      const errors = validationResult(req)
      if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() })
      }

      const { imageUrl, contentType } = req.body
      const userId = req.userId

      const result = await contentModerationService.moderateImage(imageUrl, contentType, userId)

      res.json({
        message: "Image moderated successfully",
        moderation: result,
      })
    } catch (error) {
      console.error("Moderate image error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get moderation statistics
  async getModerationStats(req, res) {
    try {
      const userId = req.userId

      const totalChecks = await ContentModerationLog.countDocuments({ userId })
      const approved = await ContentModerationLog.countDocuments({
        userId,
        result: "approved",
      })
      const rejected = await ContentModerationLog.countDocuments({
        userId,
        result: "rejected",
      })
      const flagged = await ContentModerationLog.countDocuments({
        userId,
        result: "flagged",
      })
      const requiresReview = await ContentModerationLog.countDocuments({
        userId,
        result: "requires_review",
      })

      // Get breakdown by content type
      const contentTypeStats = await ContentModerationLog.aggregate([
        { $match: { userId: userId } },
        {
          $group: {
            _id: "$contentType",
            count: { $sum: 1 },
            approved: {
              $sum: { $cond: [{ $eq: ["$result", "approved"] }, 1, 0] },
            },
            rejected: {
              $sum: { $cond: [{ $eq: ["$result", "rejected"] }, 1, 0] },
            },
          },
        },
      ])

      res.json({
        totalChecks,
        approved,
        rejected,
        flagged,
        requiresReview,
        contentTypeStats,
      })
    } catch (error) {
      console.error("Get moderation stats error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get flagged content that needs review
  async getFlaggedContent(req, res) {
    try {
      const { page = 1, limit = 20 } = req.query
      const skip = (page - 1) * limit

      // This would typically be admin-only, but for demo purposes
      const flaggedContent = await ContentModerationLog.find({
        result: { $in: ["flagged", "requires_review"] },
      })
        .populate("userId", "name email")
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(Number.parseInt(limit))

      const total = await ContentModerationLog.countDocuments({
        result: { $in: ["flagged", "requires_review"] },
      })

      res.json({
        flaggedContent,
        pagination: {
          page: Number.parseInt(page),
          limit: Number.parseInt(limit),
          total,
          pages: Math.ceil(total / limit),
        },
      })
    } catch (error) {
      console.error("Get flagged content error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Review flagged content (admin function)
  async reviewContent(req, res) {
    try {
      const { logId } = req.params
      const { decision, notes } = req.body // decision: 'approve' | 'reject' | 'ban_user'

      const log = await ContentModerationLog.findById(logId).populate("userId")

      if (!log) {
        return res.status(404).json({ message: "Moderation log not found" })
      }

      // Update the log
      log.result = decision === "approve" ? "approved" : "rejected"
      log.reviewedBy = "human"
      await log.save()

      // Take action based on decision
      if (decision === "ban_user") {
        const user = await User.findById(log.userId._id)
        if (user) {
          user.isBanned = true
          user.banReason = `Content violation: ${log.contentType}`
          await user.save()
        }
      }

      res.json({
        message: "Content reviewed successfully",
        decision,
        log,
      })
    } catch (error) {
      console.error("Review content error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }
}

module.exports = new ContentModerationController()
