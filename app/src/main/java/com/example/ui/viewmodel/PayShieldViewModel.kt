package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiAssistantRepository
import com.example.data.ai.AssistantLanguage
import com.example.data.ai.ParsedPaymentIntent
import com.example.data.local.AutoPayMandateEntity
import com.example.data.local.BankAccountEntity
import com.example.data.local.InvestmentHoldingEntity
import com.example.data.local.PayShieldDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.SecurityAuditEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserPreferences
import com.example.data.repository.PayShieldRepository
import com.example.data.repository.PaymentOutcome
import com.example.security.FraudDecision
import com.example.security.FraudRiskAssessment
import com.example.security.FraudRiskEngine
import com.example.security.PacketInspectionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PendingSecureAction {
  data class Payment(
    val title: String,
    val counterparty: String,
    val category: String,
    val amountInr: Double,
    val fromAccountId: String,
    val toAccountId: String? = null,
    val paymentRail: String = "UPI",
    val note: String = "",
    val voucherCode: String? = null,
    val wasVpaPasted: Boolean = false,
  ) : PendingSecureAction

  data class Investment(
    val holdingId: String,
    val holdingName: String,
    val amountInr: Double,
    val fromAccountId: String,
    val isSipStart: Boolean,
  ) : PendingSecureAction

  data class BalanceInquiry(
    val account: BankAccountEntity,
  ) : PendingSecureAction
}

sealed interface AiCallState {
  data object Idle : AiCallState
  data object Loading : AiCallState
  data class Streaming(val text: String) : AiCallState
  data class Success(val text: String) : AiCallState
  data class Error(val message: String) : AiCallState
}

data class AssistantChatMessage(
  val id: Long,
  val isUser: Boolean,
  val text: String,
  val suggestedPayment: PendingSecureAction.Payment? = null,
)

data class StepUpWarningState(
  val action: PendingSecureAction.Payment,
  val assessment: FraudRiskAssessment,
)

data class PayShieldUiState(
  val accounts: List<BankAccountEntity> = emptyList(),
  val transactions: List<TransactionEntity> = emptyList(),
  val autoPayMandates: List<AutoPayMandateEntity> = emptyList(),
  val investments: List<InvestmentHoldingEntity> = emptyList(),
  val securityAudits: List<SecurityAuditEntity> = emptyList(),
  val preferences: UserPreferences = UserPreferences(),
  val isAppUnlocked: Boolean = true,
  val pendingAction: PendingSecureAction? = null,
  val stepUpWarning: StepUpWarningState? = null,
  val pinError: String? = null,
  val latestReceipt: TransactionEntity? = null,
  val latestInspection: PacketInspectionResult? = null,
  val latestBalanceInquiry: BankAccountEntity? = null,
  val blockedAlertMessage: String? = null,
  val selectedLanguage: AssistantLanguage = AssistantLanguage.ENGLISH,
  val assistantCallState: AiCallState = AiCallState.Idle,
  val assistantMessages: List<AssistantChatMessage> = emptyList(),
  val insightsCallState: AiCallState = AiCallState.Idle,
  val supportCallState: AiCallState = AiCallState.Idle,
)

class PayShieldViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: PayShieldRepository by lazy {
    val db = PayShieldDatabase.getInstance(application)
    PayShieldRepository(db.dao(), PreferencesManager(application))
  }

  private val aiRepository: AiAssistantRepository by lazy { AiAssistantRepository() }

  private val _isAppUnlocked = MutableStateFlow(true)
  private val _pendingAction = MutableStateFlow<PendingSecureAction?>(null)
  private val _stepUpWarning = MutableStateFlow<StepUpWarningState?>(null)
  private val _pinError = MutableStateFlow<String?>(null)
  private val _latestReceipt = MutableStateFlow<TransactionEntity?>(null)
  private val _latestInspection = MutableStateFlow<PacketInspectionResult?>(null)
  private val _latestBalanceInquiry = MutableStateFlow<BankAccountEntity?>(null)
  private val _blockedAlertMessage = MutableStateFlow<String?>(null)

  private val _selectedLanguage = MutableStateFlow(AssistantLanguage.ENGLISH)
  private val _assistantCallState = MutableStateFlow<AiCallState>(AiCallState.Idle)
  private val _assistantMessages = MutableStateFlow<List<AssistantChatMessage>>(emptyList())
  private val _insightsCallState = MutableStateFlow<AiCallState>(AiCallState.Idle)
  private val _supportCallState = MutableStateFlow<AiCallState>(AiCallState.Idle)

  init {
    viewModelScope.launch {
      val initialPrefs = repository.userPreferences.first()
      if (initialPrefs.biometricAppLockEnabled) {
        _isAppUnlocked.value = false
      }
    }
  }

  private data class ModalBundle(
    val isAppUnlocked: Boolean,
    val pendingAction: PendingSecureAction?,
    val stepUpWarning: StepUpWarningState?,
    val pinError: String?,
    val latestReceipt: TransactionEntity?,
    val latestInspection: PacketInspectionResult?,
    val latestBalanceInquiry: BankAccountEntity?,
    val blockedAlertMessage: String?,
  )

  private val modalFlow = combine(
    _isAppUnlocked,
    _pendingAction,
    _stepUpWarning,
    _pinError,
    _latestReceipt,
  ) { unlocked, pending, stepUp, pinErr, receipt ->
    ModalBundle(unlocked, pending, stepUp, pinErr, receipt, null, null, null)
  }.combine(_latestInspection) { b, insp ->
    b.copy(latestInspection = insp)
  }.combine(_latestBalanceInquiry) { b, bal ->
    b.copy(latestBalanceInquiry = bal)
  }.combine(_blockedAlertMessage) { b, blocked ->
    b.copy(blockedAlertMessage = blocked)
  }

  private data class AiBundle(
    val language: AssistantLanguage,
    val assistantState: AiCallState,
    val messages: List<AssistantChatMessage>,
    val insightsState: AiCallState,
    val supportState: AiCallState,
  )

  private val aiFlow = combine(
    _selectedLanguage,
    _assistantCallState,
    _assistantMessages,
    _insightsCallState,
    _supportCallState,
  ) { lang, aState, msgs, iState, sState ->
    AiBundle(lang, aState, msgs, iState, sState)
  }

  private data class PersistentBundle(
    val accounts: List<BankAccountEntity>,
    val transactions: List<TransactionEntity>,
    val autoPayMandates: List<AutoPayMandateEntity>,
    val investments: List<InvestmentHoldingEntity>,
    val securityAudits: List<SecurityAuditEntity>,
    val preferences: UserPreferences,
  )

  private val persistentFlow = combine(
    repository.accounts,
    repository.transactions,
    repository.autoPayMandates,
    repository.investments,
    repository.securityAudits,
  ) { accounts, txs, mandates, investments, audits ->
    PersistentBundle(accounts, txs, mandates, investments, audits, UserPreferences())
  }.combine(repository.userPreferences) { bundle, prefs ->
    bundle.copy(preferences = prefs)
  }

  val uiState: StateFlow<PayShieldUiState> = combine(persistentFlow, modalFlow, aiFlow) { p, m, a ->
    PayShieldUiState(
      accounts = p.accounts,
      transactions = p.transactions,
      autoPayMandates = p.autoPayMandates,
      investments = p.investments,
      securityAudits = p.securityAudits,
      preferences = p.preferences,
      isAppUnlocked = m.isAppUnlocked,
      pendingAction = m.pendingAction,
      stepUpWarning = m.stepUpWarning,
      pinError = m.pinError,
      latestReceipt = m.latestReceipt,
      latestInspection = m.latestInspection,
      latestBalanceInquiry = m.latestBalanceInquiry,
      blockedAlertMessage = m.blockedAlertMessage,
      selectedLanguage = a.language,
      assistantCallState = a.assistantState,
      assistantMessages = a.messages,
      insightsCallState = a.insightsState,
      supportCallState = a.supportState,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = PayShieldUiState(),
  )

  fun unlockApp() {
    _isAppUnlocked.value = true
  }

  fun lockAppForTesting() {
    _isAppUnlocked.value = false
  }

  fun setBiometricAppLock(enabled: Boolean) {
    viewModelScope.launch {
      PreferencesManager(getApplication()).setBiometricAppLock(enabled)
    }
  }

  fun toggleEasyReadMode() {
    viewModelScope.launch { repository.toggleEasyReadMode() }
  }

  fun toggleHideBalances() {
    viewModelScope.launch { repository.toggleHideBalances() }
  }

  fun setUpiInternational(enabled: Boolean, country: String) {
    viewModelScope.launch { repository.setUpiInternational(enabled, country) }
  }

  fun toggleAutoPay(mandate: AutoPayMandateEntity) {
    viewModelScope.launch {
      repository.toggleAutoPay(mandate.id, !mandate.isActive, mandate.merchantName)
    }
  }

  fun createAutoPayMandate(merchantName: String, category: String, maxAmount: Double) {
    viewModelScope.launch {
      repository.addAutoPayMandate(merchantName, category, maxAmount)
    }
  }

  fun requestPayment(action: PendingSecureAction.Payment) {
    val recentTxs = uiState.value.transactions
    val assessment = FraudRiskEngine.evaluateTransaction(
      context = getApplication(),
      amountInr = action.amountInr,
      counterpartyVpa = action.counterparty,
      recentTransactions = recentTxs,
      wasVpaPasted = action.wasVpaPasted,
    )

    when (assessment.decision) {
      FraudDecision.BLOCK -> {
        _blockedAlertMessage.value = assessment.consumerWarningMessage ?: "Transaction blocked for safety."
      }
      FraudDecision.STEP_UP_AUTH -> {
        _stepUpWarning.value = StepUpWarningState(action = action, assessment = assessment)
      }
      FraudDecision.ALLOW -> {
        proceedAfterRiskCheck(action)
      }
    }
  }

  fun confirmStepUpWarning() {
    val action = _stepUpWarning.value?.action ?: return
    _stepUpWarning.value = null
    proceedAfterRiskCheck(action)
  }

  fun dismissStepUpWarning() {
    _stepUpWarning.value = null
  }

  private fun proceedAfterRiskCheck(action: PendingSecureAction.Payment) {
    if (action.paymentRail == "UPI_LITE" && action.amountInr <= 500.0) {
      executePayment(action)
    } else {
      _pinError.value = null
      _pendingAction.value = action
    }
  }

  fun requestInvestment(action: PendingSecureAction.Investment) {
    _pinError.value = null
    _pendingAction.value = action
  }

  fun requestBalanceInquiry(account: BankAccountEntity) {
    _pinError.value = null
    _pendingAction.value = PendingSecureAction.BalanceInquiry(account)
  }

  fun cancelPendingAction() {
    _pendingAction.value = null
    _pinError.value = null
  }

  fun verifyPinAndExecute(enteredPin: String, biometricVerified: Boolean = false) {
    if (!biometricVerified && enteredPin != "1234") {
      _pinError.value = "Incorrect UPI PIN. Use Fingerprint or demo PIN 1234."
      return
    }
    val action = _pendingAction.value ?: return
    _pendingAction.value = null
    _pinError.value = null

    when (action) {
      is PendingSecureAction.Payment -> executePayment(action)
      is PendingSecureAction.Investment -> executeInvestment(action)
      is PendingSecureAction.BalanceInquiry -> _latestBalanceInquiry.value = action.account
    }
  }

  private fun executePayment(action: PendingSecureAction.Payment) {
    viewModelScope.launch {
      when (
        val outcome = repository.executeSecurePayment(
          title = action.title,
          counterparty = action.counterparty,
          category = action.category,
          amountInr = action.amountInr,
          fromAccountId = action.fromAccountId,
          toAccountId = action.toAccountId,
          paymentRail = action.paymentRail,
          note = action.note,
          voucherCode = action.voucherCode,
        )
      ) {
        is PaymentOutcome.Success -> {
          _latestReceipt.value = outcome.transaction
          _latestInspection.value = outcome.inspection
        }
        is PaymentOutcome.Blocked -> {
          _blockedAlertMessage.value = outcome.reason
          _latestInspection.value = outcome.inspection
        }
      }
    }
  }

  private fun executeInvestment(action: PendingSecureAction.Investment) {
    viewModelScope.launch {
      when (
        val outcome = repository.investInAsset(
          holdingId = action.holdingId,
          amountInr = action.amountInr,
          fromAccountId = action.fromAccountId,
          isSipStart = action.isSipStart,
        )
      ) {
        is PaymentOutcome.Success -> {
          _latestReceipt.value = outcome.transaction
          _latestInspection.value = outcome.inspection
        }
        is PaymentOutcome.Blocked -> {
          _blockedAlertMessage.value = outcome.reason
          _latestInspection.value = outcome.inspection
        }
      }
    }
  }

  fun setLanguage(lang: AssistantLanguage) {
    _selectedLanguage.value = lang
  }

  fun sendAssistantCommand(input: String) {
    val cleanInput = input.trim()
    if (cleanInput.isEmpty()) return

    val userMsg = AssistantChatMessage(
      id = System.currentTimeMillis(),
      isUser = true,
      text = cleanInput,
    )
    _assistantMessages.value = _assistantMessages.value + userMsg
    _assistantCallState.value = AiCallState.Loading

    viewModelScope.launch {
      try {
        val primaryAcc = uiState.value.accounts.firstOrNull { it.isPrimary } ?: uiState.value.accounts.firstOrNull()
        val parsed: ParsedPaymentIntent = aiRepository.parseConversationalCommand(
          userMessage = cleanInput,
          language = _selectedLanguage.value,
          recentBalanceInr = primaryAcc?.balanceInr ?: 0.0,
        )

        val suggestedPayment = when (parsed.intentType) {
          "SEND_MONEY" -> {
            val recipient = parsed.recipientName.ifBlank { "Dad" }
            val amt = if (parsed.amountInr > 0) parsed.amountInr else 500.0
            val vpaHandle = "${recipient.lowercase().replace(" ", "")}@payshield"
            PendingSecureAction.Payment(
              title = recipient,
              counterparty = vpaHandle,
              category = "TRANSFER",
              amountInr = amt,
              fromAccountId = primaryAcc?.id ?: "acc_hdfc",
              paymentRail = if (amt <= 500.0) "UPI_LITE" else "UPI",
              note = "Voice/AI Assistant Payment",
            )
          }
          "PAY_BILL" -> {
            val biller = parsed.billCategory.ifBlank { "Electricity Bill" }
            val amt = if (parsed.amountInr > 0) parsed.amountInr else 1840.0
            PendingSecureAction.Payment(
              title = biller,
              counterparty = "${biller.lowercase().replace(" ", "")}@bbps",
              category = "BILL_PAY",
              amountInr = amt,
              fromAccountId = primaryAcc?.id ?: "acc_hdfc",
              paymentRail = "UPI",
              note = "AI Assisted Bill Payment",
            )
          }
          else -> null
        }

        if (parsed.intentType == "CHECK_BALANCE" && primaryAcc != null) {
          requestBalanceInquiry(primaryAcc)
        }

        val replyMsg = AssistantChatMessage(
          id = System.currentTimeMillis() + 1,
          isUser = false,
          text = parsed.assistantReply,
          suggestedPayment = suggestedPayment,
        )
        _assistantMessages.value = _assistantMessages.value + replyMsg
        _assistantCallState.value = AiCallState.Success(parsed.assistantReply)
      } catch (e: Exception) {
        val errorMsg = e.localizedMessage ?: "AI Assistant error."
        _assistantCallState.value = AiCallState.Error(errorMsg)
      }
    }
  }

  fun generateSmartSpendInsights() {
    _insightsCallState.value = AiCallState.Loading
    viewModelScope.launch {
      try {
        val totalInv = uiState.value.investments.sumOf { it.currentValueInr }
        var latestText = ""
        aiRepository.streamSmartSpendInsights(
          transactions = uiState.value.transactions,
          totalInvestedInr = totalInv,
          language = _selectedLanguage.value,
        ).collect { text ->
          latestText = text
          _insightsCallState.value = AiCallState.Streaming(text)
        }
        if (latestText.isNotBlank()) {
          _insightsCallState.value = AiCallState.Success(latestText)
        }
      } catch (e: Exception) {
        _insightsCallState.value = AiCallState.Error(e.localizedMessage ?: "Unable to load insights.")
      }
    }
  }

  fun troubleshootTransaction(issueDescription: String, tx: TransactionEntity?) {
    _supportCallState.value = AiCallState.Loading
    viewModelScope.launch {
      try {
        var latestText = ""
        aiRepository.streamSupportTroubleshooting(
          issueDescription = issueDescription,
          relatedTransaction = tx,
          language = _selectedLanguage.value,
        ).collect { text ->
          latestText = text
          _supportCallState.value = AiCallState.Streaming(text)
        }
        if (latestText.isNotBlank()) {
          _supportCallState.value = AiCallState.Success(latestText)
        }
      } catch (e: Exception) {
        _supportCallState.value = AiCallState.Error(e.localizedMessage ?: "Support bot failed.")
      }
    }
  }

  fun showReceipt(tx: TransactionEntity) {
    _latestReceipt.value = tx
  }

  fun dismissReceipt() {
    _latestReceipt.value = null
    _latestInspection.value = null
  }

  fun dismissBalanceInquiry() {
    _latestBalanceInquiry.value = null
  }

  fun dismissBlockedAlert() {
    _blockedAlertMessage.value = null
  }
}
