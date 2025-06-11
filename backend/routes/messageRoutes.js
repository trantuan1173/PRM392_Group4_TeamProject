const express = require("express")
const { body, query, param } = require("express-validator")
const messageController = require("../controllers/messageController")
const auth = require("../middleware/auth")

const router = express.Router()

/**
 * @swagger
 * /api/messages/{matchId}:
 *   get:
 *     summary: Get messages for a match
 *     tags: [Messages]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: matchId
 *         required: true
 *         schema:
 *           type: string
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           default: 1
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           default: 50
 *     responses:
 *       200:
 *         description: Messages retrieved successfully
 */
router.get(
  "/:matchId",
  auth,
  [
    param("matchId").isMongoId(),
    query("page").optional().isInt({ min: 1 }),
    query("limit").optional().isInt({ min: 1, max: 100 }),
  ],
  messageController.getMessages,
)

/**
 * @swagger
 * /api/messages/{matchId}:
 *   post:
 *     summary: Send a message
 *     tags: [Messages]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: matchId
 *         required: true
 *         schema:
 *           type: string
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - content
 *             properties:
 *               content:
 *                 type: string
 *                 example: "Hello! How are you doing today?"
 *               messageType:
 *                 type: string
 *                 enum: [text, image, gif]
 *                 default: text
 *     responses:
 *       201:
 *         description: Message sent successfully
 */
router.post(
  "/:matchId",
  auth,
  [
    param("matchId").isMongoId(),
    body("content").notEmpty().isLength({ max: 1000 }),
    body("messageType").optional().isIn(["text", "image", "gif"]),
  ],
  messageController.sendMessage,
)

/**
 * @swagger
 * /api/messages/{matchId}/read:
 *   put:
 *     summary: Mark messages as read
 *     tags: [Messages]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: matchId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Messages marked as read
 */
router.put("/:matchId/read", auth, [param("matchId").isMongoId()], messageController.markMessagesAsRead)

/**
 * @swagger
 * /api/messages/message/{messageId}:
 *   delete:
 *     summary: Delete a message
 *     tags: [Messages]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: messageId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Message deleted successfully
 */
router.delete("/message/:messageId", auth, [param("messageId").isMongoId()], messageController.deleteMessage)

/**
 * @swagger
 * /api/messages/{matchId}/stats:
 *   get:
 *     summary: Get message statistics
 *     tags: [Messages]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: matchId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Message statistics retrieved successfully
 */
router.get("/:matchId/stats", auth, [param("matchId").isMongoId()], messageController.getMessageStats)

module.exports = router
