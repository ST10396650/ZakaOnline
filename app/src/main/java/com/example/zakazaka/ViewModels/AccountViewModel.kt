package com.example.zakazaka.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakazaka.Models.AccountEntity
import com.example.zakazaka.Repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AccountViewModel @Inject constructor(private val repository: AccountRepository) : ViewModel() {

    // Business logic to create a new account
    fun createNewAccount(account: AccountEntity): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        repository.addAccount(account) { success ->
            result.postValue(success)
        }
        return result
    }

    // Business logic to get all accounts
    fun getAllAccounts(): LiveData<List<AccountEntity>> {
        val accountsLiveData = MutableLiveData<List<AccountEntity>>()
        repository.getAccounts { accountList ->
            accountsLiveData.postValue(accountList)
        }
        return accountsLiveData
    }

    // Get accounts by user ID
    fun getAccountsByUserId(userId: String): LiveData<List<AccountEntity>> {
        return repository.getAccountsByUserId(userId)
    }




    // Update account amount
    fun updateAccountAmount(accountId: String, newAmount: Double, callback: (Boolean) -> Unit) {
        repository.updateAccountAmount(accountId, newAmount, callback)
    }

//     fun createNewAccount(account: AccountEntity): LiveData<Long> {
//        //functionality to create a new account
//         val accNo = MutableLiveData<Long>()
//         viewModelScope.launch(Dispatchers.IO) {
//             accNo.postValue(repository.addAccount(account))
//         }
//         return accNo
//    }
//    fun getAccounts(): LiveData<List<AccountEntity>> {
//        //functionality to get all the accounts
//        return repository.getAllAccount
//    }
//    fun deleteAccount(accountID:Long){
//        //functionality to delete account
//    }
//     fun getAccountsByUserId(userId:Long): LiveData<List<AccountEntity>> {
//         val accounts = MutableLiveData<List<AccountEntity>>()
//         viewModelScope.launch(Dispatchers.IO) {
//             accounts.postValue(repository.getAccountsByUserId(userId))
//         }
//         return accounts
//    }
}