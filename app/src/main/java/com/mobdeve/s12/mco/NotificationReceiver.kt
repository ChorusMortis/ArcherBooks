package com.mobdeve.s12.mco

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "notification_channel"
        const val CHANNEL_NAME = "Notifications"
        const val NOTIF_MESSAGE = "NOTIFICATION_MESSAGE"
        const val NOTIF_ID = "NOTIFICATION_ID"

        // requestCode: unique ID of PendingIntent
        // notificationId: unique ID of notification
        // they can be the same
        fun sendNotification(activity: Activity, message: String, requestCode: String, notificationId: String, millisFromNow: Long) {
            val intent = Intent(activity, NotificationReceiver::class.java)
            intent.putExtra(NOTIF_MESSAGE, message)
            intent.putExtra(NOTIF_ID, notificationId.hashCode())
            val pendingIntent = PendingIntent.getBroadcast(activity, requestCode.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerAtMillis = System.currentTimeMillis() + millisFromNow

            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }

        // requestCode: unique ID of PendingIntent
        // notificationId: unique ID of notification
        // they can be the same
        fun cancelNotification(activity: Activity, message: String, requestCode: String, notificationId: String) {
            val intent = Intent(activity, NotificationReceiver::class.java)
            intent.putExtra(NOTIF_MESSAGE, message)
            intent.putExtra(NOTIF_ID, notificationId.hashCode())
            val pendingIntent = PendingIntent.getBroadcast(activity, requestCode.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }

    // called when intent is passed
    override fun onReceive(context: Context, intent: Intent) {
        val customMessage = intent.getStringExtra(NOTIF_MESSAGE) ?: return
        val notificationId = intent.getStringExtra(NOTIF_MESSAGE).hashCode()
        if (notificationId == 0) return // string hashcode is 0 if string is null
        createNotification(context,"Archer Books", customMessage, notificationId)
    }

    private fun createNotification(context: Context, title: String, message: String, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // No runtime permission needed for versions below Android 13
            manager.notify(notificationId, notification)
            return
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Request the permission at beginning of app
//            ActivityCompat.requestPermissions(
//                applicationContext,
//                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
//                1001
//            )
        } else {
            // Permission already granted; proceed with the notification
            manager.notify(notificationId, notification)
        }
    }
}