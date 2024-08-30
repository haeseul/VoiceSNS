package com.example.voicesns

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.voicesns.databinding.ActivityRecordBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.w3c.dom.Text

class RecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordBinding

    private lateinit var toPublic: TextView
    private lateinit var toPrivate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_record)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 공개범위 텍스트뷰
        toPublic = findViewById(R.id.record_toggle_public)
        toPrivate = findViewById(R.id.record_toggle_private)
        toPublic.setOnClickListener { setDisclosureToggle(true) }
        toPrivate.setOnClickListener { setDisclosureToggle(false) }

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavView)
        val menu = bottomNavView.menu

        // FAB가 눌렸다면 다른 탭은 체크 풀어주기
        menu.findItem(R.id.home).isChecked = false
        menu.findItem(R.id.search).isChecked = false
        menu.findItem(R.id.heart).isChecked = false
        menu.findItem(R.id.mypage).isChecked = false
    }

    @SuppressLint("ResourceAsColor")
    private fun setDisclosureToggle(isViewClicked: Boolean) {
        if (isViewClicked) {
            toPublic.setBackgroundResource(R.drawable.disclosure_select)
            toPublic.setTextColor(R.color.white)
            toPrivate.setBackgroundResource(0)
            toPrivate.setTextColor(R.color.gray)
        } else {
            toPublic.setBackgroundResource(0)
            toPublic.setTextColor(R.color.gray)
            toPrivate.setBackgroundResource(R.drawable.disclosure_select)
            toPrivate.setTextColor(R.color.white)
        }
    }
}