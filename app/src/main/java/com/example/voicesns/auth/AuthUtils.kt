package com.example.voicesns.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.auth0.android.jwt.JWT
import com.example.voicesns.common.ApplicationClass
import com.example.voicesns.GlobalApplication
import com.example.voicesns.LoginActivity
import com.example.voicesns.common.ApiService
import com.example.voicesns.register.Message
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date

private const val TAG = "AuthUtils_JJB"

object AuthUtils {
    private lateinit var apiService: ApiService
    fun refreshJWT(refreshToken: String, prefs: SharedPreferences, context: Context, callback: (Boolean) -> Unit) {

        // Retrofit 인스턴스 생성
        apiService = ApplicationClass.getClient(context = context).create(ApiService::class.java)

        // 토큰을 헤더에 실어 보냄
        apiService.refreshToken("Refresh-Token $refreshToken").enqueue(object : Callback<Message> {
            override fun onResponse(call: Call<Message>, response: Response<Message>) {
                if (response.isSuccessful){
                    saveToken(response, prefs)
                    callback(true)
                } else {
                    Log.d(TAG, "onResponse: JWT Refresh failed")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<Message>, t: Throwable) {
                Log.e(TAG, "Failed to refresh token: ${t.message}")
                callback(false) // 실패 시 false 반환
            }

        })

    }

    fun saveToken(response: Response<Message>, prefs: SharedPreferences){
        response.headers().get("Authorization")?.let { newAccessToken ->
            val token = newAccessToken.replace("Bearer ", "")
            prefs.edit().putString("access_token", token).apply()
        }

        response.headers().get("Refresh-Token")?.let { newRefreshToken ->
            prefs.edit().putString("refresh_token", newRefreshToken).apply()
        }
    }

    fun saveTokenOkHttp(response: okhttp3.Response, prefs: SharedPreferences){
        response.header("Authorization")?.let { newAccessToken ->
            val token = newAccessToken.replace("Bearer ", "")
            prefs.edit().putString("access_token", token).apply()
        }

        response.header("Refresh-Token")?.let { newRefreshToken ->
            prefs.edit().putString("refresh_token", newRefreshToken).apply()
        }
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


    fun checkJwtValidity(context: Context, callback: (Boolean) -> Unit) {
        // 페이지 이동 시 jwt access 유효성 검사 및 리프레시 요청
        Log.d(TAG, "checkJwtValidity: ")
        val prefs = GlobalApplication.prefs
        val jwtToken = prefs.getString("access_token", null)

        if (jwtToken == null || !isValidToken(jwtToken)) {
            // JWT가 유효하지 않은 경우 Refresh 요청
            val refreshToken = prefs.getString("refresh_token", null)

            if (refreshToken != null) {
                refreshJWT(refreshToken, prefs, context) { success ->
                    if (!success) {
                        Log.d(TAG, "refresh failed")
                        backToLogin(context)
                    }
                    callback(success)
                }
            } else {
                // refresh 토큰 부재 시 로그인 화면으로 전송
                Log.d(TAG, "refresh absent")
                backToLogin(context)
                callback(false)
            }
        } else {
            callback(true)
        }
    }

}