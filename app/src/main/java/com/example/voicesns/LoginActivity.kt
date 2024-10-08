package com.example.voicesns

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.voicesns.common.ApiService
import com.example.voicesns.register.Message
import com.example.voicesns.register.User
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import androidx.lifecycle.ViewModel
import com.example.voicesns.common.ApplicationClass
import com.example.voicesns.register.RegisterActivity

class SignViewModel : ViewModel() {

    // 서버에 idToken을 보내고 결과를 처리하는 메서드 예시
    fun requestGiverSignIn(idToken: String) {
        // 서버와 통신하여 사용자 인증을 처리하는 로직을 여기에 작성합니다.
        // 예: 서버 API 호출하여 사용자 인증 및 로그인 처리
    }
}

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    private val viewModel: SignViewModel by viewModels()

    // Google SignInClient 초기화
    private val googleSignInClient: GoogleSignInClient by lazy { getGoogleClient() }

    // Google Sign-In 결과 처리
    private val googleAuthLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { viewModel.requestGiverSignIn(it) } // 서버에 idToken 보내기

            // 클라단에서 바로 이름, 이메일 등이 필요하다면 아래와 같이 account를 통해 각 메소드를 불러올 수 있다.
            val userName = account.givenName
            val serverAuth = account.serverAuthCode

            moveSignUpActivity()

        } catch (e: ApiException) {
            Log.e(TAG, "Google 로그인 실패", e)
        }
    }

    private lateinit var apiService: ApiService

    // 카카오 로그인 콜백 설정
    private val kakaoCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "카카오 로그인 실패", error)
        } else if (token != null) {
            Log.i(TAG, "카카오 로그인 성공: ${token.accessToken}")
            goMain()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Retrofit 인스턴스 생성
        apiService = ApplicationClass.getClient(context = this).create(ApiService::class.java)

        // Google 로그인 버튼 클릭 이벤트 설정
        findViewById<View>(R.id.google_login_button).setOnClickListener {
            requestGoogleLogin()
        }

        // Kakao 로그인 버튼 클릭 이벤트 설정
        findViewById<View>(R.id.kakao_login_button).setOnClickListener {
            requestKakaoLogin()
        }

        // 레이아웃의 insets를 조정하여 시스템 바에 대응
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.button_register).setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


        // 이 부분 버튼이 안보여서 주석처리 해뒀습니다.
        // 로그인 버튼 클릭 시 카카오톡 또는 카카오계정으로 로그인 시도
//        findViewById<View>(R.id.login_button).setOnClickListener {
//            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
//                // 카카오톡 로그인 시도
//                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
//                    if (error != null) {
//                        Log.e(TAG, "로그인 실패 $error")
//                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
//                            return@loginWithKakaoTalk
//                        } else {
//                            // 카카오계정으로 로그인
//                            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
//                        }
//                    } else if (token != null) {
//                        Log.i(TAG, "로그인 성공 ${token.accessToken}")
//                        GoMain()
//                    }
//                }
//            } else {
//                // 카카오계정으로 로그인
//                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
//            }
//        }

        // LOGIN 버튼 클릭 시 email 기반 로그인 요청
        findViewById<View>(R.id.button_login).setOnClickListener{
            val user = User(email = findViewById<EditText>(R.id.editTextUsername).text.toString(),
                password = findViewById<EditText>(R.id.editTextPassword).text.toString())
            apiService.login(user).enqueue(object : Callback<Message> {
                override fun onResponse(call: Call<Message>, response: Response<Message>) {
                    if (response.isSuccessful) {

                        // 로그인 이후 화면으로 이동
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)

                    } else {
                        Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Message>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onFailure: ${t.message}")
                }

            })
        }
    }

    // Google 로그인 요청
    private fun requestGoogleLogin() {
        googleSignInClient.signOut() // 기존 로그인 세션 무효화
        val signInIntent = googleSignInClient.signInIntent
        googleAuthLauncher.launch(signInIntent)
    }

    private fun getGoogleClient(): GoogleSignInClient {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_login_client_id)) // 클라이언트 ID가 맞는지 확인
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(this, googleSignInOptions)
    }



    // Kakao 로그인 요청
    private fun requestKakaoLogin() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "Kakao 로그인 실패: $error")
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    } else {
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = kakaoCallback)
                    }
                } else if (token != null) {
                    Log.i(TAG, "Kakao 로그인 성공: ${token.accessToken}")
                    goMain()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = kakaoCallback)
        }
    }

    // 회원가입 화면으로 이동
    private fun moveSignUpActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    // 메인 화면으로 이동
    private fun goMain() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                Log.i(TAG, "사용자 정보 요청 성공: ${user.kakaoAccount?.profile?.nickname}")
            }
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish() // 로그인 화면을 종료하여 뒤로가기 시 다시 돌아오지 않게 함

        // 레이아웃의 insets를 조정하여 시스템 바에 대응
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // 메인 화면으로 이동하는 함수
    private fun GoMain() {
        // 여기에 메인 화면으로 이동하는 로직을 추가하세요
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // 로그인 화면을 종료하여 뒤로가기 시 다시 돌아오지 않게 합니다.
    }
}