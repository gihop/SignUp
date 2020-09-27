package com.example.signup.ui.signup

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.room.Insert
import com.example.signup.data.UserInfo
import com.example.signup.data.UserInfoDatabaseDao
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.BehaviorSubject

class SignUpViewModel(val database: UserInfoDatabaseDao,
        application: Application): AndroidViewModel(application) {
    val email: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    private val password: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    private val confirmedPassword: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    private val nickname : BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    private val birth: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val birthString: BehaviorSubject<String> = BehaviorSubject.createDefault("")

    val sex: BehaviorSubject<String> = BehaviorSubject.createDefault("남성")

    private val requiredTerms: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val optionalTerms: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val submit: io.reactivex.rxjava3.subjects.BehaviorSubject<Boolean> = io.reactivex.rxjava3.subjects.BehaviorSubject.createDefault(false)

    private val duplicated: io.reactivex.rxjava3.subjects.BehaviorSubject<Boolean> = io.reactivex.rxjava3.subjects.BehaviorSubject.createDefault(false)

    private val ageLimit: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    fun validateInput(type: Type, validated: Boolean): Boolean{
        when(type) {
            Type.EMAIL -> email.onNext(validated)
            Type.PASSWORD -> password.onNext(validated)
            Type.CONFIRM -> confirmedPassword.onNext(validated)
            Type.NICKNAME -> nickname.onNext(validated)
            Type.BIRTH -> birth.onNext(validated)
            Type.REQUIRED -> requiredTerms.onNext(validated)
        }
        return validateSubmit()
    }

    private fun validateSubmit(): Boolean{
        return if(email.value!! && password?.value!! && confirmedPassword.value!! && nickname.value!!
            && birth.value!! && requiredTerms.value!!) {
            submit.onNext(true)
            true
        } else{
            submit.onNext(false)
            false
        }
    }

    fun limitAge(nowYear: Int, nowMonth: Int, nowDay: Int, userYear: Int, userMonth: Int, userDay: Int){
        if(nowYear - userYear > 14) ageLimit.onNext(true)
        else if(nowYear - userYear == 14 && nowMonth > userMonth) ageLimit.onNext(true)
        else if(nowYear - userYear == 14 && nowMonth == userMonth && nowDay >= userDay) ageLimit.onNext(true)
        else ageLimit.onNext(false)

        if(ageLimit.value!!) {
            birthString.onNext("${userYear}년 ${userMonth}월 ${userDay}일")
        }
    }

    fun findDuplicatedEmail(email: String){
        var found = false
        database.get(email)
            .subscribe{
                found = true
                duplicated.onNext(true)
            }
        if(!found) duplicated.onNext(false)
    }

    fun addToUserInfo(userInfo: UserInfo): io.reactivex.disposables.Disposable
            = runOnIoScheduler { database.insert(userInfo) }

    fun runOnIoScheduler(func: () -> Unit): Disposable
            = Completable.fromCallable(func)
        .subscribeOn(Schedulers.io())
        .subscribe()

    fun requestSignUp(): Http{
        if(!duplicated.value && ageLimit.value!!) return Http.OK
        else if(duplicated.value) return Http.BAD_REQUEST
        else return Http.UNAUTHORIZED
    }
}

enum class Type{
    EMAIL, PASSWORD, CONFIRM, NICKNAME, BIRTH, REQUIRED
}

enum class Http(statusCode: Int){
    OK(200), BAD_REQUEST(400), UNAUTHORIZED(401)
}