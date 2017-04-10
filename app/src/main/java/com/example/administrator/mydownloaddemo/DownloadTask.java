package com.example.administrator.mydownloaddemo;

import android.content.pm.ProviderInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/4/9.
 */

public class DownloadTask extends AsyncTask<String,Integer,Integer> {

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;
    private DownloadListener listener;
    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;

    public DownloadTask(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        File file = null;
        long downloadedLength = 0;//记录已下载的长度
        String downloadUrl = strings[0];
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        String directory2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        Log.e("directoryPath",directory);
        Log.e("directoryAbsolutePath",directory2);
        file = new File(directory+fileName);
        if (file.exists()){
            downloadedLength = file.length();
        }
        try {
            long contentLength = getContentLenght(downloadUrl);
            if (contentLength == 0){
                return TYPE_FAILED;
            }else if (contentLength == downloadedLength){
                return TYPE_SUCCESS;
            }
            OkHttpClient okHttpClient = new OkHttpClient();
           Request request = new Request.Builder().addHeader("RANGE","bytes="+downloadedLength+"-").url(downloadUrl).build();
            Response response = okHttpClient.newCall(request).execute();
            if (response != null){
                inputStream = response.body().byteStream();
                randomAccessFile = new RandomAccessFile(file,"rw");
                randomAccessFile.seek(downloadedLength);
                byte[] buf = new byte[1024];
                int total = 0;
                int lenghth = 0;
                while ((lenghth = inputStream.read(buf)) != -1){
                    if (isCanceled) return TYPE_CANCELED;
                    else if (isPaused) return TYPE_PAUSED;
                    else{
                        total += lenghth;
                        randomAccessFile.write(buf,0,lenghth);
                        //计算已下载的百分比
                        int progress = (int)((total+downloadedLength)*100/contentLength);
                        publishProgress(progress);
                    }
                }
                response.close();
                return TYPE_SUCCESS;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (inputStream != null) inputStream.close();
                if (randomAccessFile != null) randomAccessFile.close();
                if (isCanceled && file != null)file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress){
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
       switch (integer){
           case TYPE_SUCCESS:
               listener.onSuccess();
               break;
           case TYPE_FAILED:
               listener.onFailed();
               break;
           case TYPE_CANCELED:
               listener.onCanceled();
               break;
           case TYPE_PAUSED:
               listener.onPaused();
               break;
       }
    }
    public void pauseDownload(){
        isPaused = true;
    }
    public void cancelDownload(){
        isCanceled = true;
    }

    private long getContentLenght(String downloadUrl) throws IOException{
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response != null && response.isSuccessful()){
            long contentLength = response.body().contentLength();
            response.close();
            return  contentLength;
        }
        return 0;

    }
}
