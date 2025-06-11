const Notification = require("../models/Notification")
const notificationService = require("../services/notificationService")
const { validationResult } = require("express-validator")

class NotificationController {
  // Get user notifications
  async getNotifications(req, res) {
    try {
      const { page = 1, limit = 20, type } = req.query
      const userId = req.userId

      const query = { userId }
      if (type) {
        query.type = type
      }

      const result = await notificationService.getUserNotifications(
        userId,
        Number.parseInt(page),
        Number.parseInt(limit),
      )

      res.json(result)
    } catch (error) {
      console.error("Get notifications error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Mark notification as read
  async markAsRead(req, res) {
    try {
      const { notificationId } = req.params
      const userId = req.userId

      const notification = await notificationService.markNotificationAsRead(notificationId, userId)

      if (!notification) {
        return res.status(404).json({ message: "Notification not found" })
      }

      res.json({
        message: "Notification marked as read",
        notification,
      })
    } catch (error) {
      console.error("Mark notification as read error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Mark all notifications as read
  async markAllAsRead(req, res) {
    try {
      const userId = req.userId

      await notificationService.markAllNotificationsAsRead(userId)

      res.json({ message: "All notifications marked as read" })
    } catch (error) {
      console.error("Mark all notifications as read error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Delete notification
  async deleteNotification(req, res) {
    try {
      const { notificationId } = req.params
      const userId = req.userId

      const notification = await Notification.findOneAndDelete({
        _id: notificationId,
        userId,
      })

      if (!notification) {
        return res.status(404).json({ message: "Notification not found" })
      }

      res.json({ message: "Notification deleted successfully" })
    } catch (error) {
      console.error("Delete notification error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get notification settings
  async getNotificationSettings(req, res) {
    try {
      const userId = req.userId

      // In a real app, you'd have a separate NotificationSettings model
      // For now, we'll return default settings
      const settings = {
        matches: true,
        messages: true,
        dateRequests: true,
        dateReminders: true,
        likes: true,
        system: true,
        pushNotifications: true,
        emailNotifications: false,
      }

      res.json({ settings })
    } catch (error) {
      console.error("Get notification settings error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Update notification settings
  async updateNotificationSettings(req, res) {
    try {
      const { settings } = req.body
      const userId = req.userId

      // In a real app, you'd update the NotificationSettings model
      // For now, we'll just return success

      res.json({
        message: "Notification settings updated successfully",
        settings,
      })
    } catch (error) {
      console.error("Update notification settings error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get unread count
  async getUnreadCount(req, res) {
    try {
      const userId = req.userId

      const unreadCount = await Notification.countDocuments({
        userId,
        isRead: false,
      })

      res.json({ unreadCount })
    } catch (error) {
      console.error("Get unread count error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Send test notification (for development)
  async sendTestNotification(req, res) {
    try {
      const { title, message, type = "system" } = req.body
      const userId = req.userId

      const result = await notificationService.sendNotification(userId, {
        title: title || "Test Notification",
        message: message || "This is a test notification",
        type,
        data: { test: true },
      })

      res.json({
        message: "Test notification sent",
        result,
      })
    } catch (error) {
      console.error("Send test notification error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }
}

module.exports = new NotificationController()
