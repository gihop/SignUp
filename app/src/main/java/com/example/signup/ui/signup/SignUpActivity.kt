package com.example.signup.ui.signup

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.signup.R
import com.example.signup.data.UserInfo
import com.example.signup.data.UserInfoDatabase
import com.example.signup.ui.main.MainActivity
import com.example.signup.ui.popup.FailedPopupActivity
import com.example.signup.ui.popup.ProgressBarPopupActivity
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.view.touches
import com.jakewharton.rxbinding4.widget.editorActionEvents
import com.jakewharton.rxbinding4.widget.textChangeEvents
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup.birth_text
import kotlinx.android.synthetic.main.activity_signup.email_text
import kotlinx.android.synthetic.main.activity_signup.nickname_text
import kotlinx.android.synthetic.main.activity_signup.password_text
import kotlinx.android.synthetic.main.activity_signup.view.*
import java.util.*

class SignUpActivity: AppCompatActivity() {
    private val disposables = io.reactivex.rxjava3.disposables.CompositeDisposable()

    lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        submit_button.setBackgroundColor(resources.getColor(R.color.mainColor))

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
                changeViewColor(birth_text, Type.BIRTH, true)
                viewModel.limitAge(year, month, day, userYear, userMonth, userDay)
            }, year, month, day);

        disposables.add(email_edit.textChangeEvents()
            .map{ it.text }
            .filter{ it.isNotEmpty() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ email ->
                requestValidation(email.toString(), Type.EMAIL)
            })

        disposables.add(password_edit.textChangeEvents()
            .map{ it.text }
            .filter{ it.isNotEmpty() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ password ->
                requestValidation(password.toString(), Type.PASSWORD)
                requestValidation(password_confirm_edit.text.toString(), Type.CONFIRM)
            })

        disposables.add(password_confirm_edit.textChangeEvents()
            .map{ it.text }
            .filter{ it.isNotEmpty() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ password ->
                requestValidation(password.toString(), Type.CONFIRM)
            })

        disposables.add(nickname_edit.textChangeEvents()
            .map{ it.text }
            .filter{ it.isNotEmpty() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ nickname ->
                requestValidation(nickname.toString(), Type.NICKNAME)
            })

        disposables.add(nickname_edit.editorActionEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                hideSoftKeyboard(nickname_edit)
                datePicker.show()
            })

        disposables.add(birth_edit.touches()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                datePicker.show()
            })

        disposables.add(sex_radio_group.men_button.clicks()
            .observeOn(AndroidSchedulers.mainThread())
           .subscribe{
                viewModel.sex.onNext("남성")
            })

        disposables.add(sex_radio_group.women_button.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                viewModel.sex.onNext("여성")
            })

        disposables.add(sex_radio_group.not_selected_button.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                viewModel.sex.onNext("선택 안 함")
            })

        disposables.add(required_terms_checkBox.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                changeViewColor(required_text, Type.REQUIRED, required_terms_checkBox.isChecked)
            })

        disposables.add(optional_terms_check_box.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                if(optional_terms_check_box.isChecked) viewModel.optionalTerms.onNext(true)
                else viewModel.optionalTerms.onNext(false)
            })

        disposables.add(viewModel.submit
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ submit ->
                submit_button.isEnabled = submit
            })

        disposables.add(submit_button.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                viewModel.findDuplicatedEmail(email_edit.text.toString())

                val intent = Intent(this, ProgressBarPopupActivity::class.java)
                startActivityForResult(intent, 1)
            })
    }

    private fun requestValidation(text: String, type: Type){
        when(type){
            Type.EMAIL -> {
                val emailPattern = android.util.Patterns.EMAIL_ADDRESS
                changeViewColor(email_text, type, emailPattern.matcher(text).matches())
            }

            Type.PASSWORD -> {
                val pwdPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}\$")
                changeViewColor(password_text, type, text.matches(pwdPattern))
            }

            Type.CONFIRM -> {
                changeViewColor(password_confirm_text, type, text == password_edit.text.toString())
            }

            Type.NICKNAME -> {
                val nicknamePattern = Regex("^[\\w\\Wㄱ-ㅎㅏ-ㅣ가-힣]{8,30}\$")
                changeViewColor(nickname_text, type, text.matches(nicknamePattern))
            }
        }
    }

    private fun changeViewColor(textView: TextView, type: Type, match: Boolean){
        if(match){
            if(viewModel.validateInput(type, true))
                submit_button.setBackgroundColor(resources.getColor(R.color.mainColor))
            else submit_button.setBackgroundColor(Color.RED)
            textView.setTextColor(resources.getColor(R.color.mainColor))
        }
        else{
            viewModel.validateInput(type, false)
            textView.setTextColor(Color.RED)
            submit_button.setBackgroundColor(Color.RED)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var httpRequest = viewModel.requestSignUp()
        when(httpRequest){
            Http.OK -> launchMainActivity()
            Http.BAD_REQUEST -> launchFailedPopupActivity("이미 가입된 이메일입니다.")
            Http.UNAUTHORIZED -> launchFailedPopupActivity("만 14세 미만은 회원가입할 수 없습니다.")
        }
    }

    private fun launchMainActivity(){
        val userInfo = UserInfo(email_edit.text.toString(), password_edit.text.toString()
            , nickname_edit.text.toString(), viewModel.birthString.value!!
            , viewModel.sex.value!!, true, viewModel.optionalTerms.value!!)
        viewModel.addToUserInfo(userInfo)

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user_email", email_edit.text.toString())
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun launchFailedPopupActivity(message: String){
        val intent = Intent(this, FailedPopupActivity::class.java)
        intent.putExtra("cause", message)
        startActivity(intent)
    }

    private fun hideSoftKeyboard(view: View) {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).run{
            hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}

