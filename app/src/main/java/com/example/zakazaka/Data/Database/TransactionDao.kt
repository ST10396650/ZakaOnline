package com.example.zakazaka.Data.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.zakazaka.Models.TransactionEntity
import java.util.Date

@Dao
interface TransactionDao {

    //inserting a new transaction
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    //getting all the transactions from latest to old
    @Query("SELECT * FROM `Transaction` ORDER BY date DESC")
    fun getAllTransactions(): LiveData<List<TransactionEntity>>

    //getting the transaction ID
    @Query("SELECT * FROM `Transaction` WHERE transactionID = :transactionId")
    suspend fun getTransactionById(transactionId: Long): TransactionEntity?

    //to see their expenses in a user selected period
    @Query("SELECT * FROM `Transaction` WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDate(startDate: Date, endDate: Date): LiveData<List<TransactionEntity>>

    //to view all of their expenses by sub category
    @Query("SELECT * FROM `Transaction` WHERE subCategoryID = :subCategoryId ORDER BY date DESC")
    fun getTransactionsBySubCategory(subCategoryId: Long): LiveData<List<TransactionEntity>>

    //view all their expenses by type of account
    @Query("SELECT * FROM `Transaction` WHERE accountID = :accountId ORDER BY date DESC")
    fun getTransactionsByAccount(accountId: Long): LiveData<List<TransactionEntity>>

    //see all their transaction by type
    @Query("SELECT * FROM `Transaction` WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): LiveData<List<TransactionEntity>>

    //see all their transactions by both date and subcategory.
    @Query("SELECT * FROM `Transaction` WHERE date BETWEEN :startDate AND :endDate AND subCategoryID = :subCategoryId ORDER BY date DESC")
    fun getTransactionsByDateAndSubCategory(startDate: Date, endDate: Date, subCategoryId: Long): LiveData<List<TransactionEntity>>

    /*getting the transactions by date and account
    @Query("SELECT * FROM `Transaction` WHERE date BETWEEN :startDate AND :endDate AND accountID = :accountId ORDER BY date DESC")
    fun getTransactionsByDateRangeAndAccount(startDate: Date, endDate: Date, accountId: Long): LiveData<List<TransactionEntity>>
*/

    //updating a transaction
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    //deleting a transaction by ID
    @Query("DELETE FROM `Transaction` WHERE transactionID = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long)

    //deleting all transactions
    @Query("DELETE FROM `Transaction`")
    suspend fun deleteAllTransactions()
}