package com.example.voicesns.register

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.voicesns.MainActivity
import com.example.voicesns.R
import com.example.voicesns.common.ApiService
import com.example.voicesns.common.ApplicationClass
import com.example.voicesns.databinding.ActivityRegisterAlarmBinding
import com.example.voicesns.utils.TimeUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import kotlin.math.log

private const val TAG = "RegisterAlarmActivity_JHS"

class RegisterAlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterAlarmBinding
    private lateinit var apiService: ApiService

    private var alarmOn: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // retrofit 인스턴스
        apiService = ApplicationClass.getClient(context = this).create(ApiService::class.java)

        val timePicker: TimePicker = binding.timePicker

        binding.skipAlarmTxt.setOnClickListener {
            val text = binding.skipAlarmTxt

            if (alarmOn) {
                AlertDialog.Builder(this)
                    .setMessage(R.string.skip_alarm_txt)
                    .setPositiveButton("예") { dialog, id ->
                        alarmOn = false
                        text.setText(R.string.skip_alarm_no)
                        Toast.makeText(this, "알람이 꺼졌습니다.", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "alarm : ${alarmOn}")
                    }
                    .setNegativeButton("취소") { dialog, id ->
                        dialog.dismiss()
                    }
                    .create().show()
            } else {
                text.setText(R.string.skip_alarm)
                alarmOn = true
                Toast.makeText(this, "알람이 켜졌습니다.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "alarm : ${alarmOn}")
            }
        }

        binding.finishButton.setOnClickListener {
            val sqlTime = TimeUtils.getSQLTime(timePicker.hour, timePicker.minute)
            var user = User(email = intent.getStringExtra("email").toString(),
                            password = intent.getStringExtra("password").toString(),
                            nickname = intent.getStringExtra("nickname").toString(),
                            alarmOn = alarmOn )
            if (alarmOn) user = user.copy(alarmTime = sqlTime)

            Log.d(TAG, "sqlTime : ${sqlTime}")
            Log.d(TAG, "user : ${user}")

            apiService.postUser(user).enqueue(object : Callback<Message> {
                override fun onResponse(call: Call<Message>, response: Response<Message>) {
                    Log.d(TAG, "onResponse: "+response)
                    Log.d(TAG, "onResponse: "+response.body())
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterAlarmActivity, response.toString(), Toast.LENGTH_SHORT).show()

                        // 로그인 이후 화면으로 이동
                        val intent = Intent(this@RegisterAlarmActivity, MainActivity::class.java)
                        startActivity(intent)

                    } else {
                        Toast.makeText(this@RegisterAlarmActivity, "회원가입 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Message>, t: Throwable) {
                    Toast.makeText(this@RegisterAlarmActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onFailure: ${t.message}")
                }

            })
        }
    }
}