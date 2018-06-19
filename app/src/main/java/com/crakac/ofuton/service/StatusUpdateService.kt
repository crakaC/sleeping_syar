package com.crakac.ofuton.service

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.crakac.ofuton.C
import com.crakac.ofuton.R
import com.crakac.ofuton.util.AppUtil
import com.crakac.ofuton.util.TwitterUtils
import com.crakac.ofuton.util.Util
import twitter4j.StatusUpdate
import twitter4j.TwitterException
import java.io.File
import java.util.*

class StatusUpdateService : IntentService("StatusUpdateService") {
    private val channelId = "status_update_service"
    private val channelName = "StatusUpdateService"

    private val rand = Random()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Util.isAfterOreo()) {
            Util.createNotificationChannel(this, channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent) {
        val id = rand.nextInt()
        val notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_send_white_24dp).setProgress(0, 0, true).setContentText(getString(R.string.sending_tweet)).setOngoing(true)
        startForeground(id, builder.build())

        val text = intent.getStringExtra(C.TEXT)
        val inReplyTo: Long = intent.getLongExtra(C.IN_REPLY_TO, -1)
        val appendedImages = intent.getSerializableExtra(C.ATTACHMENTS) as ArrayList<File>

        val twitter = TwitterUtils.getTwitterInstance()

        val appendedImageCount = appendedImages.size
        val ids = LongArray(appendedImageCount)

        try {
            for (i in 0 until appendedImageCount) {
                val m = twitter.uploadMedia(appendedImages[i])
                ids[i] = m.mediaId
            }
            val status = StatusUpdate(text)
            if (inReplyTo > 0) {
                status.inReplyToStatusId = inReplyTo
            }
            status.setMediaIds(*ids)
            twitter.updateStatus(status);
        } catch (e: TwitterException) {
            builder.setProgress(0, 0, false).setContentText(getString(R.string.impossible)).setOngoing(false)
            notifyManager.notify(id, builder.build())
            AppUtil.showToast(R.string.impossible)
            return
        }
        AppUtil.showToast(R.string.tweeted)
    }
}