const express = require("express")
const mongoose = require("mongoose")
const cors = require("cors")
const helmet = require("helmet")
const rateLimit = require("express-rate-limit")
const http = require("http")
const socketIo = require("socket.io")
require("dotenv").config()

const authRoutes = require("./routes/authRoutes")
const userRoutes = require("./routes/userRoutes")
const swipeRoutes = require("./routes/swipeRoutes")
const matchRoutes = require("./routes/matchRoutes")
const messageRoutes = require("./routes/messageRoutes")
const dateRoutes = require("./routes/dateRoutes")
const reportRoutes = require("./routes/reportRoutes")
const notificationRoutes = require("./routes/notificationRoutes")
const contentModerationRoutes = require("./routes/contentModerationRoutes")

const swaggerJsdoc = require("swagger-jsdoc")
const swaggerUi = require("swagger-ui-express")
const swaggerConfig = require("./config/swagger")

const socketHandler = require("./utils/socketHandler")
const errorHandler = require("./middleware/errorHandler")

const app = express()
app.set("trust proxy", 1);
const server = http.createServer(app)
const io = socketIo(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"],
  },
})

// Security middleware
app.use(helmet())
app.use(cors())

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
})
app.use(limiter)

// Body parsing middleware
app.use(express.json({ limit: "10mb" }))
app.use(express.urlencoded({ extended: true }))

// Swagger setup
const specs = swaggerJsdoc(swaggerConfig)
app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(specs))

// Database connection
mongoose
  .connect(process.env.MONGODB_URI, {
    dbName: "PRM392_TinderApp",
    useNewUrlParser: true,
    useUnifiedTopology: true,
  })
  .then(() => console.log("MongoDB connected"))
  .catch((err) => console.error("MongoDB connection error:", err))

// Socket.io setup
socketHandler(io)

// Routes
app.use("/api/auth", authRoutes)
app.use("/api/users", userRoutes)
app.use("/api/swipes", swipeRoutes)
app.use("/api/matches", matchRoutes)
app.use("/api/messages", messageRoutes)
app.use("/api/dates", dateRoutes)
app.use("/api/reports", reportRoutes)
app.use("/api/notifications", notificationRoutes)
app.use("/api/moderation", contentModerationRoutes)

// Error handling middleware
app.use(errorHandler)

const PORT = process.env.PORT || 3000
server.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`)
})

module.exports = { app, io }
