const swaggerConfig = {
  definition: {
    openapi: "3.0.0",
    info: {
      title: "Tinder Clone API",
      version: "1.0.0",
      description:
        "A comprehensive dating app API with advanced features including date scheduling, content moderation, and real-time messaging",
      contact: {
        name: "API Support",
        email: "support@tinderclone.com",
      },
    },
    servers: [
      {
        url: "http://localhost:3000",
        description: "Development server",
      },
      {
        url: "https://api.tinderclone.com",
        description: "Production server",
      },
    ],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: "http",
          scheme: "bearer",
          bearerFormat: "JWT",
        },
      },
      schemas: {
        User: {
          type: "object",
          properties: {
            _id: { type: "string", example: "64f1a2b3c4d5e6f7g8h9i0j1" },
            name: { type: "string", example: "John Doe" },
            email: { type: "string", example: "john@example.com" },
            age: { type: "number", example: 25 },
            gender: { type: "string", enum: ["male", "female", "other"], example: "male" },
            bio: { type: "string", example: "Love traveling and photography" },
            avatar: { type: "string", example: "https://example.com/avatar.jpg" },
            location: {
              type: "object",
              properties: {
                type: { type: "string", example: "Point" },
                coordinates: { type: "array", items: { type: "number" }, example: [106.6297, 10.8231] },
                address: { type: "string", example: "Ho Chi Minh City, Vietnam" },
              },
            },
            preferences: {
              type: "object",
              properties: {
                ageRange: {
                  type: "object",
                  properties: {
                    min: { type: "number", example: 20 },
                    max: { type: "number", example: 30 },
                  },
                },
                maxDistance: { type: "number", example: 50 },
                interestedIn: { type: "string", enum: ["male", "female", "both"], example: "female" },
              },
            },
            photos: {
              type: "array",
              items: {
                type: "object",
                properties: {
                  url: { type: "string" },
                  isMain: { type: "boolean" },
                },
              },
            },
            isOnline: { type: "boolean", example: true },
            lastSeen: { type: "string", format: "date-time" },
            createdAt: { type: "string", format: "date-time" },
          },
        },
        Match: {
          type: "object",
          properties: {
            _id: { type: "string" },
            user1: { $ref: "#/components/schemas/User" },
            user2: { $ref: "#/components/schemas/User" },
            isActive: { type: "boolean", example: true },
            lastMessageAt: { type: "string", format: "date-time" },
            createdAt: { type: "string", format: "date-time" },
          },
        },
        Message: {
          type: "object",
          properties: {
            _id: { type: "string" },
            matchId: { type: "string" },
            fromUser: { $ref: "#/components/schemas/User" },
            content: { type: "string", example: "Hello! How are you?" },
            messageType: { type: "string", enum: ["text", "image", "gif"], example: "text" },
            isRead: { type: "boolean", example: false },
            readAt: { type: "string", format: "date-time" },
            createdAt: { type: "string", format: "date-time" },
          },
        },
        DateSchedule: {
          type: "object",
          properties: {
            _id: { type: "string" },
            matchId: { type: "string" },
            proposedBy: { $ref: "#/components/schemas/User" },
            dateTime: { type: "string", format: "date-time", example: "2024-01-15T19:00:00Z" },
            location: {
              type: "object",
              properties: {
                name: { type: "string", example: "Starbucks Coffee" },
                address: { type: "string", example: "123 Main St, Ho Chi Minh City" },
                coordinates: { type: "array", items: { type: "number" } },
              },
            },
            note: { type: "string", example: "Looking forward to meeting you!" },
            status: {
              type: "string",
              enum: ["pending", "accepted", "rejected", "completed", "cancelled"],
              example: "pending",
            },
            createdAt: { type: "string", format: "date-time" },
          },
        },
        Notification: {
          type: "object",
          properties: {
            _id: { type: "string" },
            userId: { type: "string" },
            title: { type: "string", example: "New Match!" },
            message: { type: "string", example: "You matched with Sarah!" },
            type: {
              type: "string",
              enum: ["match", "message", "date_request", "date_reminder", "like", "system"],
              example: "match",
            },
            data: { type: "object" },
            isRead: { type: "boolean", example: false },
            createdAt: { type: "string", format: "date-time" },
          },
        },
        Error: {
          type: "object",
          properties: {
            message: { type: "string", example: "Error message" },
            errors: {
              type: "array",
              items: {
                type: "object",
                properties: {
                  field: { type: "string" },
                  message: { type: "string" },
                },
              },
            },
          },
        },
      },
    },
    security: [
      {
        bearerAuth: [],
      },
    ],
  },
  apis: ["./routes/*.js", "./controllers/*.js"],
}

module.exports = swaggerConfig
