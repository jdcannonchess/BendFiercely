package com.bendfiercely.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri

/**
 * Manages sound playback for the stretch timer chime.
 * Falls back to system notification sound if the custom chime isn't available.
 */
class SoundManager(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    
    fun playChime() {
        release()
        
        try {
            // Try to play custom chime first
            val resId = context.resources.getIdentifier("chime", "raw", context.packageName)
            
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(context, resId)
                
                mediaPlayer?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build()
                    )
                    setOnCompletionListener { mp ->
                        mp.release()
                    }
                    start()
                    return
                }
            }
            
            // Fall back to system notification sound
            playSystemNotification()
        } catch (e: Exception) {
            // Fall back to system notification
            playSystemNotification()
        }
    }
    
    private fun playSystemNotification() {
        try {
            val notificationUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notificationUri)
            ringtone?.play()
        } catch (e: Exception) {
            // Silent fail - not critical
        }
    }
    
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

