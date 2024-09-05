package com.example.voicesns.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.voicesns.GlobalApplication
import com.example.voicesns.MainActivity
import okhttp3.Interceptor
import okhttp3.Response

private const val TAG = "AuthInterceptor_JJB"

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        // SharedPreferences에서 액세스 토큰 가져오기
        val prefs = GlobalApplication.prefs
        val jwtToken = prefs.getString("access_token", null)

        // JWT가 null이 아닐 경우 헤더 추가
        jwtToken?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it 12")
            Log.d(TAG, "intercept: $it")
        }

        var response = chain.proceed(requestBuilder.build())

        if (!response.isSuccessful) { // 요청이 비유효한 경우
            // 리프레시 토큰을 사용하여 새로운 액세스 토큰 요청
            val refreshToken = prefs.getString("refresh_token", null)
            refreshToken?.let{
                response.close()
                response = AuthUtils.refreshJWT(it, prefs)

                // 재발급 요청이 성공했다면
                if (response.isSuccessful){
                    // 새로운 jwtToken으로 원 요청 다시 보내기
                    val newJwtToken = prefs.getString("access_token", null)
                    newJwtToken?.let { newToken ->
                        // 원래 요청을 새로운 토큰으로 재구성
                        requestBuilder
                            .removeHeader("Authorization") // 기존 헤더 제거
                            .addHeader("Authorization", "Bearer $newToken") // 새로운 헤더 추가
                            .build()
                        response = chain.proceed(requestBuilder.build()) // 원 요청 재전송
                    }
                }

            } ?: run {
                Log.d(TAG, "intercept: need login")
                Handler(Looper.getMainLooper()).post {
                    if (context is Activity) {
                        context.finish()
                    }
                    // 메인 액티비티로 전환
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                }
            }
        }

        // 응답에서 토큰을 저장
        AuthUtils.saveToken(response, prefs)

        return response
    }

}