package com.example.nexoworxcrmapp.speech

import kotlinx.coroutines.flow.StateFlow

interface SpeechEngine {
    val state: StateFlow<SpeechEngineState>
    val engineLabel: String
    /** True = tap mic once; recognition ends automatically. False = tap again to stop (Vosk manual). */
    val autoStopListening: Boolean
    fun hasRecordPermission(): Boolean
    fun prepare()
    fun startListening()
    fun stopListening()
    fun release()
}
