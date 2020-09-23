package com.example.signup.ui.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.subjects.BehaviorSubject

class SignUpViewModel: ViewModel() {
    val email: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val password: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val confirmedPassword: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val nickname : BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val birth: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val submit: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    fun validateInput(type: Type, validated: Boolean){
        when(type) {
            Type.EMAIL -> email.onNext(validated)
            Type.PASSWORD -> password.onNext(validated)
            Type.CONFIRM -> confirmedPassword.onNext(validated)
            Type.NICKNAME -> nickname.onNext(validated)
            Type.YEAR -> birth.onNext(validated)
        }
        validateSubmit()
    }

    fun validateSubmit(){
        if(email.value && password.value && confirmedPassword.value && nickname.value && birth.value) submit.onNext(true)
        else submit.onNext(false)
    }
}