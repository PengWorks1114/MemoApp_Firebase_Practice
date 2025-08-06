// Memo.kt
package com.example.memoapp_firebase_practice.model

data class Memo(
    var id: String = "",          // Firestore Document ID
    var title: String = "",
    var content: String = "",
    var timestamp: Long = 0L,      // 時間戳（儲存為毫秒）
    val favorite: Boolean = false
)
