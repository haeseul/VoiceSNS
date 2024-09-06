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

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        if (!AuthUtils.checkJwtValidity(this)){
//            // jwt가 유효하지 않고 리프레시도 실패 시 바로 종료
//            isSessionValid = false
//            val intent = Intent(this, LoginActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            startActivity(intent)
//            finish()
//            return
//        }
//    }

}