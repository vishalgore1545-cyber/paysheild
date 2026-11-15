package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ai.AssistantLanguage
import com.example.data.local.TransactionEntity
import com.example.ui.components.formatInr
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.AiCallState
import com.example.ui.viewmodel.PayShieldUiState
import com.example.ui.viewmodel.PendingSecureAction

@Composable
fun AssistantScreen(
  uiState: PayShieldUiState,
  onSelectLanguage: (AssistantLanguage) -> Unit,
  onSendCommand: (String) -> Unit,
  onRequestPayment: (PendingSecureAction.Payment) -> Unit,
  onGenerateInsights: () -> Unit,
  onTroubleshootTx: (String, TransactionEntity?) -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedSubTab by remember { mutableIntStateOf(0) }
  var chatInput by remember { mutableStateOf("") }
  var voiceNote by remember { mutableStateOf<String?>(null) }

  val speechLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      val spoken = result.data
        ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        ?.firstOrNull()
      if (!spoken.isNullOrBlank()) {
        voiceNote = null
        onSendCommand(spoken)
      }
    }
  }

  val launchVoiceInput: () -> Unit = {
    try {
      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, uiState.selectedLanguage.localeTag)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak in ${uiState.selectedLanguage.displayName}...")
      }
      speechLauncher.launch(intent)
    } catch (_: Exception) {
      voiceNote = "Voice input unavailable on this device; type or tap a quick command below."
    }
  }

  Column(modifier = modifier.fillMaxSize()) {
    // Language Selector Strip
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      AssistantLanguage.entries.forEach { lang ->
        FilterChip(
          selected = uiState.selectedLanguage == lang,
          onClick = { onSelectLanguage(lang) },
          label = { Text(lang.displayName) },
          modifier = Modifier.testTag("lang_chip_${lang.name.lowercase()}"),
        )
      }
    }

    TabRow(selectedTabIndex = selectedSubTab) {
      Tab(
        selected = selectedSubTab == 0,
        onClick = { selectedSubTab = 0 },
        text = { Text("Voice & Chat") },
        icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) },
      )
      Tab(
        selected = selectedSubTab == 1,
        onClick = { selectedSubTab = 1 },
        text = { Text("Spend Insights") },
        icon = { Icon(Icons.Filled.PieChart, contentDescription = null, modifier = Modifier.size(18.dp)) },
      )
      Tab(
        selected = selectedSubTab == 2,
        onClick = { selectedSubTab = 2 },
        text = { Text("Support Bot") },
        icon = { Icon(Icons.Filled.SupportAgent, contentDescription = null, modifier = Modifier.size(18.dp)) },
      )
    }

    when (selectedSubTab) {
      0 -> VoiceChatTab(
        uiState = uiState,
        chatInput = chatInput,
        onChatInputChange = { chatInput = it },
        voiceNote = voiceNote,
        onLaunchVoice = launchVoiceInput,
        onSendCommand = {
          val text = chatInput
          chatInput = ""
          onSendCommand(text)
        },
        onQuickCommand = onSendCommand,
        onConfirmSuggestedPayment = onRequestPayment,
      )
      1 -> SmartSpendInsightsTab(
        uiState = uiState,
        onGenerateInsights = onGenerateInsights,
      )
      2 -> SupportBotTab(
        uiState = uiState,
        onTroubleshootTx = onTroubleshootTx,
      )
    }
  }
}

@Composable
private fun VoiceChatTab(
  uiState: PayShieldUiState,
  chatInput: String,
  onChatInputChange: (String) -> Unit,
  voiceNote: String?,
  onLaunchVoice: () -> Unit,
  onSendCommand: () -> Unit,
  onQuickCommand: (String) -> Unit,
  onConfirmSuggestedPayment: (PendingSecureAction.Payment) -> Unit,
) {
  val suggestions = when (uiState.selectedLanguage) {
    AssistantLanguage.ENGLISH -> listOf("Send ₹500 to Dad", "Check my HDFC balance", "Pay Electricity bill ₹1840")
    AssistantLanguage.HINDI -> listOf("पापा को ₹500 भेजो", "मेरा बैंक बैलेंस बताओ", "बिजली बिल भरो")
    AssistantLanguage.MARATHI -> listOf("दादांना ₹500 पाठवा", "माझा बँक बॅलन्स तपासा", "वीज बिल भरा")
  }

  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    LazyColumn(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "PayShield Conversational Assistant",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
              text = "Speak or type in English, हिन्दी, or मराठी to send money, check balances, or pay bills.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
          }
        }
      }

      item {
        Row(
          modifier = Modifier.horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          suggestions.forEach { sample ->
            OutlinedButton(onClick = { onQuickCommand(sample) }) {
              Text(sample)
            }
          }
        }
      }

      items(uiState.assistantMessages, key = { it.id }) { msg ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
        ) {
          Surface(
            color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(0.88f),
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = msg.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
              )
              msg.suggestedPayment?.let { payment ->
                Button(
                  onClick = { onConfirmSuggestedPayment(payment) },
                  modifier = Modifier.fillMaxWidth().height(48.dp).testTag("ai_confirm_payment_btn"),
                ) {
                  Text("Pay ${formatInr(payment.amountInr)} to ${payment.title}")
                }
              }
            }
          }
        }
      }

      when (val state = uiState.assistantCallState) {
        AiCallState.Loading -> {
          item {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
              Text("Understanding command via Gemini AI...", style = MaterialTheme.typography.bodySmall)
            }
          }
        }
        is AiCallState.Error -> {
          item {
            AiErrorBanner(state.message)
          }
        }
        else -> Unit
      }
    }

    if (voiceNote != null) {
      Text(
        text = voiceNote,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.padding(bottom = 6.dp),
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      IconButton(
        onClick = onLaunchVoice,
        modifier = Modifier.minimumInteractiveComponentSize().testTag("ai_voice_mic_btn"),
      ) {
        Icon(Icons.Filled.Mic, contentDescription = "Voice Command", tint = MaterialTheme.colorScheme.primary)
      }
      OutlinedTextField(
        value = chatInput,
        onValueChange = onChatInputChange,
        placeholder = { Text("Ask in ${uiState.selectedLanguage.displayName}...") },
        singleLine = true,
        modifier = Modifier.weight(1f).testTag("ai_chat_input"),
      )
      IconButton(
        onClick = onSendCommand,
        enabled = chatInput.isNotBlank() && uiState.assistantCallState !is AiCallState.Loading,
        modifier = Modifier.minimumInteractiveComponentSize().testTag("ai_chat_send_btn"),
      ) {
        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
      }
    }
  }
}

