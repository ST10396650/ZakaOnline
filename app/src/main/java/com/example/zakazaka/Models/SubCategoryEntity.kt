package com.example.zakazaka.Models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Sub_Category",
    foreignKeys = [

        ForeignKey(entity = CategoryEntity::class,
            parentColumns = ["categoryID"],
            childColumns = ["categoryID"],
            onDelete = ForeignKey.CASCADE )
    ],

    indices = [Index(value = ["categoryID"])]
)//(Tonnie, 2024)
data class SubCategoryEntity (//Reference Module Manual
    @PrimaryKey(autoGenerate = false) var subCategoryID : String = "",
    var name : String = "",
    var description : String = "",
    var budgetLimit : Double = 0.0,
    var currentAmount : Double,
    var categoryID : String = ""
){
    override fun toString():String{
        return name
    }
}
