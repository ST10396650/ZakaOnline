package com.example.zakazaka.Models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Category",
    foreignKeys = [

        ForeignKey(entity = UserEntity::class,
            parentColumns = ["userID"],
            childColumns = ["userID"],
            onDelete = ForeignKey.CASCADE )
    ],

    indices = [Index(value = ["userID"])]
)
data class CategoryEntity(//Reference Module Manual
    @PrimaryKey(autoGenerate= false) var categoryID : String = "",
    var name : String,
    var budgetLimit : Double = 0.0,
    var currentAmount : Double = 0.0,
    var userID : String = ""
)