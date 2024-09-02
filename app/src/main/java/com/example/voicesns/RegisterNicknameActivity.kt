package com.example.voicesns

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.voicesns.databinding.ActivityRegisterNicknameBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "RegisterNicknameActivity_JJB"

class RegisterNicknameActivity : AppCompatActivity() {

    // view Binding
    private lateinit var binding : ActivityRegisterNicknameBinding
    private lateinit var apiService: ApiService

    // EncryptedSharedPreferences
    private lateinit var encryptedSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterNicknameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // EncryptedSharedPreferences 초기화
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        encryptedSharedPreferences = EncryptedSharedPreferences.create(
            this,
            "MyEncryptedPrefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Retrofit 인스턴스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        binding.btnNext.setOnClickListener{
            val nickname = binding.editTextId.text.toString()

            val request = RegisterRequest(intent.getStringExtra("email").toString(), intent.getStringExtra("password").toString(), nickname)

            apiService.registerUser(request).enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                    if (response.isSuccessful) {
                        // 회원가입이 성공했다면 서버에서 로그인 처리 이후 jwt를 전송함
                        val tokens = response.body()

                        // tokens를 EncryptedSharedPreferences에 저장
                        tokens?.let {
                            val editor = encryptedSharedPreferences.edit()
                            editor.putString("accessToken", it["accessToken"])
                            editor.putString("refreshToken", it["refreshToken"])
                            editor.apply()
                        }

                        // 저장된 jwt 확인
//                        val allEntries = encryptedSharedPreferences.all
//                        for ((key, value) in allEntries) {
//                            Log.d(TAG, "$key: $value")
//                        }

                        Toast.makeText(this@RegisterNicknameActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()

                        // 로그인 이후 화면으로 이동

                    } else {
                        Toast.makeText(this@RegisterNicknameActivity, "회원가입 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(this@RegisterNicknameActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onFailure: ${t.message}")
                }

            })
        }

    }
}