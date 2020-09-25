package com.example.signup.ui.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.room.Insert
import com.example.signup.data.UserInfoDatabaseDao
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.just
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_progress_bar.*

class SignUpViewModel(val database: UserInfoDatabaseDao,
        application: Application): AndroidViewModel(application) {
    private lateinit var disposable: Disposable

    val email: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val password: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val confirmedPassword: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val nickname : BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val birth: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val requiredTerms: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val optionalTerms: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val submit: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    fun validateInput(type: Type, validated: Boolean){
        when(type) {
            Type.EMAIL -> email.onNext(validated)
            Type.PASSWORD -> password.onNext(validated)
            Type.CONFIRM -> confirmedPassword.onNext(validated)
            Type.NICKNAME -> nickname.onNext(validated)
            Type.BIRTH -> birth.onNext(validated)
            Type.REQUIRED -> requiredTerms.onNext(validated)
        }
        validateSubmit()
    }

    fun validateSubmit(){
        if(email.value && password.value && confirmedPassword.value && nickname.value
            && birth.value && requiredTerms.value) submit.onNext(true)
        else submit.onNext(false)
    }

//    fun submit(email: String): Disposable{
//
//    }

//    fun findDuplicatedEmail(email: String){
//        disposable = Observable.just(database.get(email))
//
//        }
//    }
}

enum class Type{
    EMAIL, PASSWORD, CONFIRM, NICKNAME, BIRTH, REQUIRED, OPTIONAL
}