@Composable
private fun SmartSpendInsightsTab(
  uiState: PayShieldUiState,
  onGenerateInsights: () -> Unit,
) {
  val totalSpent = uiState.transactions
    .filter { !it.isCredit && it.status == "SUCCESS" }
    .sumOf { it.amountInr }
  val monthlyBudget = 25_000.0
  val budgetProgress = (totalSpent / monthlyBudget).toFloat().coerceIn(0f, 1f)

  val byCategory = uiState.transactions
    .filter { !it.isCredit && it.status == "SUCCESS" }
    .groupBy { it.category }
    .mapValues { (_, list) -> list.sumOf { it.amountInr } }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Monthly Spend & Budget Tracker", style = MaterialTheme.typography.titleLarge)
          Text(
            text = "${formatInr(totalSpent)} spent of ${formatInr(monthlyBudget)} budget",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = JetBrainsMonoFamily,
            color = MaterialTheme.colorScheme.primary,
          )
          LinearProgressIndicator(
            progress = { budgetProgress },
            modifier = Modifier.fillMaxWidth().height(10.dp),
          )
          byCategory.forEach { (cat, sum) ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(cat.replace("_", " "), style = MaterialTheme.typography.bodyMedium)
              Text(formatInr(sum), style = MaterialTheme.typography.bodyMedium, fontFamily = JetBrainsMonoFamily)
            }
          }
        }
      }
    }

    item {
      Button(
        onClick = onGenerateInsights,
        enabled = uiState.insightsCallState !is AiCallState.Loading,
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("generate_ai_insights_btn"),
      ) {
        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Generate Smart Spend & SIP Insights")
      }
    }

    item {
      when (val state = uiState.insightsCallState) {
        AiCallState.Idle -> {
          Text(
            text = "Tap above to analyze your spending categories, upcoming bill reminders, and personalized SIP tips via Gemini AI.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        AiCallState.Loading -> {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp))
            Text("Analyzing transactions with Gemini AI...")
          }
        }
        is AiCallState.Streaming -> {
          AiInsightCard(title = "Live AI Financial Insights", body = state.text)
        }
        is AiCallState.Success -> {
          AiInsightCard(title = "Smart Financial Insights", body = state.text)
        }
        is AiCallState.Error -> {
          AiErrorBanner(state.message)
        }
      }
    }
  }
}

@Composable
private fun SupportBotTab(
  uiState: PayShieldUiState,
  onTroubleshootTx: (String, TransactionEntity?) -> Unit,
) {
  val pendingOrRecent = uiState.transactions
  var selectedTx by remember(pendingOrRecent) {
    mutableStateOf(pendingOrRecent.firstOrNull { it.status != "SUCCESS" } ?: pendingOrRecent.firstOrNull())
  }
  var customIssue by remember { mutableStateOf("Money debited but payment is showing pending") }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text("24x7 Automated AI Support Bot", style = MaterialTheme.typography.titleLarge)
      Text(
        text = "Select a transaction below for instant pending/failed payment resolution & UTR tracking.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    items(pendingOrRecent.take(4), key = { it.txId }) { tx ->
      val isSelected = selectedTx?.txId == tx.txId
      Card(
        onClick = { selectedTx = tx },
        colors = CardDefaults.cardColors(
          containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(tx.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("${tx.txId} • ${tx.status}", style = MaterialTheme.typography.labelSmall)
          }
          Text(formatInr(tx.amountInr), style = MaterialTheme.typography.titleSmall, fontFamily = JetBrainsMonoFamily)
        }
      }
    }

    item {
      OutlinedTextField(
        value = customIssue,
        onValueChange = { customIssue = it },
        label = { Text("Describe your issue") },
        modifier = Modifier.fillMaxWidth().testTag("support_issue_input"),
      )
      Spacer(modifier = Modifier.height(8.dp))
      Button(
        onClick = { onTroubleshootTx(customIssue, selectedTx) },
        enabled = customIssue.isNotBlank() && uiState.supportCallState !is AiCallState.Loading,
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("support_resolve_btn"),
      ) {
        Icon(Icons.Filled.SupportAgent, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Check Status & Troubleshoot")
      }
    }

    item {
      when (val state = uiState.supportCallState) {
        AiCallState.Idle -> Unit
        AiCallState.Loading -> {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp))
            Text("Checking NPCI switch guidelines...")
          }
        }
        is AiCallState.Streaming -> AiInsightCard("AI Support Resolution", state.text)
        is AiCallState.Success -> AiInsightCard("AI Support Resolution", state.text)
        is AiCallState.Error -> AiErrorBanner(state.message)
      }
    }
  }
}

@Composable
private fun AiInsightCard(title: String, body: String) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
      Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
  }
}

@Composable
private fun AiErrorBanner(message: String) {
  Surface(
    color = MaterialTheme.colorScheme.errorContainer,
    shape = MaterialTheme.shapes.small,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
      Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
    }
  }
}
