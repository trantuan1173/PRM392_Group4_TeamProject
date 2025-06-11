const express = require("express")
const { body, query, param } = require("express-validator")
const contentModerationController = require("../controllers/contentModerationController")
const auth = require("../middleware/auth")

const router = express.Router()

/**
 * @swagger
 * /api/moderation/logs:
 *   get:
 *     summary: Get moderation logs for current user
 *     tags: [Content Moderation]
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
 *         name: contentType
 *         schema:
 *           type: string
 *           enum: [profile_photo, bio, message, profile_name]
 *       - in: query
 *         name: result
 *         schema:
 *           type: string
 *           enum: [approved, rejected, flagged, requires_review]
 *     responses:
 *       200:
 *         description: Moderation logs retrieved successfully
 */
router.get(
  "/logs",
  auth,
  [
    query("page").optional().isInt({ min: 1 }),
    query("limit").optional().isInt({ min: 1, max: 100 }),
    query("contentType").optional().isIn(["profile_photo", "bio", "message", "profile_name"]),
    query("result").optional().isIn(["approved", "rejected", "flagged", "requires_review"]),
  ],
  contentModerationController.getMyModerationLogs,
)

/**
 * @swagger
 * /api/moderation/text:
 *   post:
 *     summary: Moderate text content manually
 *     tags: [Content Moderation]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - text
 *               - contentType
 *             properties:
 *               text:
 *                 type: string
 *                 example: "This is some text to moderate"
 *               contentType:
 *                 type: string
 *                 enum: [profile_name, bio, message]
 *                 example: "bio"
 *     responses:
 *       200:
 *         description: Content moderated successfully
 */
router.post(
  "/text",
  auth,
  [
    body("text").notEmpty().withMessage("Text is required"),
    body("contentType").isIn(["profile_name", "bio", "message"]).withMessage("Valid content type is required"),
  ],
  contentModerationController.moderateText,
)

/**
 * @swagger
 * /api/moderation/image:
 *   post:
 *     summary: Moderate image content manually
 *     tags: [Content Moderation]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - imageUrl
 *               - contentType
 *             properties:
 *               imageUrl:
 *                 type: string
 *                 example: "https://example.com/image.jpg"
 *               contentType:
 *                 type: string
 *                 enum: [profile_photo]
 *                 example: "profile_photo"
 *     responses:
 *       200:
 *         description: Image moderated successfully
 */
router.post(
  "/image",
  auth,
  [
    body("imageUrl").isURL().withMessage("Valid image URL is required"),
    body("contentType").isIn(["profile_photo"]).withMessage("Valid content type is required"),
  ],
  contentModerationController.moderateImage,
)

/**
 * @swagger
 * /api/moderation/stats:
 *   get:
 *     summary: Get moderation statistics
 *     tags: [Content Moderation]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Moderation statistics retrieved successfully
 */
router.get("/stats", auth, contentModerationController.getModerationStats)

/**
 * @swagger
 * /api/moderation/flagged:
 *   get:
 *     summary: Get flagged content that needs review
 *     tags: [Content Moderation]
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
 *         description: Flagged content retrieved successfully
 */
router.get(
  "/flagged",
  auth,
  [query("page").optional().isInt({ min: 1 }), query("limit").optional().isInt({ min: 1, max: 100 })],
  contentModerationController.getFlaggedContent,
)

/**
 * @swagger
 * /api/moderation/review/{logId}:
 *   put:
 *     summary: Review flagged content (admin function)
 *     tags: [Content Moderation]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: logId
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
 *               - decision
 *             properties:
 *               decision:
 *                 type: string
 *                 enum: [approve, reject, ban_user]
 *                 example: "approve"
 *               notes:
 *                 type: string
 *                 example: "Content reviewed and approved"
 *     responses:
 *       200:
 *         description: Content reviewed successfully
 */
router.put(
  "/review/:logId",
  auth,
  [
    param("logId").isMongoId(),
    body("decision").isIn(["approve", "reject", "ban_user"]).withMessage("Valid decision is required"),
    body("notes").optional().isString(),
  ],
  contentModerationController.reviewContent,
)

module.exports = router
