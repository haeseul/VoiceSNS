package com.example.voicesns.record

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.voicesns.ApiService
import com.example.voicesns.ApplicationClass
import com.example.voicesns.R
import com.example.voicesns.databinding.ActivityNewRecordBinding
import com.example.voicesns.databinding.ActivityRecordBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.sql.Timestamp
import java.util.Date

private const val TAG = "NewRecordActivity_JHS"

class NewRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewRecordBinding
    private lateinit var apiService: ApiService

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFilePath: String? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNewRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Log.d(TAG, "onCreate: ")

        // Retrofit Client 생성
        apiService = ApplicationClass.getClient().create(ApiService::class.java)

        // X 버튼 클릭 시 뒤로가기
        binding.closeButton.setOnClickListener{ onBackPressedDispatcher.onBackPressed() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        // 녹음 버튼
        binding.recordStart.setOnClickListener {
            if (isRecording) stopRecording()
            else startRecording()
        }
    }

    private fun startRecording() {
        // 권한 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSION_CODE)
            Log.d(TAG, "startRecording: 권한 얻기")
        }

        // 앱 캐시 데이터에 임시 저장
        val fileName = "${externalCacheDir?.absolutePath}/recording_${System.currentTimeMillis()}.3gp"
        recordingFilePath = fileName
        Log.d(TAG, "startRecording: "+fileName)

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)  // 오디오 입력 소스 : 마이크
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)  // 출력 파일 형식 : 3GP
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)  // 오디오 인코더 : AMR_NB -> 음성 데이터 압축
            setOutputFile(fileName)
            prepare()
            start()
        }
        Log.d(TAG, "startRecording")
        Toast.makeText(this, "녹음 시작", Toast.LENGTH_SHORT).show()

        // 버튼 이미지 변경
        binding.recordStart.setImageResource(R.drawable.btn_stop)
        isRecording = true
    }

    private fun stopRecording() {

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            binding.recordStart.setImageResource(R.drawable.btn_record)
            Toast.makeText(this, "녹음 중지", Toast.LENGTH_SHORT).show()

            // 서버에 업로드 후 앱 데이터에서 삭제
            recordingFilePath?.let { filePath ->
                val file = File(filePath)
                uploadRecording(file)
                if (file.exists()) file.delete()
            }
        } catch (e: IOException) {
            Log.e(TAG, "stopRecording: ${e.message}")
            Toast.makeText(this, "녹음 중지 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadRecording(file: File) {
        val inputStream = FileInputStream(file)
        val byteArray = inputStream.readBytes()
        inputStream.close()
        Log.d(TAG, "uploadRecording: "+byteArray)
        val record = Record(
            rDate = Timestamp(Date().time),
            content = byteArray,
            userId = 1                  // --> 실제 유저 아이디로 대체
        )
        Log.d(TAG, "uploadRecording: "+record)
        createRecord(record)
    }

    /*
        CRUD
     */
    private fun createRecord(record: Record) {
        val call = apiService.createRecord(record)
        call.enqueue(object : Callback<Record> {
            override fun onResponse(call: Call<Record>, response: Response<Record>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@NewRecordActivity, "레코드 삽입 성공", Toast.LENGTH_SHORT).show()
                    val returnedRecord = response.body()
                    if (returnedRecord != null) {
                        // RecordActivity로 반환된 Record 객체 전달
                        val intent = Intent(this@NewRecordActivity, RecordActivity::class.java)
                        intent.putExtra("recordId", returnedRecord.recordId)
                        startActivity(intent)

                        // 성공 후 현재 액티비티 종료
                        finish()
                    } else {
                        Toast.makeText(this@NewRecordActivity, "레코드 반환 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@NewRecordActivity, "레코드 삽입 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Record>, t: Throwable) {
                Toast.makeText(this@NewRecordActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    companion object {
        private const val REQUEST_PERMISSION_CODE = 1001
    }
}