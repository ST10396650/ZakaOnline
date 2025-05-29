package com.example.zakazaka.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakazaka.Models.BudgetGoalEntity
import com.example.zakazaka.Repository.BudgetGoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class BudgetGoalViewModel @Inject constructor(private val repository: BudgetGoalRepository) :
    ViewModel() {

    fun getAllBudgetGoals(): LiveData<List<BudgetGoalEntity>> {
        val liveData = MutableLiveData<List<BudgetGoalEntity>>()
        repository.getAllBudgetGoals { goals ->
            liveData.postValue(goals)
        }
        return liveData
    }

    fun addBudgetGoal(budgetGoal: BudgetGoalEntity): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        repository.addBudgetGoal(budgetGoal) { success ->
            result.postValue(success)
        }
        return result
    }

    fun getBudgetGoalsByUserId(userId: String): LiveData<List<BudgetGoalEntity>> {
        return repository.getBudgetGoalsByUserId(userId)
    }
//    fun getAllBudgetGoals(): LiveData<List<BudgetGoalEntity>> {
//        return repository.readAllBudgetGoals
//    }
//    fun getBudgetGoal(userId:Long): LiveData<List<BudgetGoalEntity>> {
//        val budgetGoals = MutableLiveData<List<BudgetGoalEntity>>()
//        viewModelScope.launch(Dispatchers.IO) {
//            budgetGoals.postValue(repository.getBudgetGoalsByUserId(userId))
//        }
//        return budgetGoals
//    }
//    fun addBudgetGoal(budgetGoal: BudgetGoalEntity) :LiveData<Long>{
//        val id = MutableLiveData<Long>()
//        viewModelScope.launch(Dispatchers.IO) {
//            id.postValue(repository.addBudgetGoal(budgetGoal))
//        }
//        return id
//    }
//
//    fun updateBudgetGoal(budgetGoal: BudgetGoalEntity){
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.updateBudgetGoal(budgetGoal)
//        }
//    }

}