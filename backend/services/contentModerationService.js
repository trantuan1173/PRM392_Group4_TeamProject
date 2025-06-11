
const nlp = require("compromise")
const ContentModerationLog = require("../models/ContentModerationLog")
const leoProfanity = require("leo-profanity")
class ContentModerationService {
  constructor() {
    // Add custom inappropriate words (Vietnamese and English)
    leoProfanity.add(["cave", "sex", "xxx", "porn"])
    // Patterns for detecting inappropriate content
    this.inappropriatePatterns = [
      /\b(sex|porn|xxx|nude|naked)\b/gi,
      /\b(escort|prostitute|hooker)\b/gi,
      /\b(sugar daddy|sugar baby)\b/gi,
      /\b(onlyfans|premium snap)\b/gi,
      /\b(venmo|paypal|cashapp)\s*me/gi,
      /\$\d+/g, // Money amounts
      /\b(instagram|snap|kik|telegram)\s*[:@]/gi,
    ]
  }

  async moderateText(text, contentType, userId = null) {
    try {
      if (!text || typeof text !== "string") {
        return { result: "approved", confidence: 1.0, flags: [] }
      }

      const flags = []
      let confidence = 0

      // Check for profanity
      if (leoProfanity.check(text)) {
        flags.push("inappropriate_language")
        confidence += 0.8
      }

      // Check for inappropriate patterns
      for (const pattern of this.inappropriatePatterns) {
        if (pattern.test(text)) {
          flags.push("inappropriate_content")
          confidence += 0.7
          break
        }
      }

      // Check for spam patterns (excessive repetition, all caps, etc.)
      if (this.isSpammy(text)) {
        flags.push("spam")
        confidence += 0.6
      }

      // Determine result based on confidence and flags
      let result = "approved"
      if (confidence >= 0.8) {
        result = "rejected"
      } else if (confidence >= 0.5) {
        result = "flagged"
      } else if (flags.length > 0) {
        result = "requires_review"
      }

      // Log the moderation result
      if (userId) {
        await this.logModerationResult(userId, contentType, text, result, confidence, flags)
      }

      return {
        result,
        confidence: Math.min(confidence, 1.0),
        flags,
      }
    } catch (error) {
      console.error("Content moderation error:", error)
      return { result: "requires_review", confidence: 0, flags: ["error"] }
    }
  }

  async moderateImage(imageUrl, contentType, userId = null) {
    try {
      // This would integrate with an image moderation service like AWS Rekognition
      // For now, we'll return a placeholder implementation

      // In a real implementation, you would:
      // 1. Send image to AWS Rekognition or similar service
      // 2. Check for nudity, violence, inappropriate content
      // 3. Return moderation result

      const result = "approved" // Placeholder
      const confidence = 0.9
      const flags = []

      if (userId) {
        await this.logModerationResult(userId, contentType, imageUrl, result, confidence, flags)
      }

      return { result, confidence, flags }
    } catch (error) {
      console.error("Image moderation error:", error)
      return { result: "requires_review", confidence: 0, flags: ["error"] }
    }
  }

  isSpammy(text) {
    // Check for excessive repetition
    const words = text.toLowerCase().split(/\s+/)
    const wordCount = {}

    for (const word of words) {
      wordCount[word] = (wordCount[word] || 0) + 1
    }

    // If any word appears more than 3 times, consider it spam
    for (const count of Object.values(wordCount)) {
      if (count > 3) return true
    }

    // Check for excessive caps
    const capsRatio = (text.match(/[A-Z]/g) || []).length / text.length
    if (capsRatio > 0.7 && text.length > 10) return true

    // Check for excessive punctuation
    const punctRatio = (text.match(/[!?.,;:]/g) || []).length / text.length
    if (punctRatio > 0.3) return true

    return false
  }

  async logModerationResult(userId, contentType, contentValue, result, confidence, flags) {
    try {
      const log = new ContentModerationLog({
        userId,
        contentType,
        contentValue,
        result,
        confidence,
        flags,
        reviewedBy: "ai",
      })

      await log.save()
    } catch (error) {
      console.error("Error logging moderation result:", error)
    }
  }

  async getModerationLogs(userId, limit = 50) {
    try {
      return await ContentModerationLog.find({ userId }).sort({ createdAt: -1 }).limit(limit)
    } catch (error) {
      console.error("Error getting moderation logs:", error)
      return []
    }
  }
}

module.exports = new ContentModerationService()
