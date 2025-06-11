const express = require("express")
const { body, query, param } = require("express-validator")
const reportController = require("../controllers/reportController")
const auth = require("../middleware/auth")

const router = express.Router()

/**
 * @swagger
 * /api/reports:
 *   post:
 *     summary: Create a report
 *     tags: [Reports]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - reportedUserId
 *               - reason
 *             properties:
 *               reportedUserId:
 *                 type: string
 *                 example: "64f1a2b3c4d5e6f7g8h9i0j5"
 *               reason:
 *                 type: string
 *                 enum: [inappropriate_photos, harassment, fake_profile, spam, inappropriate_messages, underage, other]
 *                 example: "inappropriate_photos"
 *               description:
 *                 type: string
 *                 example: "User has inappropriate profile photos that violate community guidelines."
 *               evidence:
 *                 type: array
 *                 items:
 *                   type: string
 *                 example: ["https://example.com/screenshot1.jpg"]
 *     responses:
 *       201:
 *         description: Report submitted successfully
 */
router.post(
  "/",
  auth,
  [
    body("reportedUserId").isMongoId().withMessage("Valid user ID is required"),
    body("reason")
      .isIn([
        "inappropriate_photos",
        "harassment",
        "fake_profile",
        "spam",
        "inappropriate_messages",
        "underage",
        "other",
      ])
      .withMessage("Valid reason is required"),
    body("description").optional().isLength({ max: 1000 }).withMessage("Description must not exceed 1000 characters"),
    body("evidence").optional().isArray().withMessage("Evidence must be an array"),
  ],
  reportController.createReport,
)

/**
 * @swagger
 * /api/reports:
 *   get:
 *     summary: Get user's reports
 *     tags: [Reports]
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
 *         name: status
 *         schema:
 *           type: string
 *           enum: [pending, investigating, resolved, dismissed]
 *     responses:
 *       200:
 *         description: Reports retrieved successfully
 */
router.get(
  "/",
  auth,
  [
    query("page").optional().isInt({ min: 1 }),
    query("limit").optional().isInt({ min: 1, max: 100 }),
    query("status").optional().isIn(["pending", "investigating", "resolved", "dismissed"]),
  ],
  reportController.getMyReports,
)

/**
 * @swagger
 * /api/reports/{reportId}:
 *   get:
 *     summary: Get report details
 *     tags: [Reports]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: reportId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Report details retrieved successfully
 */
router.get("/:reportId", auth, [param("reportId").isMongoId()], reportController.getReportDetails)

/**
 * @swagger
 * /api/reports/stats:
 *   get:
 *     summary: Get report statistics
 *     tags: [Reports]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Report statistics retrieved successfully
 */
router.get("/stats", auth, reportController.getReportStats)

module.exports = router
