package com.example.ainote.data.model

data class QuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0,
    val explanation: String = ""
)
