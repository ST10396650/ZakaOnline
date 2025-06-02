package com.example.zakazaka.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.zakazaka.Models.AnalyticsModel
import com.example.zakazaka.Models.SubCategoryEntity
import com.example.zakazaka.Repository.AccountRepository
import com.example.zakazaka.Repository.BudgetGoalRepository
import com.example.zakazaka.Repository.CategoryRepository
import com.example.zakazaka.Repository.SubCategoryRepository
import com.example.zakazaka.Repository.TransactionRepository
import com.example.zakazaka.Repository.UserRepository
import javax.inject.Inject

class AnalyticsViewModel @Inject constructor(
    private val budgetGoalRepository: BudgetGoalRepository,
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubCategoryRepository,
    private val transactionRepository: TransactionRepository
) {
    lateinit var allSubCategories: MutableLiveData<List<SubCategoryEntity>>
    private val _graphData = MutableLiveData<List<AnalyticsModel>>()
    val graphData: LiveData<List<AnalyticsModel>> get() = _graphData


    fun getDefaultAnalyticsData(userId: String) :LiveData<List<AnalyticsModel>> {
        // Implement logic to fetch and process analytics data
        val categoriesLiveData = categoryRepository.getCategoriesByUserId(userId)
        val transactionsList = transactionRepository.getTransactionsByUserId(userId)
        val budgetGoals = budgetGoalRepository.getBudgetGoalsByUserId(userId)


        val transactions = transactionsList.value ?: emptyList()
        val categories = categoriesLiveData.value ?: emptyList()
        val comninedSubCategories = mutableListOf<SubCategoryEntity>()
        categories.forEach{category ->
            val subCategories = subCategoryRepository.getSubCategoriesForCategory(category.categoryID)
            val subCategoriesList = subCategories.value ?: emptyList()
            comninedSubCategories.addAll(subCategoriesList)
        }

        val subCategoryToCategoryMap = comninedSubCategories.associate { it.subCategoryID to it.categoryID }
        val categoryToNameMap = categories.associate { it.categoryID to it.name }
        val categoryTotals = mutableMapOf<String, Double>()



        transactions.forEach{ transaction ->
            val categoryID = subCategoryToCategoryMap[transaction.subCategoryID]
            if (categoryID != null) {
                val currentTotal = categoryTotals.getOrDefault(categoryID, 0.0)
                categoryTotals[categoryID] = currentTotal + transaction.amount
            }
        }
        val analyticsList = categoryTotals.map { (categoryID, totalAmount) ->
            categoryToNameMap[categoryID]?.let{categoryName ->
                AnalyticsModel(categoryName, totalAmount)
            }
        }
        _graphData.value = analyticsList.filterNotNull()
        return graphData
    }
}