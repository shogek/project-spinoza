package com.shogek.spinoza.activities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore.Images.Media.getBitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.shogek.spinoza.CONVERSATION_ID
import com.shogek.spinoza.R
import com.shogek.spinoza.models.Contact
import com.shogek.spinoza.repositories.ConversationRepository


// More information can be found at:
// https://developer.android.com/design/patterns/notifications.html

/** Helper class for showing and canceling message received notifications. */
object MessageReceivedNotification {
    /** The unique ID for this type of notification. */
    private const val NOTIFICATION_TAG = "SPINOZA_NOTIFICATION_TAG"
    private const val CHANNEL_NEW_MESSAGES = "CHANNEL_NEW_MESSAGES"
    private var isChannelCreated: Boolean = false

    /**
     *  Show a notification when a message is receiver.
     *
     * @param threadId The ID of a 'Conversation' for getting the correct 'Contact' and setting intent
     * @param strippedPhone The phone number which sent the SMS message
     * @param message The content of the SMS message
     * */
    fun notify(context: Context, threadId: Number?, strippedPhone: String, message: String) {
        this.registerNotificationChannel(context)

        var pictureUri: String? = null
        var contact: Contact? = null

        if (threadId != null) {
            contact = ConversationRepository
                .getAll(context.contentResolver)
                .find {c -> c.threadId == threadId}
                ?.contact
            pictureUri = contact?.photoUri
        }

        val notificationTitle = contact?.displayName ?: strippedPhone

        // This image is used as the notification's large icon (thumbnail)
        val picture =
            if (pictureUri != null)
                getBitmap(context.contentResolver, Uri.parse(pictureUri))
            else
                null

        val builder = Builder(context, CHANNEL_NEW_MESSAGES)
            // Set appropriate default for the notification light, sound and vibration
            .setDefaults(Notification.DEFAULT_ALL)
            // Set required fields
            .setSmallIcon(R.drawable.ic_notification_24dp)
            .setContentTitle(notificationTitle)
            .setContentText(message)
            // ----- All fields below this line are optional -----
            // Use a default priority (>= Android 4.1)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Provide a large icon, shown with the notification in the notification drawer (>= Android 3.0)
            .setLargeIcon(picture)
            // Set ticker text (preview) information for this notification.
            .setTicker("$notificationTitle: $message")
            // Automatically dismiss the notification when it is touched.
            .setAutoCancel(true)
            // Make application name and icon colourised
            .setColorized(true)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            // Set the pending intent to be initiated when the user touches the notification
            .setContentIntent(this.getPendingIntent(context, threadId))
            // Implement a specific style of possible notifications
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle(notificationTitle)
                .setSummaryText("New message")
                .bigText(message))

        this.createNotification(context, builder.build())
    }

    fun cancel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(this.NOTIFICATION_TAG, 0)
    }

    private fun createNotification(context: Context, notification: Notification) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(this.NOTIFICATION_TAG, 0, notification)
    }

    private fun registerNotificationChannel(context: Context) {
        /*
            A channel is used to separate your notifications into categories
            so that the user can disable what he thinks is not important to him
            rather than blocking all notifications from your app.
        */
        if (this.isChannelCreated)
            return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            this.CHANNEL_NEW_MESSAGES,
            "New messages",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)

        this.isChannelCreated = true
    }

    private fun getPendingIntent(context: Context, threadId: Number?): PendingIntent? {
        if (threadId == null) {
            return PendingIntent.getActivity(
                context,
                1,
                Intent(context, ConversationListActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            val intent = Intent(context, MessageListActivity::class.java)
            intent.putExtra(CONVERSATION_ID, threadId)

            // Create the back stack (pressing 'back' will navigate to the parent activity, not the home screen)
            return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}