const mongoose = require("mongoose")

const messageSchema = new mongoose.Schema(
  {
    matchId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Match",
      required: true,
    },
    fromUser: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
    },
    content: {
      type: String,
      required: true,
      maxlength: 1000,
    },
    messageType: {
      type: String,
      enum: ["text", "image", "gif"],
      default: "text",
    },
    isRead: {
      type: Boolean,
      default: false,
    },
    readAt: Date,
    isModerated: {
      type: Boolean,
      default: false,
    },
    moderationResult: {
      type: String,
      enum: ["approved", "rejected", "flagged"],
      default: "approved",
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

// Index for efficient message queries
messageSchema.index({ matchId: 1, createdAt: -1 })

module.exports = mongoose.model("Message", messageSchema)
