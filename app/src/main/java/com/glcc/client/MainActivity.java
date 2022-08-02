package com.glcc.client;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageViewLog;
    private TextView mTxtViewAppTitle;
    private TextView mTxtViewAppDescribe;
    private Button mTxtBtnSkip;
    private Animation mAnimationLog;
    private Animation mAnimationTitleDesc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide Bar
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.plane_theme));
        setContentView(R.layout.activity_main);
        // set values
        mTxtViewAppTitle = findViewById(R.id.txt_view_app_title);
        mTxtViewAppDescribe = findViewById(R.id.txt_view_app_describe);
        mImageViewLog = findViewById(R.id.imageview_log);
        mTxtBtnSkip = findViewById(R.id.txt_btn_skip);
        mAnimationLog = AnimationUtils.loadAnimation(this, R.anim.animation_log);
        mAnimationTitleDesc = AnimationUtils.loadAnimation(this, R.anim.animation_titledesc);
        // Text AntiAlias
        InitSplash();

    }

    @Override
    protected void onStop() {
        super.onStop();
        finishAfterTransition();
    }

    private void mTransitionAnimation() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        Pair[] pairs = new Pair[3];
        pairs[0] = new Pair<>(mImageViewLog, getResources().getString(R.string.logo_image_transition_name));
        pairs[1] = new Pair<>(mTxtViewAppTitle, getResources().getString(R.string.logo_text_transition_name));
        pairs[2] = new Pair<>(mTxtViewAppDescribe, getResources().getString(R.string.desc_text_transition_name));
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, pairs);
        startActivity(intent, options.toBundle());
    }

    private void InitSplash() {
        mTxtBtnSkip.setClickable(false);
        mTxtBtnSkip.setOnClickListener(new MUtils.NoShakeListener() {
            @Override
            protected void onSingleClick(View v) {
                mTransitionAnimation();
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTransitionAnimation();
            }
        }, Constants.SPLASH_SCREEN);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTxtBtnSkip.setVisibility(View.VISIBLE);
                mTxtBtnSkip.setClickable(true);
            }
        }, Constants.DISSPLASH_SCREEN);
        // Action
        mTxtViewAppDescribe.getPaint().setAntiAlias(true);
        mTxtViewAppTitle.getPaint().setAntiAlias(true);
        mImageViewLog.setAnimation(mAnimationLog);
        mTxtViewAppTitle.setAnimation(mAnimationTitleDesc);
        mTxtViewAppDescribe.setAnimation(mAnimationTitleDesc);
    }
}


