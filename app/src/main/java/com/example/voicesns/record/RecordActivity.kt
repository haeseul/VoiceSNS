package com.example.voicesns.record

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.voicesns.common.ApiService
import com.example.voicesns.common.ApplicationClass
import com.example.voicesns.R
import com.example.voicesns.databinding.ActivityRecordBinding
import com.example.voicesns.ui.common.BottomNavFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "RecordActivity_JHS"

class RecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordBinding
    private lateinit var apiService: ApiService

    private lateinit var toPublic: TextView
    private lateinit var toPrivate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }


        // Retrofit Client 생성
        apiService = ApplicationClass.getClient(context = this).create(ApiService::class.java)

        // 하단바 프래그먼트 동적으로 추가
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BottomNavFragment())
                .commit()
        }

//        val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavView)
//        val menu = bottomNavView.menu
//
//        // FAB가 눌렸다면 다른 탭은 체크 풀어주기
//        menu.findItem(R.id.home).isChecked = false
//        menu.findItem(R.id.search).isChecked = false
//        menu.findItem(R.id.heart).isChecked = false
//        menu.findItem(R.id.mypage).isChecked = false

        // 공개 범위 뷰
        toPublic = binding.recordTogglePublic
        toPrivate = binding.recordTogglePrivate
        toPublic.setOnClickListener { setDisclosureToggle(true) }
        toPrivate.setOnClickListener { setDisclosureToggle(false) }

        // 녹음 한 이후 Intent 받아오기
        val recordId = intent.getIntExtra("recordId", -1)
        if (recordId != -1) getRecord(recordId)
    }

    // 공개 범위 설정
    @SuppressLint("ResourceAsColor")
    private fun setDisclosureToggle(isPublic: Boolean) {
        val selectedColor = ContextCompat.getColor(this, R.color.white)
        val unselectedColor = ContextCompat.getColor(this, R.color.gray)
        val selectedDrawable = ContextCompat.getDrawable(this, R.drawable.disclosure_select)

        if (isPublic) {
            toPublic.background = selectedDrawable
            toPublic.setTextColor(selectedColor)
            toPrivate.background = null
            toPrivate.setTextColor(unselectedColor)
        } else {
            toPublic.background = null
            toPublic.setTextColor(unselectedColor)
            toPrivate.background = selectedDrawable
            toPrivate.setTextColor(selectedColor)
        }
    }

    // 녹음창으로 이동
    fun onRecordBlockClick(view: View) {
        startActivity(Intent(this, NewRecordActivity::class.java))
    }

    /*
        CRUD
     */
    private fun getRecord(recordId: Int) {
        val call = apiService.getRecord(recordId)
        call.enqueue(object : Callback<Record> {
            override fun onResponse(call: Call<Record>, response: Response<Record>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RecordActivity, "레코드 가져오기 성공", Toast.LENGTH_SHORT).show()
                    val record = response.body()

                } else {
                    Toast.makeText(this@RecordActivity, "레코드 가져오기 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Record>, t: Throwable) {
                Toast.makeText(this@RecordActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun getUserRecords(userId: Int) {
        val call = apiService.getUserRecords(userId)
        call.enqueue(object : Callback<List<Record>> {
            override fun onResponse(call: Call<List<Record>>, response: Response<List<Record>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RecordActivity, "유저 레코드 가져오기 성공", Toast.LENGTH_SHORT).show()
                    val records = response.body()
                } else {
                    Toast.makeText(this@RecordActivity, "유저 레코드 가져오기 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Record>>, t: Throwable) {
                Toast.makeText(this@RecordActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

}