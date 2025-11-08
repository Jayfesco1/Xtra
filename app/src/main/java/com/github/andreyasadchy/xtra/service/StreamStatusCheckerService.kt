package com.github.andreyasadchy.xtra.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.andreyasadchy.xtra.model.ui.Stream
import com.github.andreyasadchy.xtra.repository.HelixRepository
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StreamStatusCheckerService : Service() {

    @Inject
    lateinit var helixRepository: HelixRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 10 * 60 * 1000L // 10 minutes

    private val liveStreamers = mutableMapOf<String, Stream>()

    companion object {
        const val ACTION_STREAM_ENDED = "com.github.andreyasadchy.xtra.STREAM_ENDED"
        const val ACTION_RAID = "com.github.andreyasadchy.xtra.RAID"
        const val ACTION_OPEN_STREAM = "com.github.andreyasadchy.xtra.OPEN_STREAM"
    }

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkStreamStatus()
            handler.postDelayed(this, checkInterval)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STREAM_ENDED -> checkStreamStatus()
            ACTION_RAID -> handler.postDelayed({ checkStreamStatus() }, 20 * 60 * 1000L) // 20 minutes
            else -> handler.post(checkRunnable)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)
        job.cancel()
    }

    private fun checkStreamStatus() {
        if (!prefs().getBoolean(C.AUTO_PLAY_ENABLED, false)) {
            return
        }
        val rankedStreamers = prefs().getString(C.RANKED_STREAMERS_LIST, null)
        if (rankedStreamers.isNullOrEmpty()) {
            return
        }
        val rankedStreamersMap = rankedStreamers.split("\n").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) {
                parts[0].toIntOrNull()?.let { rank ->
                    Pair(rank, parts[1])
                }
            } else {
                null
            }
        }.groupBy({ it.first }, { it.second })

        if (rankedStreamersMap.isEmpty()) {
            return
        }

        scope.launch {
            try {
                val response = helixRepository.getStreams(
                    networkLibrary = prefs().getString(C.NETWORK_LIBRARY, "OkHttp"),
                    headers = TwitchApiHelper.getHelixHeaders(applicationContext),
                    logins = rankedStreamersMap.values.flatten()
                )
                val liveStreams = response.data.map {
                    Stream(
                        id = it.id,
                        channelId = it.channelId,
                        channelLogin = it.channelLogin,
                        channelName = it.channelName,
                        title = it.title,
                        viewerCount = it.viewerCount,
                        startedAt = it.startedAt,
                        thumbnailUrl = it.thumbnailUrl,
                        gameId = it.gameId,
                        gameName = it.gameName
                    )
                }
                val currentlyPlaying = prefs().getString(C.CURRENTLY_PLAYING_STREAM, null)
                var highestRankedLiveStream: Stream? = null
                var highestRank = Int.MAX_VALUE

                for (stream in liveStreams) {
                    val rank = rankedStreamersMap.entries.find { it.value.contains(stream.channelLogin) }?.key
                    if (rank != null && rank < highestRank) {
                        highestRank = rank
                        highestRankedLiveStream = stream
                    }
                }

                if (highestRankedLiveStream != null) {
                    val currentlyPlayingIsLive = liveStreams.any { it.channelLogin.equals(currentlyPlaying, ignoreCase = true) }
                    val currentRank = rankedStreamersMap.entries.find { it.value.contains(currentlyPlaying) }?.key

                    if (currentlyPlaying.isNullOrEmpty() || !currentlyPlayingIsLive || (currentRank != null && highestRank < currentRank)) {
                        prefs().edit().putString(C.CURRENTLY_PLAYING_STREAM, highestRankedLiveStream.channelLogin).apply()
                        val intent = Intent(ACTION_OPEN_STREAM).apply {
                            putExtra(MainActivity.KEY_VIDEO, highestRankedLiveStream)
                        }
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                    }
                } else {
                    prefs().edit().remove(C.CURRENTLY_PLAYING_STREAM).apply()
                }
            } catch (e: Exception) {
                Log.e("StreamStatusChecker", "Failed to check stream status", e)
            }
        }
    }
}
