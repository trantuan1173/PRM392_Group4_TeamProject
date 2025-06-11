const mongoose = require("mongoose")

const contentModerationLogSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
    },
    contentType: {
      type: String,
      enum: ["profile_photo", "bio", "message", "profile_name"],
      required: true,
    },
    contentValue: {
      type: String,
      required: true,
    },
    result: {
      type: String,
      enum: ["approved", "rejected", "flagged", "requires_review"],
      required: true,
    },
    confidence: {
      type: Number,
      min: 0,
      max: 1,
    },
    flags: [
      {
        type: String,
        enum: ["nudity", "violence", "hate_speech", "spam", "inappropriate_language"],
      },
    ],
    reviewedBy: {
      type: String,
      enum: ["ai", "human"],
      default: "ai",
    },
    createdAt: {
      type: Date,
      default: Date.now,
    },
  },
  {
    timestamps: true,
  },
)

module.exports = mongoose.model("ContentModerationLog", contentModerationLogSchema)
