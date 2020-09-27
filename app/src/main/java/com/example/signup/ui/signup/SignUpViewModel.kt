package com.example.signup.ui.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.signup.data.UserInfo
import com.example.signup.data.UserInfoDatabaseDao
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.BehaviorSubject

class SignUpViewModel(
    private val database: UserInfoDatabaseDao,
    application: Application): AndroidViewModel(application) {

    //입력된 사용자 정보의 유효성과 값을 저장하는 서브젝트입니다.
    val email: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val password: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val confirmedPassword: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val nickname : BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val birth: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    val birthString: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    val sex: BehaviorSubject<String> = BehaviorSubject.createDefault("남성")
    private val requiredTerms: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    val optionalTerms: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    //제출 가능한지 여부를 저장한 서브젝트입니다.
    val submit: io.reactivex.rxjava3.subjects.BehaviorSubject<Boolean>
            = io.reactivex.rxjava3.subjects.BehaviorSubject.createDefault(false)

    //중복된 이메일을 저장했는지 여부를 저장한 서브젝트입니다.
    private val duplicated: io.reactivex.rxjava3.subjects.BehaviorSubject<Boolean>
            = io.reactivex.rxjava3.subjects.BehaviorSubject.createDefault(true)

    //만 14세 이상인지 여부를 검사하는 서브젝트입니다.
    private val ageLimit: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    //각 유효성을 저장합니다.
    fun setValidation(type: Type, validated: Boolean){
        when(type) {
            Type.EMAIL -> email.onNext(validated)
            Type.PASSWORD -> password.onNext(validated)
            Type.CONFIRM -> confirmedPassword.onNext(validated)
            Type.NICKNAME -> nickname.onNext(validated)
            Type.BIRTH -> birth.onNext(validated)
            Type.REQUIRED -> requiredTerms.onNext(validated)
        }
    }

    //제출 가능한지 검사합니다.
    fun validateSubmit(): Boolean{
        return if(email.value!! && password?.value!! && confirmedPassword.value!! && nickname.value!!
            && birth.value!! && requiredTerms.value!!) {
            submit.onNext(true)
            true
        } else{
            submit.onNext(false)
            false
        }
    }

    //만 14세 이상인지 검사합니다.
    fun limitAge(nowYear: Int, nowMonth: Int, nowDay: Int, userYear: Int, userMonth: Int, userDay: Int){
        if(nowYear - userYear > 14) ageLimit.onNext(true)
        else if(nowYear - userYear == 14 && nowMonth > userMonth) ageLimit.onNext(true)
        else if(nowYear - userYear == 14 && nowMonth == userMonth && nowDay >= userDay) ageLimit.onNext(true)
        else ageLimit.onNext(false)

        if(ageLimit.value!!) {
            birthString.onNext("${userYear}년 ${userMonth}월 ${userDay}일")
        }
    }

    //중복된 이메일이 있는지 데이터베이스에서 찾습니다.
    fun findDuplicatedEmail(email: String){
        var found = false
        database.get(email)
            .subscribe{
                found = true
                duplicated.onNext(true)
            }
        if(!found) duplicated.onNext(false)
    }

    //사용자 정보를 데이터베이스에 추가합니다.
    fun addToUserInfo(userInfo: UserInfo): Disposable
            = runOnIoScheduler { database.insert(userInfo) }

    //비동기 처리합니다.
    private fun runOnIoScheduler(func: () -> Unit): Disposable
            = Completable.fromCallable(func)
        .subscribeOn(io())
        .subscribe()

    //가입 가능한지 검사합니다.
    fun validateSignUp(): Http{
        return if(!duplicated.value && ageLimit.value!!) Http.OK
        else if(duplicated.value) Http.BAD_REQUEST
        else Http.UNAUTHORIZED
    }
}

enum class Type{
    EMAIL, PASSWORD, CONFIRM, NICKNAME, BIRTH, REQUIRED
}

enum class Http(statusCode: Int){
    OK(200), BAD_REQUEST(400), UNAUTHORIZED(401)
}