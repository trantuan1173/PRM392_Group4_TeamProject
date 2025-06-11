const User = require("../models/User")
const Swipe = require("../models/Swipe")
const contentModerationService = require("../services/contentModerationService")
const { validationResult } = require("express-validator")
const geolib = require("geolib")

class UserController {
  // Get current user profile
  async getProfile(req, res) {
    try {
      const user = await User.findById(req.userId)
      if (!user) {
        return res.status(404).json({ message: "User not found" })
      }

      res.json({ user: user.toJSON() })
    } catch (error) {
      console.error("Get profile error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Update user profile
  async updateProfile(req, res) {
    try {
      const errors = validationResult(req)
      if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() })
      }

      const { name, bio, preferences, photos } = req.body
      const userId = req.userId

      const user = await User.findById(userId)
      if (!user) {
        return res.status(404).json({ message: "User not found" })
      }

      // Content moderation for name and bio
      if (name && name !== user.name) {
        const nameModeration = await contentModerationService.moderateText(name, "profile_name", userId)
        if (nameModeration.result === "rejected") {
          return res.status(400).json({
            message: "Name contains inappropriate content",
            moderation: nameModeration,
          })
        }
        user.name = name
      }

      if (bio !== undefined && bio !== user.bio) {
        const bioModeration = await contentModerationService.moderateText(bio, "bio", userId)
        if (bioModeration.result === "rejected") {
          return res.status(400).json({
            message: "Bio contains inappropriate content",
            moderation: bioModeration,
          })
        }
        user.bio = bio
      }

      // Update preferences
      if (preferences) {
        user.preferences = { ...user.preferences, ...preferences }
      }

      // Update photos
      if (photos) {
        user.photos = photos
      }

      await user.save()

      res.json({
        message: "Profile updated successfully",
        user: user.toJSON(),
      })
    } catch (error) {
      console.error("Update profile error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get users for discovery (swiping)
  async getDiscoverUsers(req, res) {
    try {
      const { page = 1, limit = 10 } = req.query
      const skip = (page - 1) * limit
      const userId = req.userId

      const currentUser = await User.findById(userId)
      if (!currentUser) {
        return res.status(404).json({ message: "User not found" })
      }

      // Get users that current user has already swiped
      const swipedUsers = await Swipe.find({ fromUser: userId }).distinct("toUser")

      // Build discovery query
      const query = {
        _id: { $ne: userId, $nin: swipedUsers },
        isBanned: false,
        age: {
          $gte: currentUser.preferences.ageRange.min,
          $lte: currentUser.preferences.ageRange.max,
        },
      }

      // Filter by interested gender
      if (currentUser.preferences.interestedIn !== "both") {
        query.gender = currentUser.preferences.interestedIn
      }

      // Location-based filtering
      if (currentUser.location.coordinates[0] !== 0 && currentUser.location.coordinates[1] !== 0) {
        query.location = {
          $near: {
            $geometry: {
              type: "Point",
              coordinates: currentUser.location.coordinates,
            },
            $maxDistance: currentUser.preferences.maxDistance * 1000, // Convert km to meters
          },
        }
      }

      const users = await User.find(query)
        .select("name age gender bio avatar photos location createdAt")
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(Number.parseInt(limit))

      // Calculate distance for each user
      const usersWithDistance = users.map((user) => {
        let distance = null
        if (
          currentUser.location.coordinates[0] !== 0 &&
          currentUser.location.coordinates[1] !== 0 &&
          user.location.coordinates[0] !== 0 &&
          user.location.coordinates[1] !== 0
        ) {
          distance = geolib.getDistance(
            {
              latitude: currentUser.location.coordinates[1],
              longitude: currentUser.location.coordinates[0],
            },
            {
              latitude: user.location.coordinates[1],
              longitude: user.location.coordinates[0],
            },
          )
          distance = Math.round(distance / 1000) // Convert to km
        }

        return {
          ...user.toJSON(),
          distance,
        }
      })

      const total = await User.countDocuments(query)

      res.json({
        users: usersWithDistance,
        pagination: {
          page: Number.parseInt(page),
          limit: Number.parseInt(limit),
          total,
          pages: Math.ceil(total / limit),
        },
      })
    } catch (error) {
      console.error("Get discover users error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Update user location
  async updateLocation(req, res) {
    try {
      const { latitude, longitude, address } = req.body
      const userId = req.userId

      const user = await User.findById(userId)
      if (!user) {
        return res.status(404).json({ message: "User not found" })
      }

      user.location = {
        type: "Point",
        coordinates: [longitude, latitude],
        address: address || "",
      }

      await user.save()

      res.json({
        message: "Location updated successfully",
        location: user.location,
      })
    } catch (error) {
      console.error("Update location error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Upload profile photos
  async uploadPhotos(req, res) {
    try {
      // This would integrate with Cloudinary or similar service
      // For now, we'll accept URLs directly

      const { photos } = req.body
      const userId = req.userId

      if (!photos || !Array.isArray(photos)) {
        return res.status(400).json({ message: "Photos array is required" })
      }

      // Content moderation for photos
      for (const photo of photos) {
        const moderation = await contentModerationService.moderateImage(photo.url, "profile_photo", userId)
        if (moderation.result === "rejected") {
          return res.status(400).json({
            message: "One or more photos contain inappropriate content",
            photo: photo.url,
          })
        }
      }

      const user = await User.findById(userId)
      if (!user) {
        return res.status(404).json({ message: "User not found" })
      }

      user.photos = photos
      await user.save()

      res.json({
        message: "Photos uploaded successfully",
        photos: user.photos,
      })
    } catch (error) {
      console.error("Upload photos error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Get user by ID (for viewing profiles)
  async getUserById(req, res) {
    try {
      const { userId } = req.params

      const user = await User.findById(userId).select("name age gender bio avatar photos location createdAt")

      if (!user || user.isBanned) {
        return res.status(404).json({ message: "User not found" })
      }

      res.json({ user })
    } catch (error) {
      console.error("Get user by ID error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Delete user account
  async deleteAccount(req, res) {
    try {
      const userId = req.userId

      // Soft delete - mark as banned instead of actual deletion
      await User.findByIdAndUpdate(userId, {
        isBanned: true,
        banReason: "Account deleted by user",
        email: `deleted_${Date.now()}@deleted.com`,
      })

      res.json({ message: "Account deleted successfully" })
    } catch (error) {
      console.error("Delete account error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }
}

module.exports = new UserController()
