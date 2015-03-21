package pl.tajchert.notificationresponse;


import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;


public class NotificationReceiver extends NotificationListenerService {
    private static final String TAG = "NotificationReceiver";



    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        android.os.Debug.waitForDebugger();

        ArrayList<NotificationWear> wearNotifications = new ArrayList<>();



        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender(sbn.getNotification());
        if(wearableExtender != null) {
            List<NotificationCompat.Action> actions = wearableExtender.getActions();
            NotificationWear notificationWear = new NotificationWear();
            for(NotificationCompat.Action act : actions) {
                notificationWear.remoteInputs.addAll(Arrays.asList(act.getRemoteInputs()));
            }

            List<Notification> pages = wearableExtender.getPages();
            notificationWear.pages.addAll(pages);
            notificationWear.pendingIntent = sbn.getNotification().contentIntent;

            wearNotifications.add(notificationWear);
            EventBus.getDefault().post(notificationWear);
        }

        Log.d(TAG, "onNotificationPosted " + wearNotifications);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.d(TAG, "onNotificationRemoved ");
    }
}
