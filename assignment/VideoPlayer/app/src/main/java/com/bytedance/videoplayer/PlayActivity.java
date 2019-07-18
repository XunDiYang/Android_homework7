package com.bytedance.videoplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.videoplayer.player.VideoPlayerIJK;
import com.bytedance.videoplayer.player.VideoPlayerListener;

import java.util.Arrays;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayActivity extends AppCompatActivity {

    private VideoPlayerIJK ijkPlayer;
    private MediaPlayer player;
    private SurfaceHolder holder;
    private SeekBar seekBar;
    private SeekBarThread seekBarThread;
    static boolean isPlay = true;
    private static final String TAG = "注意： ";
    private Uri mSelectedVideo;
    private static final int PICK_VIDEO = 2;
    private static final int GRANT_PERMISSION = 3;

    private String[] mPermissionsArrays = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};
    private final static int REQUEST_PERMISSION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //检查权限
        if (!checkPermissionAllGranted(mPermissionsArrays)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(mPermissionsArrays, REQUEST_PERMISSION);
            } else {
                // TODO
            }
        } else {
            Toast.makeText(PlayActivity.this, "已经获取所有所需权限", Toast.LENGTH_SHORT).show();
        }

        int currentOrientation = getResources().getConfiguration().orientation;
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.avtivity_play_landscape);
        }
        else if(currentOrientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.avtivity_play_protrait);
        }
        //播放视频
        mSelectedVideo = getIntent().getData();

        ijkPlayer = findViewById(R.id.ijkPlayer);

        try{
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        }catch (Exception e){
            this.finish();
        }
        ijkPlayer.setListener(new VideoPlayerListener());

        if(mSelectedVideo == null){
            ijkPlayer.setVideoResource(R.raw.bytedance);
        }else{
            ijkPlayer.setVideoPath(ResourceUtils.getRealPath(PlayActivity.this, mSelectedVideo));
        }

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlay){
                    ijkPlayer.pause();
                    isPlay = false;
                }
                else{
                    ijkPlayer.start();
                    isPlay = true;
                }
            }
        });

        //进度条
        seekBar = findViewById(R.id.seekBar);
        seekBar.setEnabled(true);
        final TextView tv = findViewById(R.id.tv);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO ex1-2: 这里应该调用哪个函数呢
                seekBar.setMax((int) ijkPlayer.getDuration());
                ijkPlayer.seekTo(progress);
                tv.setText("time is " + Integer.toString(progress / 1000) + " seconds");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private boolean requestReadExternalStoragePermission(String explanation) {
        if (ActivityCompat.checkSelfPermission(PlayActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(PlayActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "You should grant external storage permission to continue " + explanation, Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(PlayActivity.this, new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, GRANT_PERMISSION);
            }
            return false;
        } else {
            return true;
        }
    }

    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO);
        mSelectedVideo = intent.getData();
//        Log.d(TAG,"video的uri："+String.valueOf(mSelectedVideo));
    }

    @Override
    protected void onStart(){
        super.onStart();
        seekBarThread = new SeekBarThread("name" , ijkPlayer);
        seekBarThread.start();
    }

    @Override
    protected  void onStop(){
        super.onStop();
//        seekBarThread.interrupt();
        super.onStop();
        if (ijkPlayer.isPlaying()) {
            ijkPlayer.stop();
        }
        Log.d(TAG, "onStop() called");
        if (seekBarThread != null) {
            Log.d(TAG, "interrupt!");
            seekBarThread.interrupt();
        }
        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
    }

    private boolean checkPermissionAllGranted(String[] permissions) {
        // 6.0以下不需要
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            Toast.makeText(this, "已经授权" + Arrays.toString(permissions), Toast.LENGTH_LONG).show();
        }
    }
}
