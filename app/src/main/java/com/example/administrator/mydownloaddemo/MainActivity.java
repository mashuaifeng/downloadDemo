package com.example.administrator.mydownloaddemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder = (DownloadService.DownloadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startDownloadBtn = (Button) findViewById(R.id.start_download);
        Button pauseDownloadBtn = (Button) findViewById(R.id.pause_download);
        Button cancelDownloadBtn = (Button) findViewById(R.id.cancel_download);
        startDownloadBtn.setOnClickListener(this);
        pauseDownloadBtn.setOnClickListener(this);
        cancelDownloadBtn.setOnClickListener(this);
        Intent intent = new Intent(this,DownloadService.class);
        startService(intent);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       // super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "拒绝权限将无法使用下载服务", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onClick(View view) {
        if (downloadBinder == null) return;
        switch (view.getId()){
            case R.id.start_download:
                downloadBinder.startDownload("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1491844302770&di=78a3d3cfc77b517f3249934830f2f2d2&imgtype=0&src=http%3A%2F%2Fbbs.szcw.cn%2Fdata%2Fattachment%2Fforum%2Fdvbbs%2F2007-9%2F200792621151011628.jpg");
                break;
            case R.id.pause_download:
                downloadBinder.pauseDownload();
                break;
            case R.id.cancel_download:
                downloadBinder.cancelDownload();
                break;
        }
    }
}
