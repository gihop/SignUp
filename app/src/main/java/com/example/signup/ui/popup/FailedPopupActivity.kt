package com.example.signup.ui.popup

import android.app.Activity
import android.os.Bundle
import android.view.Window
import com.example.signup.R
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_failed_popup.*

class FailedPopupActivity: Activity() {
    private val disposables = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_failed_popup)

        val intent = intent
        message_text.text = intent.getStringExtra("cause")

        disposables.add(confirm_button.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                finish()
            })
    }
}