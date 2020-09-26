package com.example.signup.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.signup.R
import com.example.signup.data.UserInfoDatabase
import com.example.signup.ui.popup.ProgressBarPopupActivity
import com.example.signup.ui.signup.SignUpActivity
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.activity_failed_popup.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = intent
        val email = intent.getStringExtra("user_email")

        val database = UserInfoDatabase.getInstance(application).userInfoDatabaseDao
        val subscribe = database.get(email!!)
            .subscribe { userInfo ->
                user_email_text.text = userInfo.email
                user_password_text.text = userInfo.password
                user_nickname_text.text = userInfo.nickname
                user_birth_text.text = userInfo.birth
                user_sex_text.text = userInfo.sex
                user_required_terms_text.text = "동의"
                if (userInfo.optionalTerm) user_optional_terms_text.text = "동의"
                else user_optional_terms_text.text = "비동의"
            }

        disposable.add(restart_button.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val restartIntent = Intent(this, SignUpActivity::class.java)
                restartIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                startActivity(restartIntent)
            })
    }
}