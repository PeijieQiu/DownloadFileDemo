package com.example.ricardochiu.servicebestpractice;

import android.app.*;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {

    private static final String channelID = "channel_1";
    private static final String channelName = "channel_name_1";
    private DownloadTask downloadTask;
    private String downloadUrl;
    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            if (Build.VERSION.SDK_INT >= 26) {
            getNotificationManager().notify(1, getNotification("Downloading...", progress));}
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            stopForeground(true);
            if (Build.VERSION.SDK_INT >= 26) {
            getNotificationManager().notify(1, getNotification("Download Success", -1));}
            Toast.makeText(DownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            if (Build.VERSION.SDK_INT >= 26) {
            getNotificationManager().notify(1, getNotification("Download Failed", -1));}
            Toast.makeText(DownloadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(DownloadService.this, "Paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancelled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "Cancelled", Toast.LENGTH_SHORT).show();
        }
    };

    public DownloadService() {

    }

    private DownloadBinder mBinder = new DownloadBinder();

    class DownloadBinder extends Binder {
        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                if (Build.VERSION.SDK_INT >= 26) {
                    startForeground(1, getNotification("Downloading...", 0));
                }
                Toast.makeText(DownloadService.this, "Downloading...", Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.cancelDownload();
            } else {
                if (downloadUrl != null) {
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if (file.exists()) {
                        boolean delete = file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        Log.d("DownloadService", "getNotification Executed");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivities(this, 0, new Intent[]{intent}, 0);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            getNotificationManager().createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(this, channelID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setAutoCancel(false)
                    .setContentIntent(pi);
            if (progress >= 0) {
                builder.setContentText(progress + "%");
                builder.setProgress(100, progress, false);
            }
            return builder.build();
        } else {
            return null;
        }
    }
}
