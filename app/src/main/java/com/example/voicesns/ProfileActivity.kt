package com.example.voicesns

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.voicesns.auth.AuthUtils
import com.example.voicesns.common.ApiService
import com.example.voicesns.common.ApplicationClass
import com.example.voicesns.databinding.ActivityProfileBinding
import com.example.voicesns.register.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "ProfileActivity_JJB"

class ProfileActivity : AppCompatActivity() {

    // view Binding
    private lateinit var binding : ActivityProfileBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // jwt 여부 확인
        if (!AuthUtils.checkJwtValidity(context = this)){
            return
        }

        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrofit 인스턴스 생성
        apiService = ApplicationClass.getClient(context = this).create(ApiService::class.java)

        apiService.getUserSelfInfo().enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful){
                    val user = response.body()
                    Log.d(TAG, "onResponse: "+response)
                    Log.d(TAG, "onResponse: "+user)
                    binding.textViewUserId.text = user!!.email
                    binding.textViewNickName.text = user!!.nickname
                } else {
                    Log.d(TAG, "onResponse: "+response.message())
                    Toast.makeText(this@ProfileActivity, "회원 정보 불러오기 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onFailure: ${t.message}")
            }

        })

    }
}