package com.example.voicesns.ui.common

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.voicesns.MainActivity
import com.example.voicesns.ProfileActivity
import com.example.voicesns.R
import com.example.voicesns.record.RecordActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class BottomNavFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bottom_nav, container, false)

        // BottomNav 각 버튼 설정
        val bottomNavView: BottomNavigationView = view.findViewById(R.id.bottomNavView)
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(activity, MainActivity::class.java))
                    true
                }
                R.id.search -> {
                    startActivity(Intent(activity, MainActivity::class.java))
                    true
                }
                R.id.heart -> {
                    startActivity(Intent(activity, MainActivity::class.java))
                    true
                }
                R.id.mypage -> {
                    startActivity(Intent(activity, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // FloatingActingButton 설정
        val fab: FloatingActionButton = view.findViewById(R.id.fab_record)
        fab.setOnClickListener { startActivity(Intent(activity, RecordActivity::class.java)) }

        return view
    }
}