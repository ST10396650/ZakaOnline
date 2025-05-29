package com.example.zakazaka.Data.Database
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.zakazaka.Models.AccountEntity
import com.example.zakazaka.Models.BudgetGoalEntity
import com.example.zakazaka.Models.CategoryEntity
import com.example.zakazaka.Models.SubCategoryEntity
import com.example.zakazaka.Models.TransactionEntity
import com.example.zakazaka.Models.UserEntity


//@Database(entities =
//[UserEntity::class,
//    AccountEntity::class,
//    BudgetGoalEntity::class,
//    CategoryEntity::class,
//    SubCategoryEntity::class,
//    TransactionEntity::class],
//    version = 8)
//@TypeConverters(AppDatabase.Converters::class)
abstract class AppDatabase : RoomDatabase() {
//
//    //Defining the DAO's
//    abstract  fun userDao():UserDao
//    abstract  fun accountDao():AccountDao
//    abstract  fun budgetGoalDao():BudgetGoalDao
//    abstract  fun categoryDao():CategoryDao
//    abstract  fun subCategoryDao():SubCategoryDao
//    abstract  fun transactionDao():TransactionDao
//
//
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "personal_budget_tracker_db"
//                )
//                    .fallbackToDestructiveMigration()
//                    .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    } //(The IIE, 2025)
//    class Converters {
//        @TypeConverter
//        fun TimestampToDate(value: Long?): java.util.Date? {
//            return value?.let { java.util.Date(it) }
//        }
//
//        @TypeConverter
//        fun dateToTimestamp(date: java.util.Date?): Long? {
//            return date?.time
//        }
//    }
}
