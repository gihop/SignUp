package com.example.signup.ui.signup

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.subjects.BehaviorSubject

class SignUpViewModel: ViewModel() {
    val email: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val password: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val confirmedPassword: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val nickname : BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val birth: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
}