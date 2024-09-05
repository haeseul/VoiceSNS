package com.example.voicesns.register

import java.sql.Time

data class User(
    val userId: Int = 0,
    val email: String? = null,
    val password: String? = null,
    val nickname: String? = null,
    val profilePicture: String? = null, // 기본값으로 빈 배열
    val alarmOn: Boolean = false,
    val alarmTime: Time? = null,
    val google: Boolean = false,
    val kakao: Boolean = false
)
