package com.example.signup.ui.signup

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
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
                if(viewModel.validateInput(Type.BIRTH, true))
                    submit_button.setBackgroundColor(resources.getColor(R.color.mainColor))
                else submit_button.setBackgroundColor(Color.RED)
                viewModel.limitAge(year, month, day, userYear, userMonth, userDay)
            }, year, month, day);


        disposables.add(email_edit.textChangeEvents()
            .map{ it.text }
            .filter{ it.isNotEmpty() }
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{ email ->
                validate(email.toString(), Type.EMAIL)
            })

        disposables.add(password_edit.textChangeEvents()
            .map{ it.text }
            .filter{ it.isNotEmpty() }
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{ password ->
                validate(password.toString(), Type.PASSWORD)
                validate(password_confirm_edit.text.toString(), Type.CONFIRM)
            })

        disposables.add(password_confirm_edit.textChangeEvents()
            .map{ it.text }
            .filter{ it.isNotEmpty() }
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{ password ->
                validate(password.toString(), Type.CONFIRM)
            })

        disposables.add(nickname_edit.textChangeEvents()
            .map{ it.text }
            .filter{ it.isNotEmpty() }
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{ nickname ->
                validate(nickname.toString(), Type.NICKNAME)
            })

        disposables.add(nickname_edit.editorActionEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                hideSoftKeyboard(nickname_edit)
                datePicker.show()
            })

        disposables.add(birth_edit.touches()
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{
                datePicker.show()
            })

        disposables.add(sex_radio_group.men_button.clicks()
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
           .subscribe{
                viewModel.sex.onNext("남성")
            })

        disposables.add(sex_radio_group.women_button.clicks()
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{
                viewModel.sex.onNext("여성")
            })

        disposables.add(sex_radio_group.not_selected_button.clicks()
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{
                viewModel.sex.onNext("선택 안 함")
            })

        disposables.add(required_terms_checkBox.clicks()
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe{
                if(required_terms_checkBox.isChecked){
                    if(viewModel.validateInput(Type.REQUIRED, true))
                        submit_button.setBackgroundColor(resources.getColor(R.color.mainColor))
                    else submit_button.setBackgroundColor(Color.RED)
                }
                else{
                    viewModel.validateInput(Type.REQUIRED, false)
                    submit_button.setBackgroundColor(Color.RED)
                }
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
            .subscribe{
                viewModel.findDuplicatedEmail(email_edit.text.toString())

                val intent = Intent(this, ProgressBarPopupActivity::class.java)
                startActivityForResult(intent, 1)
            })
    }

    private fun validate(text: String, type: Type){
        when(type){
            Type.EMAIL -> {
                val emailPattern = android.util.Patterns.EMAIL_ADDRESS
                if(emailPattern.matcher(text).matches()){
                    if(viewModel.validateInput(type, true))
                        submit_button.setBackgroundColor(resources.getColor(R.color.mainColor))
                    else submit_button.setBackgroundColor(Color.RED)
                    email_text.setTextColor(resources.getColor(R.color.mainColor))
                }
                else{
                    viewModel.validateInput(type, false)
                    email_text.setTextColor(Color.RED)
                    submit_button.setBackgroundColor(Color.RED)
                }
            }

            Type.PASSWORD -> {
                val pwdPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}\$")
                if(text.matches(pwdPattern)) {
                    if(viewModel.validateInput(type, true))
                        submit_button.setBackgroundColor(resources.getColor(R.color.mainColor))
                    else submit_button.setBackgroundColor(Color.RED)
                    password_text.setTextColor(resources.getColor(R.color.mainColor))
                }
                else{
                    viewModel.validateInput(type, false)
                    password_text.setTextColor(Color.RED)
                    submit_button.setBackgroundColor(Color.RED)
                }
            }

            Type.CONFIRM -> {
                if(text == password_edit.text.toString()){
                    if(viewModel.validateInput(type, true))
                        submit_button.setBackgroundColor(resources.getColor(R.color.mainColor))
                    else submit_button.setBackgroundColor(Color.RED)
                    password_confirm_text.setTextColor(resources.getColor(R.color.mainColor))
                }
                else{
                    viewModel.validateInput(type, false)
                    password_confirm_text.setTextColor(Color.RED)
                    submit_button.setBackgroundColor(Color.RED)
                }
            }

            Type.NICKNAME -> {
                val nicknamePattern = Regex("^[\\w\\Wㄱ-ㅎㅏ-ㅣ가-힣]{8,30}\$")
                if(text.matches(nicknamePattern)) {
                    if(viewModel.validateInput(type, true))
                        submit_button.setBackgroundColor(resources.getColor(R.color.mainColor))
                    else submit_button.setBackgroundColor(Color.RED)
                    nickname_text.setTextColor(resources.getColor(R.color.mainColor))
                }
                else{
                    viewModel.validateInput(type, false)
                    nickname_text.setTextColor(Color.RED)
                    submit_button.setBackgroundColor(Color.RED)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var httpRequest = viewModel.requestSignUp()
        when(httpRequest){
            Http.OK -> {
                val userInfo = UserInfo(email_edit.text.toString(), password_edit.text.toString()
                    , nickname_edit.text.toString(), viewModel.birthString.value!!
                    , viewModel.sex.value!!, true, viewModel.optionalTerms.value!!)
                viewModel.addToUserInfo(userInfo)

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("user_email", email_edit.text.toString())
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }

            Http.BAD_REQUEST -> {
                val intent = Intent(this, FailedPopupActivity::class.java)
                intent.putExtra("cause", "이미 가입된 이메일입니다.")
                startActivity(intent)
            }

            Http.UNAUTHORIZED -> {
                val intent = Intent(this, FailedPopupActivity::class.java)
                intent.putExtra("cause", "만 14세 미만은 회원가입할 수 없습니다.")
                startActivity(intent)
            }
        }
    }

    private fun hideSoftKeyboard(view: View) {
        //별도의 변수 없이 획득한 인스턴스의 범위 내에서 작업을 수행한다.
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).run{
            hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}

