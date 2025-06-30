const User = require("../models/User")
const jwt = require("jsonwebtoken")
const { validationResult } = require("express-validator")
const contentModerationService = require("../services/contentModerationService")

class AuthController {
  // Register new user
  async register(req, res) {
    try {
      const errors = validationResult(req)
      if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() })
      }

      const { name, email, password, dob, gender, bio } = req.body

      // Check if user already exists
      const existingUser = await User.findOne({ email })
      if (existingUser) {
        return res.status(409).json({ message: "User already exists" })
      }

      // Content moderation for name and bio
      const nameModeration = await contentModerationService.moderateText(name, "profile_name")
      const bioModeration = bio ? await contentModerationService.moderateText(bio, "bio") : { result: "approved" }

      if (nameModeration.result === "rejected" || bioModeration.result === "rejected") {
        return res.status(400).json({
          message: "Profile contains inappropriate content",
          details: {
            name: nameModeration.result,
            bio: bioModeration.result,
          },
        })
      }

      // Create new user
      const user = new User({
        name,
        email,
        password,
        dob,
        gender,
        bio: bio || "",
      })

      await user.save()

      // Generate JWT token
      const token = jwt.sign({ userId: user._id }, process.env.JWT_SECRET, { expiresIn: "7d" })

      res.status(201).json({
        message: "User registered successfully",
        token,
        user: user.toJSON(),
      })
    } catch (error) {
      console.error("Registration error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Login user
  async login(req, res) {
    try {
      const errors = validationResult(req)
      if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() })
      }

      const { email, password } = req.body

      // Find user by email
      const user = await User.findOne({ email })
      if (!user) {
        return res.status(400).json({ message: "Invalid credentials" })
      }

      // Check if user is banned
      if (user.isBanned) {
        return res.status(403).json({
          message: "Account is banned",
          reason: user.banReason,
        })
      }

      // Verify password
      const isMatch = await user.comparePassword(password)
      if (!isMatch) {
        return res.status(400).json({ message: "Invalid credentials" })
      }

      // Update online status
      user.isOnline = true
      user.lastSeen = new Date()
      await user.save()

      // Generate JWT token
      const token = jwt.sign({ userId: user._id }, process.env.JWT_SECRET, { expiresIn: "7d" })

      res.json({
        message: "Login successful",
        token,
        user: user.toJSON(),
      })
    } catch (error) {
      console.error("Login error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Logout user
  async logout(req, res) {
    try {
      const user = await User.findById(req.userId)
      if (user) {
        user.isOnline = false
        user.lastSeen = new Date()
        await user.save()
      }

      res.json({ message: "Logout successful" })
    } catch (error) {
      console.error("Logout error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }

  // Update FCM token
  async updateFCMToken(req, res) {
    try {
      const { fcmToken } = req.body

      await User.findByIdAndUpdate(req.userId, { fcmToken })

      res.json({ message: "FCM token updated successfully" })
    } catch (error) {
      console.error("FCM token update error:", error)
      res.status(500).json({ message: "Server error" })
    }
  }
}

module.exports = new AuthController()
