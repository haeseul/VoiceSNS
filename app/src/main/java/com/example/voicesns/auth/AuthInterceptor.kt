package com.example.voicesns.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.voicesns.GlobalApplication
import com.example.voicesns.LoginActivity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

private const val TAG = "AuthInterceptor_JJB"

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val prefs = GlobalApplication.prefs
        val jwtToken = prefs.getString("access_token", null)

        jwtToken?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
            Log.d(TAG, "intercept: $it")
        }

        var response = chain.proceed(requestBuilder.build())

        if (response.isSuccessful) {
            AuthUtils.saveTokenOkHttp(response, prefs)
            return response
        }

        val refreshToken = prefs.getString("refresh_token", null)

        if (refreshToken == null) {
            handleLogout(context)
            return response
        }

        val deferred = CompletableDeferred<Response>()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                response.close()
                Log.d(TAG, "intercept: closed")

                AuthUtils.refreshJWT(refreshToken, prefs, context) { success ->
                    if (success) {
                        Log.d(TAG, "intercept: refresh success")
                        val newJwtToken = prefs.getString("access_token", null)

                        newJwtToken?.let { newToken ->
                            requestBuilder
                                .removeHeader("Authorization")
                                .addHeader("Authorization", "Bearer $newToken")

                            // 원 요청 재전송
                            try {
                                val newResponse = chain.proceed(requestBuilder.build())
                                deferred.complete(newResponse)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error while proceeding request: ${e.message}")
                                deferred.complete(response) // 기존 응답 반환
                            }
                        } ?: deferred.complete(response) // 토큰이 없을 경우 기존 응답 반환
                    } else {
                        handleLogout(context)
                        deferred.complete(response)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "intercept error: ${e.message}")
                handleLogout(context)
                deferred.complete(response)
            }
        }

        response = runBlocking { deferred.await() }
        Log.d(TAG, "intercept: end of intercept")
        return response
    }

    private fun handleLogout(context: Context) {
        Log.d(TAG, "intercept: loggedOut")
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }
}