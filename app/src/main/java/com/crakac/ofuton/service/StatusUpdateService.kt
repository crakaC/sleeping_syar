package com.crakac.ofuton.service

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.NotificationCompat
import com.crakac.ofuton.C
import com.crakac.ofuton.R
import com.crakac.ofuton.util.AppUtil
import com.crakac.ofuton.util.BitmapUtil
import com.crakac.ofuton.util.TwitterUtils
import twitter4j.StatusUpdate
import twitter4j.TwitterException

class StatusUpdateService : IntentService("StatusUpdateService") {
    override fun onHandleIntent(intent: Intent) {
        val notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, "StatusUpdateService")

        val text = intent.getStringExtra(C.TEXT)
        val inReplyTo: Long = intent.getLongExtra(C.IN_REPLY_TO, -1)
        val appendedImages = intent.getParcelableArrayListExtra<Uri>(C.ATTACHMENTS)

        val twitter = TwitterUtils.getTwitterInstance()

        val appendedImageCount = appendedImages.size
        val ids = LongArray(appendedImageCount)

        builder.setSmallIcon(R.drawable.ic_send_white_24dp).setProgress(0, 0, true).setContentText("ツイート送信中").setOngoing(true)
        startForeground(1, builder.build())
        try {
            for (i in 0 until appendedImageCount) {
                val appending = appendedImages[i];
                val media = BitmapUtil.createTemporaryResizedImage(contentResolver, appending, C.MAX_APPEND_PICTURE_EDGE_LENGTH)
                val m = twitter.uploadMedia(media)
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
            notifyManager.notify(1, builder.build())
            AppUtil.showToast(R.string.impossible)
            return
        }
        AppUtil.showToast(R.string.tweeted)
        stopForeground(true)
    }
}