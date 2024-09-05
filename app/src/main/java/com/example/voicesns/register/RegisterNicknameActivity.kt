package com.example.voicesns.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.voicesns.common.ApiService
import com.example.voicesns.common.ApplicationClass
import com.example.voicesns.MainActivity
import com.example.voicesns.R
import com.example.voicesns.databinding.ActivityRegisterNicknameBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "RegisterNicknameActivity_JJB"

class RegisterNicknameActivity : AppCompatActivity() {

    // view Binding
    private lateinit var binding : ActivityRegisterNicknameBinding
    private lateinit var apiService: ApiService

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

        // Retrofit 인스턴스 생성
        apiService = ApplicationClass.getClient(context = this).create(ApiService::class.java)

        binding.btnNext.setOnClickListener{
            val nickname = binding.editTextId.text.toString()

            val user = User(email = intent.getStringExtra("email").toString(),
                            password = intent.getStringExtra("password").toString(),
                            nickname = nickname)

            apiService.postUser(user).enqueue(object : Callback<Message> {
                override fun onResponse(call: Call<Message>, response: Response<Message>) {
                    Log.d(TAG, "onResponse: "+response)
                    Log.d(TAG, "onResponse: "+response.body())
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterNicknameActivity, response.toString(), Toast.LENGTH_SHORT).show()

                        // 로그인 이후 화면으로 이동
                        val intent = Intent(this@RegisterNicknameActivity, MainActivity::class.java)
                        startActivity(intent)

                    } else {
                        Toast.makeText(this@RegisterNicknameActivity, "회원가입 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Message>, t: Throwable) {
                    Toast.makeText(this@RegisterNicknameActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onFailure: ${t.message}")
                }

            })
        }

    }
}