package com.glcc.client;

import static android.content.ContentValues.TAG;

import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_ERROR_FAILED;
import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_OK;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ClickUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.glcc.client.manager.ContourModel;
import com.glcc.client.manager.UserModel;
import com.glcc.client.manager.VideoModel;
import com.google.android.material.textfield.TextInputEditText;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.PopMenu;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.kongzue.dialogx.interfaces.OnBindView;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener;
import com.kongzue.dialogx.util.TextInfo;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.Response;

public class ShowVideo extends AppCompatActivity {
    private Handler handler;
    private MyLivePlayer mALivePlayer;
    private Bundle login_bundle;
    private Spinner mSpinnerVideoSource;
    private TextView mTxtVideoSourceRegister;
    private TextView mTxtVideoSourceDelete;
    private TextView mTxtVideoSourceDraw;
    private Toolbar mVideoShowToolBar;
    private RadioGroup mRadioGroupVideoSourceDelete;
    private UserModel userModel;
    private ArrayAdapter<String> videoSourceSpinnerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.setMyStatusBar(ShowVideo.this);
        setContentView(R.layout.activity_show_video);
        initialize();
        mVideoShowToolBar = findViewById(R.id.video_show_toolbar);
        BarUtils.addMarginTopEqualStatusBarHeight(mVideoShowToolBar);
    }

    private void initialize() {
        Bundle bundle = getIntent().getBundleExtra("bundle");
        String login_username = bundle.getString("login_username");
        userModel = UserModel.loadUserModel(login_username);
        handler = new Handler();
        initVideoSourceSpinner();
        initVideoSourceRegister();
        initVideoSourceDelete();
        initVideoSourceDraw();
        setListener();
        mALivePlayer = new MyLivePlayer();
        mALivePlayer.initialize();
    }


    private void initVideoSourceSpinner() {
        mSpinnerVideoSource = findViewById(R.id.video_source_spinner);
        mSpinnerVideoSource.setPrompt("Please Choose Video Source");

        videoSourceSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.video_source_spinner, R.id.video_source_item);
        mSpinnerVideoSource.setAdapter(videoSourceSpinnerAdapter);
        mSpinnerVideoSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mALivePlayer.stopPlay();
                mALivePlayer.startPlay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Map<String, VideoModel> videoMap = VideoModel.loadAllVideoModel(userModel.getUserName());
        for (Map.Entry<String, VideoModel> item : videoMap.entrySet()) {
            videoSourceSpinnerAdapter.add(item.getValue().getVideoName());
        }
    }


    private void initVideoSourceRegister() {
        mTxtVideoSourceRegister = findViewById(R.id.view_video_source_register);
    }

    private void initVideoSourceDelete() {
        mTxtVideoSourceDelete = findViewById(R.id.view_video_source_delete); }

    private void initVideoSourceDraw() {
        mTxtVideoSourceDraw = findViewById(R.id.view_video_draw_lattice);
    }

    private void setListener() {
        OnClick onClick = new OnClick();
        mTxtVideoSourceRegister.setOnClickListener(onClick);
        mTxtVideoSourceDelete.setOnClickListener(onClick);
        mTxtVideoSourceDraw.setOnClickListener(onClick);
    }

    protected class OnClick extends ClickUtils.OnDebouncingClickListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onDebouncingClick(@NonNull View view) {
            switch (view.getId()) {
                case R.id.view_video_source_register: {
                    BottomDialog.show("REGISTER VIDEO SOURCE", "Please enter your video information: ",
                                    new OnBindView<BottomDialog>(R.layout.video_source_register_dialog) {
                            @Override
                            public void onBind(BottomDialog dialog, View v) {
                            }
                        })
                        .setCancelButton("Cancel", new OnDialogButtonClickListener<BottomDialog>() {
                            @Override
                            public boolean onClick(BottomDialog baseDialog, View v) {
                                return false;
                            }
                        })
                        .setOkButton("OK", new OnDialogButtonClickListener<BottomDialog>() {
                            @Override
                            public boolean onClick(BottomDialog baseDialog, View v) {
                                RadioGroup mRadioGroupVideoSourceRegister = findViewById(R.id.url_type_radio_group);
                                TextInputEditText mEditVideoSourceRegister = findViewById(R.id.video_source_url_edit);
                                TextInputEditText mEditVideoSourceRegisterName = findViewById(R.id.video_source_name_edit);
                                String url_text = Objects.requireNonNull(mEditVideoSourceRegister.getText()).toString();
                                String name_text = Objects.requireNonNull(mEditVideoSourceRegisterName.getText()).toString();
                                if (ObjectUtils.isEmpty(name_text)) {
                                    Toast.makeText(ShowVideo.this, "Video Name Can't be empty!", Toast.LENGTH_SHORT).show();
                                    return true;
                                }
                                if (ObjectUtils.isEmpty(url_text)) {
                                    Toast.makeText(ShowVideo.this, "Video Url Can't be empty!", Toast.LENGTH_SHORT).show();
                                    return true;
                                }

                                else {
                                    int checked_id = mRadioGroupVideoSourceRegister.getCheckedRadioButtonId();
                                    RadioButton checked_btn = findViewById(checked_id);
                                    String checked_text = checked_btn.getText().toString();
                                    JSONObject register_video_info = new JSONObject();
                                    boolean use_template_url = checked_text.equals("Use Template Url");
                                    register_video_info.put("use_template_url", use_template_url);
                                    register_video_info.put("video_url", url_text);
                                    register_video_info.put("video_name", name_text);
                                    register_video_info.put("user_name", userModel.getUserName());
                                    register_video_info.put("user_password", userModel.getPassword());
                                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                                    WaitDialog.show("Please wait...");
                                    Callable<Boolean> task = new Callable<Boolean>() {
                                        @Override
                                        public Boolean call() throws Exception {
                                            Response response = GLCCClient.doCommonPost(Constants.GLCC_REGISTER_VIDEO_URL, register_video_info.toString());
                                            if (ObjectUtils.isEmpty(response)) {
                                                handler.post(()->{
                                                    Toast.makeText(ShowVideo.this, "Request Error!", Toast.LENGTH_SHORT).show();
                                                });
                                                return true;
                                            } else {
                                                if (response.code() == 200) {
                                                    String body = response.body().string();
                                                    JSONObject json = JSONObject.fromObject(body);
                                                    String video_name = json.getString("video_name");
                                                    String video_url = json.getString("video_url");
                                                    handler.post(()->{
                                                        mEditVideoSourceRegister.setText("");
                                                        mEditVideoSourceRegisterName.setText("");
                                                        Toast.makeText(ShowVideo.this, "Register Success!", Toast.LENGTH_SHORT).show();
                                                        videoSourceSpinnerAdapter.add(video_name);
                                                    });
                                                    VideoModel videoModel = new VideoModel();
                                                    videoModel.setVideoName(video_name);
                                                    videoModel.setVideoUrl(video_url);
                                                    videoModel.saveVideoModel(userModel.getUserName());
                                                    Log.d("ShowVideoThread", userModel.getUserName() + ":" + video_name + ":" + video_url);
                                                    return false;
                                                } else {
                                                    handler.post(()->{
                                                        Toast.makeText(ShowVideo.this, "Register Failed! Please your information!", Toast.LENGTH_SHORT).show();
                                                    });
                                                    return true;
                                                }
                                            }
                                        }
                                    };
                                    boolean state = false;
                                    Future<Boolean> future = executorService.submit(task);
                                    try {
                                        state = future.get();
                                    } catch (ExecutionException | InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    WaitDialog.dismiss();
                                    return state;
                                }
                            }
                        })
                        .setOnBackPressedListener(new OnBackPressedListener() {
                            @Override
                            public boolean onBackPressed() {
                                return false;
                            }
                        });
                    break;
                }
                case R.id.view_video_source_delete: {
                    BottomDialog.show("DELETE VIDEO SOURCE", "Please Enter your video information:", new OnBindView<BottomDialog>(R.layout.video_source_delete_dialog) {
                        @Override
                        public void onBind(BottomDialog dialog, View v) {
                        }
                    })
                    .setOkButton("OK", new OnDialogButtonClickListener<BottomDialog>() {
                        @Override
                        public boolean onClick(BottomDialog baseDialog, View v) {
                            TextInputEditText txtInputEdit = findViewById(R.id.video_source_delete_name_edit);
                            String video_name = Objects.requireNonNull(txtInputEdit.getText()).toString();
                            if (ObjectUtils.isEmpty(video_name)) {
                                handler.post(()->{
                                    Toast.makeText(ShowVideo.this, "Input name can't be empty", Toast.LENGTH_SHORT).show();
                                });
                            }
                            JSONObject reqJson = new JSONObject();
                            reqJson.put("user_name", userModel.getUserName());
                            reqJson.put("user_password", userModel.getPassword());
                            reqJson.put("video_name", video_name);
                            ExecutorService executorService = Executors.newSingleThreadExecutor();
                            Callable<Boolean> task = new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    WaitDialog.show("Please wait...");
                                    Response response = GLCCClient.doCommonPost(Constants.GLCC_DELETE_VIDEO_URL, reqJson.toString());
                                    WaitDialog.dismiss();
                                    if (ObjectUtils.isEmpty(response)) {
                                        handler.post(()->{
                                            Toast.makeText(ShowVideo.this, "Request Error!", Toast.LENGTH_SHORT).show();
                                        });
                                        return true;
                                    } else {
                                        if (response.code() == 200) {
                                            handler.post(()->{
                                                Toast.makeText(ShowVideo.this, "Delete Success", Toast.LENGTH_SHORT).show();
                                            });
                                            VideoModel.removeVideoModel(userModel.getUserName(), video_name);
                                            if (mSpinnerVideoSource.getSelectedItem().toString().equals(video_name)) {
                                                if (mALivePlayer.mLivePlayer.isPlaying() == 1) {
                                                    mALivePlayer.stopPlay();
                                                }
                                            }
                                            videoSourceSpinnerAdapter.remove(video_name);
                                            return false;
                                        } else {
                                            handler.post(()->{
                                                Toast.makeText(ShowVideo.this, "Please check your information", Toast.LENGTH_SHORT).show();
                                            });
                                            return true;
                                        }
                                    }
                                }
                            };
                            boolean state = false;
                            Future<Boolean> future = executorService.submit(task);
                            try {
                                state = future.get();
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            return state;
                        }
                    })
                    .setCancelButton("Cancel",new OnDialogButtonClickListener<BottomDialog>() {
                        @Override
                        public boolean onClick(BottomDialog baseDialog, View v) {
                            return false;
                        }
                    });
                   break;
                }
                case R.id.view_video_draw_lattice: {
                    if (mALivePlayer.mIsPlaying) {
                        mALivePlayer.toggleDraw();
                    } else{
                        Toast.makeText(ShowVideo.this, "Please use the function on playing", Toast.LENGTH_SHORT).show();
                    }
//                    if (mALivePlayer.mIsDrawing) {
//                        mALivePlayer.stopDraw();
//                    } else {
//                        if (mALivePlayer.mIsPlaying) {
//                            mALivePlayer.startDraw();
//                        } else {
//                            mALivePlayer.stopDraw();
//                            Toast.makeText(ShowVideo.this, "Please use the function on playing", Toast.LENGTH_SHORT).show();
//                        }
//                    }
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + view.getId());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mALivePlayer.startPlay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mALivePlayer.stopPlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mALivePlayer.destroy();
    }

    @Override
    public void onBackPressed() {
        mALivePlayer.stopPlay();
    }


    class MyLivePlayer {
        private TXCloudVideoView mVideoView;
        private V2TXLivePlayer mLivePlayer;
        private ImageView mImageLoading;
        private ImageButton mBtnVideoPlay;
        private ImageButton mBtnVideoWide;
        private ImageView mImageVideoLogo;
        private RelativeLayout mVideoShowLayoutRoot;
        private RelativeLayout mVideoShowPlayToolLayoutRoot;
        private RelativeLayout mVideoSourceToolLayoutRoot;
        private RelativeLayout mVideoShowLatticDrawLayoutRoot;
        private TextView mVideoShowInfoLayoutRoot;
//        private androidx.appcompat.widget.Toolbar mVideoShowToolbar;
        private com.glcc.client.DrawView mDrawViewDrawCanvas;
        private com.github.clans.fab.FloatingActionButton mDrawViewUndoBtn;
        private com.github.clans.fab.FloatingActionButton mDrawViewRedoBtn;
        private com.github.clans.fab.FloatingActionButton mDrawViewFindBtn;
        private com.github.clans.fab.FloatingActionButton mDrawViewClearBtn;
        private com.github.clans.fab.FloatingActionButton mDrawViewConfirmBtn;
        private com.github.clans.fab.FloatingActionMenu mDrawViewMenuBtn;
        private V2TXLiveDef.V2TXLiveFillMode mRenderMode =  V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFit; // 长边填充
        private V2TXLiveDef.V2TXLiveRotation mRenderRotation = V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0; // 旋转角度
        private android.view.ViewGroup.LayoutParams mVideoShowLayoutRootParam;
        private Animation VideoLoadingAnimation;
        private boolean mIsPlaying = false;
        private boolean mIsWiding = false;
        private boolean mIsDrawing = false;

        MyLivePlayer(){
            mVideoView = findViewById(R.id.video_view);
            mImageLoading = findViewById(R.id.video_loading);
            mBtnVideoPlay = findViewById(R.id.video_play_btn);
            mBtnVideoWide = findViewById(R.id.video_play_wide);
            mImageVideoLogo = findViewById(R.id.video_play_logo);
            mVideoShowLayoutRoot = findViewById(R.id.video_show_layout_root);
//            mVideoShowToolbar = findViewById(R.id.video_show_toolbar);
            mVideoShowInfoLayoutRoot = findViewById(R.id.video_show_info_bar); // to recycle
            mVideoShowPlayToolLayoutRoot = findViewById(R.id.video_show_play_tool_bar);
            mVideoSourceToolLayoutRoot = findViewById(R.id.video_source_toolbar);

            mVideoShowLatticDrawLayoutRoot = findViewById(R.id.video_show_draw_layout_root);
            mDrawViewDrawCanvas = findViewById(R.id.video_show_draw_canvas);
            mDrawViewRedoBtn = findViewById(R.id.video_show_lattice_draw_redo);
            mDrawViewUndoBtn = findViewById(R.id.video_show_lattice_draw_undo);
            mDrawViewClearBtn = findViewById(R.id.video_show_lattice_draw_clear);
            mDrawViewFindBtn = findViewById(R.id.video_show_lattice_draw_find);
            mDrawViewConfirmBtn = findViewById(R.id.video_show_lattice_draw_confirm);
            mDrawViewMenuBtn = findViewById(R.id.video_show_lattice_draw_menu);

            mVideoShowLayoutRootParam = mVideoShowLayoutRoot.getLayoutParams();
            VideoLoadingAnimation = AnimationUtils.loadAnimation(ShowVideo.this, R.anim.animation_loading);
        }


        private void initialize() {
            initPlayView();
            initPlayButton();
            initDrawButton();
            startPlay();
        }

        protected void initPlayView() {
            mVideoView.setLogMargin(12, 12, 110, 60);
            mVideoView.showLog(false);
//            mLivePlayer.setRenderView(mVideoView);
            mLivePlayer = new V2TXLivePlayerImpl(ShowVideo.this);
        }

        protected void initPlayButton() {
            VideoOnclick onClick = new VideoOnclick();
            mBtnVideoPlay.setOnClickListener(onClick);
            mBtnVideoWide.setOnClickListener(onClick);
        }

        protected void initDrawButton() {
            DrawOnclick onclick = new DrawOnclick(200);
            mDrawViewDrawCanvas.setmDrawViewClearBtn(mDrawViewClearBtn);
            mDrawViewDrawCanvas.setmDrawViewFindBtn(mDrawViewFindBtn);
            mDrawViewDrawCanvas.setmDrawViewRedoBtn(mDrawViewRedoBtn);
            mDrawViewDrawCanvas.setmDrawViewConfirmBtn(mDrawViewConfirmBtn);
            mDrawViewDrawCanvas.setmDrawViewUndoBtn(mDrawViewUndoBtn);
            mDrawViewDrawCanvas.setmDrawViewMenuBtn(mDrawViewMenuBtn);
            mDrawViewRedoBtn.setOnClickListener(onclick);
            mDrawViewUndoBtn.setOnClickListener(onclick);
            mDrawViewClearBtn.setOnClickListener(onclick);
            mDrawViewFindBtn.setOnClickListener(onclick);
            mDrawViewConfirmBtn.setOnClickListener(onclick);
        }


        protected void startPlay() {
            int code;
            startLoadingAnimation();
            mBtnVideoPlay.setClickable(false);
            if (!ObjectUtils.isEmpty(mSpinnerVideoSource.getSelectedItem())) {
                String video_name = mSpinnerVideoSource.getSelectedItem().toString();
                Log.d("ShowVideo", video_name);
                VideoModel videoModel = VideoModel.loadVideoModel(userModel.getUserName(), video_name);
                if (!ObjectUtils.isEmpty(videoModel) && fetchDetectVideo(videoModel)) {
                    videoModel = VideoModel.loadVideoModel(userModel.getUserName(), video_name);
                    if (!ObjectUtils.isEmpty(videoModel)) {
                        code = checkPlayURL(videoModel.getRoomUrl());
                    } else{
                        code = V2TXLIVE_ERROR_FAILED;
                    }
                    if (code != V2TXLIVE_OK) {
                        Toast.makeText(ShowVideo.this, "Invalidate Url", Toast.LENGTH_SHORT).show();
                    } else {
                        mLivePlayer.setRenderView(mVideoView);
                        mLivePlayer.setObserver(new MyPlayerObserver());
                        mLivePlayer.setRenderRotation(mRenderRotation);
                        mLivePlayer.setRenderFillMode(mRenderMode);
                        code = mLivePlayer.startPlay(videoModel.getRoomUrl());
                    }
                } else {
                    code = V2TXLIVE_ERROR_FAILED;
                }
            } else {
                code = V2TXLIVE_ERROR_FAILED;
            }
            mBtnVideoPlay.setClickable(true);
            mIsPlaying = code == V2TXLIVE_OK;
            onPlayStart(code);
        }

        protected void stopPlay() {
            if (!mIsPlaying) {
                return;
            }
            if (mLivePlayer != null) {
                mLivePlayer.stopPlay();
                mLivePlayer.setObserver(null);
            }
            mIsPlaying = false;
            onPlayStop();
        }

        protected void onPlayStart(int code) {
            if (code != V2TXLIVE_OK) {
                mBtnVideoPlay.setBackgroundResource(R.drawable.play_button);
                mVideoShowLayoutRoot.setBackgroundResource(R.drawable.bg_video_show_root_layout);
                mImageVideoLogo.setVisibility(View.VISIBLE);
                stopLoadingAnimation();
                if (mIsDrawing) {
                    stopDraw();
                }
                // TODO: Add Logo Info
            } else {
                mBtnVideoPlay.setBackgroundResource(R.drawable.stop_button);
                mVideoShowLayoutRoot.setBackgroundResource(R.drawable.bg_video_show_root_layout2);
                mImageVideoLogo.setVisibility(View.GONE);
            }
        }

        protected void onPlayStop() {
            mBtnVideoPlay.setBackgroundResource(R.drawable.play_button);
            mVideoShowLayoutRoot.setBackgroundResource(R.drawable.bg_video_show_root_layout);
            mImageVideoLogo.setVisibility(View.VISIBLE);
            stopLoadingAnimation();
            if (mIsDrawing) {
                stopDraw();
            }
        }

        protected void toggleDraw() {
            if (mIsDrawing) {
                stopDraw();
            } else {
                if (!ObjectUtils.isEmpty(mSpinnerVideoSource.getSelectedItem())) {
                    startDraw();
                } else {
                    stopDraw();
                    handler.post(()->{
                        Toast.makeText(ShowVideo.this, "No video is selected", Toast.LENGTH_SHORT).show();
                    });
                }

            }
        }

        protected void togglePlay() {
            if (mIsPlaying) {
                stopPlay();
            } else {
                startPlay();
            }
        }

        protected void toggleWide() {
            if (mIsWiding) {
                mIsWiding = false;
                ScreenUtils.setPortrait(ShowVideo.this);
                toShrinkState();
            } else {
                mIsWiding = true;
                ScreenUtils.setLandscape(ShowVideo.this);
                toWideState();
            }
        }

        protected void startDraw() {
            mIsDrawing = true;
            mDrawViewDrawCanvas.loadAllContourModel(userModel.getUserName(),
                    mSpinnerVideoSource.getSelectedItem().toString());
            mVideoShowPlayToolLayoutRoot.setVisibility(View.GONE);
            mVideoShowLatticDrawLayoutRoot.setVisibility(View.VISIBLE);
            mDrawViewDrawCanvas.updateCanvas();
        }

        protected void stopDraw() {
            mIsDrawing = false;
            mVideoShowPlayToolLayoutRoot.setVisibility(View.VISIBLE);
            mVideoShowLatticDrawLayoutRoot.setVisibility(View.GONE);
            mDrawViewDrawCanvas.clear();
        }

        protected void onWideState() {
            if (ScreenUtils.isPortrait()) {
                toShrinkState();
            } else {
                toWideState();
            }
        }

        protected void toShrinkState() {
            mBtnVideoWide.setBackgroundResource(R.drawable.wide);
            mVideoShowToolBar.setVisibility(View.VISIBLE);
            mVideoShowInfoLayoutRoot.setVisibility(View.VISIBLE);
            mVideoSourceToolLayoutRoot.setVisibility(View.VISIBLE);
            mVideoShowLayoutRootParam.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            mVideoShowLayoutRootParam.height = SizeUtils.dp2px(280);
        }

        protected void toWideState() {
            mBtnVideoWide.setBackgroundResource(R.drawable.reduce);
            mVideoShowToolBar.setVisibility(View.GONE);
            mVideoShowInfoLayoutRoot.setVisibility(View.GONE);
            mVideoSourceToolLayoutRoot.setVisibility(View.GONE);
            mVideoShowLayoutRootParam.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            mVideoShowLayoutRootParam.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        }

        protected void startLoadingAnimation() {
            if (mImageLoading != null) {
                mImageLoading.setVisibility(View.VISIBLE);
                mImageLoading.startAnimation(VideoLoadingAnimation);
            }
        }

        private void stopLoadingAnimation() {
            if (mImageLoading.getAnimation() != null) {
                mImageLoading.clearAnimation();
                mImageLoading.setVisibility(View.GONE);
            }
        }

        public boolean fetchDetectVideo(VideoModel videoModel) {
            if (!ObjectUtils.isEmpty(videoModel)) {
                WaitDialog.show("Connect to server, wait...");
                JSONObject json = new JSONObject();
                json.put("video_url", videoModel.getVideoUrl());
                json.put("video_name", videoModel.getVideoName());
                json.put("user_name", userModel.getUserName());
                json.put("user_password", userModel.getPassword());
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Callable<Boolean> task = new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        Response response = GLCCClient.doCommonPost(Constants.GLCC_DECT_VIDEO_URL, json.toString());
                        if (ObjectUtils.isEmpty(response)) {
                            handler.post(() -> {
                                Toast.makeText(ShowVideo.this, "Video Request Error!", Toast.LENGTH_SHORT).show();
                                stopPlay();
                            });
                            return false;
                        } else {
                            if (response.code() == 200) {
                                String body = null;
                                try {
                                    body = response.body().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                JSONObject response_map = JSONObject.fromObject(body);
                                String room_name = response_map.getString("room_name");
                                // TODO: More General (flv to any type)
                                String room_url = String.format("%s/%s.flv", Constants.GLCC_VIDEO_PLAYER_BASE_URL, room_name);
                                Log.d("ShowVideo", room_url);
                                videoModel.setRoomUrl(room_url);
                                videoModel.saveVideoModel(userModel.getUserName());
                                return true;
                            } else {
                                handler.post(() -> {
                                    Toast.makeText(ShowVideo.this, "Video Show Error", Toast.LENGTH_SHORT).show();
                                    stopPlay();
                                });
                                return false;
                            }
                        }
                    }
                };

                Future<Boolean> future = executor.submit(task);
                boolean state = false;
                try {
                    state = future.get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                WaitDialog.dismiss();
                return state;
            } else {
                return false;
            }
        }

    protected void dismissAllDetectTask() {
        Map<String, VideoModel> videoMap = VideoModel.loadAllVideoModel(userModel.getUserName());
            JSONObject json = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (Map.Entry<String, VideoModel> item : videoMap.entrySet()) {
                jsonArray.add(item.getValue().getRoomUrl());
            }
            json.put("user_name", userModel.getUserName());
            json.put("user_password", userModel.getPassword());
            json.element("room_url", jsonArray);
            Log.d("DismissDectVideo", json.toString());
            new Thread(()->{
                Response response = GLCCClient.doCommonPost(Constants.GLCC_DISDECT_VIDEO_URL, json.toString());
                if (ObjectUtils.isEmpty(response)) {
                    Log.d("DismissDectVideo", "none response");
                } else {
                    if (response.code() == 200) {
                        String body = null;
                        try {
                            body = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        JSONObject response_map = JSONObject.fromObject(body);
                        Log.d("DismissDectVideo", response_map.toString());
                    } else {
                        Log.d("dismissAllDetectTask", "Dismiss all detector tasks failed!");
                    }
                }
            }).start();
        }

    protected void dismissDetectVideo(VideoModel videoModel) {
            JSONObject json = new JSONObject();
            json.put("user_name", userModel.getUserName());
            json.put("user_password", userModel.getPassword());
            json.put("room_url", videoModel.getRoomUrl());
            new Thread(()->{
                Response response = GLCCClient.doCommonPost(Constants.GLCC_DISDECT_VIDEO_URL, json.toString());
                if (ObjectUtils.isEmpty(response)) {
                    Log.d("DismissDectVideo", "none response");
                } else {
                    if (response.code() == 200) {
                        String body = null;
                        try {
                            body = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        JSONObject response_map = JSONObject.fromObject(body);
                        Log.d("DismissDectVideo", response_map.toString());
                    } else {
                        Log.d("dismissAllDetectTask", "Dismiss single detector tasks failed!");
                    }
                }
            }).start();
        }

        private int checkPlayURL(String video_url) {
            boolean is_prefix = false;
            if (!ObjectUtils.isEmpty(video_url)) {
                for (String prefix : Constants.SUPPORT_VIDEO_PREFIX) {
                    if (video_url.startsWith(prefix)) {
                        is_prefix = true;
                        break;
                    }
                }
                boolean is_suffix = false;
                for (String suffix: Constants.SUPPORT_VIDEO_SUFFIX) {
                    if (video_url.endsWith(suffix)) {
                        is_suffix = true;
                        break;
                    }
                }
                return is_suffix && is_prefix ? V2TXLIVE_OK : V2TXLIVE_ERROR_FAILED;
            } else {
                return V2TXLIVE_ERROR_FAILED;
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

        protected class VideoOnclick extends ClickUtils.OnDebouncingClickListener {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onDebouncingClick(@NonNull View view) {
                switch (view.getId()) {
                    case R.id.video_play_btn: {
                        togglePlay();
                        break;
                    }
                    case R.id.video_play_wide: {
                        toggleWide();
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unexpected value: " + view.getId());
                }
            }
        }

        protected class DrawOnclick extends ClickUtils.OnDebouncingClickListener {

            public DrawOnclick(final long duration) {
                super(true, duration);
            }

            @SuppressLint("NonConstantResourceId")
            @Override
            public void onDebouncingClick(@NonNull View view) {
                switch (view.getId()) {
                    case R.id.video_show_lattice_draw_undo: {
                        mDrawViewDrawCanvas.undo();
                        break;
                    }
                    case R.id.video_show_lattice_draw_redo: {
                        mDrawViewDrawCanvas.redo();
                        break;
                    }
                    case R.id.video_show_lattice_draw_clear: {
                        mDrawViewDrawCanvas.clear();
                        break;
                    }
                    case R.id.video_show_lattice_draw_find: {
                        PopMenu.show(view, mDrawViewDrawCanvas.getmConfirmPathMap().keySet().toArray(new String[0]))
                                .setOnMenuItemClickListener(new OnMenuItemClickListener<PopMenu>() {
                                    @Override
                                    public boolean onClick(PopMenu dialog, CharSequence text, int index) {
                                        PopTip.show(R.drawable.trash_confirm, "Do you want to delete [" + text + "]", "OK").showLong()
                                                .setButton(new OnDialogButtonClickListener<PopTip>() {
                                            @Override
                                            public boolean onClick(PopTip popTip, View v) {
                                                JSONObject reqJson = new JSONObject();
                                                String video_name = mSpinnerVideoSource.getSelectedItem().toString();
                                                String contour_name = text.toString();
                                                reqJson.put("video_name", video_name);
                                                reqJson.put("contour_name", contour_name);
                                                reqJson.put("user_name", userModel.getUserName());
                                                reqJson.put("user_password", userModel.getPassword());
                                                new Thread(()->{
                                                    WaitDialog.show("Please Wait...");
                                                    Response response = GLCCClient.doCommonPost(Constants.GLCC_DISPUT_LATTICE_URL, reqJson.toString());
                                                    WaitDialog.dismiss();
                                                    if (ObjectUtils.isEmpty(response)) {
                                                        handler.post(()->{
                                                            Toast.makeText(ShowVideo.this, "Request error", Toast.LENGTH_SHORT).show();
                                                        });
                                                        return;
                                                    }
                                                    if (response.code() == 200) {
                                                        ContourModel.removeContourModel(userModel.getUserName(), video_name, contour_name);
                                                        mDrawViewDrawCanvas.removeConfirmPathMapRecycle(contour_name);
                                                        mDrawViewDrawCanvas.updateCanvas();
                                                        handler.post(()->{
                                                            Toast.makeText(ShowVideo.this, "Delete " + text + " success", Toast.LENGTH_SHORT).show();
                                                        });
                                                    } else {
                                                        handler.post(()->{
                                                            Toast.makeText(ShowVideo.this, "Please check your information", Toast.LENGTH_SHORT).show();
                                                        });
                                                    }
                                                }).start();
                                                //点击“撤回”按钮回调
                                                return false;
                                            }
                                        });
                                        return false;
                                    }
                                })
                                .setOverlayBaseView(false);
                        break;
                    }
                    case R.id.video_show_lattice_draw_confirm: {
                        MessageDialog.show("Confirm Contour", "Please input the name")
                                .setCustomView(new OnBindView<MessageDialog>(R.layout.video_show_draw_confirm_edit) {
                                    @Override
                                    public void onBind(MessageDialog dialog, View v) {

                                    }
                                })
                                .setOkButton("OK", new OnDialogButtonClickListener<MessageDialog>() {
                                    @Override
                                    public boolean onClick(MessageDialog baseDialog, View v) {
                                        String videoName = mSpinnerVideoSource.getSelectedItem().toString();
                                        String userName = userModel.getUserName();
                                        String password = userModel.getPassword();
                                        TextInputEditText txtInputNameEdit = findViewById(R.id.video_draw_path_name_edit);
                                        String contourName = Objects.requireNonNull(txtInputNameEdit.getText()).toString();
                                        if (contourName.length() == 0) {
                                            Toast.makeText(ShowVideo.this, "Contour name can't be none", Toast.LENGTH_SHORT).show();
                                            return true;
                                        }
                                        if(mDrawViewDrawCanvas.canConfirm()){
                                            JSONObject reqJson = new JSONObject();
                                            reqJson.put("user_name", userName);
                                            reqJson.put("user_password", password);
                                            reqJson.put("video_name", videoName);
                                            reqJson.put("contour_name", contourName);
                                            new Thread(()->{
                                                List<Float> points = mDrawViewDrawCanvas.getPointsOfShowPath();
                                                ContourModel contourModel = new ContourModel();
                                                contourModel.setContourName(contourName);
                                                contourModel.setContourPath(points);
                                                reqJson.element("contour_path", points);
                                                Response response = GLCCClient.doCommonPost(Constants.GLCC_PUT_LATTICE_URL, reqJson.toString());
                                                Log.d("ShowVideo", reqJson.toString());
                                                if (ObjectUtils.isEmpty(response)) {
                                                    handler.post(()->{
                                                        Toast.makeText(ShowVideo.this, "Connect server failed, Please try again", Toast.LENGTH_SHORT).show();
                                                    });
                                                    return;
                                                }
                                                if (response.code() == 200) {
                                                    String body = null;
                                                    try {
                                                        body = response.body().string();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    if (!ObjectUtils.isEmpty(body)) {
                                                        JSONObject bodyJson = JSONObject.fromObject(body);
                                                        String contourName_t = bodyJson.getString("contour_name");
                                                        List<Float> contourPath = JSONArray.toList(bodyJson.getJSONArray("contour_path"), Float.class);
                                                        Log.d("ShowVideo", bodyJson.toString());
                                                        contourModel.setContourName(contourName_t);
                                                        contourModel.setContourPath(contourPath);
                                                        contourModel.saveContourModel(userName, videoName);
                                                        Path path = DrawView.points2Path(contourPath, mDrawViewDrawCanvas.getmCanvasRatio());
                                                        List<Path> pathList = new ArrayList<>();
                                                        pathList.add(path);
                                                        mDrawViewDrawCanvas.getmConfirmPathMap().put(contourName_t, pathList);
                                                        mDrawViewDrawCanvas.getmShowPathList().clear();
                                                        mDrawViewDrawCanvas.updateCanvas();
                                                    } else {
                                                        handler.post(()->{
                                                            Toast.makeText(ShowVideo.this, "Error in server", Toast.LENGTH_SHORT).show();
                                                        });
                                                    }
                                                } else {
                                                    handler.post(()->{
                                                        Toast.makeText(ShowVideo.this, "Please check contour information", Toast.LENGTH_SHORT).show();
                                                    });
                                                }
                                            }).start();
                                            return false;
                                        } else {
                                            Toast.makeText(ShowVideo.this, "Can't confirm, please check", Toast.LENGTH_SHORT).show();
                                            return true;
                                        }
                                    }
                                })
                                .setCancelButton("Cancel", new OnDialogButtonClickListener<MessageDialog>() {
                                    @Override
                                    public boolean onClick(MessageDialog baseDialog, View v) {
                                        return false;
                                    }
                                });
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unexpected value: " + view.getId());
                }
            }
        }

        // play/stop butto

        protected class MyPlayerObserver extends V2TXLivePlayerObserver {
            @Override
            public void onWarning(V2TXLivePlayer player, int code, String msg, Bundle extrainfo) {
                Log.w(TAG, "[Player] onWarning: player-" + player + " code-" + code + " msg-" + msg + " info-" + extrainfo);
            }

            @Override
            public void onError(V2TXLivePlayer player, int code, String msg, Bundle extrainfo) {
                Log.e(TAG, "[Player] onError: player-" + player + " code-" + code + " msg-" + msg + " info-" + extrainfo);
                stopPlay();
            }

            @Override
            public void onSnapshotComplete(V2TXLivePlayer v2TXLivePlayer, Bitmap bitmap) {
            }

            @Override
            public void onStatisticsUpdate(V2TXLivePlayer v2TXLivePlayer, V2TXLiveDef.V2TXLivePlayerStatistics statistics) {
                Bundle netStatus = new Bundle();
                netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH, statistics.width);
                netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT, statistics.height);
                int appCpu = statistics.appCpu / 10;
                int totalCpu = statistics.systemCpu / 10;
                String strCpu = appCpu + "/" + totalCpu + "%";
                netStatus.putCharSequence(TXLiveConstants.NET_STATUS_CPU_USAGE, strCpu);
                netStatus.putInt(TXLiveConstants.NET_STATUS_NET_SPEED, statistics.videoBitrate + statistics.audioBitrate);
                netStatus.putInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE, statistics.audioBitrate);
                netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE, statistics.videoBitrate);
                netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_FPS, statistics.fps);
                netStatus.putInt(TXLiveConstants.NET_STATUS_AUDIO_CACHE, 0);
                netStatus.putInt(TXLiveConstants.NET_STATUS_VIDEO_CACHE, 0);
                netStatus.putInt(TXLiveConstants.NET_STATUS_V_SUM_CACHE_SIZE, 0);
                netStatus.putInt(TXLiveConstants.NET_STATUS_V_DEC_CACHE_SIZE, 0);
                netStatus.putString(TXLiveConstants.NET_STATUS_AUDIO_INFO, "");
                Log.d(TAG, "Current status, CPU:" + netStatus.getString(TXLiveConstants.NET_STATUS_CPU_USAGE) +
                        ", RES:" + netStatus.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) + "*" + netStatus.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT) +
                        ", SPD:" + netStatus.getInt(TXLiveConstants.NET_STATUS_NET_SPEED) + "Kbps" +
                        ", FPS:" + netStatus.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS) +
                        ", ARA:" + netStatus.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps" +
                        ", VRA:" + netStatus.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps");
                Log.d(TAG, "My: " + mVideoView.getHeight() + "*" + mVideoView.getWidth());
                Log.d(TAG, "My: " + mDrawViewDrawCanvas.getmCanvasRatio()[0] + "*" + mDrawViewDrawCanvas.getmCanvasRatio()[1]);
                if (mDrawViewDrawCanvas.getmCanvasRealSize()[0] != statistics.height || mDrawViewDrawCanvas.getmCanvasRealSize()[1] != statistics.width) {
                    mDrawViewDrawCanvas.setmCanvasRealSize(statistics.height, statistics.width);
                }
                if (mDrawViewDrawCanvas.getHeight() != mVideoView.getHeight() || mDrawViewDrawCanvas.getHeight() != mDrawViewDrawCanvas.getHeight()) {
                    ViewGroup.LayoutParams params = mDrawViewDrawCanvas.getLayoutParams();
                    params.height = mVideoView.getHeight();
                    params.width = mVideoView.getWidth();
                    mDrawViewDrawCanvas.setLayoutParams(params);
                }
//                mLogInfoWindow.setLogText(netStatus, null, 0);
            }

            @Override
            public void onVideoPlaying(V2TXLivePlayer v2TXLivePlayer, boolean firstPlay, Bundle extraInfo) {
                stopLoadingAnimation();
            }

            @Override
            public void onVideoLoading(V2TXLivePlayer v2TXLivePlayer, Bundle extrainfo) {
                startLoadingAnimation();
            }

        }

    }

}

