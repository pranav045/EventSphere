
const { setGlobalOptions } = require("firebase-functions");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const logger = require("firebase-functions/logger");
const OpenAI = require("openai");

setGlobalOptions({ maxInstances: 10 });

const groqApiKey = defineSecret("GROQ_API_KEY");

exports.chatWithAI = onCall(
  {
    secrets: [groqApiKey],
    timeoutSeconds: 60,
    maxInstances: 5
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "Please sign in to use the chatbot."
      );
    }

    const message = request.data?.message;

    if (
      typeof message !== "string" ||
      !message.trim() ||
      message.length > 2000
    ) {
      throw new HttpsError(
        "invalid-argument",
        "Enter a message between 1 and 2000 characters."
      );
    }

    try {
      const client = new OpenAI({
        apiKey: groqApiKey.value(),
        baseURL: "https://api.groq.com/openai/v1"
      });

      const response = await client.responses.create({
        model: "openai/gpt-oss-20b",
        instructions:
          "You are EventSphere's friendly campus assistant. Help students with campus events, registrations, event planning, and college-related questions. Be clear, concise, and helpful. Do not invent event details or claim access to live EventSphere data.",
        input: message.trim()
      });

      return {
        reply: response.output_text ||
          "Sorry, I couldn't generate a response."
      };
    } catch (error) {
      logger.error("Groq chatbot error:", error);

      throw new HttpsError(
        "internal",
        "Unable to get a response right now. Please try again."
      );
    }
  }
);
