package com.example.voicesns

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_record)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavView)
        val menu = bottomNavView.menu

        // FAB가 눌렸다면 다른 탭은 체크 풀어주기
        menu.findItem(R.id.home).isChecked = false
        menu.findItem(R.id.search).isChecked = false
        menu.findItem(R.id.heart).isChecked = false
        menu.findItem(R.id.mypage).isChecked = false
    }
}