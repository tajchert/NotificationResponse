package pl.tajchert.notificationresponse;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Stack;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";
    private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private Stack<NotificationWear> notificationsStack = new Stack<NotificationWear>();
    private AdapterNotifList mAdapter;

    @InjectView(R.id.recyclerView)
    RecyclerView recyclerNotifList;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        recyclerNotifList.setLayoutManager(new LinearLayoutManager(this));
        udateRecycler();
    }
    
    public void onEvent(NotificationWear notificationWear) {
        //New notification with RemoteInput incoming
        notificationsStack.push(notificationWear);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                udateRecycler();
            }
        });
    }

    private void udateRecycler() {
        mAdapter = new AdapterNotifList(Arrays.asList(notificationsStack.toArray(new NotificationWear[notificationsStack.size()])));
        recyclerNotifList.setAdapter(mAdapter);
        recyclerNotifList.setItemAnimator(new DefaultItemAnimator());
    }

    //Most interesting code here - start
    
    @OnClick(R.id.buttonReply) void replyToLastNotification(){
        //We take last notification with option to replay from stack and try to fill RemoteInput with our text and send it back
        if(notificationsStack.isEmpty()){
            Toast.makeText(MainActivity.this, "No notification :(", Toast.LENGTH_LONG).show();
            return;
        }

        NotificationWear notificationWear = notificationsStack.pop();
        if(notificationWear == null) {
            Toast.makeText(MainActivity.this, "No notification :(", Toast.LENGTH_LONG).show();
            return;
        }
        RemoteInput[] remoteInputs = new RemoteInput[notificationWear.remoteInputs.size()];

        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle localBundle = notificationWear.bundle;
        int i = 0;
        for(RemoteInput remoteIn : notificationWear.remoteInputs){
            getDetailsOfNotification(remoteIn);
            remoteInputs[i] = remoteIn;
            localBundle.putCharSequence(remoteInputs[i].getResultKey(), "Our answer");//This work, apart from Hangouts as probably they need additional parameter (notification_tag?)
            i++;
        }

        /*
        //Others that I had tried, and failed
        localBundle.putCharSequence(resultKey, "Random1 answer");
        localIntent.putExtra(resultKey, "Random2 answer");
        localIntent.putExtra("resultKey", "Random3 Answer");
        localIntent.setClipData(ClipData.newIntent("android.remoteinput.results", localIntent));*/
        RemoteInput.addResultsToIntent(remoteInputs, localIntent, localBundle);
        try {
            notificationWear.pendingIntent.send(MainActivity.this, 0, localIntent);
            //TODO find how to call it and not display Activity
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, "replyToLastNotification error: " + e.getLocalizedMessage());
        }
        udateRecycler();
    }

    //Most interesting code here - end

    private void getDetailsOfNotification(RemoteInput remoteInput) {
        //Some more details of RemoteInput... no idea what for but maybe it will be useful at some point
        String resultKey = remoteInput.getResultKey();
        String label = remoteInput.getLabel().toString();
        Boolean canFreeForm = remoteInput.getAllowFreeFormInput();
        if(remoteInput.getChoices() != null && remoteInput.getChoices().length > 0) {
            String[] possibleChoices = new String[remoteInput.getChoices().length];
            for(int i = 0; i < remoteInput.getChoices().length; i++){
                possibleChoices[i] = remoteInput.getChoices()[i].toString();
            }
        }
    }


    @OnClick(R.id.buttonRandomNotif) void sendRandomNotification() {
        //To release sample notification with WearableExtender and RemoteInput - in test purposes
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
        if(!EventBus.getDefault().isRegistered(this)){
            //As we unregister only onDestroy
            EventBus.getDefault().register(this);
        }
        //If you are debugging you need to turn it off and on again on each new app deployment
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

        //Test if our test notification works
        String textFromInput = null;
        try {
            textFromInput = getMessageText(getIntent()).toString();
        } catch (Exception e) {
            //no text from RemoteInput, carry on.
        }
        if(textFromInput != null && textFromInput.length() > 0 ){
            Toast.makeText(MainActivity.this, textFromInput, Toast.LENGTH_LONG).show();
        }
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
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
