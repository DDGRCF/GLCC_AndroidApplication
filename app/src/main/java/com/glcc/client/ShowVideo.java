package com.glcc.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import net.sf.json.JSONObject;

import java.util.Objects;

import okhttp3.Response;

public class ShowVideo extends AppCompatActivity {
    private TXCloudVideoView mCloudView;
    private V2TXLivePlayer mLivePlayer;
    private ImageView mImageLoading;
    private ImageButton mBtnVideoPlay;
    private ImageButton mBtnVideoWide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);
        mCloudView = findViewById(R.id.video_view);
        mImageLoading = findViewById(R.id.video_loading);
        mBtnVideoPlay = findViewById(R.id.video_play_btn);
        mBtnVideoWide = findViewById(R.id.video_play_wide);
    }

    private void initPlayView() {
        mCloudView.setLogMargin(12, 12, 110, 60);
        mCloudView.showLog(false);
        mLivePlayer = new V2TXLivePlayerImpl(ShowVideo.this);
    }

    private void initPlayButton() {
        OnClick onclick = new OnClick();
        mBtnVideoPlay.setOnClickListener(onclick);
    }


    protected class OnClick extends MUtils.NoShakeListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onSingleClick(@NonNull View view) {
//            switch (view.getId()) {
//                case R.id.video_play_btn: {
//
//                    break;
//                }
//            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mLivePlayer.resumeLive();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mLivePlayer.pause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mLivePlayer.stopPlay(true);
        mCloudView.onDestroy();
    }
}