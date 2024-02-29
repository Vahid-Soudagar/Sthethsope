package com.example.myapplication

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.myapplication.databinding.ActivityMediaPlayerBinding

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPlayerBinding

    private lateinit var mediaPlayer: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("url")
        binding.textView.text = url

        mediaPlayer = ExoPlayer.Builder(this).build()

        binding.playerView.player = mediaPlayer

        val mediaItem = MediaItem.fromUri(url!!)
        mediaPlayer.setMediaItem(mediaItem)
        mediaPlayer.prepare()
        mediaPlayer.play()
    }
}