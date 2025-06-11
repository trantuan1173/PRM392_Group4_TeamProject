const express = require("express")
const { query, param } = require("express-validator")
const matchController = require("../controllers/matchController")
const auth = require("../middleware/auth")

const router = express.Router()

/**
 * @swagger
 * /api/matches:
 *   get:
 *     summary: Get user's matches
 *     tags: [Matches]
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
 *         description: Matches retrieved successfully
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 matches:
 *                   type: array
 *                   items:
 *                     $ref: '#/components/schemas/Match'
 *                 pagination:
 *                   type: object
 */
router.get(
  "/",
  auth,
  [query("page").optional().isInt({ min: 1 }), query("limit").optional().isInt({ min: 1, max: 100 })],
  matchController.getMatches,
)

/**
 * @swagger
 * /api/matches/{matchId}:
 *   get:
 *     summary: Get match details
 *     tags: [Matches]
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
 *         description: Match details retrieved successfully
 */
router.get("/:matchId", auth, [param("matchId").isMongoId()], matchController.getMatchDetails)

/**
 * @swagger
 * /api/matches/{matchId}:
 *   delete:
 *     summary: Unmatch (deactivate match)
 *     tags: [Matches]
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
 *         description: Unmatched successfully
 */
router.delete("/:matchId", auth, [param("matchId").isMongoId()], matchController.unmatch)

/**
 * @swagger
 * /api/matches/stats:
 *   get:
 *     summary: Get match statistics
 *     tags: [Matches]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Match statistics retrieved successfully
 */
router.get("/stats", auth, matchController.getMatchStats)

module.exports = router
