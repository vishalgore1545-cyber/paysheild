package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
  @PrimaryKey val id: String,
  val bankName: String,
  val maskedAccount: String,
  val ifscCode: String,
  val upiId: String,
  val balanceInr: Double,
  val isPrimary: Boolean,
  val accountType: String, // "SAVINGS" or "UPI_LITE"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey val txId: String,
  val title: String,
  val counterparty: String,
  val category: String, // "TRANSFER", "QR_PAY", "BILL_PAY", "RECHARGE", "WEALTH", "TRAVEL", "VOUCHER"
  val amountInr: Double,
  val isCredit: Boolean,
  val status: String, // "SUCCESS", "BLOCKED"
  val paymentRail: String, // "UPI", "UPI_LITE", "UPI_INTL", "AUTOPAY"
  val accountId: String,
  val note: String,
  val firewallSummary: String,
  val voucherCode: String? = null,
  val timestampMillis: Long,
)

@Entity(tableName = "autopay_mandates")
data class AutoPayMandateEntity(
  @PrimaryKey val id: String,
  val merchantName: String,
  val category: String,
  val maxAmountInr: Double,
  val frequency: String,
  val nextDebitDate: String,
  val isActive: Boolean,
)

@Entity(tableName = "investment_holdings")
data class InvestmentHoldingEntity(
  @PrimaryKey val id: String,
  val name: String,
  val assetType: String, // "MUTUAL_FUND", "DIGITAL_GOLD", "DIGITAL_SILVER", "TAX_SAVER"
  val investedInr: Double,
  val currentValueInr: Double,
  val monthlySipInr: Double,
  val unitsOrGrams: Double,
  val cagrPercent: Double,
)

@Entity(tableName = "security_audits")
data class SecurityAuditEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val eventTitle: String,
  val firewallTier: String,
  val osiLayer: String,
  val decision: String, // "ALLOW" or "BLOCK"
  val payloadSnippet: String,
  val timestampMillis: Long,
)
