const jwt = require("jsonwebtoken")
const User = require("../models/User")

const auth = async (req, res, next) => {
  try {
    const token = req.header("Authorization")?.replace("Bearer ", "")

    if (!token) {
      return res.status(401).json({ message: "No token, authorization denied" })
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET)

    // Check if user still exists and is not banned
    const user = await User.findById(decoded.userId)
    if (!user) {
      return res.status(401).json({ message: "Token is not valid" })
    }

    if (user.isBanned) {
      return res.status(403).json({
        message: "Account is banned",
        reason: user.banReason,
      })
    }

    req.userId = decoded.userId
    req.user = user
    next()
  } catch (error) {
    console.error("Auth middleware error:", error)
    res.status(401).json({ message: "Token is not valid" })
  }
}

module.exports = auth
