const DateSchedule = require("../models/DateSchedule")
const Match = require("../models/Match")
const User = require("../models/User")
const notificationService = require("../services/notificationService")
const { validationResult } = require("express-validator")

class DateController {
  async createDateRequest(req, res) {
    try {
      const errors = validationResult(req)
      if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() })
      }

      const { matchId, dateTime, location, note } = req.body
      const proposedBy = req.userId

      // Verify match exists and user is part of it
      const match = await Match.findOne({
        _id: matchId,
        $or: [{ user1: proposedBy }, { user2: proposedBy }],
        isActive: true,
      }).populate("user1 user2", "name")

      if (!match) {
        return res.status(404).json({ message: "Match not found" })
      }

      // Create date request
      const dateSchedule = new DateSchedule({
        matchId,
        proposedBy,
        dateTime: new Date(dateTime),
        location,
        note,
      })

      await dateSchedule.save()

      // Get the other user
      const otherUserId = match.user1._id.toString() === proposedBy ? match.user2._id : match.user1._id
      const proposerName = match.user1._id.toString() === proposedBy ? match.user1.name : match.user2.name

      // Send notification
      await notificationService.sendNotification(otherUserId, {
        title: "Date Request 📅",
        message: `${proposerName} wants to schedule a date with you!`,
        type: "date_request",
        data: { dateId: dateSchedule._id, matchId },
      })

      res.status(201).json({
        message: "Date request created successfully",
        dateSchedule,
      })
    } catch (error) {
      console.error("Create date request error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  async respondToDateRequest(req, res) {
    try {
      const { dateId } = req.params
      const { status } = req.body
      const userId = req.userId

      const dateSchedule = await DateSchedule.findById(dateId).populate({
        path: "matchId",
        populate: { path: "user1 user2", select: "name" },
      })

      if (!dateSchedule) {
        return res.status(404).json({ message: "Date request not found" })
      }

      // Verify user is part of the match and not the proposer
      const match = dateSchedule.matchId
      const isUserInMatch = match.user1._id.toString() === userId || match.user2._id.toString() === userId
      const isProposer = dateSchedule.proposedBy.toString() === userId

      if (!isUserInMatch || isProposer) {
        return res.status(403).json({ message: "Not authorized to respond to this date request" })
      }

      // Update date schedule
      dateSchedule.status = status
      dateSchedule.responseBy = userId
      dateSchedule.respondedAt = new Date()

      await dateSchedule.save()

      // Send notification to proposer
      const responderName = match.user1._id.toString() === userId ? match.user1.name : match.user2.name
      const proposerId = dateSchedule.proposedBy

      const notificationMessage =
        status === "accepted"
          ? `${responderName} accepted your date request! 🎉`
          : `${responderName} declined your date request.`

      await notificationService.sendNotification(proposerId, {
        title: status === "accepted" ? "Date Accepted! 🎉" : "Date Declined",
        message: notificationMessage,
        type: "date_request",
        data: { dateId: dateSchedule._id, status },
      })

      res.json({
        message: `Date request ${status} successfully`,
        dateSchedule,
      })
    } catch (error) {
      console.error("Respond to date request error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  async getDateSchedules(req, res) {
    try {
      const { status, page = 1, limit = 20 } = req.query
      const skip = (page - 1) * limit
      const userId = req.userId

      // Build query
      const matchQuery = {
        $or: [{ user1: userId }, { user2: userId }],
        isActive: true,
      }

      const matches = await Match.find(matchQuery).select("_id")
      const matchIds = matches.map((match) => match._id)

      const dateQuery = { matchId: { $in: matchIds } }
      if (status) {
        dateQuery.status = status
      }

      const dateSchedules = await DateSchedule.find(dateQuery)
        .populate({
          path: "matchId",
          populate: { path: "user1 user2", select: "name avatar" },
        })
        .populate("proposedBy responseBy", "name avatar")
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(Number.parseInt(limit))

      const total = await DateSchedule.countDocuments(dateQuery)

      res.json({
        dateSchedules,
        pagination: {
          page: Number.parseInt(page),
          limit: Number.parseInt(limit),
          total,
          pages: Math.ceil(total / limit),
        },
      })
    } catch (error) {
      console.error("Get date schedules error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }
}

module.exports = new DateController()
