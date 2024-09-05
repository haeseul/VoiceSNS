package com.example.voicesns.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.voicesns.databinding.ActivityRegisterBinding

private const val TAG = "RegisterActivity_JJB"

class RegisterActivity : AppCompatActivity() {

    // view Binding
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Next버튼 터치 시
        binding.btnNext.setOnClickListener{
            // email이 비어있는 경우
            if (binding.editTextUsername.toString()==""){
                Toast.makeText(this, "올바른 이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 비밀번호가 비어있는 경우
            if (binding.editTextPassword.toString()==""){
                Toast.makeText(this, "올바른 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 비밀번호 확인이 비밀번호와 불일치하는 경우
            if (binding.editTextPassword.toString()!=binding.editTextPassword.toString()){
                Toast.makeText(this, "비밀번호 확인이 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // RegisterNicknameActivity 호출
            val intent = Intent(this, RegisterNicknameActivity::class.java)
            intent.putExtra("email", binding.editTextUsername.text.toString())
            intent.putExtra("password", binding.editTextPassword.text.toString())
            startActivity(intent)
        }
    }

}