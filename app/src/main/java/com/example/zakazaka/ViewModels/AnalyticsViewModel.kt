package com.example.zakazaka.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.zakazaka.Models.AnalyticsModel
import com.example.zakazaka.Models.CategoryEntity
import com.example.zakazaka.Models.TransactionEntity
import com.example.zakazaka.Repository.BudgetGoalRepository
import com.example.zakazaka.Repository.CategoryRepository
import com.example.zakazaka.Repository.SubCategoryRepository
import com.example.zakazaka.Repository.TransactionRepository
import javax.inject.Inject

class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubCategoryRepository,
    private val bugetGoalRepository: BudgetGoalRepository,
) : ViewModel() {
    lateinit var graphInfo : LiveData<List<GraphInfo>>
    fun getDefaultGraphData(userID : String) {
        val categories = categoryRepository.getCategoriesByUserId(userID)
        for (category in categories) {
            var subCategories = subCategoryRepository.getSubCategoriesForCategory(category.id)
            
        }
    }

}
class GraphInfo(
    val categoryName : String,
    val categoryAmount : Double
)
