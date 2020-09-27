package com.example.signup.ui.signup

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
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
        required_terms_dummy_text.movementMethod = ScrollingMovementMethod()
        optional_terms_dummy_text.movementMethod = ScrollingMovementMethod()

        //뷰모델을 생성합니다.
        val application = requireNotNull(this).application
        val dataSource = UserInfoDatabase.getInstance(application).userInfoDatabaseDao
        val viewModelFactory = SignUpViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SignUpViewModel::class.java)

        //현재 날짜를 불러옵니다.
        val mcurrentTime = Calendar.getInstance()
        val year = mcurrentTime.get(Calendar.YEAR)
        val month = mcurrentTime.get(Calendar.MONTH)
        val day = mcurrentTime.get(Calendar.DAY_OF_MONTH)

        //생년월일 입력을 위한 DatePicker를 생성합니다.
        val datePicker = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { view, userYear, userMonth, userDay ->
                birth_edit.setText(String.format("%d년 %d월 %d일", userYear, userMonth + 1, userDay))

                //changeViewColor를 통해서 뷰모델에 생년월일이 입력됐음을 알립니다.
                changeViewColor(birth_text, Type.BIRTH, true)

                //입력한 사용자의 나이가 만 14세 이상인지 확인합니다.
                viewModel.limitAge(year, month, day, userYear, userMonth, userDay)
            }, year, month, day);

        //이메일 입력 이벤트를 구독합니다.
        disposables.add(email_edit.textChangeEvents()
            .map{ it.text }
            .filter{ it.isNotEmpty() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ email ->
                //입력된 이메일의 유효성을 검사합니다.
                validateInput(email.toString(), Type.EMAIL)
            })

        //비밀번호 입력 이벤트를 구독합니다.
        disposables.add(password_edit.textChangeEvents()
            .map{ it.text }
            .filter{ it.isNotEmpty() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ password ->
                //비밀번호 유효성을 검사합니다.
                validateInput(password.toString(), Type.PASSWORD)

                //비밀번호 확인과 같은지 검사합니다.
                validateInput(password_confirm_edit.text.toString(), Type.CONFIRM)
            })

        //비밀번호 확인 입력 이벤트를 구독합니다.
        disposables.add(password_confirm_edit.textChangeEvents()
            .map{ it.text }
            .filter{ it.isNotEmpty() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ password ->
                //비밀번호와 같은지 검사합니다.
                validateInput(password.toString(), Type.CONFIRM)
            })

        //닉네임 입력 이벤트를 구독합니다.
        disposables.add(nickname_edit.textChangeEvents()
            .map{ it.text }
            .filter{ it.isNotEmpty() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ nickname ->
                //닉네임 유효성을 검사합니다.
                validateInput(nickname.toString(), Type.NICKNAME)
            })

        //닉네임 입력 중 액션 키보드 버튼 이벤트를 구독합니다.
        disposables.add(nickname_edit.editorActionEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                //키보드를 숨깁니다.
                hideSoftKeyboard(nickname_edit)

                //생년월일을 선택하도록 합니다.
                datePicker.show()
            })

        //생년월일 EditText 터치 이벤트를 구독합니다.
        disposables.add(birth_edit.touches()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                //생년월일을 선택하도록 합니다.
                datePicker.show()
            })

        //성별 클릭 이벤트를 구독합니다.
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

        //필수 약관 클릭 이벤트를 구독합니다.
        disposables.add(required_terms_checkBox.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                changeViewColor(required_text, Type.REQUIRED, required_terms_checkBox.isChecked)
            })

        //선택 약관 클릭 이벤트를 구독합니다.
        disposables.add(optional_terms_check_box.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                if(optional_terms_check_box.isChecked) viewModel.optionalTerms.onNext(true)
                else viewModel.optionalTerms.onNext(false)
            })

        //뷰모델의 submit 서브젝트를 구독해서 상태에 따라 버튼을 활성화 시킵니다.
        disposables.add(viewModel.submit
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ submit ->
                submit_button.isEnabled = submit
            })

        //제출 버튼 클릭 이벤트를 구독합니다.
        disposables.add(submit_button.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                //중복되는 이메일이 이미 있는지 검사합니다.
                viewModel.findDuplicatedEmail(email_edit.text.toString())

                //progressBar 액티비티를 시작합니다.
                val intent = Intent(this, ProgressBarPopupActivity::class.java)
                startActivityForResult(intent, 1)
            })
    }

    //입력받은 값의 유효성을 검사합니다.
    private fun validateInput(text: String, type: Type){
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

    //유효성 검사에 따라 뷰의 색깔을 변경합니다.
    private fun changeViewColor(textView: TextView, type: Type, match: Boolean){
        //유효성 검사 결과를 뷰모델에 저장합니다.
        viewModel.setValidation(type, match)

        if(match){
            //제출 가능한지 검사합니다.
            if(viewModel.validateSubmit())
                submit_button.setBackgroundColor(resources.getColor(R.color.mainColor))
            else submit_button.setBackgroundColor(Color.RED)

            textView.setTextColor(resources.getColor(R.color.mainColor))
        }
        else{
            textView.setTextColor(Color.RED)
            submit_button.setBackgroundColor(Color.RED)
        }
    }

    //progressBar 액티비티가 완료된 경우 호출됩니다.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //가입 가능한지 검사합니다.
        var httpRequest = viewModel.validateSignUp()

        when(httpRequest){
            Http.OK -> launchMainActivity()
            Http.BAD_REQUEST -> launchFailedPopupActivity("이미 가입된 이메일입니다.")
            Http.UNAUTHORIZED -> launchFailedPopupActivity("만 14세 미만은 회원가입할 수 없습니다.")
        }
    }

    //MainActivity를 시작합니다.
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

    //Failed Popup을 띄웁니다.
    private fun launchFailedPopupActivity(message: String){
        val intent = Intent(this, FailedPopupActivity::class.java)
        intent.putExtra("cause", message)
        startActivity(intent)
    }

    //키보드를 숨깁니다.
    private fun hideSoftKeyboard(view: View) {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).run{
            hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    //디스포저블을 해제합니다.
    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}

