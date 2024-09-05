package com.example.voicesns.record

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
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
import com.example.voicesns.utils.TimeUtils
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
    private var isRecording: Boolean = false
    private var startTime: Long = 0
    private val handler = Handler()
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            val elapsedTime = SystemClock.elapsedRealtime() - startTime
            val seconds = (elapsedTime / 1000).toInt()
            binding.recordTime.text = TimeUtils.formatElapsed(seconds)

            // 1분 30초 지나면 자동 종료
            if (seconds >= 90) stopRecording()
            else handler.postDelayed(this, 1000)
        }
    }

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
            else checkPermission()      // 권한 체크 후 녹음 시작
        }
    }

    // 권한 체크
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청 필요 여부 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                // 사용자가 "다시 묻지 않기"를 체크하지 않은 상태에서 권한을 거부한 경우
                Toast.makeText(this, "녹음을 위해 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            } else {    // 권한 요청
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSION_CODE)
            }
        } else {    // 이미 권한이 부여된 경우 녹음 시작
            startRecording()
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()  // 권한이 허용되면 녹음을 시작
            } else {    // 권한이 거부된 경우 처리
                Toast.makeText(this, "녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)  // 오디오 입력 소스 : 마이크
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)  // 출력 파일 형식 : 3GP
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)  // 오디오 인코더 : AMR_NB -> 음성 데이터 압축

            // 앱 캐시 데이터에 임시 저장
            val fileName = "${externalCacheDir?.absolutePath}/recording_${System.currentTimeMillis()}.3gp"
            setOutputFile(fileName)
            recordingFilePath = fileName
            Log.d(TAG, "startRecording: $fileName")

            try {
                prepare()
                start()
                Log.d(TAG, "startRecording: 녹음 시작")
                Toast.makeText(this@NewRecordActivity, "녹음 시작", Toast.LENGTH_SHORT).show()

                binding.recordStart.setImageResource(R.drawable.btn_stop)   // 버튼 이미지 변경
                isRecording = true      // 녹음 상태 변경

                // 녹음 시간 업데이트
                startTime = SystemClock.elapsedRealtime()
                handler.post(updateTimeRunnable)

            } catch (e: Exception) {
                Log.e(TAG, "startRecording: MediaRecorder prepare/start failed", e)
                releaseMediaRecorder()  // 에러 발생 시 MediaRecorder 리소스 해제
            }
        }
    }

    private fun releaseMediaRecorder() {
        mediaRecorder?.apply {
            reset()   // MediaRecorder 초기화
            release() // MediaRecorder 리소스 해제
            mediaRecorder = null
        }
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
            handler.removeCallbacks(updateTimeRunnable)
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
        Log.d(TAG, "byteArray: "+byteArray)

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
                    val returnedRecord = response.body()
                    Log.d(TAG, "레코드 삽입 성공: "+returnedRecord)
                    if (returnedRecord != null) {
                        // RecordActivity로 반환된 Record 객체 전달
                        val resultIntent = Intent()
                        resultIntent.putExtra("recordId", returnedRecord.recordId)
                        resultIntent.putExtra("clickedViewId", getIntent().getIntExtra("clickedViewId", -1))

                        // 성공 후 결과 전달 및 현재 액티비티 종료
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Log.d(TAG, "onResponse: 레코드 반환 실패")
                    }
                } else {
                    Log.d(TAG, "onResponse: 레코드 삽입 실패: ${response.message()}")
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