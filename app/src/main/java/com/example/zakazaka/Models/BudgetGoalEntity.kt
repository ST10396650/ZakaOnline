package com.example.zakazaka.Models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Budget_Goal",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class,
            parentColumns = ["userID"],
            childColumns = ["userID"],
            onDelete = ForeignKey.CASCADE )
    ],

    indices = [Index(value = ["userID"])]
)//(Tonnie, 2024)
data class BudgetGoalEntity (
    @PrimaryKey(autoGenerate = false) val budgetGoalID : String = "",
    var minAmount : Double = 0.0,
    var maxAmount : Double = 0.0,
    var month : String = "" ,
    var status : String = "" ,
    var userID : String = ""

)
