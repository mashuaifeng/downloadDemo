package com.example.administrator.mydownloaddemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {

    private DownloadTask downloadTask;
    private String downloadUrl;
    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1,getNotification("downloading...",progress));

        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            stopForeground(true);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1,getNotification("download success",-1));
            Toast.makeText(DownloadService.this,"download success",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1,getNotification("download fail",-1));
            Toast.makeText(DownloadService.this,"download fail",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1,getNotification("download paused",-1));
            Toast.makeText(DownloadService.this,"download paused",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1,getNotification("download canceled",-1));
            Toast.makeText(DownloadService.this,"download canceled",Toast.LENGTH_SHORT).show();
        }
    };
    private Notification getNotification(String title,int progress){
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        if (progress >0){
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,true);
        }
        return builder.build();

    }

    public DownloadService() {
    }

    private DownloadBinder mBinder = new DownloadBinder();
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    class DownloadBinder extends Binder{

        public void startDownload(String url){
            if (downloadTask == null){
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("downloading...",0));
                Toast.makeText(DownloadService.this, "downloading...", Toast.LENGTH_SHORT).show();
            }
        }
        public void pauseDownload(){
            if (downloadTask != null){
                downloadTask.pauseDownload();
            }
        }
        public void cancelDownload(){
            if (downloadTask != null){
                downloadTask.cancelDownload();
            }else{
                if (downloadUrl != null){
                    //取消下载需要将文件删除，并将通知关闭
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory+fileName);
                    if (file.exists())file.delete();
                    ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "canceled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
