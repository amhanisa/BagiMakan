package com.amhanisa.bagimakan;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static com.amhanisa.bagimakan.App.CHANNEL_1_ID;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String title;
    String message;
    String click_action;

    String MAKANAN_KEY;
    String MAKANAN_USER_ID;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);



        Log.e("DEF", "Message data payload: " + remoteMessage.getData());

        if (remoteMessage.getData().size() > 0) {
            Log.e("ASD", "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
            title = data.get("title");
            message = data.get("body");
            click_action = data.get("click_action");

            MAKANAN_KEY = data.get("makananKey");
            MAKANAN_USER_ID = data.get("userId");
        }

        Intent intent = new Intent(click_action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("key", MAKANAN_KEY);
        intent.putExtra("userId", MAKANAN_USER_ID);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_1_ID);
        notificationBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
