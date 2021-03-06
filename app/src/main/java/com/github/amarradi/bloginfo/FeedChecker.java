package com.github.amarradi.bloginfo;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;

public class FeedChecker implements Runnable {

    private static final String LAST_FEED_CONTENT_STORAGE_KEY = "LAST_FEED_CONTENT_STORAGE_KEY";


    private final Context context;
    private final boolean showToast;

    public FeedChecker(Context context, boolean showToast) {


        this.context = context;
        this.showToast = showToast;
    }

    public void check() {
        new Thread(this).start();
    }

    @Override
    public void run() {

        String currentFeedContent = readFeedContent();
        if (currentFeedContent != null) {

            String lastFeedContent = getLastFeedContent();
            if (!currentFeedContent.equals(lastFeedContent)) {
                notifyUser();
            } else if (this.showToast) {
                Handler h = new Handler(context.getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {

                        Toast toast = Toast.makeText(context, R.string.noUpdate, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
            setLastFeedContent(currentFeedContent);
        }
    }

    private String readFeedContent() {
        InputStream in = null;
        try {
            in = new URL(MainActivity.FEED_URL).openStream();
            return IOUtils.toString(in, "utf-8").trim();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }
        return null;
    }

    private void notifyUser() {
        String textTitle = this.context.getString(R.string.notificationTitle);
        String textContent = this.context.getString(R.string.notificationText);

        NotificationManager notificationManager = (NotificationManager) this.context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEB_URL));
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, browserIntent, 0);

        @SuppressLint("ResourceAsColor")
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.timmy_turtle_logo_transparent)
                .setContentTitle(textTitle)
                .setContentText(textContent)
              //  .setDefaults(DEFAULT_LIGHTS)
                .setVibrate(new long[]{250,250,250})
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.timmy_turtle_logo_transparent))
                .setColorized(true)
                .setColor(ContextCompat.getColor(this.context, R.color.colorPrimaryDark))
                .setLights(R.color.colorPrimaryLight, 1000, 1000)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(0, mBuilder.build());
        }
    }

    private String getLastFeedContent() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        return preferences.getString(LAST_FEED_CONTENT_STORAGE_KEY, "").trim();
    }

    private void setLastFeedContent(String feedContent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        preferences.edit().putString(LAST_FEED_CONTENT_STORAGE_KEY, feedContent).apply();
    }
}
