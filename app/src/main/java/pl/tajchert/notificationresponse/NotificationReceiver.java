package pl.tajchert.notificationresponse;


import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Primosz on 2015-03-21.
 */
public class NotificationReceiver extends NotificationListenerService {
    private static final String TAG = "NotificationReceiver";



    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        android.os.Debug.waitForDebugger();
        Bundle bundle = sbn.getNotification().extras;
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);

            if(key != null && value != null){
                Log.d(TAG, String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
            }
            if("android.wearable.EXTENSIONS".equals(key)){
                Bundle wearBundle = ((Bundle) value);
                for (String keyInner : wearBundle.keySet()) {
                    Object valueInner = wearBundle.get(keyInner);

                    if(keyInner != null && valueInner != null){
                        if("actions".equals(keyInner) && valueInner instanceof ArrayList){
                            ArrayList<Notification.Action> actions = new ArrayList<>();
                            actions.addAll((ArrayList) valueInner);
                            for(Notification.Action act : actions){
                                if(act.getRemoteInputs() != null){
                                    new NotificationWear().remoteInputs.addAll(Arrays.asList(act.getRemoteInputs()));
                                }
                            }
                        }
                    }
                }
            }
        }
        Log.d(TAG, "onNotificationPosted ");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.d(TAG, "onNotificationRemoved ");
    }
}
