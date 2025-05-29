package com.example.zakazaka.Data.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.zakazaka.Models.CategoryEntity

@Dao
interface CategoryDao {
    //adding the new user category to the  database
    @Insert
    suspend fun insert(categoryEntity: CategoryEntity): Long


    @Query("SELECT * FROM Category WHERE categoryID = :categoryId")
    fun getCategoryById(categoryId: Long): CategoryEntity?

    //so here we are going to be retrieving the categories created by a specific user.
    @Query("SELECT * FROM Category WHERE userId = :userId")
    fun getCategoriesByUserId(userId: Long): List<CategoryEntity>

    //so here we are going to be retrieving the categories created by a specific user.
    @Query("SELECT * FROM Category WHERE userID = :userId")
    suspend fun getUserCategories(userId: Long): List<CategoryEntity>

    // Get all categories as LiveData
    @Query("SELECT * FROM Category ORDER BY categoryID ASC")
    fun readAllData(): LiveData<List<CategoryEntity>>

    //here we are checking if the user has went over budget
    @Query("SELECT * FROM Category WHERE userID = :userId AND currentAmount > budgetLimit")
    suspend fun getOverBudgetCategories(userId: Long): List<CategoryEntity>

    @Update
    suspend fun updateCategory(category: CategoryEntity): Int

    /* WHEN WE WANT TO GET THE CURRENT AMOUNT FOR THE SPECIFIC CATEGORY. WE HAVE TO GET
    THE SUM OF ALL SUB CATEGORY TO MAKE SURE THEY STAY WITHIN BUDGET. */
    @Query("UPDATE Category SET currentAmount = :amount WHERE categoryID = :categoryId")
    suspend fun updateCategoryAmount(categoryId: Long, amount: Double)

    //when user wants to change the budget limit for the category
    @Query("UPDATE Category SET budgetLimit = :budgetLimit WHERE categoryID = :categoryId")
    suspend fun updateCategoryBudgetLimit(categoryId: Long, budgetLimit: Double): Int

    // when user wants to delete a specific category
    @Query("DELETE FROM Category WHERE categoryID = :categoryId")
    suspend fun deleteACategory(categoryId: Long): Int

    // deleting all categories
    @Query("DELETE FROM Category")
    suspend fun deleteAllCategories()


} //(The IIE, 2025)

