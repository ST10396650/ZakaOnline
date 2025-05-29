package com.example.zakazaka.Data.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.zakazaka.Models.SubCategoryEntity

@Dao
interface SubCategoryDao {
    //when adding data to the table
    @Insert
    suspend fun insert(subCategoryEntity : SubCategoryEntity): Long

    @Query("SELECT * FROM Sub_Category WHERE subCategoryID = :subCategoryId")
     fun getSubCategoryById(subCategoryId: Long): SubCategoryEntity?

    //getting the sub categories for a specific category
    @Query("SELECT * FROM Sub_Category WHERE CategoryID = :categoryId")
    suspend fun getSubCategoriesForCategory(categoryId: Long): List<SubCategoryEntity>

    // Get all categories as LiveData
    @Query("SELECT * FROM Sub_Category ORDER BY subCategoryID ASC")
    fun readAllData(): LiveData<List<SubCategoryEntity>>

    //updating the budget limit for the specific sub category
    @Query("UPDATE Sub_Category SET budgetLimit = :budgetLimit WHERE subCategoryID = :subCategoryId")
    suspend fun updateSubCategoryBudgetLimit(subCategoryId: Long, budgetLimit: Double): Int

    // updating the name and description on the sub category
    @Query("UPDATE Sub_Category SET name = :name, description = :description WHERE subCategoryID = :subCategoryId")
    suspend fun updateSubCategoryDetails(subCategoryId: Long, name: String, description: String): Int

    @Query("UPDATE Sub_Category SET currentAmount = :currentAmount WHERE subCategoryID = :subCategoryId")
    suspend fun updateSubCategoryAmount(subCategoryId: Long, currentAmount: Double): Int


    // when user wants to delete a sub category
    @Query("DELETE FROM Sub_Category WHERE categoryID = :categoryId")
    suspend fun deleteASubCategoriesForCategory(categoryId: Long): Int


    /*ADD IMPLEMENTATION FOR TRANSFER BETWEEN SUB CATEGORIES
     Method to transfer amount between subcategories with budget check */
    @Transaction
    /*this translation annotation make it easier when we have grouped database operations,
      it makes sure that its either this method/operation performed in the database they
      fail/ become successful together. */
    suspend fun transferBetweenSubCategories( //these are the parameters
        fromSubCategoryId: Long,
        toSubCategoryId: Long,
        amount: Double
    ): TransferResult {
        /*
        Get current amounts and budget limits
          this getSubcategoryID method that we have above will be used to get the id
          and all the attributes associated with the specific ID */
        val fromSubCategory = getSubCategoryById(fromSubCategoryId)
        val toSubCategory = getSubCategoryById(toSubCategoryId)

        //checking if both sub categories exists
        if (fromSubCategory == null || toSubCategory == null) {
            return TransferResult.ERROR_SUBCATEGORY_NOT_FOUND
        }

        // checking if fromSubCategory has enough funds
        if (fromSubCategory.currentAmount < amount) {
            return TransferResult.ERROR_INSUFFICIENT_FUNDS
        }

        // calculating the new amounts
        val newFromAmount = fromSubCategory.currentAmount - amount
        val newToAmount = toSubCategory.currentAmount + amount

        /* checking if the new amount would go over budget in the sub category
            its making the transfer to*/
        val willExceedBudget = newToAmount > toSubCategory.budgetLimit

        // updating both subcategories with the new amounts
        updateSubCategoryAmount(fromSubCategoryId, newFromAmount)
        updateSubCategoryAmount(toSubCategoryId, newToAmount)

        //enum class is called and the appropriate message is used.
        return if (willExceedBudget) {
            TransferResult.SUCCESS_BUT_OVER_BUDGET
        } else {
            TransferResult.SUCCESS
        }
    }

    /* this enum class that i have used is like a group of messages/constants that are under a
       specific category. this enum class represents the possible messages to send to the user
       for a specific result. */

    enum class TransferResult {
        SUCCESS,
        SUCCESS_BUT_OVER_BUDGET,
        ERROR_INSUFFICIENT_FUNDS,
        ERROR_SUBCATEGORY_NOT_FOUND
    }


}