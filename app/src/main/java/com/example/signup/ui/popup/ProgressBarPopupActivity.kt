package com.example.signup.ui.popup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import com.example.signup.R
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers.io
import kotlinx.android.synthetic.main.activity_progress_bar_popup.*

class ProgressBarPopupActivity: Activity(){
    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_progress_bar_popup)

        progress_bar.progress = 0

        disposable = Observable.just(progress_bar)
            .subscribeOn(io())
            .subscribe {
                var count = 1
                while(count <= 100) {
                    Thread.sleep(20)
                    it.progress = count
                    percent_text.text = "$count%"
                    count++
                }
                val intent = Intent()
                intent.putExtra("Server Communication", "Done")
                setResult(RESULT_OK, intent)
                finish()
            }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return event?.action != MotionEvent.ACTION_OUTSIDE
    }

    override fun onBackPressed() {
        return
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }
}