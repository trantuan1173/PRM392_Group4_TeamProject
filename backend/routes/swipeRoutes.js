const express = require("express")
const { body, query } = require("express-validator")
const swipeController = require("../controllers/swipeController")
const auth = require("../middleware/auth")

const router = express.Router()

/**
 * @swagger
 * /api/swipes:
 *   post:
 *     summary: Create a swipe (like or pass)
 *     tags: [Swipes]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - toUserId
 *               - isLike
 *             properties:
 *               toUserId:
 *                 type: string
 *                 example: "64f1a2b3c4d5e6f7g8h9i0j1"
 *               isLike:
 *                 type: boolean
 *                 example: true
 *     responses:
 *       201:
 *         description: Swipe recorded successfully
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Swipe recorded successfully"
 *                 isMatch:
 *                   type: boolean
 *                   example: true
 *                 matchId:
 *                   type: string
 *                   example: "64f1a2b3c4d5e6f7g8h9i0j2"
 */
router.post(
  "/",
  auth,
  [
    body("toUserId").isMongoId().withMessage("Valid user ID is required"),
    body("isLike").isBoolean().withMessage("isLike must be a boolean"),
  ],
  swipeController.createSwipe,
)

/**
 * @swagger
 * /api/swipes/history:
 *   get:
 *     summary: Get user's swipe history
 *     tags: [Swipes]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           default: 1
 *         description: Page number
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           default: 20
 *         description: Number of items per page
 *     responses:
 *       200:
 *         description: Swipe history retrieved successfully
 */
router.get(
  "/history",
  auth,
  [
    query("page").optional().isInt({ min: 1 }).withMessage("Page must be a positive integer"),
    query("limit").optional().isInt({ min: 1, max: 100 }).withMessage("Limit must be between 1 and 100"),
  ],
  swipeController.getSwipeHistory,
)

/**
 * @swagger
 * /api/swipes/likes:
 *   get:
 *     summary: Get users who liked current user
 *     tags: [Swipes]
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
 *     responses:
 *       200:
 *         description: Likes retrieved successfully
 */
router.get(
  "/likes",
  auth,
  [
    query("page").optional().isInt({ min: 1 }).withMessage("Page must be a positive integer"),
    query("limit").optional().isInt({ min: 1, max: 100 }).withMessage("Limit must be between 1 and 100"),
  ],
  swipeController.getLikes,
)

module.exports = router
