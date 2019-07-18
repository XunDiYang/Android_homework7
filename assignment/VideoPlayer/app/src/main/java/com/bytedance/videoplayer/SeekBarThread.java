package com.bytedance.videoplayer;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.bytedance.videoplayer.player.VideoPlayerIJK;

public class SeekBarThread extends HandlerThread implements Handler.Callback {

    public  static  final int MSG_SEEKBAR = 100;
    private Handler mHandler;
    private VideoPlayerIJK ijkPlayer;

    public SeekBarThread(String name, VideoPlayerIJK mVideoPlayerIJK){
        super(name);
        this.ijkPlayer = mVideoPlayerIJK;
    }

    public SeekBarThread(String name){
        super(name);
    }

    @Override
    public void run(){
        super.run();
        if(!(isInterrupted())){
            return;
        }
        else{
            mHandler.sendEmptyMessage(MSG_SEEKBAR);
        }
    }

    @Override
    protected  void onLooperPrepared(){
        mHandler = new Handler(getLooper(),this);
        mHandler.sendEmptyMessage(MSG_SEEKBAR);
    }

    @Override
    public boolean handleMessage(Message msg){
        switch ((msg.what)){
            case MSG_SEEKBAR:
                ijkPlayer.postInvalidate();
                mHandler.sendEmptyMessageDelayed(MSG_SEEKBAR,1000);
                break;
        }
        return true;
    }

}
