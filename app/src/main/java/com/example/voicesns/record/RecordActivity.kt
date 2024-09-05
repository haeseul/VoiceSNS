package com.example.voicesns.record

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.voicesns.ApiService
import com.example.voicesns.ApplicationClass
import com.example.voicesns.R
import com.example.voicesns.databinding.ActivityRecordBinding
import com.example.voicesns.databinding.RecordInfoBlockBinding
import com.example.voicesns.ui.common.BottomNavFragment
import com.example.voicesns.utils.DateUtils
import com.example.voicesns.utils.TimeUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private const val TAG = "RecordActivity_JHS"

class RecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordBinding
    private lateinit var apiService: ApiService
    private lateinit var newRecordLauncher: ActivityResultLauncher<Intent>

    // 클릭된 블록(viewId) 기준 매핑
    private val recordIdMap = mutableMapOf<Int, Int>()
    private val mediaPlayerMap = mutableMapOf<Int, MediaPlayer?>()
    private val isPlayingMap = mutableMapOf<Int, Boolean>()

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
        apiService = ApplicationClass.getClient().create(ApiService::class.java)

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

        // 어떤 레코드 블록이 클릭되었는지 분기
        binding.record1Block.setOnClickListener { onRecordBlockClick(it.id) }
        binding.record2Block.setOnClickListener { onRecordBlockClick(it.id) }
        binding.record3Block.setOnClickListener { onRecordBlockClick(it.id) }

        // 녹음 한 이후 Intent 받아오기
        newRecordLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val recordId = data?.getIntExtra("recordId", -1)
                val clickedViewId = data?.getIntExtra("clickedViewId", -1)

                if (recordId != null && recordId != -1 && clickedViewId != null && clickedViewId != -1) {
                    // UI 업데이트
                    Log.d(TAG, "ActivityResult: Record ID = $recordId, clickedViewId = $clickedViewId")
                    recordIdMap[clickedViewId] = recordId // 뷰와 레코드 매핑
                    getRecord(recordId) { record ->
                        Log.d(TAG, "call back : "+record)
                        updateUI(clickedViewId, record)
                    }
                } else {
                    Log.d(TAG, "ActivityResult: 데이터 수신 실패")
                }
            } else {
                Log.d(TAG, "ActivityResult 코드 불일치: resultCode = " + result.resultCode + ", ok code = " + Activity.RESULT_OK)
            }
        }
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
    fun onRecordBlockClick(viewId: Int) {
        val intent = Intent(this, NewRecordActivity::class.java)
        intent.putExtra("clickedViewId", viewId)
        Log.d(TAG, "onRecordBlockClick viewId: $viewId")
        newRecordLauncher.launch(intent)
    }

    // 레이아웃 동적 교체
    private fun updateUI(viewId: Int, record: Record) {
        // 클릭된 블록에 해당하는 레이아웃 참조
        val blockLayout: LinearLayout = when(viewId) {
            binding.record1Block.id -> binding.record1Block
            binding.record2Block.id -> binding.record2Block
            binding.record3Block.id -> binding.record3Block
            else -> return
        }

        blockLayout.removeAllViews()    // 기존 뷰 제거

        // 새로운 레이아웃 인플레이트
        val recordInfoBinding = RecordInfoBlockBinding.inflate(layoutInflater, blockLayout, false)

        val recordId = recordIdMap[viewId]
        Log.d(TAG, "잠시체크 : $recordId 와 ${record.recordId} 가 같은가?")

        // UI 업데이트
        recordInfoBinding.recordTime.text = DateUtils.formatDate(record.rDate)

        // 이미 부모가 있다면 제거 후 추가
        (recordInfoBinding.recordInfoLayout.parent as? ViewGroup)?.removeView(recordInfoBinding.recordInfoLayout)
        blockLayout.addView(recordInfoBinding.recordInfoLayout)

        // 레이아웃 요소 참조
        val seekBar = recordInfoBinding.recordSeekbar
        val currentTimeText = recordInfoBinding.recordCurrentTime
        val totalTimeText = recordInfoBinding.recordTotalTime
        val recordStart = recordInfoBinding.recordStart
        val recordPost = recordInfoBinding.recordPost

        // MediaPlayer 셋업
        val filePath = getTempFilePath(record.content)
        val mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
        }
        mediaPlayerMap[viewId] = mediaPlayer

        totalTimeText.text = TimeUtils.formatDuration(mediaPlayer.duration)

        // seekbar 셋업
        seekBar.max = mediaPlayer.duration
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer.seekTo(progress)
                currentTimeText.text = TimeUtils.formatDuration(mediaPlayer.currentPosition)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        // 재생 버튼 클릭 리스너
        isPlayingMap[viewId] = false
        recordStart.setOnClickListener {
            if (isPlayingMap[viewId] == true) {
                mediaPlayer.pause()
                isPlayingMap[viewId] = false
                blockLayout.setBackgroundResource(R.drawable.record_empty_block)
                recordPost.setBackgroundResource(R.drawable.register_button)
                recordStart.setImageResource(R.drawable.btn_start)
            } else {
                mediaPlayer.start()
                isPlayingMap[viewId] = true
                blockLayout.setBackgroundResource(R.drawable.record_playing_block)
                recordPost.setBackgroundResource(R.drawable.light_button)
                recordStart.setImageResource(R.drawable.btn_pause)

                // seekbar 및 시간 업데이트 핸들러
                val handler = Handler(Looper.getMainLooper())
                handler.post(object: Runnable {
                    override fun run() {
                        if (mediaPlayer.isPlaying) {
                            seekBar.progress = mediaPlayer.currentPosition
                            currentTimeText.text = TimeUtils.formatDuration(mediaPlayer.currentPosition)
                            handler.postDelayed(this, 50)
                        }
                    }
                })
            }
            Log.d(TAG, "음성 재생 여부 : $isPlayingMap[viewId]")
        }

        // 녹음 재생 완료 처리
        mediaPlayer.setOnCompletionListener {
            seekBar.progress = seekBar.max
            currentTimeText.text = TimeUtils.formatDuration(seekBar.max)
            isPlayingMap[viewId] = false
            blockLayout.setBackgroundResource(R.drawable.record_empty_block)
            recordPost.setBackgroundResource(R.drawable.register_button)
            recordStart.setImageResource(R.drawable.btn_start)
        }
    }


    // 녹음 데이터(byte[]) -> 임시 파일로 저장 -> 경로 추출
    private fun getTempFilePath(audioData: ByteArray): String {
        val tempFile = File.createTempFile("recording", ".3gp", cacheDir)
        tempFile.outputStream().use { it.write(audioData) }
        return tempFile.absolutePath
    }


    /*
        CRUD
     */
    // 콜백 비동기 통신으로 객체 가져오기
    private fun getRecord(recordId: Int, callback: (Record) -> Unit) {
        val call = apiService.getRecord(recordId)
        call.enqueue(object : Callback<Record> {
            override fun onResponse(call: Call<Record>, response: Response<Record>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RecordActivity, "레코드 가져오기 성공", Toast.LENGTH_SHORT).show()
                    val record = response.body()
                    if (record != null) {
                        callback(record)  // 콜백을 통해 record 객체 전달
                    } else {
                        Log.d(TAG, "getRecord: 서버 응답에 레코드가 없습니다.")
                    }
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