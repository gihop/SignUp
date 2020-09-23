package com.example.signup.ui.signup

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.signup.R
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.widget.textChangeEvents
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_signup.*

class SignUpActivity: AppCompatActivity() {
    internal val disposables = CompositeDisposable()

    private val viewModel: SignUpViewModel by lazy {
        ViewModelProvider(this).get(SignUpViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        submit_button.isEnabled = false

        disposables.add(email_edit.textChangeEvents()
            .map{ it.text }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ email ->
                validate(email.toString(), Type.EMAIL)
            })

        disposables.add(password_edit.textChangeEvents()
            .map{ it.text }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ password ->
                validate(password.toString(), Type.PASSWORD)
            })

        disposables.add(password_confirm_edit.textChangeEvents()
            .map{ it.text }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ password ->
                validate(password.toString(), Type.CONFIRM)
            })

        disposables.add(nickname_edit.textChangeEvents()
            .map{ it.text }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ nickname ->
                validate(nickname.toString(), Type.NICKNAME)
            })

        disposables.add(birth_year_edit.textChangeEvents()
            .map{ it.text }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ year ->
                validate(year.toString(), Type.YEAR)
            })

        disposables.add(birth_month_edit.textChangeEvents()
            .map{ it.text }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { month ->
                validate(month.toString(), Type.MONTH)
            })

        disposables.add(birth_day_edit.textChangeEvents()
            .map{ it.text }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { day ->
                validate(day.toString(), Type.DAY)
            })

        disposables.add(required_terms_checkBox.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                if(required_terms_checkBox.isChecked) viewModel.validateInput(Type.REQUIRED, true)
                else viewModel.validateInput(Type.REQUIRED, false)
            })

        disposables.add(viewModel.submit
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ submit ->
                submit_button.isEnabled = submit
            })

        disposables.add(submit_button.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                Toast.makeText(this, "clicked!", Toast.LENGTH_SHORT).show()
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

            Type.YEAR -> {
                if(text.length == 4) viewModel.validateInput(type, true)
                else viewModel.validateInput(type, false)
            }

            Type.MONTH -> {
                if(text.length in 1..2 && text.toInt() <= 12) viewModel.validateInput(type, true)
                else viewModel.validateInput(type, false)
            }

            Type.DAY -> {
                if(text.length in 1..2 && text.toInt() in 1..31) viewModel.validateInput(type, true)
                else viewModel.validateInput(type, false)
            }
        }
    }
}

