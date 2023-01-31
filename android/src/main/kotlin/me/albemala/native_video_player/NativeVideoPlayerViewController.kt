package me.albemala.native_video_player

import android.annotation.TargetApi
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.VideoView
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.platform.PlatformView
import me.albemala.native_video_player.platform_interface.*


class NativeVideoPlayerViewController(
    messenger: BinaryMessenger,
    viewId: Int,
    context: Context?,
    private val api: NativeVideoPlayerApi = NativeVideoPlayerApi(messenger, viewId),
) : PlatformView,
    NativeVideoPlayerApiDelegate,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener
{

    private var mediaPlayer: MediaPlayer? = null
    private val videoView: VideoView
    private val context: Context?



    init {
        api.delegate = this

        videoView = VideoView(context)
        videoView.setBackgroundColor(0)
        // videoView.setZOrderOnTop(true)
        videoView.setOnPreparedListener(this)
        videoView.setOnCompletionListener(this)
        videoView.setOnErrorListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            videoView.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE)
        }
        this.context = context
    }

    override fun getView(): View {
        return videoView
    }

    override fun dispose() {
        videoView.stopPlayback()
        videoView.setOnPreparedListener(null)
        videoView.setOnErrorListener(null)
        videoView.setOnCompletionListener(null)
        api.dispose()
        mediaPlayer = null
    }

    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        this.mediaPlayer = mediaPlayer
        this.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.mediaPlayer?.setVolume(0f, 0f)
        videoView.seekTo(1)
        api.onPlaybackReady()
    }

    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        api.onPlaybackEnded()
    }

    override fun onError(mediaPlayer: MediaPlayer?, what: Int, extra: Int): Boolean {
        api.onError(Error("$what $extra"))
        return true
    }

    override fun loadVideoSource(videoSource: VideoSource) {
        videoView.stopPlayback()
        mediaPlayer = null
        when (videoSource.type) {
            VideoSourceType.Asset -> videoView.setVideoPath(videoSource.path)
            VideoSourceType.File -> videoView.setVideoPath(videoSource.path)
            VideoSourceType.Network -> videoView.setVideoURI(Uri.parse(videoSource.path))
        }
    }

    override fun getVideoInfo(): VideoInfo {
        return VideoInfo(
            mediaPlayer?.videoWidth ?: 0,
            mediaPlayer?.videoHeight ?: 0,
            videoView.duration / 1000
        )
    }

    override fun getPlaybackPosition(): Int {
        return videoView.currentPosition / 1000
    }

    override fun play() {
        videoView.start()
    }

    override fun pause() {
        videoView.pause()
    }

    override fun stop() {
        videoView.pause()
        videoView.seekTo(0)
    }

    override fun isPlaying(): Boolean {
        return videoView.isPlaying
    }

    override fun seekTo(position: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            mediaPlayer?.seekTo((position * 1000).toLong(), MediaPlayer.SEEK_CLOSEST)
        else
            videoView.seekTo(position * 1000)
    }

    override fun setPlaybackSpeed(speed: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed.toFloat()) ?: return
        else
            api.onError(Error("Playback speed is not supported on this device"))
    }

    override fun setVolume(volume: Double) {
        mediaPlayer?.setVolume(volume.toFloat(), volume.toFloat())
    }
}