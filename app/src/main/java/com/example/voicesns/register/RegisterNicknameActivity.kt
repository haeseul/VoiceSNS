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

        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")

        binding.btnNext.setOnClickListener{

            if (binding.editTextId.text.toString().isEmpty()){
                Toast.makeText(this, "올바른 아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // RegisterAlarmActivity 호출
            val intent = Intent(this, RegisterAlarmActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("password", password)
            intent.putExtra("nickname", binding.editTextId.text.toString())
            startActivity(intent)

        }
    }
}