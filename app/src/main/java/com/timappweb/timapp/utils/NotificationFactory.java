package com.timappweb.timapp.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.config.IntentsUtils;

import java.util.Map;

/**
 * Created by stephane on 4/2/2016.
 */
public class NotificationFactory {

    private static final String TAG = "NotificationFactory";
    static int mId = 1;

    public static int build(Context context, int icon, String title, String text, Intent resultIntent){

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setContentText(text);
// The stack simpleMessage object will contain an artificial back stack for the
// started UserActivity.
// This ensures that navigating backward from the UserActivity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(EventActivity.class);
// Adds the Intent that starts the UserActivity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId++, mBuilder.build());
        return mId;
    }



    public static int invite(Context context, Map<String, String> data) {
            //Bundle notification = data.get("notification");
            //if (notification == null){
        //    Log.e(TAG, "Received a null notification");
        //       return -1;
        //  }
            try {
                Log.v(TAG, "Received notification " + data);
                String body = data.get("body");
                String title = data.get("title");
                String icon = data.get("icon");
                long placeId = Long.valueOf(data.get("place_id")); // TODO cst
                Intent resultIntent = IntentsUtils.buildIntentViewEvent(context, placeId);
                int categoryId = R.drawable.category_unknown; // TODO set proper category
                return NotificationFactory.build(context, categoryId, title, body, resultIntent);
            }
            catch (Exception ex){
                Log.e(TAG, "Error while parsing the notification: " + ex.getMessage());
                return -1;
            }
    }
}
