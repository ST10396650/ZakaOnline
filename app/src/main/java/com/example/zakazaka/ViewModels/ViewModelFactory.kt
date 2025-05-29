package com.example.zakazaka.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.zakazaka.Repository.*
class ViewModelFactory(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val budgetGoalRepository: BudgetGoalRepository,
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubCategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginRegistrationViewModel::class.java) ->
                LoginRegistrationViewModel(userRepository) as T

            modelClass.isAssignableFrom(AccountViewModel::class.java) ->
                AccountViewModel(accountRepository) as T
//
            modelClass.isAssignableFrom(BudgetGoalViewModel::class.java) ->
               BudgetGoalViewModel(budgetGoalRepository) as T
//
            modelClass.isAssignableFrom(CategoryViewModel::class.java) ->
                CategoryViewModel(categoryRepository) as T
//
            modelClass.isAssignableFrom(SubCategoryViewModel::class.java) ->
                SubCategoryViewModel(subCategoryRepository) as T
//
            modelClass.isAssignableFrom(TransactionViewModel::class.java) ->
               TransactionViewModel(transactionRepository) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
