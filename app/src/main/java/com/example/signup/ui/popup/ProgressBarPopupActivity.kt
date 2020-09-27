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

        //2초 동안 progressBar를 증가시키고 팝업 액티비티를 종료합니다.
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

    //터치 이벤트를 막습니다.
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return event?.action != MotionEvent.ACTION_OUTSIDE
    }

    //백 버튼을 막습니다.
    override fun onBackPressed() {
        return
    }

    //디스포저블을 해제합니다.
    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }
}