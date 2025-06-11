const express = require("express")
const { body, param, query } = require("express-validator")
const dateController = require("../controllers/dateController")
const auth = require("../middleware/auth")

const router = express.Router()

/**
 * @swagger
 * /api/dates:
 *   post:
 *     summary: Create a date request
 *     tags: [Dates]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - matchId
 *               - dateTime
 *             properties:
 *               matchId:
 *                 type: string
 *                 example: "64f1a2b3c4d5e6f7g8h9i0j1"
 *               dateTime:
 *                 type: string
 *                 format: date-time
 *                 example: "2024-01-15T19:00:00Z"
 *               location:
 *                 type: object
 *                 properties:
 *                   name:
 *                     type: string
 *                     example: "Starbucks Coffee"
 *                   address:
 *                     type: string
 *                     example: "123 Main St, Ho Chi Minh City"
 *                   coordinates:
 *                     type: array
 *                     items:
 *                       type: number
 *                     example: [106.6297, 10.8231]
 *               note:
 *                 type: string
 *                 example: "Looking forward to meeting you!"
 *     responses:
 *       201:
 *         description: Date request created successfully
 */
router.post("/", auth, dateController.createDateRequest)

/**
 * @swagger
 * /api/dates/{dateId}/respond:
 *   put:
 *     summary: Respond to a date request
 *     tags: [Dates]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: dateId
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
 *               - status
 *             properties:
 *               status:
 *                 type: string
 *                 enum: [accepted, rejected]
 *                 example: "accepted"
 *     responses:
 *       200:
 *         description: Date response recorded successfully
 */
router.put("/:dateId/respond", auth, dateController.respondToDateRequest)

/**
 * @swagger
 * /api/dates:
 *   get:
 *     summary: Get user's date schedules
 *     tags: [Dates]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: status
 *         schema:
 *           type: string
 *           enum: [pending, accepted, rejected, completed, cancelled]
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
 *     responses:
 *       200:
 *         description: Date schedules retrieved successfully
 */
router.get("/", auth, dateController.getDateSchedules)

module.exports = router
