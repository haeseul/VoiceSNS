package com.example.voicesns

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import android.view.View
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"

    // 카카오 로그인 콜백 설정
    private val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "카카오계정으로 로그인 실패", error)
        } else if (token != null) {
            Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
            GoMain()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // 로그인 버튼 클릭 시 카카오톡 또는 카카오계정으로 로그인 시도
        findViewById<View>(R.id.login_button).setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                // 카카오톡 로그인 시도
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "로그인 실패 $error")
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        } else {
                            // 카카오계정으로 로그인
                            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                        }
                    } else if (token != null) {
                        Log.i(TAG, "로그인 성공 ${token.accessToken}")
                        GoMain()
                    }
                }
            } else {
                // 카카오계정으로 로그인
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

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