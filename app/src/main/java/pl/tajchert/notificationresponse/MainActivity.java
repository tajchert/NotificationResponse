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
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";
    private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private NotificationWear latestNotification;  //Latest notification with RemoteInput that we can reply to
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }
    
    public void onEvent(NotificationWear notificationWear){
        //New notification with RemoteInput incoming
        latestNotification = notificationWear;
        Toast.makeText(MainActivity.this, "New cached notification.", Toast.LENGTH_SHORT).show();
    }
    
    @OnClick(R.id.buttonReply) void replyToLastNotification(){
        if(latestNotification == null || latestNotification.remoteInputs == null || latestNotification.remoteInputs.size() == 0) {
            Toast.makeText(MainActivity.this, "No notification :(", Toast.LENGTH_LONG).show();
            return;
        }
        RemoteInput remoteInput = latestNotification.remoteInputs.get(0);
        String resultKey = remoteInput.getResultKey();
        String label = remoteInput.getLabel().toString();
        Boolean canFreeForm = remoteInput.getAllowFreeFormInput();
        Bundle bundle = remoteInput.getExtras();
        if(remoteInput.getChoices() != null && remoteInput.getChoices().length > 0) {
            String[] possibleChoices = new String[remoteInput.getChoices().length];
            for(int i = 0; i < remoteInput.getChoices().length; i++){
                possibleChoices[i] = remoteInput.getChoices()[i].toString();
            }
        }

        bundle.putString(resultKey, "Answer");
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(latestNotification.pendingIntent.getCreatorPackage());
        RemoteInput[] remoteInputs = new RemoteInput[1];
        remoteInputs[0] = remoteInput;
        RemoteInput.addResultsToIntent(remoteInputs,launchIntent, bundle);
        startActivity(launchIntent);
        launchIntent.putExtra(resultKey, "Answer");

        Log.d(TAG, "replyToLastNotification ");
    }
    
    
    @OnClick(R.id.buttonRandomNotif) void sendRandomNotification() {
        Intent intent =  new Intent(MainActivity.this, MainActivity.class);
        String[] replyChoices ={"Yes", "No"};

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel("Label")
                .setChoices(replyChoices)
                .build();

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.mipmap.ic_launcher,
                        "Get Input", PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                        .addRemoteInput(remoteInput)
                        .build();

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().addAction(action);


        intent.putExtra("our_passed_id", "12345");
        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .extend(wearableExtender)
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
        EventBus.getDefault().register(this);
        if(Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners") != null) {
            if (Settings.Secure.getString(this.getContentResolver(),"enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
                //service is enabled do nothing
            } else {
                //service is not enabled try to enabled
                getApplicationContext().startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } else {
            Log.d(TAG, "onResume no Google Play Services");
        }
        Toast.makeText(MainActivity.this, getMessageText(getIntent()), Toast.LENGTH_LONG).show();
    }

    private CharSequence getMessageText(Intent intent) {
        String reply = "";
        if(intent.getExtras() != null) {
            reply = intent.getExtras().getString(EXTRA_VOICE_REPLY);
        }
        Log.d(TAG, "getMessageText reply" + reply);
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_VOICE_REPLY);
        }
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
}
