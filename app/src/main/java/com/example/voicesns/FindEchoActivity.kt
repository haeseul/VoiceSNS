package com.example.voicesns

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


fun TestAPI() {
    val TAG = "APIActivity"

    val client = OkHttpClient()

    val request = Request.Builder()
        .url("http://172.30.1.53:8080/connection/check-friend/1/4")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            // 요청 실패 시 호출
            Log.e(TAG, "API 호출 실패", e)
        }

        override fun onResponse(call: okhttp3.Call, response: Response) {
            // 요청 성공 시 호출
            if (!response.isSuccessful) {
                Log.e(TAG, "API 호출 실패: 코드 ${response}")
            } else {
                // 응답 본문 처리
                Log.d(TAG, "API 호출 성공: ${response.body?.string()}")

            }
        }
    })
}

class FindEchoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_find_echo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        TestAPI()

        val searchBar = findViewById<EditText>(R.id.search_bar)
        searchBar.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT)


        // Immersive Mode 활성화
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)


    }
}

