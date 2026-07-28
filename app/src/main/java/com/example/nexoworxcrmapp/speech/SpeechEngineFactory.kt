package com.example.nexoworxcrmapp.speech

import android.content.Context
import android.speech.SpeechRecognizer

object SpeechEngineFactory {
    /**
     * Prefer Android [SpeechRecognizer] when available (works on most phones & emulators with Play).
     * Falls back to Vosk offline when Google/device speech is unavailable.
     */
    fun create(context: Context): SpeechEngine {
        return if (SpeechRecognizer.isRecognitionAvailable(context)) {
            PlatformSpeechEngine(context)
        } else {
            VoskSpeechEngine(context)
        }
    }
}
