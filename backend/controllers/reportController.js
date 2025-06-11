const Report = require("../models/Report")
const User = require("../models/User")
const ContentModerationLog = require("../models/ContentModerationLog")
const { validationResult } = require("express-validator")

class ReportController {
  // Create a report
  async createReport(req, res) {
    try {
      const errors = validationResult(req)
      if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() })
      }

      const { reportedUserId, reason, description, evidence } = req.body
      const reporterId = req.userId

      // Check if reported user exists
      const reportedUser = await User.findById(reportedUserId)
      if (!reportedUser) {
        return res.status(404).json({ message: "Reported user not found" })
      }

      // Check if user is trying to report themselves
      if (reporterId === reportedUserId) {
        return res.status(400).json({ message: "Cannot report yourself" })
      }

      // Check if user has already reported this user for the same reason
      const existingReport = await Report.findOne({
        reporter: reporterId,
        reportedUser: reportedUserId,
        reason,
        status: { $in: ["pending", "investigating"] },
      })

      if (existingReport) {
        return res.status(400).json({ message: "You have already reported this user for this reason" })
      }

      // Create the report
      const report = new Report({
        reporter: reporterId,
        reportedUser: reportedUserId,
        reason,
        description,
        evidence: evidence || [],
      })

      await report.save()

      // Auto-ban for certain severe violations
      if (reason === "underage" || reason === "harassment") {
        await this.autoModerationAction(reportedUserId, reason)
      }

      res.status(201).json({
        message: "Report submitted successfully",
        reportId: report._id,
      })
    } catch (error) {
      console.error("Create report error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get user's reports
  async getMyReports(req, res) {
    try {
      const { page = 1, limit = 20, status } = req.query
      const skip = (page - 1) * limit
      const reporterId = req.userId

      const query = { reporter: reporterId }
      if (status) {
        query.status = status
      }

      const reports = await Report.find(query)
        .populate("reportedUser", "name avatar")
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(Number.parseInt(limit))

      const total = await Report.countDocuments(query)

      res.json({
        reports,
        pagination: {
          page: Number.parseInt(page),
          limit: Number.parseInt(limit),
          total,
          pages: Math.ceil(total / limit),
        },
      })
    } catch (error) {
      console.error("Get my reports error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get report details (admin only - for now, any user can view their own reports)
  async getReportDetails(req, res) {
    try {
      const { reportId } = req.params
      const userId = req.userId

      const report = await Report.findOne({
        _id: reportId,
        reporter: userId, // Only allow users to view their own reports
      })
        .populate("reporter", "name avatar")
        .populate("reportedUser", "name avatar")
        .populate("reviewedBy", "name")

      if (!report) {
        return res.status(404).json({ message: "Report not found" })
      }

      res.json({ report })
    } catch (error) {
      console.error("Get report details error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Auto-moderation action based on report
  async autoModerationAction(userId, reason) {
    try {
      const user = await User.findById(userId)
      if (!user) return

      let action = "none"
      let banReason = ""

      switch (reason) {
        case "underage":
          action = "permanent_ban"
          banReason = "Reported as underage"
          break
        case "harassment":
          action = "temporary_ban"
          banReason = "Reported for harassment"
          break
        case "inappropriate_photos":
          action = "warning"
          banReason = "Inappropriate profile photos"
          break
        default:
          action = "none"
      }

      if (action === "permanent_ban" || action === "temporary_ban") {
        user.isBanned = true
        user.banReason = banReason
        await user.save()
      }

      // Log the moderation action
      const log = new ContentModerationLog({
        userId,
        contentType: "profile",
        contentValue: `Report: ${reason}`,
        result: action === "none" ? "flagged" : "rejected",
        flags: [reason],
        reviewedBy: "ai",
      })

      await log.save()
    } catch (error) {
      console.error("Auto-moderation action error:", error)
    }
  }

  // Get report statistics
  async getReportStats(req, res) {
    try {
      const userId = req.userId

      const totalReports = await Report.countDocuments({ reporter: userId })
      const pendingReports = await Report.countDocuments({
        reporter: userId,
        status: "pending",
      })
      const resolvedReports = await Report.countDocuments({
        reporter: userId,
        status: "resolved",
      })

      res.json({
        totalReports,
        pendingReports,
        resolvedReports,
      })
    } catch (error) {
      console.error("Get report stats error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }
}

module.exports = new ReportController()
