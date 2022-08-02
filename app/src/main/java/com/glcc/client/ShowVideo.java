package com.glcc.client;

import static android.content.ContentValues.TAG;

import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_OK;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.rtmp.ui.TXCloudVideoView;

public class ShowVideo extends AppCompatActivity {
    private String VideoURL;
    private Handler handler;
    private MLivePlayer mLivePlayer;
    private Bundle login_bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);
        handler = new Handler();
        mLivePlayer = new MLivePlayer();
        login_bundle = getIntent().getBundleExtra("bundle");
//        GLCCClient.doCommonPost();
    }

    protected class OnClick extends MUtils.NoShakeListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onSingleClick(@NonNull View view) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLivePlayer.destroy();
    }

    @Override
    public void onBackPressed() {
        mLivePlayer.stopPaly();
    }


    class MLivePlayer {
        private TXCloudVideoView mVideoView;
        private V2TXLivePlayer mLivePlayer;
        private ImageView mImageLoading;
        private ImageButton mBtnVideoPlay;
        private ImageButton mBtnVideoWide;
        private ImageView mImageVideoLogo;
        private RelativeLayout mVideoLayoutRoot;
        private V2TXLiveDef.V2TXLiveFillMode mRenderMode =  V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFit; // 长边填充
        private V2TXLiveDef.V2TXLiveRotation mRenderRotation = V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0; // 旋转角度
        private boolean mIsPlaying = false;

        MLivePlayer(){
            mVideoView = findViewById(R.id.video_view);
            mImageLoading = findViewById(R.id.video_loading);
            mBtnVideoPlay = findViewById(R.id.video_play_btn);
            mBtnVideoWide = findViewById(R.id.video_play_wide);
            mImageVideoLogo = findViewById(R.id.video_play_logo);
            mVideoLayoutRoot = findViewById(R.id.video_layout_root);
        }

        private void initialize() {
            initPlayView();
            initPlayButton();
            startPlay();
        }

        protected void initPlayView() {
            mVideoView.setLogMargin(12, 12, 110, 60);
            mVideoView.showLog(false);
            mLivePlayer = new V2TXLivePlayerImpl(ShowVideo.this);
        }

        protected void initPlayButton() {
            OnClick onclick = new OnClick();
            mBtnVideoPlay.setOnClickListener(onclick);
        }

        protected void startPlay() {
            int code = checkPlayURL(VideoURL);
            if (code != Constants.PLAY_STATUS_SUCCESS) {
                Log.d("ShowVideo", "Url is invalidate");
            } else {
                mLivePlayer.setRenderView(mVideoView);
                mLivePlayer.setObserver(new MPlayerObserver());
                mLivePlayer.setRenderRotation(mRenderRotation);
                mLivePlayer.setRenderFillMode(mRenderMode);
                code = mLivePlayer.startPlay(VideoURL);
                mIsPlaying = code == V2TXLIVE_OK; // TODO: Other Conditions
                Log.d("video render", "timetrack start play");
            }
            onPlayStart(code);
        }

        protected void onPlayStart(int code) {
            if (code != V2TXLIVE_OK) {
                mBtnVideoPlay.setBackgroundResource(R.drawable.play_button);
                mVideoLayoutRoot.setBackgroundResource(R.drawable.bg_videoplayer_content);
                mImageVideoLogo.setVisibility(View.VISIBLE);
                // TODO: Add Logo Info
            } else {
                mBtnVideoPlay.setBackgroundResource(R.drawable.stop_button);
                mVideoLayoutRoot.setBackgroundColor(getResources().getColor(R.color.videoplayer_content_stop_color));
                mImageVideoLogo.setVisibility(View.GONE);
            }
        }

        protected void onPlayStop() {
            mBtnVideoPlay.setBackgroundResource(R.drawable.play_button);
            mVideoLayoutRoot.setBackgroundResource(R.drawable.bg_videoplayer_content);
            mImageVideoLogo.setVisibility(View.VISIBLE);
            stopLoadingAnimation();
        }

        protected void stopPaly() {
            if (!mIsPlaying) {
                return;
            }
            if (mLivePlayer != null) {
                mLivePlayer.setObserver(null);
                mLivePlayer.stopPlay();
            }
            onPlayStop();
        }

        private int checkPlayURL(String video_url) {
            // TODO: checkPlayURL
            return Constants.PLAY_STATUS_SUCCESS;
        }

        private void startLoadingAnimation() {
            if (mImageLoading != null) {
                mImageLoading.setVisibility(View.VISIBLE);
                ((AnimationDrawable) mImageLoading.getDrawable()).start();
            }
        }

        private void stopLoadingAnimation() {
            if (mImageLoading != null) {
                mImageLoading.setVisibility(View.GONE);
                ((AnimationDrawable) mImageLoading.getDrawable()).stop();
            }
        }

        protected void destroy() {
            if (mLivePlayer != null) {
                mLivePlayer.stopPlay();
                mLivePlayer = null;
            }
            if (mVideoView != null) {
                mVideoView.onDestroy();
                mVideoView = null;
            }
        }

        protected class MPlayerObserver extends V2TXLivePlayerObserver {
            @Override
            public void onWarning(V2TXLivePlayer player, int code, String msg, Bundle extrainfo) {
                Log.w(TAG, "[Player] onWarning: player-" + player + " code-" + code + " msg-" + msg + " info-" + extrainfo);
            }

            @Override
            public void onError(V2TXLivePlayer player, int code, String msg, Bundle extrainfo) {
                Log.e(TAG, "[Player] onError: player-" + player + " code-" + code + " msg-" + msg + " info-" + extrainfo);
            }

            @Override
            public void onSnapshotComplete(V2TXLivePlayer v2TXLivePlayer, Bitmap bitmap) {
            }

            @Override
            public void onStatisticsUpdate(V2TXLivePlayer v2TXLivePlayer, V2TXLiveDef.V2TXLivePlayerStatistics statistics) {

            }

            @Override
            public void onVideoPlaying(V2TXLivePlayer v2TXLivePlayer, boolean firstPlay, Bundle extraInfo) {
                if (firstPlay) {
                    stopLoadingAnimation();
                }
            }

            @Override
            public void onVideoLoading(V2TXLivePlayer v2TXLivePlayer, Bundle extrainfo) {
                startLoadingAnimation();
            }


        }

    }

}
