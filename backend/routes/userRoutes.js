const express = require("express")
const { body, query } = require("express-validator")
const userController = require("../controllers/userController")
const auth = require("../middleware/auth")

const router = express.Router()

/**
 * @swagger
 * /api/users/profile:
 *   get:
 *     summary: Get current user profile
 *     tags: [Users]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: User profile retrieved successfully
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 user:
 *                   $ref: '#/components/schemas/User'
 */
router.get("/profile", auth, userController.getProfile)

/**
 * @swagger
 * /api/users/profile:
 *   put:
 *     summary: Update user profile
 *     tags: [Users]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               name:
 *                 type: string
 *                 example: "John Updated"
 *               bio:
 *                 type: string
 *                 example: "Updated bio - Love hiking and cooking"
 *               preferences:
 *                 type: object
 *                 properties:
 *                   ageRange:
 *                     type: object
 *                     properties:
 *                       min:
 *                         type: number
 *                         example: 22
 *                       max:
 *                         type: number
 *                         example: 35
 *                   maxDistance:
 *                     type: number
 *                     example: 30
 *                   interestedIn:
 *                     type: string
 *                     enum: [male, female, both]
 *                     example: "female"
 *     responses:
 *       200:
 *         description: Profile updated successfully
 */
router.put(
  "/profile",
  auth,
  [
    body("name").optional().trim().isLength({ min: 2, max: 50 }),
    body("bio").optional().isLength({ max: 500 }),
    body("preferences.ageRange.min").optional().isInt({ min: 18, max: 100 }),
    body("preferences.ageRange.max").optional().isInt({ min: 18, max: 100 }),
    body("preferences.maxDistance").optional().isInt({ min: 1, max: 500 }),
    body("preferences.interestedIn").optional().isIn(["male", "female", "both"]),
  ],
  userController.updateProfile,
)

/**
 * @swagger
 * /api/users/discover:
 *   get:
 *     summary: Get users for discovery (swiping)
 *     tags: [Users]
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
 *           default: 10
 *     responses:
 *       200:
 *         description: Discovery users retrieved successfully
 */
router.get(
  "/discover",
  auth,
  [query("page").optional().isInt({ min: 1 }), query("limit").optional().isInt({ min: 1, max: 50 })],
  userController.getDiscoverUsers,
)

/**
 * @swagger
 * /api/users/location:
 *   put:
 *     summary: Update user location
 *     tags: [Users]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - latitude
 *               - longitude
 *             properties:
 *               latitude:
 *                 type: number
 *                 example: 10.8231
 *               longitude:
 *                 type: number
 *                 example: 106.6297
 *               address:
 *                 type: string
 *                 example: "Ho Chi Minh City, Vietnam"
 *     responses:
 *       200:
 *         description: Location updated successfully
 */
router.put(
  "/location",
  auth,
  [
    body("latitude").isFloat({ min: -90, max: 90 }),
    body("longitude").isFloat({ min: -180, max: 180 }),
    body("address").optional().isString(),
  ],
  userController.updateLocation,
)

/**
 * @swagger
 * /api/users/photos:
 *   put:
 *     summary: Upload profile photos
 *     tags: [Users]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - photos
 *             properties:
 *               photos:
 *                 type: array
 *                 items:
 *                   type: object
 *                   properties:
 *                     url:
 *                       type: string
 *                     isMain:
 *                       type: boolean
 *     responses:
 *       200:
 *         description: Photos uploaded successfully
 */
router.put("/photos", auth, userController.uploadPhotos)

/**
 * @swagger
 * /api/users/{userId}:
 *   get:
 *     summary: Get user by ID
 *     tags: [Users]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: User retrieved successfully
 */
router.get("/:userId", auth, userController.getUserById)

/**
 * @swagger
 * /api/users/account:
 *   delete:
 *     summary: Delete user account
 *     tags: [Users]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Account deleted successfully
 */
router.delete("/account", auth, userController.deleteAccount)

module.exports = router
