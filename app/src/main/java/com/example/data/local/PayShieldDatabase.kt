package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    BankAccountEntity::class,
    TransactionEntity::class,
    AutoPayMandateEntity::class,
    InvestmentHoldingEntity::class,
    SecurityAuditEntity::class,
  ],
  version = 1,
  exportSchema = false,
)
abstract class PayShieldDatabase : RoomDatabase() {

  abstract fun dao(): PayShieldDao

  companion object {
    @Volatile private var INSTANCE: PayShieldDatabase? = null

    fun getInstance(context: Context): PayShieldDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          PayShieldDatabase::class.java,
          "payshield_secure_ledger.db",
        )
          .addCallback(SeedCallback())
          .build()
        INSTANCE = instance
        instance
      }
    }
  }

  private class SeedCallback : Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      val instance = INSTANCE ?: return
      CoroutineScope(Dispatchers.IO).launch {
        seedInitialData(instance.dao())
      }
    }

    suspend fun seedInitialData(dao: PayShieldDao) {
      val now = System.currentTimeMillis()
      dao.upsertAccounts(
        listOf(
          BankAccountEntity(
            id = "acc_hdfc",
            bankName = "HDFC Bank",
            maskedAccount = "•••• 4821",
            ifscCode = "HDFC0000240",
            upiId = "aarav.sharma@payshield",
            balanceInr = 84650.75,
            isPrimary = true,
            accountType = "SAVINGS",
          ),
          BankAccountEntity(
            id = "acc_sbi",
            bankName = "State Bank of India",
            maskedAccount = "•••• 9104",
            ifscCode = "SBIN0001892",
            upiId = "aarav.sbi@payshield",
            balanceInr = 32400.00,
            isPrimary = false,
            accountType = "SAVINGS",
          ),
          BankAccountEntity(
            id = "acc_lite",
            bankName = "UPI Lite Wallet",
            maskedAccount = "PIN-less < ₹500",
            ifscCode = "NPCI0UPILITE",
            upiId = "lite.aarav@payshield",
            balanceInr = 1450.00,
            isPrimary = false,
            accountType = "UPI_LITE",
          ),
        )
      )

      dao.upsertAutoPayMandates(
        listOf(
          AutoPayMandateEntity(
            id = "ap_bescom",
            merchantName = "BESCOM Electricity",
            category = "Electricity",
            maxAmountInr = 5000.0,
            frequency = "Monthly",
            nextDebitDate = "05 Dec 2026",
            isActive = true,
          ),
          AutoPayMandateEntity(
            id = "ap_sip_nifty",
            merchantName = "Nifty 50 Index Direct SIP",
            category = "Mutual Fund SIP",
            maxAmountInr = 5000.0,
            frequency = "Monthly (10th)",
            nextDebitDate = "10 Dec 2026",
            isActive = true,
          ),
          AutoPayMandateEntity(
            id = "ap_airtel",
            merchantName = "Airtel Fiber Broadband",
            category = "Broadband",
            maxAmountInr = 1200.0,
            frequency = "Monthly",
            nextDebitDate = "28 Nov 2026",
            isActive = true,
          ),
          AutoPayMandateEntity(
            id = "ap_ott",
            merchantName = "StreamPlus Family Plan",
            category = "Subscription",
            maxAmountInr = 649.0,
            frequency = "Monthly",
            nextDebitDate = "01 Dec 2026",
            isActive = false,
          ),
        )
      )

      dao.upsertInvestments(
        listOf(
          InvestmentHoldingEntity(
            id = "inv_nifty50",
            name = "Nifty 50 Index Direct Growth",
            assetType = "MUTUAL_FUND",
            investedInr = 60000.0,
            currentValueInr = 71480.0,
            monthlySipInr = 5000.0,
            unitsOrGrams = 342.18,
            cagrPercent = 16.4,
          ),
          InvestmentHoldingEntity(
            id = "inv_elss",
            name = "PayShield Tax Saver ELSS (80C)",
            assetType = "TAX_SAVER",
            investedInr = 45000.0,
            currentValueInr = 52890.0,
            monthlySipInr = 3500.0,
            unitsOrGrams = 510.42,
            cagrPercent = 18.2,
          ),
          InvestmentHoldingEntity(
            id = "inv_gold",
            name = "24K Digital Gold Vault (MMTC)",
            assetType = "DIGITAL_GOLD",
            investedInr = 22000.0,
            currentValueInr = 26656.0,
            monthlySipInr = 0.0,
            unitsOrGrams = 3.40,
            cagrPercent = 14.8,
          ),
          InvestmentHoldingEntity(
            id = "inv_silver",
            name = "999 Pure Digital Silver",
            assetType = "DIGITAL_SILVER",
            investedInr = 7500.0,
            currentValueInr = 8460.0,
            monthlySipInr = 0.0,
            unitsOrGrams = 90.0,
            cagrPercent = 11.2,
          ),
        )
      )

      dao.insertTransaction(
        TransactionEntity(
          txId = "UTR631984220194",
          title = "FreshMart Organics",
          counterparty = "freshmart@okmerchant",
          category = "QR_PAY",
          amountInr = 340.0,
          isCredit = false,
          status = "SUCCESS",
          paymentRail = "UPI_LITE",
          accountId = "acc_lite",
          note = "Weekly groceries • PIN-less UPI Lite",
          firewallSummary = "5/5 Firewalls Passed • Parameterized Ledger Commit",
          timestampMillis = now - 3_600_000L * 4,
        )
      )
      dao.insertTransaction(
        TransactionEntity(
          txId = "UTR631983110842",
          title = "Priya Nair",
          counterparty = "9876543210@payshield",
          category = "TRANSFER",
          amountInr = 1500.0,
          isCredit = true,
          status = "SUCCESS",
          paymentRail = "UPI",
          accountId = "acc_hdfc",
          note = "Dinner split settled",
          firewallSummary = "5/5 Firewalls Passed • mTLS Verified",
          timestampMillis = now - 3_600_000L * 26,
        )
      )
      dao.insertTransaction(
        TransactionEntity(
          txId = "UTR631980045210",
          title = "BESCOM Electricity Bill",
          counterparty = "bescom.ka@bbps",
          category = "BILL_PAY",
          amountInr = 1840.0,
          isCredit = false,
          status = "SUCCESS",
          paymentRail = "AUTOPAY",
          accountId = "acc_hdfc",
          note = "Consumer #KA-904821 • AutoPay",
          firewallSummary = "5/5 Firewalls Passed • BBPS Signed",
          timestampMillis = now - 3_600_000L * 72,
        )
      )
      dao.insertTransaction(
        TransactionEntity(
          txId = "UTR631979012440",
          title = "Mahanagar Gas Bill",
          counterparty = "mgl.gas@bbps",
          category = "BILL_PAY",
          amountInr = 785.0,
          isCredit = false,
          status = "PENDING",
          paymentRail = "UPI",
          accountId = "acc_hdfc",
          note = "Awaiting biller bank confirmation",
          firewallSummary = "NPCI Switch Pending • T+1 Auto-Settle",
          timestampMillis = now - 3_600_000L * 2,
        )
      )

      dao.insertSecurityAudit(
        SecurityAuditEntity(
          eventTitle = "UPI Transfer Verified",
          firewallTier = "All 5 Firewalls",
          osiLayer = "L1-L7 End-to-End",
          decision = "ALLOW",
          payloadSnippet = "pay:freshmart@okmerchant ₹340.00",
          timestampMillis = now - 3_600_000L * 4,
        )
      )
      dao.insertSecurityAudit(
        SecurityAuditEntity(
          eventTitle = "Blocked SQL Injection Probe",
          firewallTier = "Firewall 3: Host WAF",
          osiLayer = "Layer 7 Application",
          decision = "BLOCK",
          payloadSnippet = "vpa=' OR 1=1; DROP TABLE ledger;--",
          timestampMillis = now - 3_600_000L * 12,
        )
      )
      dao.insertSecurityAudit(
        SecurityAuditEntity(
          eventTitle = "Blocked Volumetric DDoS Burst",
          firewallTier = "Firewall 1: Perimeter Edge",
          osiLayer = "Layer 3/4 Network",
          decision = "BLOCK",
          payloadSnippet = "185.220.101.4 • 2,400 req/s SYN",
          timestampMillis = now - 3_600_000L * 30,
        )
      )
    }
  }
}
