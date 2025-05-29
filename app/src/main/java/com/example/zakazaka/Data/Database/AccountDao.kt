package com.example.zakazaka.Data.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.zakazaka.Models.AccountEntity

@Dao
interface AccountDao {
    //getting all of the accounts in Live Data
    @Query("SELECT * FROM Account ORDER BY accountID ASC")
    fun getAllAccounts(): LiveData<List<AccountEntity>>

    //getting account by ID
    @Query("SELECT * FROM Account WHERE accountID = :accountId")
    suspend fun getAccountById(accountId: Long): AccountEntity?

    //get all of the accounts by a specific user
    @Query("SELECT * FROM Account WHERE UserID = :userId")
    suspend fun getAccountsByUserId(userId: Long): List<AccountEntity>

    //inserts into an account
    @Insert
    suspend fun insertAccount(account: AccountEntity): Long

    //updating any existing accounts
    @Update
    suspend fun updateAccount(account: AccountEntity)

    //deleting an account
    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    //deleting an account by ID
    @Query("DELETE FROM Account WHERE accountID = :accountId")
    suspend fun deleteAccountById(accountId: Long)
}