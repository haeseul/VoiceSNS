package com.example.voicesns.record

import java.sql.Timestamp

data class Record(
    val recordId: Int? = null,  // AUTO_INCREMENT 필드로 null 허용
    val rDate: Timestamp,
    val content: ByteArray,
    val userId: Int
) {
    // 기본 생성자
    constructor() :
            this(null, Timestamp(System.currentTimeMillis()), ByteArray(0), 0)

    // recordId를 제외한 생성자
    constructor(rDate: Timestamp, content: ByteArray, userId: Int) :
            this(null, rDate, content, userId)
}