package com.patrick.refundly.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.patrick.refundly.Controller;
import com.patrick.refundly.R;
import com.patrick.refundly.view.FragmentContainer;
import com.patrick.refundly.view.MapFragmentCollector;
import com.patrick.refundly.view.NotificationReceived;

import org.json.JSONException;
import org.json.JSONObject;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    private JSONObject notification;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        System.out.println("BUNDLE"+data);
        String msg = data.getString("message");
        String latitude = data.getString("latitude");
        String longtitude = data.getString("longtitude");
        String bagCount = data.getString("bagcount");
        String posterComment = data.getString("postercomment");
        String collectionId = data.getString("collectionid");
        String distance = data.getString("distance");


        System.out.println("Received data:\n");
        System.out.println(msg);
        System.out.println(latitude);
        System.out.println(longtitude);
        System.out.println(bagCount);
        System.out.println(posterComment);
        System.out.println(distance);

        Controller.controller.getNotification().setMessage(msg);
        Controller.controller.getNotification().setPostercomment(posterComment);
        Controller.controller.getNotification().setLatitude(Double.parseDouble(latitude));
        Controller.controller.getNotification().setLongtitude(Double.parseDouble(longtitude));
        Controller.controller.getNotification().setBagcount(Integer.parseInt(bagCount));
        Controller.controller.getNotification().setCollectionId(Integer.parseInt(collectionId));
        Controller.controller.getNotification().setDistance(Integer.parseInt(distance));

        sendNotification(msg);

    }


    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.collection)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        mBuilder.setAutoCancel(true);


        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, NotificationReceived.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(NotificationReceived.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());


    }
}
