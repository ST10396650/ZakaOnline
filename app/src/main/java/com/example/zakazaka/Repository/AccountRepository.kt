package com.example.zakazaka.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.zakazaka.Data.Database.AccountDao
import com.example.zakazaka.Models.AccountEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AccountRepository() {

    private val firebaseDatabase = FirebaseDatabase.getInstance().getReference("Accounts")
    private val auth = FirebaseAuth.getInstance()

    // Adds a new account under the current Firebase user UID
    fun addAccount(account: AccountEntity, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(false)
            return
        }

        val accountId = firebaseDatabase.child(userId).push().key
        if (accountId == null) {
            callback(false)
            return
        }

        val accountMap = mapOf(
            "accountID" to accountId,
            "accountName" to account.accountName,
            "amount" to account.amount,
            "type" to account.type,
            "bankName" to account.bankName,
            "userID" to userId
        )

        firebaseDatabase.child(userId).child(accountId).setValue(accountMap)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    // Get accounts by user ID
    fun getAccountsByUserId(userId: String): LiveData<List<AccountEntity>> {
        val accountsLiveData = MutableLiveData<List<AccountEntity>>()

        firebaseDatabase.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val accounts = mutableListOf<AccountEntity>()
                for (accountSnapshot in snapshot.children) {
                    val accountData = accountSnapshot.value as? Map<String, Any>
                    if (accountData != null) {
                        val account = AccountEntity(
                            accountID = accountData["accountID"] as? String ?: "",
                            accountName = accountData["accountName"] as? String ?: "",
                            amount = accountData["amount"] as? Double ?: 0.0,
                            type = accountData["type"] as? String ?: "",
                            bankName = accountData["bankName"] as? String ?: "",
                            userID = accountData["userID"] as? String ?: ""
                        )
                        accounts.add(account)
                    }
                }
                accountsLiveData.value = accounts
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                accountsLiveData.value = emptyList()
            }
        })

        return accountsLiveData
    }

    // Update account amount
    fun updateAccountAmount(accountId: String, newAmount: Double, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(false)
            return
        }

        firebaseDatabase.child(userId).child(accountId).child("amount").setValue(newAmount)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }


    // Retrieves all accounts for current Firebase user
    fun getAccounts(callback: (List<AccountEntity>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(emptyList())
            return
        }

        firebaseDatabase.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val accounts = mutableListOf<AccountEntity>()
                for (child in snapshot.children) {
                    val account = child.getValue(AccountEntity::class.java)
                    account?.let { accounts.add(it) }
                }
                callback(accounts)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
}
//    // getting all the accounts
//    val getAllAccount: LiveData<List<AccountEntity>> = accountDao.getAllAccounts()
//
//    //getting an account by ID
//    suspend fun getAccountById(accountId: Long): AccountEntity? {
//        return accountDao.getAccountById(accountId)
//    }
//
//    //get all the account connected to a specific user
//    suspend fun getAccountsByUserId(userId: Long): List<AccountEntity> {
//        return accountDao.getAccountsByUserId(userId)
//    }
//
//    //creating a new account
//    suspend fun addAccount(account: AccountEntity) : Long {
//       return accountDao.insertAccount(account)
//    }
//
//    //updating an account that already exists
//    suspend fun updateAccount(account: AccountEntity) {
//        accountDao.updateAccount(account)
//    }
//
//    //deleting an account by user ID
//    suspend fun deleteAnAccount(userId: Long) {
//        accountDao.deleteAccountById(userId)
//    }
//
//    /*deleting an account
//    suspend fun deleteAccount(account: AccountEntity) {
//        accountDao.deleteAccount(account)
//    } */



