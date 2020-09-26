package com.example.signup.ui.signup

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.signup.R
import com.example.signup.data.UserInfo
import com.example.signup.data.UserInfoDatabase
import com.example.signup.ui.progressbar.ProgressBarActivity
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.view.touches
import com.jakewharton.rxbinding4.widget.textChangeEvents
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.*

class SignUpActivity: AppCompatActivity() {
    internal val disposables = io.reactivex.rxjava3.disposables.CompositeDisposable()

    lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val application = requireNotNull(this).application

        val dataSource = UserInfoDatabase.getInstance(application).userInfoDatabaseDao

        val viewModelFactory = SignUpViewModelFactory(dataSource, application)

        viewModel = ViewModelProvider(this, viewModelFactory).get(SignUpViewModel::class.java)

        val mcurrentTime = Calendar.getInstance()
        val year = mcurrentTime.get(Calendar.YEAR)
        val month = mcurrentTime.get(Calendar.MONTH)
        val day = mcurrentTime.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { view, userYear, userMonth, userDay ->
                birth_edit.setText(String.format("%d년 %d월 %d일", userYear, userMonth + 1, userDay))
                viewModel.validateInput(Type.BIRTH, true)
                viewModel.limitAge(year, month, day, userYear, userMonth, userDay)
            }, year, month, day);


        disposables.add(email_edit.textChangeEvents()
            .map{ it.text }
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{ email ->
                validate(email.toString(), Type.EMAIL)
            })

        disposables.add(password_edit.textChangeEvents()
            .map{ it.text }
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{ password ->
                validate(password.toString(), Type.PASSWORD)
                validate(password_confirm_edit.text.toString(), Type.CONFIRM)
            })

        disposables.add(password_confirm_edit.textChangeEvents()
            .map{ it.text }
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{ password ->
                validate(password.toString(), Type.CONFIRM)
            })

        disposables.add(nickname_edit.textChangeEvents()
            .map{ it.text }
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{ nickname ->
                validate(nickname.toString(), Type.NICKNAME)
            })

        disposables.add(birth_edit.touches()
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{
                datePicker.show()
            })

        disposables.add(required_terms_checkBox.clicks()
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{
                if(required_terms_checkBox.isChecked) viewModel.validateInput(Type.REQUIRED, true)
                else viewModel.validateInput(Type.REQUIRED, false)
            })

        disposables.add(optional_terms_check_box.clicks()
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{
                if(optional_terms_check_box.isChecked) viewModel.optionalTerms.onNext(true)
                else viewModel.optionalTerms.onNext(false)
            })

        disposables.add(viewModel.submit
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{ submit ->
                submit_button.isEnabled = submit
            })

        disposables.add(submit_button.clicks()

            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .doOnSubscribe { Toast.makeText(this, viewModel.duplicated.toString(), Toast.LENGTH_SHORT).show() }
            .subscribe{
                val intent = Intent(this, ProgressBarActivity::class.java)
                startActivityForResult(intent, 1)
                val userInfo = UserInfo(email = email_edit.text.toString())
//                viewModel.addToUserInfo(userInfo)
                viewModel.findDuplicatedEmail(email_edit.text.toString())
            })

        disposables.add(viewModel.duplicated
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{ duplicated ->
                if(duplicated) Toast.makeText(this, "duplicated", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "not duplicated", Toast.LENGTH_SHORT).show()
            })
    }

    private fun validate(text: String, type: Type){
        when(type){
            Type.EMAIL -> {
                val emailPattern = android.util.Patterns.EMAIL_ADDRESS
                if(emailPattern.matcher(text).matches()) viewModel.validateInput(type, true)
                else viewModel.validateInput(type, false)
            }

            Type.PASSWORD -> {
                val pwdPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}\$")
                if(text.matches(pwdPattern)) viewModel.validateInput(type, true)
                else viewModel.validateInput(type, false)
            }

            Type.CONFIRM -> {
                if(text == password_edit.text.toString()) viewModel.validateInput(type, true)
                else viewModel.validateInput(type, false)
            }

            Type.NICKNAME -> {
                val nicknamePattern = Regex("^[\\w\\Wㄱ-ㅎㅏ-ㅣ가-힣]{8,30}\$")
                if(text.matches(nicknamePattern)) viewModel.validateInput(type, true)
                else viewModel.validateInput(type, false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(!viewModel.duplicated.value && viewModel.ageLimit.value!!) Toast.makeText(
            this, "Success", Toast.LENGTH_SHORT).show()
        else if(viewModel.duplicated.value)
            Toast.makeText(this, "Duplicated Fail", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(this, "Age limit Fail", Toast.LENGTH_SHORT).show()
    }
}

