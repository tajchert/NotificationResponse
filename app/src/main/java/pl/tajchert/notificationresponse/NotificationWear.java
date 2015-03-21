package pl.tajchert.notificationresponse;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Bundle;

import java.util.ArrayList;

public class NotificationWear {
    public PendingIntent pendingIntent;
    public ArrayList<android.support.v4.app.RemoteInput> remoteInputs = new ArrayList<>();
    public ArrayList<Notification> pages = new ArrayList<>();
    public Bundle bundle;
    public String tag;
}
