package com.example.zakazaka.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.zakazaka.Data.Database.TransactionDao
import com.example.zakazaka.Models.TransactionEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Date

class TransactionRepository() {

    private val firebaseDatabase = FirebaseDatabase.getInstance().getReference("Transactions")
    private val auth = FirebaseAuth.getInstance()
    private val categoriesRef = FirebaseDatabase.getInstance().getReference("Category")
    private val subCategoriesRef = FirebaseDatabase.getInstance().getReference("Sub_Category")


    // LiveData for all transactions
    private val _allTransactions = MutableLiveData<List<TransactionEntity>>()
    val getAllTransactions: LiveData<List<TransactionEntity>> = _allTransactions

    init {
        // Use the existing method instead of setupTransactionListener
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Use your existing getTransactionsByUserId method
            getTransactionsByUserId(userId).observeForever { transactions ->
                _allTransactions.value = transactions
            }
        }
    }

    private fun setupTransactionListener(userId: String) {
        firebaseDatabase.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactions = mutableListOf<TransactionEntity>()

                Log.d("TransactionRepo", "Total transactions found: ${snapshot.childrenCount}")

                for (transactionSnapshot in snapshot.children) {
                    val transactionData = transactionSnapshot.value as? Map<String, Any>
                    if (transactionData != null) {
                        // Log the raw data to see what Firebase is returning
                        Log.d("TransactionRepo", "Raw transaction data: $transactionData")

                        val amount = transactionData["amount"] as? Double ?: 0.0
                        Log.d("TransactionRepo", "Parsed amount: $amount")

                        val transaction = TransactionEntity(
                            transactionID = transactionData["transactionID"] as? String ?: "",
                            date = Date(transactionData["date"] as? Long ?: 0L),
                            endDate = Date(transactionData["endDate"] as? Long ?: 0L),
                            amount = amount,
                            repeat = transactionData["repeat"] as? String ?: "",
                            description = transactionData["description"] as? String ?: "",
                            type = transactionData["type"] as? String ?: "",
                            currency = transactionData["currency"] as? String ?: "",
                            subCategoryID = transactionData["subCategoryID"] as? String ?: "",
                            accountID = transactionData["accountID"] as? String ?: "",
                            imagePath = transactionData["imagePath"] as? String
                        )

                        Log.d("TransactionRepo", "Created transaction: ID=${transaction.transactionID}, Amount=${transaction.amount}")
                        transactions.add(transaction)
                    }
                }

                Log.d("TransactionRepo", "Final transactions list size: ${transactions.size}")
                _allTransactions.value = transactions
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TransactionRepo", "Database error: ${error.message}")
                _allTransactions.value = emptyList()
            }
        })
    }


    // Add new transaction
    fun addTransaction(transaction: TransactionEntity, callback: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(false, null)
            return
        }

        val transactionId = firebaseDatabase.child(userId).push().key
        if (transactionId == null) {
            callback(false, null)
            return
        }

        val transactionMap = mapOf(
            "transactionID" to transactionId,
            "date" to transaction.date.time,
            "endDate" to transaction.endDate.time,
            "amount" to transaction.amount,
            "repeat" to transaction.repeat,
            "description" to transaction.description,
            "type" to transaction.type,
            "currency" to transaction.currency,
            "subCategoryID" to transaction.subCategoryID.toString(),
            "accountID" to transaction.accountID.toString(),
            "imagePath" to transaction.imagePath

        )

        firebaseDatabase.child(userId).child(transactionId).setValue(transactionMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, transactionId)
                } else {
                    callback(false, null)
                }
            }
    }

    // Get transaction by ID
    fun getTransactionById(transactionId: String, callback: (TransactionEntity?) -> Unit) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.d("TransactionRepository", "User ID is null")
            callback(null)
            return
        }

        Log.d("TransactionRepository", "Looking for transaction ID: $transactionId")
        Log.d("TransactionRepository", "Firebase path: $userId/$transactionId")

        firebaseDatabase.child(userId).child(transactionId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("TransactionRepository", "Snapshot exists: ${snapshot.exists()}")
                    Log.d("TransactionRepository", "Snapshot value: ${snapshot.value}")

                    if (snapshot.exists()) {
                        val transactionData = snapshot.value as? Map<String, Any>
                        if (transactionData != null) {
                            try {
                                // Better handling of amount conversion
                                val amount = when (val amt = transactionData["amount"]) {
                                    is Double -> amt
                                    is Long -> amt.toDouble()
                                    is String -> amt.toDoubleOrNull() ?: 0.0
                                    else -> 0.0
                                }

                                // Better handling of date conversion
                                val dateValue = transactionData["date"] as? Long ?: System.currentTimeMillis()
                                val endDateValue = transactionData["endDate"] as? Long ?: System.currentTimeMillis()

                                val transaction = TransactionEntity(
                                    transactionID = transactionData["transactionID"] as? String ?: transactionId,
                                    date = Date(dateValue),
                                    endDate = Date(endDateValue),
                                    amount = amount,
                                    repeat = transactionData["repeat"] as? String ?: "",
                                    description = transactionData["description"] as? String ?: "",
                                    type = transactionData["type"] as? String ?: "",
                                    currency = transactionData["currency"] as? String ?: "",
                                    subCategoryID = transactionData["subCategoryID"] as? String ?: "",
                                    accountID = transactionData["accountID"] as? String ?: "",
                                    imagePath = transactionData["imagePath"] as? String
                                )

                                Log.d("TransactionRepository", "Transaction created: ${transaction.description}, Amount: ${transaction.amount}")
                                callback(transaction)
                            } catch (e: Exception) {
                                Log.e("TransactionRepository", "Error parsing transaction data", e)
                                callback(null)
                            }
                        } else {
                            Log.d("TransactionRepository", "Transaction data is null")
                            callback(null)
                        }
                    } else {
                        Log.d("TransactionRepository", "Transaction snapshot does not exist")
                        callback(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TransactionRepository", "Database error: ${error.message}")
                    callback(null)
                }
            })
    }

    // Get transactions between dates (for filtering) with enhanced amount parsing
    fun getTransactionsBetweenDates(startDate: Date, endDate: Date, callback: (List<TransactionEntity>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(emptyList())
            return
        }

        firebaseDatabase.child(userId)
            .orderByChild("date")
            .startAt(startDate.time.toDouble())
            .endAt(endDate.time.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "getTransactionsBetweenDates - onDataChange triggered")
                    Log.d(TAG, "Snapshot exists: ${snapshot.exists()}")
                    Log.d(TAG, "Children count: ${snapshot.childrenCount}")

                    val transactions = mutableListOf<TransactionEntity>()

                    for (transactionSnapshot in snapshot.children) {
                        Log.d(TAG, "Processing transaction with key: ${transactionSnapshot.key}")

                        val transactionData = transactionSnapshot.value as? Map<String, Any>
                        if (transactionData != null) {
                            Log.d(TAG, "Transaction data: $transactionData")

                            // Enhanced amount parsing - same logic as getTransactionsByUserId
                            val amount = when (val amountValue = transactionData["amount"]) {
                                is Number -> {
                                    val doubleAmount = amountValue.toDouble()
                                    Log.d(TAG, "Amount parsed as Number: $doubleAmount")
                                    doubleAmount
                                }
                                is String -> {
                                    val parsedAmount = amountValue.toDoubleOrNull() ?: 0.0
                                    Log.d(TAG, "Amount parsed as String: '$amountValue' -> $parsedAmount")
                                    parsedAmount
                                }
                                else -> {
                                    Log.w(TAG, "Unexpected amount type: ${amountValue?.javaClass}, value: $amountValue")
                                    0.0
                                }
                            }

                            val transaction = TransactionEntity(
                                transactionID = transactionData["transactionID"] as? String ?: "",
                                date = Date(transactionData["date"] as? Long ?: 0L),
                                endDate = Date(transactionData["endDate"] as? Long ?: 0L),
                                amount = amount, // Using the enhanced parsed amount
                                repeat = transactionData["repeat"] as? String ?: "",
                                description = transactionData["description"] as? String ?: "",
                                type = transactionData["type"] as? String ?: "",
                                currency = transactionData["currency"] as? String ?: "",
                                subCategoryID = transactionData["subCategoryID"] as? String ?: "",
                                accountID = transactionData["accountID"] as? String ?: "",
                                imagePath = transactionData["imagePath"] as? String
                            )

                            Log.d(TAG, "Created transaction: ${transaction.description} - ${transaction.amount}")
                            transactions.add(transaction)
                        } else {
                            Log.w(TAG, "Transaction data is null for key: ${transactionSnapshot.key}")
                            Log.w(TAG, "Raw value: ${transactionSnapshot.value}")
                            Log.w(TAG, "Value type: ${transactionSnapshot.value?.javaClass}")
                        }
                    }

                    Log.d(TAG, "Total filtered transactions created: ${transactions.size}")
                    val totalAmount = transactions.sumOf { it.amount }
                    Log.d(TAG, "Sum of filtered transaction amounts: $totalAmount")

                    callback(transactions)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Database error in getTransactionsBetweenDates: ${error.message}")
                    Log.e(TAG, "Error code: ${error.code}")
                    callback(emptyList())
                }
            })
    }

    companion object {
        private const val TAG = "TransactionRepository"
    }

    // Get transactions by user ID
    fun getTransactionsByUserId(userId: String): LiveData<List<TransactionEntity>> {
        val transactionsLiveData = MutableLiveData<List<TransactionEntity>>()

        Log.d(TAG, "Fetching transactions for userId: $userId")
        Log.d(TAG, "Firebase path: Transactions/$userId")

        firebaseDatabase.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange triggered")
                Log.d(TAG, "Snapshot exists: ${snapshot.exists()}")
                Log.d(TAG, "Children count: ${snapshot.childrenCount}")

                val transactions = mutableListOf<TransactionEntity>()

                for (transactionSnapshot in snapshot.children) {
                    Log.d(TAG, "Processing transaction with key: ${transactionSnapshot.key}")

                    val transactionData = transactionSnapshot.value as? Map<String, Any>
                    if (transactionData != null) {
                        Log.d(TAG, "Transaction data: $transactionData")

                        // Enhanced amount parsing
                        val amount = when (val amountValue = transactionData["amount"]) {
                            is Number -> {
                                val doubleAmount = amountValue.toDouble()
                                Log.d(TAG, "Amount parsed as Number: $doubleAmount")
                                doubleAmount
                            }
                            is String -> {
                                val parsedAmount = amountValue.toDoubleOrNull() ?: 0.0
                                Log.d(TAG, "Amount parsed as String: '$amountValue' -> $parsedAmount")
                                parsedAmount
                            }
                            else -> {
                                Log.w(TAG, "Unexpected amount type: ${amountValue?.javaClass}, value: $amountValue")
                                0.0
                            }
                        }

                        val transaction = TransactionEntity(
                            transactionID = transactionData["transactionID"] as? String ?: "",
                            date = Date(transactionData["date"] as? Long ?: 0L),
                            endDate = Date(transactionData["endDate"] as? Long ?: 0L),
                            amount = amount,
                            repeat = transactionData["repeat"] as? String ?: "",
                            description = transactionData["description"] as? String ?: "",
                            type = transactionData["type"] as? String ?: "",
                            currency = transactionData["currency"] as? String ?: "",
                            subCategoryID = transactionData["subCategoryID"] as? String ?: "",
                            accountID = transactionData["accountID"] as? String ?: "",
                            imagePath = transactionData["imagePath"] as? String
                        )

                        Log.d(TAG, "Created transaction: ${transaction.description} - ${transaction.amount}")
                        transactions.add(transaction)
                    } else {
                        Log.w(TAG, "Transaction data is null for key: ${transactionSnapshot.key}")
                        Log.w(TAG, "Raw value: ${transactionSnapshot.value}")
                        Log.w(TAG, "Value type: ${transactionSnapshot.value?.javaClass}")
                    }
                }

                Log.d(TAG, "Total transactions created: ${transactions.size}")
                val totalAmount = transactions.sumOf { it.amount }
                Log.d(TAG, "Sum of all transaction amounts: $totalAmount")

                transactionsLiveData.value = transactions
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
                Log.e(TAG, "Error code: ${error.code}")
                transactionsLiveData.value = emptyList()
            }
        })

        return transactionsLiveData
    }


    // Get user-specific transactions with category and subcategory information
    fun getTransactionsWithCategoryInfo(userId: String, callback: (List<TransactionEntity>) -> Unit) {
        firebaseDatabase.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactions = mutableListOf<TransactionEntity>()
                for (transactionSnapshot in snapshot.children) {
                    val transactionData = transactionSnapshot.value as? Map<String, Any>
                    if (transactionData != null) {
                        val transaction = TransactionEntity(
                            transactionID = transactionData["transactionID"] as? String ?: "",
                            date = Date(transactionData["date"] as? Long ?: 0L),
                            endDate = Date(transactionData["endDate"] as? Long ?: 0L),
                            amount = transactionData["amount"] as? Double ?: 0.0,
                            repeat = transactionData["repeat"] as? String ?: "",
                            description = transactionData["description"] as? String ?: "",
                            type = transactionData["type"] as? String ?: "",
                            currency = transactionData["currency"] as? String ?: "",
                            subCategoryID = transactionData["subCategoryID"] as? String ?: "",
                            accountID = transactionData["accountID"] as? String ?: "",
                            imagePath = transactionData["imagePath"] as? String

                        )
                        transactions.add(transaction)
                    }
                }
                callback(transactions)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

//    // getting all the live transactions
//    val getAllTransctions: LiveData<List<TransactionEntity>> = transactionDao.getAllTransactions()
//
//    //creating a new transaction
//    suspend fun addTransaction(transaction: TransactionEntity): Long {
//        return transactionDao.insertTransaction(transaction)
//    }
//
//    //getting transaction by id
//    suspend fun getTransactionById(transactionId: Long): TransactionEntity? {
//        return transactionDao.getTransactionById(transactionId)
//    }
//
//    //get transaction by date
//    fun getTransactionsBySelectedPeriod(startDate: Date, endDate: Date): LiveData<List<TransactionEntity>> {
//        return transactionDao.getTransactionsByDate(startDate, endDate)
//    }
//
//    //get transaction by sub category
//    fun getTransactionsBySubCategory(subCategoryId: Long): LiveData<List<TransactionEntity>> {
//        return transactionDao.getTransactionsBySubCategory(subCategoryId)
//    }
//
//    //get transaction by account
//    fun getTransactionsByAccount(accountId: Long): LiveData<List<TransactionEntity>> {
//        return transactionDao.getTransactionsByAccount(accountId)
//    }
//
//    //get transaction by type
//    fun getTransactionsByType(type: String): LiveData<List<TransactionEntity>> {
//        return transactionDao.getTransactionsByType(type)
//    }
//
//    //get transaction by date and subcategory
//    fun getTransactionsByDateRangeAndSubCategory(startDate: Date, endDate: Date, subCategoryId: Long): LiveData<List<TransactionEntity>> {
//        return transactionDao.getTransactionsByDateAndSubCategory(startDate, endDate, subCategoryId)
//    }
//
//    /*get by date and account
//    fun getTransactionsByDateRangeAndAccount(startDate: Date, endDate: Date, accountId: Long): LiveData<List<TransactionEntity>> {
//        return transactionDao.getTransactionsByDateRangeAndAccount(startDate, endDate, accountId)
//    }*/
//
//
//
//    //updating a transaction
//    suspend fun updateTransaction(transaction: TransactionEntity) {
//        transactionDao.updateTransaction(transaction)
//    }
//
//    //delete a transaction by ID
//    suspend fun deleteTransaction(transactionId: Long) {
//        transactionDao.deleteTransactionById(transactionId)
//    }
//
//    //deleting all transactions
//    suspend fun deleteAllTransactions() {
//        transactionDao.deleteAllTransactions()
//    }
//



}