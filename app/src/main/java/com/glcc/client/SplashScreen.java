package com.glcc.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ClickUtils;
import com.blankj.utilcode.util.PermissionUtils;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    private ImageView mImageViewLog;
    private TextView mTxtViewAppTitle;
    private TextView mTxtViewAppDescribe;
    private Button mTxtBtnSkip;
    private Animation mAnimationLog;
    private Animation mAnimationTitleDesc;
    private Handler handler;
    private Timer timer;
    private int time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.setMyStatusBar(SplashScreen.this);
        setContentView(R.layout.activity_main);
        // set values
        mTxtViewAppTitle = findViewById(R.id.txt_view_app_title);
        mTxtViewAppDescribe = findViewById(R.id.txt_view_app_describe);
        mImageViewLog = findViewById(R.id.imageview_log);
        mTxtBtnSkip = findViewById(R.id.txt_btn_skip);
        mAnimationLog = AnimationUtils.loadAnimation(this, R.anim.animation_log);
        mAnimationTitleDesc = AnimationUtils.loadAnimation(this, R.anim.animation_titledesc);
        InitSplash();

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void mTransitionAnimation() {
        Intent intent = new Intent(SplashScreen.this, Login.class);
        Pair[] pairs = new Pair[3];
        pairs[0] = new Pair<>(mImageViewLog, getResources().getString(R.string.logo_image_transition_name));
        pairs[1] = new Pair<>(mTxtViewAppTitle, getResources().getString(R.string.logo_text_transition_name));
        pairs[2] = new Pair<>(mTxtViewAppDescribe, getResources().getString(R.string.desc_text_transition_name));
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this, pairs);
        startActivity(intent, options.toBundle());
    }

    private void InitSplash() {
        time = Constants.SPLASH_SCREEN;
        mTxtBtnSkip.setClickable(false);
        mTxtBtnSkip.setVisibility(View.INVISIBLE);
        Handler handler = new Handler(new Handler.Callback() {
            @SuppressLint("SetTextI18n")
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 0:
                        mTxtBtnSkip.setText("Skip: " + (int)(time / 1000));
                        break;
                    case 1:
                        timer.cancel();
                        mTransitionAnimation();
                        break;
                }
                return false;
            }
        });
        mTxtBtnSkip.setOnClickListener(new ClickUtils.OnDebouncingClickListener() {
            @Override
            public void onDebouncingClick(View v) {
                handler.sendEmptyMessage(1);
            }
        });

        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                time = Math.max(time - 1000, 0);
                if (time < Constants.SPLASH_SCREEN - Constants.DISSPLASH_SCREEN) {
                    if (mTxtBtnSkip.getVisibility() != View.VISIBLE)
                        handler.post(()->{
                            mTxtBtnSkip.setClickable(true);
                            mTxtBtnSkip.setVisibility(View.VISIBLE);
                        });
                }
                handler.sendEmptyMessage(time == 0 ? 1 : 0);
            }
        }, 1000, 1000);

        // Action
        mTxtViewAppDescribe.getPaint().setAntiAlias(true);
        mTxtViewAppTitle.getPaint().setAntiAlias(true);
        mImageViewLog.setAnimation(mAnimationLog);
        mTxtViewAppTitle.setAnimation(mAnimationTitleDesc);
        mTxtViewAppDescribe.setAnimation(mAnimationTitleDesc);
        BarUtils.addMarginTopEqualStatusBarHeight(mTxtBtnSkip);
    }
}