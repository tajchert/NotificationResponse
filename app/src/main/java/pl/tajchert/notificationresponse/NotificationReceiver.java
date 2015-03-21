package pl.tajchert.notificationresponse;


import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;


public class NotificationReceiver extends NotificationListenerService {
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        android.os.Debug.waitForDebugger();

        ArrayList<NotificationWear> wearNotifications = new ArrayList<>();

        //Most interesting code here - start

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender(sbn.getNotification());
        //TODO test on "com.whatsapp", "com.facebook.orca", "com.google.android.talk", "jp.naver.line.android", "org.telegram.messenger"
        List<NotificationCompat.Action> actions = wearableExtender.getActions();
        NotificationWear notificationWear = new NotificationWear();
        for(NotificationCompat.Action act : actions) {
            notificationWear.remoteInputs.addAll(Arrays.asList(act.getRemoteInputs()));
        }
        notificationWear.bundle = sbn.getNotification().extras;
        notificationWear.tag = sbn.getTag();

        List<Notification> pages = wearableExtender.getPages();
        notificationWear.pages.addAll(pages);
        notificationWear.pendingIntent = sbn.getNotification().contentIntent;

        if(!sbn.isOngoing()) {
            //As probably we don want to add ongoing notifications
            wearNotifications.add(notificationWear);
            EventBus.getDefault().post(notificationWear);
        }
        //Most interesting code here - end
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }
}
