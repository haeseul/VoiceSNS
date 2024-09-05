package com.example.voicesns.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.auth0.android.jwt.JWT
import com.example.voicesns.common.ApplicationClass
import com.example.voicesns.GlobalApplication
import com.example.voicesns.LoginActivity
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Date

private const val TAG = "AuthUtils_JJB"

object AuthUtils {
    fun refreshJWT(refreshToken: String, prefs: SharedPreferences): Response {
        val url = ApplicationClass.getURL() +"auth/refresh"
        val client = OkHttpClient()

        // POST 요청 생성
        val requestBody = RequestBody.create(MediaType.parse("application/json"), "{}")
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Refresh-Token", refreshToken)
            .build()

        // 동기적으로 요청 처리
        val response = client.newCall(request).execute()
        Log.d(TAG, "refreshJWT: " + response)
        if (!response.isSuccessful) {
            throw IOException("Failed to refresh token: ${response.code()}")
            // 로그인 화면으로 강제이동
        } else {
            // 응답에서 새로운 토큰 추출
            saveToken(response, prefs)
        }

        return response  // 토큰 추출 메소드
    }

    fun saveToken(response: Response, prefs: SharedPreferences){
        response.header("Authorization")?.let { newAccessToken ->
            val token = newAccessToken.replace("Bearer ", "")
            prefs.edit().putString("access_token", token).apply()
        }

        response.header("Refresh-Token")?.let { newRefreshToken ->
            prefs.edit().putString("refresh_token", newRefreshToken).apply()
        }
    }

    fun checkJwtValidity(context: Context): Boolean {
        // 페이지 이동 시 jwt access 유효성 검사 및 리프레시 요청
        val prefs = GlobalApplication.prefs
        val jwtToken = prefs.getString("access_token", null)

        if (jwtToken == null || !isValidToken(jwtToken)) {
            // JWT가 유효하지 않은 경우 Refresh 요청
            // 리프레시 토큰을 사용하여 새로운 액세스 토큰 요청
            val refreshToken = prefs.getString("refresh_token", null)
            var response : Response? = null
            refreshToken?.let{
                response = refreshJWT(it, prefs)
            } ?: run {
                // refresh 토큰 부재 시 로그인 화면으로 전송
                Log.d(TAG, "refresh absent")
                backToLogin(context = context)
                return false
            }

            if (!response?.isSuccessful!!){
                // refresh 요청 실패 시 로그인 화면으로 전송
                Log.d(TAG, "refresh failed")
                backToLogin(context = context)
                return false
            }

        }
        return true
    }

    fun isValidToken(token: String): Boolean {
        // JWT 유효기간 검사
        val jwt = JWT(token)
        val expiration = jwt.expiresAt

        return expiration != null && expiration.after(Date())
    }

    private fun backToLogin(context: Context){
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }


}