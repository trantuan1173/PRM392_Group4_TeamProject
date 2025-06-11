const express = require("express")
const { body, query, param } = require("express-validator")
const notificationController = require("../controllers/notificationController")
const auth = require("../middleware/auth")

const router = express.Router()

/**
 * @swagger
 * /api/notifications:
 *   get:
 *     summary: Get user notifications
 *     tags: [Notifications]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           default: 1
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           default: 20
 *       - in: query
 *         name: type
 *         schema:
 *           type: string
 *           enum: [match, message, date_request, date_reminder, like, system]
 *     responses:
 *       200:
 *         description: Notifications retrieved successfully
 */
router.get(
  "/",
  auth,
  [
    query("page").optional().isInt({ min: 1 }),
    query("limit").optional().isInt({ min: 1, max: 100 }),
    query("type").optional().isIn(["match", "message", "date_request", "date_reminder", "like", "system"]),
  ],
  notificationController.getNotifications,
)

/**
 * @swagger
 * /api/notifications/{notificationId}/read:
 *   put:
 *     summary: Mark notification as read
 *     tags: [Notifications]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: notificationId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Notification marked as read
 */
router.put("/:notificationId/read", auth, [param("notificationId").isMongoId()], notificationController.markAsRead)

/**
 * @swagger
 * /api/notifications/read-all:
 *   put:
 *     summary: Mark all notifications as read
 *     tags: [Notifications]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: All notifications marked as read
 */
router.put("/read-all", auth, notificationController.markAllAsRead)

/**
 * @swagger
 * /api/notifications/{notificationId}:
 *   delete:
 *     summary: Delete notification
 *     tags: [Notifications]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: notificationId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Notification deleted successfully
 */
router.delete(
  "/:notificationId",
  auth,
  [param("notificationId").isMongoId()],
  notificationController.deleteNotification,
)

/**
 * @swagger
 * /api/notifications/settings:
 *   get:
 *     summary: Get notification settings
 *     tags: [Notifications]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Notification settings retrieved successfully
 */
router.get("/settings", auth, notificationController.getNotificationSettings)

/**
 * @swagger
 * /api/notifications/settings:
 *   put:
 *     summary: Update notification settings
 *     tags: [Notifications]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               settings:
 *                 type: object
 *                 properties:
 *                   matches:
 *                     type: boolean
 *                   messages:
 *                     type: boolean
 *                   dateRequests:
 *                     type: boolean
 *                   pushNotifications:
 *                     type: boolean
 *     responses:
 *       200:
 *         description: Notification settings updated successfully
 */
router.put("/settings", auth, notificationController.updateNotificationSettings)

/**
 * @swagger
 * /api/notifications/unread-count:
 *   get:
 *     summary: Get unread notification count
 *     tags: [Notifications]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Unread count retrieved successfully
 */
router.get("/unread-count", auth, notificationController.getUnreadCount)

/**
 * @swagger
 * /api/notifications/test:
 *   post:
 *     summary: Send test notification (development only)
 *     tags: [Notifications]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: false
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               title:
 *                 type: string
 *                 example: "Test Notification"
 *               message:
 *                 type: string
 *                 example: "This is a test notification"
 *               type:
 *                 type: string
 *                 default: system
 *     responses:
 *       200:
 *         description: Test notification sent
 */
router.post("/test", auth, notificationController.sendTestNotification)

module.exports = router
