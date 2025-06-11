const admin = require("firebase-admin")
const User = require("../models/User")
const Notification = require("../models/Notification")

class NotificationService {
  constructor() {
    // Initialize Firebase Admin SDK
    if (!admin.apps.length) {
      admin.initializeApp({
        credential: admin.credential.cert({
          projectId: process.env.FIREBASE_PROJECT_ID,
          clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
          privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, "\n"),
        }),
      })
    }
  }

  async sendNotification(userId, notificationData) {
    try {
      const { title, message, type, data = {} } = notificationData

      // Save notification to database
      const notification = new Notification({
        userId,
        title,
        message,
        type,
        data,
      })

      await notification.save()

      // Get user's FCM token
      const user = await User.findById(userId)
      if (!user || !user.fcmToken) {
        console.log(`No FCM token found for user ${userId}`)
        return { success: false, reason: "No FCM token" }
      }

      // Prepare FCM message
      const fcmMessage = {
        token: user.fcmToken,
        notification: {
          title,
          body: message,
        },
        data: {
          type,
          notificationId: notification._id.toString(),
          ...Object.fromEntries(Object.entries(data).map(([key, value]) => [key, String(value)])),
        },
        android: {
          notification: {
            icon: "ic_notification",
            color: "#FF6B6B",
            sound: "default",
          },
        },
      }

      // Send FCM notification
      const response = await admin.messaging().send(fcmMessage)
      console.log("FCM notification sent successfully:", response)

      return { success: true, messageId: response }
    } catch (error) {
      console.error("Error sending notification:", error)

      // If token is invalid, remove it from user
      if (error.code === "messaging/registration-token-not-registered") {
        await User.findByIdAndUpdate(userId, { $unset: { fcmToken: 1 } })
      }

      return { success: false, error: error.message }
    }
  }

  async sendBulkNotifications(userIds, notificationData) {
    const results = []

    for (const userId of userIds) {
      const result = await this.sendNotification(userId, notificationData)
      results.push({ userId, ...result })
    }

    return results
  }

  async getUserNotifications(userId, page = 1, limit = 20) {
    try {
      const skip = (page - 1) * limit

      const notifications = await Notification.find({ userId }).sort({ createdAt: -1 }).skip(skip).limit(limit)

      const total = await Notification.countDocuments({ userId })
      const unreadCount = await Notification.countDocuments({
        userId,
        isRead: false,
      })

      return {
        notifications,
        pagination: {
          page,
          limit,
          total,
          pages: Math.ceil(total / limit),
        },
        unreadCount,
      }
    } catch (error) {
      console.error("Error getting user notifications:", error)
      throw error
    }
  }

  async markNotificationAsRead(notificationId, userId) {
    try {
      const notification = await Notification.findOneAndUpdate(
        { _id: notificationId, userId },
        {
          isRead: true,
          readAt: new Date(),
        },
        { new: true },
      )

      return notification
    } catch (error) {
      console.error("Error marking notification as read:", error)
      throw error
    }
  }

  async markAllNotificationsAsRead(userId) {
    try {
      await Notification.updateMany(
        { userId, isRead: false },
        {
          isRead: true,
          readAt: new Date(),
        },
      )

      return { success: true }
    } catch (error) {
      console.error("Error marking all notifications as read:", error)
      throw error
    }
  }

  // Notification templates
  getMatchNotification(matchedUserName) {
    return {
      title: "New Match! 💕",
      message: `You matched with ${matchedUserName}!`,
      type: "match",
    }
  }

  getMessageNotification(senderName, messagePreview) {
    return {
      title: `New message from ${senderName}`,
      message: messagePreview,
      type: "message",
    }
  }

  getDateRequestNotification(requesterName) {
    return {
      title: "Date Request 📅",
      message: `${requesterName} wants to schedule a date with you!`,
      type: "date_request",
    }
  }

  getDateReminderNotification(partnerName, dateTime) {
    return {
      title: "Date Reminder ⏰",
      message: `Don't forget your date with ${partnerName} at ${dateTime}`,
      type: "date_reminder",
    }
  }
}

module.exports = new NotificationService()
