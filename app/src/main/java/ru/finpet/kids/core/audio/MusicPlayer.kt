package ru.finpet.kids.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.finpet.kids.R
import ru.finpet.kids.core.data.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Фоновый музыкальный плеер.
 * Единственный трек, зациклен, громкость управляется из настроек.
 * Пауза при сворачивании приложения, возобновление — при возврате.
 */
@Singleton
class MusicPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Состояние
    private var userEnabled = true      // из настроек
    private var inForeground = false    // из lifecycle
    private var volume = 0.7f
    private var started = false

    /** Вызывается один раз из MainActivity.onCreate. */
    fun start() {
        if (started) return
        started = true

        // Наблюдаем настройки
        scope.launch {
            settingsRepository.isMusicEnabled.collect { enabled ->
                userEnabled = enabled
                updatePlayback()
            }
        }
        scope.launch {
            settingsRepository.musicVolume.collect { vol ->
                volume = vol
                applyVolume()
            }
        }

        // Готовим плеер
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.soundtrack)?.apply {
                isLooping = true
                setVolume(volume, volume)
            }
            Log.d("MUSIC", "MediaPlayer создан, трек: ${R.raw.soundtrack}")
        } catch (e: Exception) {
            Log.e("MUSIC", "Не удалось создать MediaPlayer", e)
        }
    }

    /** Приложение вышло на передний план — можно играть. */
    fun onAppForeground() {
        inForeground = true
        updatePlayback()
    }

    /** Приложение свернулось — ставим на паузу. */
    fun onAppBackground() {
        inForeground = false
        updatePlayback()
    }

    /** Освободить ресурсы (не обязательно вызывать — Singleton живёт с приложением). */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        started = false
    }

    private fun updatePlayback() {
        val player = mediaPlayer ?: return
        val shouldPlay = userEnabled && inForeground

        if (shouldPlay) {
            if (!player.isPlaying) {
                try {
                    player.start()
                    Log.d("MUSIC", "▶ Играем")
                } catch (e: Exception) {
                    Log.e("MUSIC", "Ошибка start()", e)
                }
            }
        } else {
            if (player.isPlaying) {
                try {
                    player.pause()
                    Log.d("MUSIC", "⏸ Пауза (userEnabled=$userEnabled, inForeground=$inForeground)")
                } catch (e: Exception) {
                    Log.e("MUSIC", "Ошибка pause()", e)
                }
            }
        }
    }

    private fun applyVolume() {
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            Log.e("MUSIC", "Ошибка setVolume", e)
        }
    }
}