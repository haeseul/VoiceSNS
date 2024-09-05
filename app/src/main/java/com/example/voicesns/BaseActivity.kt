package com.example.voicesns

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.voicesns.auth.AuthUtils
import okhttp3.Response

private const val TAG = "BaseActivity_JJB"

open class BaseActivity : AppCompatActivity() {
    protected var isSessionValid = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkJwtValidity()){
            // jwt가 유효하지 않고 리프레시도 실패 시 바로 종료
            isSessionValid = false
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
            return
        }
    }

    private fun checkJwtValidity() : Boolean {
        val prefs = GlobalApplication.prefs
        val jwtToken = prefs.getString("access_token", null)

        if (jwtToken == null || !AuthUtils.isValidToken(jwtToken)) {
            // JWT가 유효하지 않은 경우 Refresh 요청
            // 리프레시 토큰을 사용하여 새로운 액세스 토큰 요청
            val refreshToken = prefs.getString("refresh_token", null)
            var response : Response? = null
            refreshToken?.let{
                response = AuthUtils.refreshJWT(it, prefs)
            } ?: run {
                // refresh 토큰 부재 시 로그인 화면으로 전송
                Log.d(TAG, "refresh absent")
                return false
            }

            if (!response?.isSuccessful!!){
                // refresh 요청 실패 시 로그인 화면으로 전송
                Log.d(TAG, "refresh failed")
                return false
            }

        }
        return true
    }



}