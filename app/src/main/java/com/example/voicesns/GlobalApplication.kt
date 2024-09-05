package com.example.voicesns

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {

    companion object{
        lateinit var prefs : SharedPreferences
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Kakao Sdk 초기화
        KakaoSdk.init(this, "403a2492b785640ac2a09fa6b6c537be")

        // MasterKey 생성
        val voiceSNSMasterKey = MasterKey.Builder(
            applicationContext,
            "VoiceSNSMasterKey", // 마스터 키의 이름
        ).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

        // EncryptedSharedPreferences 초기화
        prefs = EncryptedSharedPreferences.create(
            applicationContext, // Context
            "EncryptedPrefs", // 파일 이름
            voiceSNSMasterKey, // MasterKey의 문자열 이름
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // 키 암호화 방식
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // 값 암호화 방식
        )
    }

}