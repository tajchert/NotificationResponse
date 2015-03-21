package pl.tajchert.notificationresponse;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }
    @OnClick(R.id.buttonRandomNotif) void sendRandomNotification() {
        Intent intent =  new Intent(MainActivity.this, MainActivity.class);
        String[] replyChoices ={"Yes", "No"};

        RemoteInput remoteInput = new RemoteInput.Builder("extra_voice_reply")
                .setLabel("Label")
                .setChoices(replyChoices)
                .build();

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.mipmap.ic_launcher,
                        "Get Input", PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                        .addRemoteInput(remoteInput)
                        .build();


        intent.putExtra("our_passed_id", "12345");
        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .addAction(action)
                        .setAutoCancel(true)
                        .setContentTitle("Random Notification")
                        .setContentText("Appears!")
                        .setContentIntent(contentIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);

        notificationManager.notify(888, notificationBuilder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Settings.Secure.getString(this.getContentResolver(),"enabled_notification_listeners") != null) {
            if (Settings.Secure.getString(this.getContentResolver(),"enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
                //service is enabled do nothing
            } else {
                //service is not enabled try to enabled
                getApplicationContext().startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } else {
            Log.d(TAG, "onResume no Google Play Services");
        }
    }
}
