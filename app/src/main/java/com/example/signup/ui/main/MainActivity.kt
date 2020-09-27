package com.example.signup.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.signup.R
import com.example.signup.data.UserInfoDatabase
import com.example.signup.data.UserInfoDatabaseDao
import com.example.signup.ui.signup.SignUpActivity
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.Completable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val disposables = CompositeDisposable()

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

        disposables.add(restart_button.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val restartIntent = Intent(this, SignUpActivity::class.java)
                restartIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                startActivity(restartIntent)
            })

        disposables.add(data_reset_button.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                resetDatabase(database)
                Toast.makeText(this, "데이터 초기화 완료", Toast.LENGTH_LONG).show()
            })
    }

    private fun resetDatabase(database: UserInfoDatabaseDao): io.reactivex.disposables.Disposable
            = runOnIoScheduler { database.clear() }

    private fun runOnIoScheduler(func: () -> Unit): io.reactivex.disposables.Disposable
            = Completable.fromCallable(func)
        .subscribeOn(io.reactivex.schedulers.Schedulers.io())
        .subscribe()

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}