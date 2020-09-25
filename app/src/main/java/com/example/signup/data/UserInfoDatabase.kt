package com.example.signup.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserInfo::class], version = 1, exportSchema = false)
abstract class UserInfoDatabase: RoomDatabase(){
    abstract val userInfoDatabaseDao: UserInfoDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: UserInfoDatabase? = null

        fun getInstance(context: Context): UserInfoDatabase{
            synchronized(this){
                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        UserInfoDatabase::class.java,
                        "user_info_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}