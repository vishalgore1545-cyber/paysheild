package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PayShieldDao {

  @Query("SELECT * FROM bank_accounts ORDER BY isPrimary DESC, accountType ASC")
  fun observeAccounts(): Flow<List<BankAccountEntity>>

  @Query("SELECT * FROM bank_accounts WHERE id = :accountId LIMIT 1")
  suspend fun getAccountById(accountId: String): BankAccountEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAccounts(accounts: List<BankAccountEntity>)

  @Query("UPDATE bank_accounts SET balanceInr = balanceInr + :delta WHERE id = :accountId")
  suspend fun adjustBalance(accountId: String, delta: Double)

  @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC")
  fun observeTransactions(): Flow<List<TransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(tx: TransactionEntity)

  @Query("SELECT * FROM autopay_mandates ORDER BY merchantName ASC")
  fun observeAutoPayMandates(): Flow<List<AutoPayMandateEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAutoPayMandates(mandates: List<AutoPayMandateEntity>)

  @Query("UPDATE autopay_mandates SET isActive = :isActive WHERE id = :mandateId")
  suspend fun setAutoPayActive(mandateId: String, isActive: Boolean)

  @Query("SELECT * FROM investment_holdings ORDER BY currentValueInr DESC")
  fun observeInvestments(): Flow<List<InvestmentHoldingEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertInvestments(items: List<InvestmentHoldingEntity>)

  @Query("SELECT * FROM investment_holdings WHERE id = :id LIMIT 1")
  suspend fun getInvestmentById(id: String): InvestmentHoldingEntity?

  @Query("SELECT * FROM security_audits ORDER BY timestampMillis DESC LIMIT 40")
  fun observeSecurityAudits(): Flow<List<SecurityAuditEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSecurityAudit(audit: SecurityAuditEntity)

  @Transaction
  suspend fun commitPaymentAtomically(
    fromAccountId: String,
    toAccountId: String?,
    amountInr: Double,
    tx: TransactionEntity,
    audit: SecurityAuditEntity,
  ) {
    adjustBalance(fromAccountId, -amountInr)
    if (toAccountId != null) {
      adjustBalance(toAccountId, amountInr)
    }
    insertTransaction(tx)
    insertSecurityAudit(audit)
  }

  @Transaction
  suspend fun commitInvestmentAtomically(
    fromAccountId: String,
    holding: InvestmentHoldingEntity,
    tx: TransactionEntity,
    audit: SecurityAuditEntity,
  ) {
    adjustBalance(fromAccountId, -tx.amountInr)
    upsertInvestments(listOf(holding))
    insertTransaction(tx)
    insertSecurityAudit(audit)
  }
}
