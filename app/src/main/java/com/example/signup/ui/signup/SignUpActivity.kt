package com.example.signup.ui.signup

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.signup.R
import com.jakewharton.rxbinding4.widget.textChangeEvents
import com.jakewharton.rxbinding4.widget.textChanges
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

        disposables.add(email_edit.textChangeEvents()
            .map{ it.text }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ text ->
                validate(text.toString(), Type.EMAIL)
            })
    }

    private fun validate(text: String, type: Type){
        when(type){
            Type.EMAIL -> {
                val pattern = android.util.Patterns.EMAIL_ADDRESS
                if(pattern.matcher(text).matches()){
                    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
                }
            }
        }


    }
}

enum class Type{
    EMAIL, PASSWORD, CONFIRM, NICKNAME, YEAR, MONTH, DAY
}