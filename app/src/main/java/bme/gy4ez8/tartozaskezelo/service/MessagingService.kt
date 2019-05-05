package bme.gy4ez8.tartozaskezelo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import bme.gy4ez8.tartozaskezelo.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)

        showNotification(p0!!.notification!!.title!!, p0.notification!!.body!!)
    }

    private fun showNotification(title: String, body: String) {
        var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var CHANNEL_ID = "bme.gy4ez8.tartozaskezelo"

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            var notificationChannel = NotificationChannel(CHANNEL_ID, "Tartozaskezelo", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = "Tartozaskezelo"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationManager.createNotificationChannel(notificationChannel)
        }

        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_notification_money)
                .setContentTitle(title)
                .setContentText(body)

        notificationManager.notify(Random.nextInt(), builder.build())
    }

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        Log.d("FIREBASETOKEN", p0)
    }
}