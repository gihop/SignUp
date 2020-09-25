package com.example.signup.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Flowable


@Dao
interface UserInfoDatabaseDao{
    @Insert
    fun insert(userInfo: UserInfo)

    @Query("SELECT * from user_info_table WHERE email = :email")
    fun get(email: String): Flowable<UserInfo>

    @Query("DELETE FROM user_info_table")
    fun clear()
}