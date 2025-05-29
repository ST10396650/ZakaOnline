package com.example.zakazaka.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakazaka.Models.CategoryEntity
import com.example.zakazaka.Repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoryViewModel @Inject constructor(private val repository: CategoryRepository) : ViewModel() {

    fun createCategoryAndReturn(category: CategoryEntity): LiveData<CategoryEntity?> {
        val result = MutableLiveData<CategoryEntity?>()
        repository.createCategory(category) { addedCategory ->
            result.postValue(addedCategory)
        }
        return result
    }

    // Get categories by user ID
    fun getCategoriesByUserId(userId: String): LiveData<List<CategoryEntity>> {
        return repository.getCategoriesByUserId(userId)
    }

    // Update category current amount
    fun updateCategoryCurrentAmount(categoryId: String, additionalAmount: Double, callback: (Boolean) -> Unit) {
        repository.updateCategoryCurrentAmount(categoryId, additionalAmount, callback)
    }
    fun getCategoryById(categoryId: String, callback: (CategoryEntity?) -> Unit) {
        repository.getCategoryById(categoryId, callback)
    }

//    val categories : LiveData<List<CategoryEntity>> = repository.readAllData
//
//    fun createCategoryAndReturn(category: CategoryEntity): LiveData<CategoryEntity?> {
//        val result = MutableLiveData<CategoryEntity?>()
//        viewModelScope.launch(Dispatchers.IO) {
//            val id = repository.createCategory(category)
//            if(id  > 0){
//                val createdCategory = repository.getCategoryById(id)
//                result.postValue(createdCategory)
//            } else {
//                result.postValue(null)
//            }
//        }
//        return result
//    }
//    fun getCategorybyId(categoryId: Long): LiveData<CategoryEntity?> {
//        val category = MutableLiveData<CategoryEntity>()
//        viewModelScope.launch(Dispatchers.IO) {
//            category.postValue(repository.getCategoryById(categoryId))
//        }
//        return category
//    }
//
//    fun deleteCategory(categoryID:Long){
//        //functionality to delete a category
//    }
//    fun updateCategoryLimit(categoryID:Long,newLimit:Double){
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.updateCategoryLimit(categoryID, newLimit)
//        }
//        //functionality to update the limit of a category, normallly when user deposits money from into an account
//    }
//    fun updateCategoryCurrentAmount(categoryID:Long,amount:Double){
//        var newAmount : Double = amount
//        //functionality to Update the amount spent in a category, normally would be called in TransactionViewModel
//        viewModelScope.launch(Dispatchers.IO) {
//            val category = repository.getCategoryById(categoryID)
//            if(category != null)
//                newAmount = category.currentAmount + amount
//            repository.updateCategoryCurrentAmount(categoryID, newAmount)
//        }
//    }
//     fun getCategoriesByUserId(userId:Long): LiveData<List<CategoryEntity>> {
//        val categories = MutableLiveData<List<CategoryEntity>>()
//        viewModelScope.launch(Dispatchers.IO) {
//            categories.postValue(repository.getCategory(userId))
//        }
//        return categories
//        //return repository.getCategory(userId)
//    }

}