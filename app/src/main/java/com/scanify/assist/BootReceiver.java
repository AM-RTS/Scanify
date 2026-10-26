package com.scanify.assist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

/**
 * After a reboot, remind the user if MIUI has fallen back to Google.
 * This cannot rewrite the assistant setting without privileged access.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "assistant";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !"android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            return;
        }
        if (isVoiceServiceSelected(context)) {
            return;
        }
        showReminder(context);
    }

    private boolean isVoiceServiceSelected(Context context) {
        String value = Settings.Secure.getString(
                context.getContentResolver(), "voice_interaction_service");
        if (value == null) {
            return false;
        }
        ComponentName expected = new ComponentName(context, ScanifyVoiceInteractionService.class);
        return expected.equals(ComponentName.unflattenFromString(value));
    }

    private void showReminder(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(channel);

        Intent open = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(
                context,
                0,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build();
        manager.notify(NOTIFICATION_ID, notification);
    }
}
