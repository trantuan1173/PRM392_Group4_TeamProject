const mongoose = require("mongoose")

const reportSchema = new mongoose.Schema(
  {
    reporter: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
    },
    reportedUser: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
    },
    reason: {
      type: String,
      required: true,
      enum: [
        "inappropriate_photos",
        "harassment",
        "fake_profile",
        "spam",
        "inappropriate_messages",
        "underage",
        "other",
      ],
    },
    description: {
      type: String,
      maxlength: 1000,
    },
    evidence: [
      {
        type: String, // URLs to screenshots or other evidence
      },
    ],
    status: {
      type: String,
      enum: ["pending", "investigating", "resolved", "dismissed"],
      default: "pending",
    },
    reviewedBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
    },
    reviewedAt: Date,
    action: {
      type: String,
      enum: ["none", "warning", "temporary_ban", "permanent_ban", "content_removal"],
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

module.exports = mongoose.model("Report", reportSchema)
