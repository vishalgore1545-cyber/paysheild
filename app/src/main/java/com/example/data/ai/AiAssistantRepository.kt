package com.example.data.ai

import android.util.Log
import com.example.data.local.TransactionEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiAssistantRepository"
private const val MODEL_NAME = "gemini-3.8-flash"

enum class AssistantLanguage(val displayName: String, val localeTag: String) {
  ENGLISH("English", "en-IN"),
  HINDI("हिन्दी", "hi-IN"),
  MARATHI("मराठी", "mr-IN"),
}

data class ParsedPaymentIntent(
  val intentType: String, // "SEND_MONEY", "CHECK_BALANCE", "PAY_BILL", "SUPPORT_HELP", "GENERAL"
  val recipientName: String,
  val amountInr: Double,
  val billCategory: String,
  val assistantReply: String,
)

class AiAssistantRepository {

  private val commandSchema = Schema.obj(
    properties = mapOf(
      "intentType" to Schema.enumeration(
        values = listOf("SEND_MONEY", "CHECK_BALANCE", "PAY_BILL", "SUPPORT_HELP", "GENERAL"),
        description = "Detected user financial intent",
      ),
      "recipientName" to Schema.string(description = "Person, contact, or biller name if mentioned, else empty"),
      "amountInr" to Schema.double(description = "Numeric INR amount if mentioned, else 0.0"),
      "billCategory" to Schema.string(description = "Bill category like Electricity, Mobile, Gas, FASTag if relevant, else empty"),
      "assistantReply" to Schema.string(description = "Warm, concise, accessible reply in the requested language (English, Hindi, or Marathi)"),
    ),
  )

  private val commandModel by lazy {
    Firebase.ai.generativeModel(
      modelName = MODEL_NAME,
      generationConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = commandSchema
      },
      systemInstruction = content {
        text(
          "You are PayShield Assistant, a warm, simple, accessible fintech voice/chat assistant for everyday users in India. " +
            "Understand commands in English, Hindi, and Marathi (including Romanized Hindi/Marathi). " +
            "Always reply in the user's selected language clearly without technical jargon. " +
            "Extract payment amounts, recipient names (like Dad, Priya, Rohan), bill categories, or balance check intents accurately."
        )
      },
    )
  }

  private val generalModel by lazy {
    Firebase.ai.generativeModel(
      modelName = MODEL_NAME,
      systemInstruction = content {
        text(
          "You are PayShield Smart Financial & Support Coach. Give concise, friendly, actionable insights " +
            "about spending, monthly budgets, SIP savings, and UPI transaction troubleshooting in plain language."
        )
      },
    )
  }

  suspend fun parseConversationalCommand(
    userMessage: String,
    language: AssistantLanguage,
    recentBalanceInr: Double,
  ): ParsedPaymentIntent {
    try {
      val prompt = """
        Selected Language: ${language.displayName}
        User Primary Bank Balance: ₹${"%.2f".format(recentBalanceInr)}
        User Input: "$userMessage"
        
        Parse the user's intent and respond warmly in ${language.displayName}.
      """.trimIndent()

      val response = withTimeout(60_000L) {
        commandModel.generateContent(prompt)
      }
      val jsonStr = response.text ?: throw IllegalStateException("Empty response from Gemini.")
      val json = JSONObject(jsonStr)
      return ParsedPaymentIntent(
        intentType = json.optString("intentType", "GENERAL"),
        recipientName = json.optString("recipientName", ""),
        amountInr = json.optDouble("amountInr", 0.0),
        billCategory = json.optString("billCategory", ""),
        assistantReply = json.optString("assistantReply", "I'm ready to help with your payment."),
      )
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "Command parsing timed out", e)
      throw IllegalStateException("AI Assistant timed out after 60s. Please try again.")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Command parsing failed", e)
      throw IllegalStateException(e.localizedMessage ?: "Unable to reach PayShield AI right now.")
    }
  }

  fun streamSmartSpendInsights(
    transactions: List<TransactionEntity>,
    totalInvestedInr: Double,
    language: AssistantLanguage,
  ): Flow<String> = flow {
    val txSummary = transactions.take(10).joinToString("\n") {
      "- ${it.title} (${it.category}): ₹${"%.0f".format(it.amountInr)} [${it.status}]"
    }
    val prompt = """
      Language: ${language.displayName}
      Analyze these recent transactions and investment total (₹${"%.0f".format(totalInvestedInr)}):
      $txSummary
      
      Provide 3 short, friendly bullet points:
      1. Top spending category & monthly budget tip
      2. Upcoming bill / AutoPay reminder
      3. Personalized SIP / Gold savings recommendation
    """.trimIndent()

    val accumulated = StringBuilder()
    try {
      withTimeout(60_000L) {
        generalModel.generateContentStream(prompt).collect { chunk ->
          val part = chunk.text.orEmpty()
          if (part.isNotEmpty()) {
            accumulated.append(part)
            emit(accumulated.toString())
          }
        }
      }
      if (accumulated.isBlank()) {
        throw IllegalStateException("Model returned empty financial insights.")
      }
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "Insights streaming timed out", e)
      throw IllegalStateException("Insights generation timed out. Please retry.")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Insights streaming failed", e)
      throw IllegalStateException(e.localizedMessage ?: "Failed to generate spend insights.")
    }
  }

  fun streamSupportTroubleshooting(
    issueDescription: String,
    relatedTransaction: TransactionEntity?,
    language: AssistantLanguage,
  ): Flow<String> = flow {
    val txContext = if (relatedTransaction != null) {
      "Transaction UTR: ${relatedTransaction.txId}, Title: ${relatedTransaction.title}, " +
        "Amount: ₹${relatedTransaction.amountInr}, Status: ${relatedTransaction.status}, Rail: ${relatedTransaction.paymentRail}"
    } else {
      "No specific transaction selected."
    }

    val prompt = """
      Language: ${language.displayName}
      User Issue: "$issueDescription"
      Transaction Context: $txContext
      
      Provide clear, reassuring step-by-step UPI resolution guidance (NPCI T+1/T+5 auto-reversal SLA, UTR tracking, and next actions). Keep it concise and easy for first-time users.
    """.trimIndent()

    val accumulated = StringBuilder()
    try {
      withTimeout(60_000L) {
        generalModel.generateContentStream(prompt).collect { chunk ->
          val part = chunk.text.orEmpty()
          if (part.isNotEmpty()) {
            accumulated.append(part)
            emit(accumulated.toString())
          }
        }
      }
      if (accumulated.isBlank()) {
        throw IllegalStateException("Support bot returned an empty response.")
      }
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "Support bot timed out", e)
      throw IllegalStateException("Support assistant timed out. Please try again.")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Support bot failed", e)
      throw IllegalStateException(e.localizedMessage ?: "Support assistant error.")
    }
  }
}
