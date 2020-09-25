package com.example.signup.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_info_table")
data class UserInfo(
    @PrimaryKey
    val email: String = "",

    @ColumnInfo(name = "password")
    val password: String = "",

    @ColumnInfo(name = "nickname")
    val nickname: String = "",

    @ColumnInfo(name = "birth")
    val birth: String = "",

    @ColumnInfo(name = "sex")
    val sex: String = "",

    @ColumnInfo(name = "required_term")
    val requiredTerm: Boolean = true,

    @ColumnInfo(name = "optional_term")
    val optionalTerm: Boolean = false
)