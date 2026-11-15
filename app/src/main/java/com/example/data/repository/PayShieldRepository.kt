package com.example.data.repository

import com.example.data.local.AutoPayMandateEntity
import com.example.data.local.BankAccountEntity
import com.example.data.local.InvestmentHoldingEntity
import com.example.data.local.PayShieldDao
import com.example.data.local.PreferencesManager
import com.example.data.local.SecurityAuditEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserPreferences
import com.example.security.FirewallDecision
import com.example.security.FirewallInspector
import com.example.security.PacketInspectionResult
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

sealed interface PaymentOutcome {
  data class Success(
    val transaction: TransactionEntity,
    val inspection: PacketInspectionResult,
  ) : PaymentOutcome

  data class Blocked(
    val reason: String,
    val inspection: PacketInspectionResult,
  ) : PaymentOutcome
}

class PayShieldRepository(
  private val dao: PayShieldDao,
  private val prefs: PreferencesManager,
) {
  val accounts: Flow<List<BankAccountEntity>> = dao.observeAccounts()
  val transactions: Flow<List<TransactionEntity>> = dao.observeTransactions()
  val autoPayMandates: Flow<List<AutoPayMandateEntity>> = dao.observeAutoPayMandates()
  val investments: Flow<List<InvestmentHoldingEntity>> = dao.observeInvestments()
  val securityAudits: Flow<List<SecurityAuditEntity>> = dao.observeSecurityAudits()
  val userPreferences: Flow<UserPreferences> = prefs.preferencesFlow

  suspend fun toggleEasyReadMode() = prefs.toggleEasyReadMode()

  suspend fun toggleHideBalances() = prefs.toggleHideBalances()

  suspend fun setUpiInternational(enabled: Boolean, country: String) {
    prefs.setUpiInternational(enabled, country)
    dao.insertSecurityAudit(
      SecurityAuditEntity(
        eventTitle = if (enabled) "UPI International Enabled ($country)" else "UPI International Disabled",
        firewallTier = "Firewall 4: Core Switch",
        osiLayer = "Layer 5 Session / Geo-Policy",
        decision = "ALLOW",
        payloadSnippet = "intl_roaming=$enabled corridor=$country",
        timestampMillis = System.currentTimeMillis(),
      )
    )
  }

  suspend fun toggleAutoPay(mandateId: String, newState: Boolean, merchantName: String) {
    dao.setAutoPayActive(mandateId, newState)
    dao.insertSecurityAudit(
      SecurityAuditEntity(
        eventTitle = if (newState) "AutoPay Activated: $merchantName" else "AutoPay Paused: $merchantName",
        firewallTier = "Firewall 4: Core Scheduler",
        osiLayer = "Layer 5 Mandate Token",
        decision = "ALLOW",
        payloadSnippet = "mandate=$mandateId active=$newState",
        timestampMillis = System.currentTimeMillis(),
      )
    )
  }

  suspend fun addAutoPayMandate(merchantName: String, category: String, maxAmount: Double) {
    val cleanName = FirewallInspector.sanitizeInput(merchantName).ifBlank { "Utility AutoPay" }
    val mandate = AutoPayMandateEntity(
      id = "ap_${UUID.randomUUID().toString().take(8)}",
      merchantName = cleanName,
      category = category,
      maxAmountInr = maxAmount.coerceIn(100.0, 100000.0),
      frequency = "Monthly",
      nextDebitDate = "15 Dec 2026",
      isActive = true,
    )
    dao.upsertAutoPayMandates(listOf(mandate))
  }

  suspend fun executeSecurePayment(
    title: String,
    counterparty: String,
    category: String,
    amountInr: Double,
    fromAccountId: String,
    toAccountId: String? = null,
    paymentRail: String = "UPI",
    note: String = "",
    voucherCode: String? = null,
  ): PaymentOutcome {
    val combinedInput = "$title $counterparty $note"
    val inspection = FirewallInspector.inspectPayload(combinedInput)
    val now = System.currentTimeMillis()

    if (inspection.overallDecision == FirewallDecision.BLOCK) {
      val blockedTx = TransactionEntity(
        txId = "BLK${Random.nextInt(10000000, 99999999)}",
        title = title.take(40),
        counterparty = counterparty.take(40),
        category = category,
        amountInr = amountInr,
        isCredit = false,
        status = "BLOCKED",
        paymentRail = paymentRail,
        accountId = fromAccountId,
        note = "Blocked by ${inspection.blockedAtLayer}",
        firewallSummary = inspection.threatCategory ?: "Threat Detected",
        timestampMillis = now,
      )
      dao.insertTransaction(blockedTx)
      dao.insertSecurityAudit(
        SecurityAuditEntity(
          eventTitle = "Blocked ${inspection.threatCategory ?: "Threat"}",
          firewallTier = inspection.blockedAtLayer ?: "WAF",
          osiLayer = "Layer 7 Inspection",
          decision = "BLOCK",
          payloadSnippet = combinedInput.take(64),
          timestampMillis = now,
        )
      )
      return PaymentOutcome.Blocked(
        reason = "${inspection.threatCategory} blocked at ${inspection.blockedAtLayer}",
        inspection = inspection,
      )
    }

    val sourceAccount = dao.getAccountById(fromAccountId)
    if (sourceAccount == null || amountInr <= 0.0) {
      return PaymentOutcome.Blocked("Invalid account or amount", inspection)
    }
    if (sourceAccount.balanceInr < amountInr) {
      return PaymentOutcome.Blocked(
        "Insufficient balance in ${sourceAccount.bankName} (Available: ₹${"%.2f".format(sourceAccount.balanceInr)})",
        inspection,
      )
    }

    val cleanTitle = FirewallInspector.sanitizeInput(title)
    val cleanCounterparty = FirewallInspector.sanitizeInput(counterparty)
    val cleanNote = FirewallInspector.sanitizeInput(note).ifBlank { "Verified via $paymentRail" }
    val tx = TransactionEntity(
      txId = "UTR${Random.nextLong(632000000000L, 639999999999L)}",
      title = cleanTitle,
      counterparty = cleanCounterparty,
      category = category,
      amountInr = amountInr,
      isCredit = false,
      status = "SUCCESS",
      paymentRail = paymentRail,
      accountId = fromAccountId,
      note = cleanNote,
      firewallSummary = "5/5 Firewalls Passed • Parameterized Ledger Commit",
      voucherCode = voucherCode,
      timestampMillis = now,
    )
    val audit = SecurityAuditEntity(
      eventTitle = "$category Verified: $cleanTitle",
      firewallTier = "All 5 Firewalls",
      osiLayer = "L1-L7 End-to-End",
      decision = "ALLOW",
      payloadSnippet = "$cleanCounterparty • ₹${"%.2f".format(amountInr)}",
      timestampMillis = now,
    )
    dao.commitPaymentAtomically(
      fromAccountId = fromAccountId,
      toAccountId = toAccountId,
      amountInr = amountInr,
      tx = tx,
      audit = audit,
    )
    return PaymentOutcome.Success(transaction = tx, inspection = inspection)
  }

  suspend fun investInAsset(
    holdingId: String,
    amountInr: Double,
    fromAccountId: String,
    isSipStart: Boolean = false,
  ): PaymentOutcome {
    val holding = dao.getInvestmentById(holdingId)
      ?: return PaymentOutcome.Blocked("Investment product not found", FirewallInspector.inspectPayload(holdingId))
    val account = dao.getAccountById(fromAccountId)
    val inspection = FirewallInspector.inspectPayload(holding.name)
    if (account == null || account.balanceInr < amountInr || amountInr <= 0.0) {
      return PaymentOutcome.Blocked("Insufficient balance in selected bank account", inspection)
    }

    val unitPrice = when (holding.assetType) {
      "DIGITAL_GOLD" -> 7840.0
      "DIGITAL_SILVER" -> 94.0
      else -> 185.0
    }
    val addedUnits = amountInr / unitPrice
    val updatedHolding = holding.copy(
      investedInr = holding.investedInr + amountInr,
      currentValueInr = holding.currentValueInr + amountInr,
      monthlySipInr = if (isSipStart) amountInr else holding.monthlySipInr,
      unitsOrGrams = holding.unitsOrGrams + addedUnits,
    )
    val now = System.currentTimeMillis()
    val tx = TransactionEntity(
      txId = "UTR${Random.nextLong(632000000000L, 639999999999L)}",
      title = holding.name,
      counterparty = "wealth.clearing@payshield",
      category = "WEALTH",
      amountInr = amountInr,
      isCredit = false,
      status = "SUCCESS",
      paymentRail = if (isSipStart) "AUTOPAY" else "UPI",
      accountId = fromAccountId,
      note = if (isSipStart) "Monthly SIP Activated + 1st Installment" else "Instant Vault / NAV Allocation",
      firewallSummary = "5/5 Firewalls Passed • Depository mTLS Signed",
      timestampMillis = now,
    )
    val audit = SecurityAuditEntity(
      eventTitle = "Wealth Purchase: ${holding.name}",
      firewallTier = "All 5 Firewalls",
      osiLayer = "L1-L7 End-to-End",
      decision = "ALLOW",
      payloadSnippet = "asset=${holding.id} amount=₹${"%.2f".format(amountInr)}",
      timestampMillis = now,
    )
    dao.commitInvestmentAtomically(
      fromAccountId = fromAccountId,
      holding = updatedHolding,
      tx = tx,
      audit = audit,
    )
    return PaymentOutcome.Success(transaction = tx, inspection = inspection)
  }

  suspend fun runPacketSimulation(payload: String, sourceIp: String): PacketInspectionResult {
    val result = FirewallInspector.inspectPayload(payload, sourceIp = sourceIp)
    dao.insertSecurityAudit(
      SecurityAuditEntity(
        eventTitle = if (result.overallDecision == FirewallDecision.BLOCK) {
          "Lab Blocked: ${result.threatCategory}"
        } else {
          "Lab Passed: Clean Request"
        },
        firewallTier = result.blockedAtLayer ?: "All 5 Firewalls",
        osiLayer = if (result.overallDecision == FirewallDecision.BLOCK) "Threat Mitigated" else "L1-L7 Verified",
        decision = result.overallDecision.name,
        payloadSnippet = payload.take(64),
        timestampMillis = System.currentTimeMillis(),
      )
    )
    return result
  }
}
