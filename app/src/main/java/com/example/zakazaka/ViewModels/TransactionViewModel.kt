package com.example.zakazaka.ViewModels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakazaka.Models.TransactionEntity
import com.example.zakazaka.Repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class TransactionViewModel @Inject constructor(private val repository: TransactionRepository) : ViewModel() {


    // LiveData for filtered transactions (for date range filtering)
    private val _filteredTransactions = MutableLiveData<List<TransactionEntity>>()
    val filteredTransactions: LiveData<List<TransactionEntity>> = _filteredTransactions


    // Get all transactions (this returns the repository's LiveData)
    fun getAllTransactions(): LiveData<List<TransactionEntity>> {
        return repository.getAllTransactions
    }

    fun enterNewTransaction(transaction: TransactionEntity, callback: (Boolean, String?) -> Unit) {
        repository.addTransaction(transaction) { success, transactionId ->
            callback(success, transactionId)
        }
    }

    val getAllTransactions: LiveData<List<TransactionEntity>> = repository.getAllTransactions

    // Get transaction by ID with callback
    fun getTransactionById(transactionId: String, callback: (TransactionEntity?) -> Unit) {
        repository.getTransactionById(transactionId, callback)
    }

    // Get transactions between dates
    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): LiveData<List<TransactionEntity>> {
        repository.getTransactionsBetweenDates(startDate, endDate) { transactions ->
            _filteredTransactions.value = transactions
        }
        return _filteredTransactions
    }

    // Business logic: Sort transactions by date (newest first)
    fun sortTransactionsByDate(transactions: List<TransactionEntity>, ascending: Boolean = false): List<TransactionEntity> {
        return if (ascending) {
            transactions.sortedBy { it.date }
        } else {
            transactions.sortedByDescending { it.date }
        }
    }

    // Get transactions with category information for specific user
    fun getTransactionsWithCategoryInfo(userId: String, callback: (List<TransactionEntity>) -> Unit) {
        repository.getTransactionsWithCategoryInfo(userId, callback)
    }

    // Business logic: Filter transactions by type
    fun filterTransactionsByType(transactions: List<TransactionEntity>, type: String): List<TransactionEntity> {
        return transactions.filter { it.type.equals(type, ignoreCase = true) }
    }

    // Business logic: Calculate total amount for transactions
    fun calculateTotalAmount(transactions: List<TransactionEntity>): Double {
        return transactions.sumOf { it.amount }
    }

    // Get transactions by user ID
    fun getTransactionsByUserId(userId: String): LiveData<List<TransactionEntity>> {
        return repository.getTransactionsByUserId(userId)
    }

//    //functionality to register a new transaction
//    suspend fun enterNewTransaction(transaction: TransactionEntity):Long{
//        return repository.addTransaction(transaction)
//    }
//    fun getAllTransactions(): LiveData<List<TransactionEntity>> {
//        return repository.getAllTransctions
//        //functionality to return a list of all transactions
//    }
//    fun deleteTransaction(transactionID:Long){
//        //functionality to delete a transaction
//    }
//    //functionality to return a list of all transactions between two dates
//    //this function will be called when user wants to see all transactions in a time period but filtered through the subcategory and main category as well.
//    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): LiveData<List<TransactionEntity>> {
//        return repository.getTransactionsBySelectedPeriod(startDate,endDate)
//    }
//    fun getTransactionById(transactionID:Long): LiveData<TransactionEntity> {
//        val transaction = MutableLiveData<TransactionEntity>()
//        viewModelScope.launch(Dispatchers.IO) {
//            transaction.postValue (repository.getTransactionById(transactionID))
//        }
//        return transaction
//    }
//    //functionality to return a list of all transactions by a subcategory
//    fun getTransactionsBySubCategory(subCategoryID:Long): LiveData<List<TransactionEntity>> {
//        return repository.getTransactionsBySubCategory(subCategoryID)
//    }
//    //functionality to return a list of all transactions by an account
//    fun getTransactionsByAccount(accountID:Long): LiveData<List<TransactionEntity>> {
//        return repository.getTransactionsByAccount(accountID)
//    }

}