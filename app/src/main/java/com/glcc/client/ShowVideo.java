package com.glcc.client;

import static android.content.ContentValues.TAG;

import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_ERROR_FAILED;
import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_OK;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ClickUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.glcc.client.manager.ContourModel;
import com.glcc.client.manager.UserModel;
import com.glcc.client.manager.VideoModel;
import com.glcc.client.manager.VideoRecorderModel;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.dialogs.InputDialog;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.PopMenu;
import com.kongzue.dialogx.dialogs.PopNotification;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.kongzue.dialogx.interfaces.OnBindView;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialogx.interfaces.OnInputDialogButtonClickListener;
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener;
import com.kongzue.dialogx.util.InputInfo;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.Response;

public class ShowVideo extends AppCompatActivity {
    private static int toolbarFadeTime = 500;
    private Handler handler;
    private MyLivePlayer mALivePlayer;
    private MyFileLivePlayer mFileLivePlayer;
    private Spinner mSpinnerVideoSource;
    private TextView mTxtVideoSourceRegister;
    private TextView mTxtVideoSourceDelete;
    private TextView mTxtVideoSourceDraw;
    private Toolbar mVideoShowToolBar;
    private TextView mVideoShowInfoBar;
    private DrawerLayout mVideoShowDrawerLayout;
    private NavigationView mVideoShowNavigationView;
    private VideoRecyclerViewAdapter mVideoRecyclerViewAdapter;
    private RecyclerView mVideoRecyclerView;
    private RecyclerView.LayoutManager mVideoRecyclerViewLayoutManger;
    private UserModel userModel;
    private ArrayAdapter<String> videoSourceSpinnerAdapter;
    private ImageView videoRecyclerLoading;
    private Animation videoRecyclerLoadingAnimation;
    private Timer fetchVideoTimer;
    private Timer infoPlayTimer;
    private FetchVideoTimerTask fetchVideoTimerTask;
    private boolean isNotifyInformation = false;
    private final String turnOnNotifications = "Turn On Notifications";
    private final String turnOffNotifications = "Turn Off Notifications";
    private final String infoMsg1 = "Welcome to Cat Cat Application";
    private final String infoMsg2 = "Take care of your cat";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.setMyStatusBar(ShowVideo.this);
        setContentView(R.layout.activity_show_video);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initialize() {
        Bundle bundle = getIntent().getBundleExtra("bundle");
        String login_username = bundle.getString("login_username");
        userModel = UserModel.loadUserModel(login_username);
        handler = new Handler();
        initVideoSourceSpinner();
        initVideoSourceRegister();
        initVideoSourceDelete();
        initVideoSourceDraw();
        initToolBar();
        initNavigation();
        setListener();
        mALivePlayer = new MyLivePlayer();
        mALivePlayer.initialize();
        mFileLivePlayer = new MyFileLivePlayer();
        mFileLivePlayer.initialize();
        initVideoRecycleView();
        initVideoShowInfoBar();
    }

    private void initVideoShowInfoBar() {
        mVideoShowInfoBar = findViewById(R.id.video_show_info_bar);
        infoPlayTimer = new Timer();
        infoPlayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
            if (!ObjectUtils.isEmpty(mSpinnerVideoSource.getSelectedItem())) {
                String videoSourceName = "VideoSource: " + mSpinnerVideoSource.getSelectedItem().toString();
                if (mVideoShowInfoBar.getText().equals(videoSourceName)) {
                    mVideoShowInfoBar.setText(infoMsg1);
                } else if (mVideoShowInfoBar.getText().equals(infoMsg1)) {
                    mVideoShowInfoBar.setText(infoMsg2);
                } else {
                    mVideoShowInfoBar.setText(videoSourceName);
                }
            } else {
                if (mVideoShowInfoBar.getText().equals(infoMsg1)) {
                    mVideoShowInfoBar.setText(infoMsg2);
                } else {
                    mVideoShowInfoBar.setText(infoMsg1);
                }
            }
            }
        }, 1000 * 60, 1000 * 60);
    }

    private void initVideoRecycleView() {
        videoRecyclerLoading = findViewById(R.id.recycler_view_loading_image);
        videoRecyclerLoadingAnimation = AnimationUtils.loadAnimation(ShowVideo.this, R.anim.animation_recycler_loading);
        fetchVideoTimerTask = new FetchVideoTimerTask();
        new Thread(new Runnable() {
            @Override
            public void run() {
                fetchVideoTimerTask.init();
            }
        }).start();
        fetchVideoTimer = new Timer();
        fetchVideoTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchVideoTimerTask.update();
            }
        }, 2 * 1000 * 60,2 * 60 * 1000);
    }


    private void initToolBar() {
        mVideoShowToolBar = findViewById(R.id.video_show_toolbar);
        setSupportActionBar(mVideoShowToolBar);
//        MyUtils.setMyStatusBar(ShowVideo.this);
        BarUtils.addMarginTopEqualStatusBarHeight(mVideoShowToolBar);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void initNavigation() {
        mVideoShowDrawerLayout = findViewById(R.id.video_show_drawerlayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mVideoShowDrawerLayout, mVideoShowToolBar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mVideoShowNavigationView = findViewById(R.id.video_show_navigation_view);
        mVideoShowNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.video_show_open_notify:
                    {
                        setNotifyInformation(item);
                        break;
                    }
                    case R.id.video_show_set_server:
                    {
                        MessageDialog.show("Server Setting", "Please enter play port: ", "OK", "Cancel")
                                .setCustomView(new OnBindView<MessageDialog>(R.layout.video_show_server_play_setting_dialog) {
                                    @Override
                                    public void onBind(MessageDialog dialog, View v) {
                                        TextInputEditText mEditTxtPlayPort = findViewById(R.id.video_show_server_play_port_setting);
                                        int playPort = SPUtils.getInstance(Constants.GLCC_SERVER_SETTING_TAG).getInt("serverPortSetting");
                                        if (!ObjectUtils.isEmpty(playPort) && playPort != -1) {
                                            mEditTxtPlayPort.setText(playPort + "");
                                        } else {
                                            mEditTxtPlayPort.setText(Constants.GLCC_PLAYER_PORT + "");
                                        }
                                    }
                                })
                                .setOkButton(new OnDialogButtonClickListener<MessageDialog>() {
                                    @Override
                                    public boolean onClick(MessageDialog baseDialog, View v) {
                                        TextInputEditText mEditTxtPlayPort = findViewById(R.id.video_show_server_play_port_setting);
                                        String inputStr = Objects.requireNonNull(mEditTxtPlayPort.getText()).toString();
                                        if (!ObjectUtils.isEmpty(inputStr)) {
                                            Constants.GLCC_PLAYER_PORT = Integer.parseInt(inputStr);
                                            SPUtils.getInstance(Constants.GLCC_SERVER_SETTING_TAG).put("serverPortSetting", Constants.GLCC_PLAYER_PORT);
                                            Constants.reLoadData();
                                        } else {
                                            Toast.makeText(ShowVideo.this, "Port can't be empty!", Toast.LENGTH_SHORT).show();
                                            return true;
                                        }
                                        return false;
                                    }
                                })
                                .setCancelButton(new OnDialogButtonClickListener<MessageDialog>() {
                                    @Override
                                    public boolean onClick(MessageDialog baseDialog, View v) {
                                        return false;
                                    }
                                });
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unknown value " + item.getItemId());

                }
                return false;
            }
        });
        mVideoShowDrawerLayout.addDrawerListener(toggle);
        View headerView = mVideoShowNavigationView.getHeaderView(0);
        TextView textView = headerView.findViewById(R.id.video_show_nav_user_nickname);
        String helloMsg = textView.getText().toString() + " " + userModel.getNickName();
        textView.setText(helloMsg);
        toggle.syncState();

        if (!ObjectUtils.isEmpty(SPUtils.getInstance(Constants.GLCC_NOTIFICATION_TAG).getBoolean("isNotifyInformation"))) {
            isNotifyInformation = SPUtils.getInstance(Constants.GLCC_NOTIFICATION_TAG).getBoolean("isNotifyInformation");
        }
        if (isNotifyInformation) {
            if (!PermissionUtils.isGrantedDrawOverlays()) {
                PermissionUtils.requestDrawOverlays(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        isNotifyInformation = true;
                    }

                    @Override
                    public void onDenied() {
                        isNotifyInformation = false;
                    }
                });
            }
        }
        MenuItem menuItem = mVideoShowNavigationView.getMenu().getItem(0);
        if (isNotifyInformation) {
            menuItem.setTitle(turnOffNotifications);
        } else {
            menuItem.setTitle(turnOnNotifications);
        }

        int serverPortSetting = SPUtils.getInstance(Constants.GLCC_SERVER_SETTING_TAG).getInt("serverPortSetting");
        if (!ObjectUtils.isEmpty(serverPortSetting) && serverPortSetting != -1) {
            Constants.GLCC_PLAYER_PORT = serverPortSetting;
            Constants.reLoadData();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void setNotifyInformation(MenuItem item) {
        if (item.getTitle().equals(turnOnNotifications)) {
            if (!PermissionUtils.isGrantedDrawOverlays()) {
                PermissionUtils.requestDrawOverlays(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        isNotifyInformation = true;
                    }

                    @Override
                    public void onDenied() {
                        isNotifyInformation = false;
                    }
                });
            } else {
                isNotifyInformation = true;
            }
        } else {
            isNotifyInformation = false;
        }

        if (isNotifyInformation){
            item.setTitle(turnOffNotifications);
        } else {
            item.setTitle(turnOnNotifications);
        }

        SPUtils.getInstance(Constants.GLCC_NOTIFICATION_TAG).put("isNotifyInformation", isNotifyInformation);
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

    protected class RecyclerViewOnClick implements VideoRecyclerViewAdapter.OnItemClickListener {
        @Override
        public void onItemClick(int position) {
            VideoRecyclerViewAdapter.ViewItem item = mVideoRecyclerViewAdapter.getSingleItem(position);
            if (item.getType() == VideoRecyclerViewAdapter.TYPE_CHILD) {
                VideoRecyclerViewAdapter.ViewChild child = (VideoRecyclerViewAdapter.ViewChild) item;
            }
        }
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
//                                    boolean state = false;
                                    Future<Boolean> future = executorService.submit(task);
//                                    try {
//                                        state = future.get();
//                                    } catch (ExecutionException | InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
                                    return false;
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
                                    Response response = GLCCClient.doCommonPost(Constants.GLCC_DELETE_VIDEO_URL, reqJson.toString());
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
//                            try {
//                                state = future.get();
//                            } catch (ExecutionException | InterruptedException e) {
//                                e.printStackTrace();
//                            }
                            return false;
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
        if (!ObjectUtils.isEmpty(mFileLivePlayer.roomName)) {
            mFileLivePlayer.toggleExit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mALivePlayer.destroy();
        if (!ObjectUtils.isEmpty(mFileLivePlayer.roomName)) {
            mFileLivePlayer.toggleExit();
        }
        mFileLivePlayer.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mVideoShowDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mVideoShowDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mALivePlayer.stopPlay();
        }
        if (!ObjectUtils.isEmpty(mFileLivePlayer.roomName)) {
            mFileLivePlayer.toggleExit();
        }
    }


    class MyLivePlayer {
        private TXCloudVideoView mVideoView;
        private V2TXLivePlayer mLivePlayer;
        private ImageView mImageLoading;
        private ImageButton mBtnVideoPlay;
        private ImageButton mBtnVideoWide;
        private ImageButton mBtnVideoInfo;
        private ImageButton mBtnVideoFetch;
        private ImageView mImageVideoLogo;
//        private LogInfoWindow
        //        private androidx.appcompat.widget.Toolbar mVideoShowToolbar;
        private LinearLayout mVideoShowRoot;
        private com.glcc.client.VideoShowRelativeLayout mVideoShowLayout;
        private RelativeLayout mVideoShowPlayToolLayout;
        private RelativeLayout mVideoSourceToolLayout;
        private RelativeLayout mVideoShowLatticDrawLayout;
        private RelativeLayout mVideoShowInfoToolLayout;
        private TextView mVideoShowInfoLayout;
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
        private boolean mIsPlayToolAppearing = false;

        MyLivePlayer(){
            mVideoView = findViewById(R.id.video_view);
            mImageLoading = findViewById(R.id.video_loading);
            mBtnVideoPlay = findViewById(R.id.video_play_btn);
            mBtnVideoWide = findViewById(R.id.video_play_wide);
            mBtnVideoFetch = findViewById(R.id.video_play_fetch);
            mBtnVideoInfo = findViewById(R.id.video_info_btn);
            mImageVideoLogo = findViewById(R.id.video_play_logo);
            mVideoShowLayout = findViewById(R.id.video_show_layout_root);
//            mVideoShowToolbar = findViewById(R.id.video_show_toolbar);
            mVideoShowInfoLayout = findViewById(R.id.video_show_info_bar); // to recycle
            mVideoShowPlayToolLayout = findViewById(R.id.video_show_play_tool_bar);
            mVideoShowInfoToolLayout = findViewById(R.id.video_info_layout);
            mVideoSourceToolLayout = findViewById(R.id.video_source_toolbar);

            mVideoShowRoot = findViewById(R.id.video_show_root);
            mVideoShowLatticDrawLayout = findViewById(R.id.video_show_draw_layout_root);
            mDrawViewDrawCanvas = findViewById(R.id.video_show_draw_canvas);
            mDrawViewRedoBtn = findViewById(R.id.video_show_lattice_draw_redo);
            mDrawViewUndoBtn = findViewById(R.id.video_show_lattice_draw_undo);
            mDrawViewClearBtn = findViewById(R.id.video_show_lattice_draw_clear);
            mDrawViewFindBtn = findViewById(R.id.video_show_lattice_draw_find);
            mDrawViewConfirmBtn = findViewById(R.id.video_show_lattice_draw_confirm);
            mDrawViewMenuBtn = findViewById(R.id.video_show_lattice_draw_menu);

            mVideoShowLayoutRootParam = mVideoShowLayout.getLayoutParams();
            VideoLoadingAnimation = AnimationUtils.loadAnimation(ShowVideo.this, R.anim.animation_loading);
        }

        private void initialize() {
            initPlayView();
            initPlayButton();
            initDrawButton();
            initLogInfo();
            initVideoPlayLayout();
            startPlay();
        }


        protected void initPlayView() {
            mVideoView.setLogMargin(12, 12, 110, 60);
            mVideoView.showLog(true);
            mLivePlayer = new V2TXLivePlayerImpl(ShowVideo.this);
        }

        protected void initVideoPlayLayout() {
            VideoOnclick onclick = new VideoOnclick(500);
            mVideoShowLayout.setOnClickListener(onclick);
        }

        protected void initPlayButton() {
            VideoOnclick onClick = new VideoOnclick();
            mBtnVideoPlay.setOnClickListener(onClick);
            mBtnVideoWide.setOnClickListener(onClick);
            mBtnVideoFetch.setOnClickListener(onClick);
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

        protected void initLogInfo() {
            VideoOnclick videoOnclick = new VideoOnclick(500);
            mBtnVideoInfo.setOnClickListener(videoOnclick);
        }


        protected void startPlay() {
            int code;
            mBtnVideoPlay.setBackgroundResource(R.drawable.stop_button);
            mVideoShowLayout.setBackgroundResource(R.drawable.bg_video_show_root_layout2);
            mImageVideoLogo.setVisibility(View.GONE);
            mVideoShowPlayToolLayout
                    .animate()
                    .alpha(0f)
                    .setDuration(toolbarFadeTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mVideoShowPlayToolLayout.setVisibility(View.GONE);
                        }
                    });
            mVideoShowInfoToolLayout
                    .animate()
                    .alpha(0f)
                    .setDuration(toolbarFadeTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mVideoShowInfoToolLayout.setVisibility(View.GONE);
                        }
                    });
            mVideoShowLayout.setClickable(true);
            mBtnVideoPlay.setClickable(false);
            startLoadingAnimation();
            if (!ObjectUtils.isEmpty(mSpinnerVideoSource.getSelectedItem()) && !mIsPlaying) {
                String video_name = mSpinnerVideoSource.getSelectedItem().toString();
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
//                mLivePlayer.pauseVideo();
                mLivePlayer.setObserver(null);
            }
            mIsPlaying = false;
            onPlayStop();
        }

        protected void pausePlay() {
            if (!mIsPlaying) {
                return;
            }
            if (mLivePlayer != null) {
                mLivePlayer.pauseVideo();
            }
            mIsPlaying = false;
            onPlayPause();
        }

        protected void resumePlay() {
            if (mIsPlaying) {
                return;
            }
            int code;
            if (mLivePlayer != null) {
                code = mLivePlayer.resumeVideo();
            } else {
                code = V2TXLIVE_ERROR_FAILED;
            }
            if (code == V2TXLIVE_OK) {
                mIsPlaying = true;
                onPlayResume();
            }
        }

        protected void onPlayResume() {
            mBtnVideoPlay.setBackgroundResource(R.drawable.stop_button);
            mImageVideoLogo.setVisibility(View.GONE);
            mVideoShowPlayToolLayout
                    .animate()
                    .alpha(0f)
                    .setDuration(toolbarFadeTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mVideoShowPlayToolLayout.setVisibility(View.GONE);
                        }
                    });
            mVideoShowInfoToolLayout
                    .animate()
                    .alpha(0f)
                    .setDuration(toolbarFadeTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mVideoShowInfoToolLayout.setVisibility(View.GONE);
                        }
                    });
            mVideoShowLayout.setClickable(true);
        }

        protected void onPlayPause() {
            mBtnVideoPlay.setBackgroundResource(R.drawable.play_button);
            mVideoShowPlayToolLayout.setVisibility(View.VISIBLE);
            mVideoShowPlayToolLayout
                    .animate()
                    .alpha(1f)
                    .setDuration(toolbarFadeTime).setListener(null);
            mVideoShowInfoToolLayout.setVisibility(View.VISIBLE);
            mVideoShowInfoToolLayout
                    .animate()
                    .alpha(1f)
                    .setDuration(toolbarFadeTime).setListener(null);
            mVideoShowLayout.setClickable(false);
            stopLoadingAnimation();
        }


        protected void onPlayStart(int code) {
            if (code != V2TXLIVE_OK) {
                mBtnVideoPlay.setBackgroundResource(R.drawable.play_button);
                mVideoShowLayout.setBackgroundResource(R.drawable.bg_video_show_root_layout);
                mImageVideoLogo.setVisibility(View.VISIBLE);
                stopLoadingAnimation();
                mVideoShowPlayToolLayout.setVisibility(View.VISIBLE);
                mVideoShowPlayToolLayout
                        .animate()
                        .alpha(1f)
                        .setDuration(toolbarFadeTime).setListener(null);
                mVideoShowInfoToolLayout.setVisibility(View.VISIBLE);
                mVideoShowInfoToolLayout
                        .animate()
                        .alpha(1f)
                        .setDuration(toolbarFadeTime).setListener(null);
                mVideoShowLayout.setClickable(false);
                if (mIsDrawing) {
                    stopDraw();
                }
                // TODO: Add Logo Info
            } else {
                mBtnVideoPlay.setBackgroundResource(R.drawable.stop_button);
                mVideoShowLayout.setBackgroundResource(R.drawable.bg_video_show_root_layout2);
                mImageVideoLogo.setVisibility(View.GONE);
                mVideoShowPlayToolLayout
                        .animate()
                        .alpha(0f)
                        .setDuration(toolbarFadeTime)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mVideoShowPlayToolLayout.setVisibility(View.GONE);
                            }
                        });
                mVideoShowInfoToolLayout
                        .animate()
                        .alpha(0f)
                        .setDuration(toolbarFadeTime)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mVideoShowInfoToolLayout.setVisibility(View.GONE);
                            }
                        });
                mVideoShowLayout.setClickable(true);
            }
        }

        protected void onPlayStop() {
            mBtnVideoPlay.setBackgroundResource(R.drawable.play_button);
            mVideoShowLayout.setBackgroundResource(R.drawable.bg_video_show_root_layout);
            mImageVideoLogo.setVisibility(View.VISIBLE);
            mVideoShowPlayToolLayout.setVisibility(View.VISIBLE);
            mVideoShowPlayToolLayout
                    .animate()
                    .alpha(1f)
                    .setDuration(toolbarFadeTime).setListener(null);
            mVideoShowInfoToolLayout.setVisibility(View.VISIBLE);
            mVideoShowInfoToolLayout
                    .animate()
                    .alpha(1f)
                    .setDuration(toolbarFadeTime).setListener(null);
            mVideoShowLayout.setClickable(false);
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

        protected void playToolAppearing() {
            mIsPlayToolAppearing = true;
            mVideoShowPlayToolLayout.setVisibility(View.VISIBLE);
            mVideoShowPlayToolLayout
                    .animate()
                    .alpha(1f)
                    .setDuration(toolbarFadeTime).setListener(null);
            mVideoShowInfoToolLayout.setVisibility(View.VISIBLE);
            mVideoShowInfoToolLayout
                    .animate()
                    .alpha(1f)
                    .setDuration(toolbarFadeTime).setListener(null);
            mVideoShowLayout.setClickable(true);
        }

        protected void playToolDisappearing() {
            mIsPlayToolAppearing = false;
            mVideoShowPlayToolLayout
                    .animate()
                    .alpha(0f)
                    .setDuration(toolbarFadeTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mVideoShowPlayToolLayout.setVisibility(View.GONE);
                        }
                    });
            mVideoShowInfoToolLayout
                    .animate()
                    .alpha(0f)
                    .setDuration(toolbarFadeTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mVideoShowInfoToolLayout.setVisibility(View.GONE);
                        }
                    });
            mVideoShowLayout.setClickable(true);
        }

        protected void togglePlay() {
            if (mIsPlaying) {
                stopPlay();
            } else {
                startPlay();
            }
        }

        protected void toggleResume() {
            if (mIsPlaying) {
                pausePlay();
            } else {
                resumePlay();
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

        protected void togglePlayToolAppear() {
            if (mIsPlayToolAppearing) {
                playToolDisappearing();
            } else {
                playToolAppearing();
            }
        }

        protected void startDraw() {
            mIsDrawing = true;
            mDrawViewDrawCanvas.loadAllContourModel(userModel.getUserName(),
                    mSpinnerVideoSource.getSelectedItem().toString());
            mVideoShowPlayToolLayout.setVisibility(View.GONE);
            mVideoShowInfoToolLayout.setVisibility(View.GONE);
            mVideoShowLatticDrawLayout.setVisibility(View.VISIBLE);
            mDrawViewDrawCanvas.updateCanvas();
        }

        protected void stopDraw() {
            mIsDrawing = false;
            mVideoShowPlayToolLayout.setVisibility(View.VISIBLE);
            mVideoShowInfoToolLayout.setVisibility(View.VISIBLE);
            mVideoShowLatticDrawLayout.setVisibility(View.GONE);
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
            mVideoShowInfoLayout.setVisibility(View.VISIBLE);
            mVideoSourceToolLayout.setVisibility(View.VISIBLE);
            mVideoShowLayout.setRotateMode(VideoShowRelativeLayout.PORTRAIT);
            mVideoShowLayoutRootParam.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            mVideoShowRoot.setGravity(Gravity.CENTER_HORIZONTAL);
            BarUtils.setStatusBarVisibility(ShowVideo.this, true);
            mVideoShowRoot.setBackgroundColor(getResources().getColor(R.color.main_theme));
//            mVideoShowLayoutRootParam.height = SizeUtils.dp2px(280);
        }

        protected void toWideState() {
            mBtnVideoWide.setBackgroundResource(R.drawable.reduce);
            mVideoShowToolBar.setVisibility(View.GONE);
            mVideoShowInfoLayout.setVisibility(View.GONE);
            mVideoSourceToolLayout.setVisibility(View.GONE);
            mVideoShowLayout.setRotateMode(VideoShowRelativeLayout.LANDSCAPE);
//            mVideoShowLayoutRootParam.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            mVideoShowLayoutRootParam.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            mVideoShowRoot.setGravity(Gravity.CENTER);
            BarUtils.setStatusBarVisibility(ShowVideo.this, false);
            mVideoShowRoot.setBackgroundColor(getResources().getColor(R.color.black));
            BarUtils.addMarginTopEqualStatusBarHeight(mVideoShowToolBar);
//            mVideoShowLayout.setGravity(Gravity.CENTER_HORIZONTAL);
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
                                JSONObject resp = JSONObject.fromObject(body);
                                String room_name = resp.getString("room_name");
                                // TODO: More General (flv to any type)
                                String room_url = String.format("%s/%s.flv", Constants.GLCC_VIDEO_PLAYER_BASE_URL, room_name);
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
            public VideoOnclick() {
                super();
            }
            public VideoOnclick(final long duration) {
                super(true, duration);
            }

            @SuppressLint("NonConstantResourceId")
            @Override
            public void onDebouncingClick(@NonNull View view) {
                switch (view.getId()) {
                    case R.id.video_play_btn: {
                        toggleResume();
//                        togglePlay();
                        break;
                    }
                    case R.id.video_play_wide: {
                        toggleWide();
                        break;
                    }
                    case R.id.video_show_layout_root: {
                        togglePlayToolAppear();
                        break;
                    }
                    case R.id.video_info_btn: {
                        // TODO:
                        break;
                    }
                    case R.id.video_play_fetch: {
                        if (!ObjectUtils.isEmpty(mSpinnerVideoSource.getSelectedItem())) {
                            stopPlay();
                            startPlay();
                        }
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
                                                    Response response = GLCCClient.doCommonPost(Constants.GLCC_DISPUT_LATTICE_URL, reqJson.toString());
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
                Log.d(TAG, "My: Code: " + code);
                String videoName = mSpinnerVideoSource.getSelectedItem().toString();
            }

            @Override
            public void onError(V2TXLivePlayer player, int code, String msg, Bundle extrainfo) {
                Log.e(TAG, "[Player] onError: player-" + player + " code-" + code + " msg-" + msg + " info-" + extrainfo);
                String videoName = mSpinnerVideoSource.getSelectedItem().toString();
                stopPlay();
            }

            @Override
            public void onSnapshotComplete(V2TXLivePlayer v2TXLivePlayer, Bitmap bitmap) {
            }

            @Override
            public void onVideoResolutionChanged(V2TXLivePlayer player, int width, int height) {
                if (!ObjectUtils.isEmpty(mDrawViewDrawCanvas)) {
                    double ratioOrigin = height / (width * 1.);
                    double ratioView = mVideoView.getHeight() / (mVideoView.getWidth() * 1.);
                    double ratioCanvas = mDrawViewDrawCanvas.getHeight() / (mDrawViewDrawCanvas.getWidth() * 1.);
                    if (mDrawViewDrawCanvas.getmCanvasRealSize()[0] != height
                            || mDrawViewDrawCanvas.getmCanvasRealSize()[1] != width) {
                        mDrawViewDrawCanvas.setmCanvasRealSize(height, width);
                    }
                    if (mDrawViewDrawCanvas.getWidth() != mVideoView.getWidth()
                            || mDrawViewDrawCanvas.getHeight() != mVideoView.getWidth()
                            || ratioCanvas != ratioOrigin) {
                        if (ratioOrigin < ratioView) {
                            ViewGroup.LayoutParams params = mDrawViewDrawCanvas.getLayoutParams();
                            params.width = mVideoView.getWidth();
                            params.height = (int)(mVideoView.getHeight() * ratioOrigin / ratioView);
                            mDrawViewDrawCanvas.setLayoutParams(params);
                        } else if (ratioOrigin > ratioView) {
                            ViewGroup.LayoutParams params = mDrawViewDrawCanvas.getLayoutParams();
                            params.height = mVideoView.getHeight();
                            params.width = (int)(mVideoView.getWidth() * ratioView / ratioOrigin);
                            mDrawViewDrawCanvas.setLayoutParams(params);
//                            mDrawViewDrawCanvas.getmConfirmPathMap().clear();
                        } else {
                            ViewGroup.LayoutParams params = mDrawViewDrawCanvas.getLayoutParams();
                            params.height = mVideoView.getHeight();
                            params.width = mVideoView.getWidth();
                            mDrawViewDrawCanvas.setLayoutParams(params);
                        }
                        if (!ObjectUtils.isEmpty(mSpinnerVideoSource.getSelectedItem())) {
                            String videoName = mSpinnerVideoSource.getSelectedItem().toString();
                            mDrawViewDrawCanvas.loadAllContourModel(userModel.getUserName(), videoName);
                        }
                    }
                }

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
            }

            @Override
            public void onVideoPlaying(V2TXLivePlayer v2TXLivePlayer, boolean firstPlay, Bundle extraInfo) {
                if (firstPlay) {
                    stopLoadingAnimation();
                    Log.d(TAG, "My: firstPlay");
                } else {
                    Log.d(TAG, "My: SecondPlay");
                }
            }

            @Override
            public void onVideoLoading(V2TXLivePlayer v2TXLivePlayer, Bundle extrainfo) {
                startLoadingAnimation();
            }

        }
    }

    class FetchVideoTimerTask {

        public List<VideoRecyclerViewAdapter.ViewItem> fetchVideo(JSONArray videoNameList) {
            List<VideoRecyclerViewAdapter.ViewItem> videoRecyclerViewItemList = new ArrayList<>();
            if (videoNameList.size() > 0) {
                JSONObject videoFileFetchBody = new JSONObject();
                videoFileFetchBody.element("user_name", userModel.getUserName());
                videoFileFetchBody.element("user_password", userModel.getPassword());
                videoFileFetchBody.element("video_name", videoNameList);
                Response response = GLCCClient.doCommonPost(
                        Constants.GLCC_FETCH_VIDEO_FILE_URL, videoFileFetchBody.toString());
                if (ObjectUtils.isEmpty(response)) {
                    handler.post(()->{
                        Toast.makeText(ShowVideo.this, "Request Error!", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                } else {
                    if (response.code() == 200) {
                        String body = null;
                        try {
                            body = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (!ObjectUtils.isEmpty(body)) {
                            JSONObject respBody = JSONObject.fromObject(body);
                            Iterator<String> keys = respBody.keys();
                            String videoName;
                            int groupPos = 0;
                            while (keys.hasNext()) {
                                videoName = keys.next();
                                JSONArray videoPathList = respBody.getJSONArray(videoName);
                                VideoRecyclerViewAdapter.ViewGroup group = new VideoRecyclerViewAdapter.ViewGroup();
                                group.setPosition(groupPos);
                                group.setTitle(videoName);
                                videoRecyclerViewItemList.add(group);
                                Map<String, VideoRecorderModel> videoRecorderModelMap = VideoRecorderModel
                                        .loadAllVideoRecorderModel(userModel.getUserName(), videoName);

                                for (int childPos = 0; childPos < videoPathList.size(); childPos++) {
                                    JSONObject perVideoInfo = videoPathList.getJSONObject(childPos);
                                    String videoUrl = perVideoInfo.getString("video_url");
                                    String imageUrl = videoUrl.split("\\.")[0] + ".jpg";
                                    String startTime = perVideoInfo.getString("start_time");
                                    String endTime = perVideoInfo.getString("end_time");

                                    VideoRecorderModel videoRecorderModel;
                                    if (videoRecorderModelMap.containsKey(videoUrl)) {
                                        videoRecorderModel = videoRecorderModelMap.get(videoUrl);
                                    } else {
                                        videoRecorderModel = new VideoRecorderModel();
                                    }
                                    if (!ObjectUtils.isEmpty(videoUrl)) {
                                        videoRecorderModel.setVideoUrl(videoUrl);
                                    }

                                    if (!ObjectUtils.isEmpty(imageUrl)) {
                                        videoRecorderModel.setImageUrl(imageUrl);
                                    }
                                    if (!ObjectUtils.isEmpty(startTime)) {
                                        videoRecorderModel.setStartTime(startTime);
                                    }
                                    if (!ObjectUtils.isEmpty(endTime)) {
                                        videoRecorderModel.setEndTime(endTime);
                                    }

                                    if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                                        String message = "Cat has gone into the dangerous zone at " +
                                                startTime + " .And quit the dangerous zone at " + endTime + ".";
                                        videoRecorderModel.setMessage(message);
                                    }
                                    videoRecorderModel.saveVideoRecorderModel(userModel.getUserName(), videoName);
                                    if (ObjectUtils.isEmpty(videoRecorderModel.getBitMap())) {
                                        JSONObject imageMapFetchBody = new JSONObject();
                                        imageMapFetchBody.element("user_name", userModel.getUserName());
                                        imageMapFetchBody.element("user_password", userModel.getPassword());
                                        imageMapFetchBody.element("video_name", videoName);
                                        imageMapFetchBody.element("video_url", imageUrl);
                                        Response imageMapResponse = GLCCClient.doCommonPost(
                                                Constants.GLCC_TRANSMISS_VIDEO_FILE_URL,
                                                imageMapFetchBody.toString()

                                        );
                                        if (!ObjectUtils.isEmpty(imageMapResponse)) {
                                            if (imageMapResponse.code() != 200) {
                                                Log.d(TAG, "Fetch image: " + imageUrl + " Error");
                                            } else {
                                                byte[] imageBuf = new byte[0];
                                                try {
                                                    imageBuf = imageMapResponse.body().bytes();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                Log.d(TAG, "Fetch Image Size: " + imageBuf.length);

                                                Bitmap bitmap;
                                                if (imageBuf.length > 0) {
                                                    bitmap = BitmapFactory.decodeByteArray(imageBuf, 0, imageBuf.length);
                                                } else {
                                                    bitmap = null;
                                                    Log.d(TAG, "Fetch image: " + videoUrl + " Error");
                                                }
                                                if (!ObjectUtils.isEmpty(bitmap)) {
                                                    videoRecorderModel.setBitMap(bitmap);
                                                }
                                            }
                                        }
                                    }

                                    VideoRecyclerViewAdapter.ViewChild child = new VideoRecyclerViewAdapter.ViewChild();
                                    child.setPosition(childPos);
                                    child.setGroupName(group.getTitle());
                                    child.setVideoRecorderModel(videoRecorderModel);
                                    videoRecyclerViewItemList.add(child);
                                }
                            }
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            }
            return videoRecyclerViewItemList;
        }

        public void init() {
//            VideoRecorderModel.clearRecorderModel(userModel.getUserName());
            handler.post(()->{
                videoRecyclerLoading.startAnimation(videoRecyclerLoadingAnimation);
                videoRecyclerLoading.setVisibility(View.VISIBLE);
            });
            Map<String, VideoModel> videoModelMap = VideoModel.loadAllVideoModel(userModel.getUserName());
            JSONArray videoNameList = new JSONArray();
            for (Map.Entry<String, VideoModel> item : videoModelMap.entrySet()) {
                videoNameList.add(item.getValue().getVideoName());
            }
            List<VideoRecyclerViewAdapter.ViewItem> videoRecyclerViewItemList = fetchVideo(videoNameList);

            if (ObjectUtils.isEmpty(videoRecyclerViewItemList)) {
                videoRecyclerViewItemList = new ArrayList<>();
            }
            List<VideoRecyclerViewAdapter.ViewItem> finalVideoRecyclerViewItemList = videoRecyclerViewItemList;
            handler.post(new Runnable() {
                @Override
                public void run() {

                    mVideoRecyclerViewAdapter = new VideoRecyclerViewAdapter(finalVideoRecyclerViewItemList);
                    mVideoRecyclerViewLayoutManger = new LinearLayoutManager(
                            ShowVideo.this, LinearLayoutManager.VERTICAL, false);
                    mVideoRecyclerView = findViewById(R.id.recorder_video_recycler_view);
                    mVideoRecyclerView.setLayoutManager(mVideoRecyclerViewLayoutManger);
                    mVideoRecyclerView.setAdapter(mVideoRecyclerViewAdapter);
                    mVideoRecyclerView.addItemDecoration(new DividerItemDecoration(
                            ShowVideo.this, DividerItemDecoration.VERTICAL));
                    mVideoRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewOnClick());
                    mVideoRecyclerViewAdapter.setOnImageViewClickListener(new ClickUtils.OnDebouncingClickListener() {
                        @Override
                        public void onDebouncingClick(View v) {
                            ImageView imageView = (ImageView) v;
                            String description;
                            String videoName;
                            String videoUrl;
                            String [] splitStr = null;
                            if (!ObjectUtils.isEmpty(imageView.getContentDescription())) {
                                description = imageView.getContentDescription().toString();
                                splitStr = description.split(",");
                                videoName = splitStr[0];
                                videoUrl = splitStr[1];
                            } else {
                                videoName = null;
                                videoUrl = null;
                            }
                            mFileLivePlayer.setVideoName(videoName);
                            mFileLivePlayer.setVideoUrl(videoUrl);
                            mFileLivePlayer.togglePlay();
                        }
                    });
                    videoRecyclerLoading.clearAnimation();
                    videoRecyclerLoading.setVisibility(View.GONE);
                }
            });
        }

        public void update() {
            Map<String, VideoModel> videoModelMap = VideoModel.loadAllVideoModel(userModel.getUserName());
            JSONArray videoNameList = new JSONArray();
            for (Map.Entry<String, VideoModel> item : videoModelMap.entrySet()) {
                videoNameList.add(item.getValue().getVideoName());
            }
            List<VideoRecyclerViewAdapter.ViewItem> newVideoRecyclerViewItemList = fetchVideo(videoNameList);

            if (!ObjectUtils.isEmpty(newVideoRecyclerViewItemList)) {
                if (isNotifyInformation) {
                    if (newVideoRecyclerViewItemList.size() > mVideoRecyclerViewAdapter.getAllDataItem().size()) {
                        handler.post(()->{
                            PopNotification.show(R.drawable.notify_small, "The cat may be in danger...")
                                    .setAutoTintIconInLightOrDarkMode(false).autoDismiss(8000);
                        });
                    }
                }
                mVideoRecyclerViewAdapter.setDataItem(newVideoRecyclerViewItemList);
                handler.post(()->{
                    mVideoRecyclerViewAdapter.notifyDataSetChanged();
                });
            }
        }
    }

    class MyFileLivePlayer {
        private RelativeLayout mVideoFileLayout;
        private VideoShowRelativeLayout mVideoFilePlayerLayout;
        private LinearLayout mVideoShowLayout;
        private ImageView mVideoFilePlayLogo;
        private TXCloudVideoView mVideoFileVideoView;
        private RelativeLayout mVideoFileInfoLayout;
        private ImageButton mVideoFileInfoBtn;
        private RelativeLayout mVideoFilePlayToolBar;
        private ImageButton mVideoFilePlayBtn;
        private ImageButton mVideoFileReduceBtn;
        private ImageButton mVideoFileFetchBtn;
        private ImageView mVideoFileLoadingImage;
        private Animation mVideoFileVideoLoadingAnimation;
        private V2TXLivePlayer mVideoFileLivePlayer;
        private boolean mLivePlayerIsPlaying = false;
        private boolean mIsPlaying = false;
        private boolean mIsPlayToolAppearing = false;
        private String videoUrl = null;
        private String videoName = null;
        private String roomName = null;

        MyFileLivePlayer() {
            mVideoFileLayout = findViewById(R.id.video_file_layout);
            mVideoShowLayout = findViewById(R.id.video_show_root);
            mVideoFilePlayerLayout = findViewById(R.id.video_file_player_layout);
            mVideoFilePlayLogo = findViewById(R.id.video_file_play_logo);
            mVideoFileVideoView = findViewById(R.id.video_file_video_view);
            mVideoFileInfoLayout = findViewById(R.id.video_file_info_layout);
            mVideoFileInfoBtn = findViewById(R.id.video_file_video_info_btn);
            mVideoFilePlayToolBar = findViewById(R.id.video_file_play_tool_bar);
            mVideoFilePlayBtn = findViewById(R.id.video_file_play_btn);
            mVideoFileReduceBtn = findViewById(R.id.video_file_play_reduce);
            mVideoFileFetchBtn = findViewById(R.id.video_file_play_fetch);
            mVideoFileLoadingImage = findViewById(R.id.video_file_video_loading);
            mVideoFileVideoLoadingAnimation = AnimationUtils.loadAnimation(
                    ShowVideo.this, R.anim.animation_loading);
        }

        void setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
        }

        public String getVideoUrl() {
            return videoUrl;
        }

        public void setVideoName(String videoName) {
            this.videoName = videoName;
        }

        public String getVideoName() {
            return videoName;
        }


        void initialize() {
            mVideoFileLivePlayer = new V2TXLivePlayerImpl(ShowVideo.this);
            VideoOnClick onClick = new VideoOnClick(500);
            mVideoFilePlayerLayout.setOnClickListener(onClick);
            mVideoFilePlayBtn.setOnClickListener(onClick);
            mVideoFileReduceBtn.setOnClickListener(onClick);
            mVideoFileFetchBtn.setOnClickListener(onClick);
        }


        protected void onDestroy() {
            if (mVideoFileVideoView != null) {
                mVideoFileVideoView.onDestroy();
                mVideoFileVideoView = null;
            }
        }

        protected void toggleExit() {
            mVideoShowLayout.setVisibility(View.VISIBLE);
            mVideoFileLayout.setVisibility(View.GONE);
            ScreenUtils.setPortrait(ShowVideo.this);
            if (mLivePlayerIsPlaying) {
                mALivePlayer.resumePlay();
            } else {
                mALivePlayer.stopPlay();
            }

            mVideoFileLivePlayer.stopPlay();
            BarUtils.setStatusBarVisibility(ShowVideo.this, true);
            new Thread(()->{
                JSONObject reqBody = new JSONObject();
                reqBody.put("user_name", userModel.getUserName());
                reqBody.put("user_password", userModel.getPassword());
                reqBody.put("room_name", roomName);
                Response response = GLCCClient.doCommonPost(
                        Constants.GLCC_KICK_DECT_VIDEO_FILE_URL, reqBody.toString());
                if (ObjectUtils.isEmpty(response)) {
                    Log.d(TAG, "Kick video room_name " + roomName + " fail!");
                } else {
                    if (response.code() == 200) {
                        Log.d(TAG, "Kick video room_name " + roomName + " success!");
                    } else {
                        Log.d(TAG, "Kick video room_name " + roomName + " fail!");
                    }
                }
                videoUrl = null;
                videoName = null;
                roomName = null;
            }).start();
        }

        protected void togglePlay() {
            startLoadingAnimation(); mVideoFilePlayLogo.setVisibility(View.GONE);
            mVideoShowLayout.setVisibility(View.GONE);
            mVideoFileLayout.setVisibility(View.VISIBLE);
            mLivePlayerIsPlaying = mALivePlayer.mIsPlaying;
            if (mLivePlayerIsPlaying) {
                mALivePlayer.pausePlay();
            } else {
                mALivePlayer.stopPlay();
            }
            ScreenUtils.setLandscape(ShowVideo.this);
            mVideoFilePlayerLayout.setRotateMode(VideoShowRelativeLayout.LANDSCAPE);
            BarUtils.setStatusBarVisibility(ShowVideo.this, false);
            BarUtils.addMarginTopEqualStatusBarHeight(mVideoShowToolBar);
            fetchVideo();
        }

        protected void toggleResume() {
            if (mIsPlaying) {
                pausePlay();
            } else {
                resumePlay();
            }
        }

        protected void togglePlayToolAppear() {
            if (mIsPlayToolAppearing) {
                playToolDisappearing();
            } else {
                playToolAppearing();
            }
        }

        void fetchVideo() {
            mVideoFilePlayBtn.setClickable(false);
            mVideoFilePlayLogo.setVisibility(View.GONE);
            startLoadingAnimation();
            Log.d(TAG, "Detect Video File: "  + this.videoName + this.videoUrl);
            if (ObjectUtils.isEmpty(videoUrl) || ObjectUtils.isEmpty(videoName)) {
                Toast.makeText(ShowVideo.this, "Missing videoUrl or videoName to play", Toast.LENGTH_SHORT).show();
                stopLoadingAnimation();
                mVideoFilePlayLogo.setVisibility(View.VISIBLE);
                mVideoFilePlayBtn.setClickable(true);
            } else {
                JSONObject reqBody = new JSONObject();
                reqBody.put("user_name", userModel.getUserName());
                reqBody.put("user_password", userModel.getPassword());
                reqBody.put("video_name", videoName);
                reqBody.put("video_url", videoUrl);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Response response = GLCCClient.doCommonPost(
                                Constants.GLCC_DECT_VIDEO_FILE_URL, reqBody.toString());
                        int playCode = V2TXLIVE_ERROR_FAILED;
                        if (ObjectUtils.isEmpty(response)) {
                            handler.post(()->{
                                Toast.makeText(ShowVideo.this, "Detect Video File Request Error", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            if (response.code() == 200) {
                                JSONObject respBody = null;
                                try {
                                    respBody = JSONObject.fromObject(response.body().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (!ObjectUtils.isEmpty(reqBody)) {
                                    roomName = respBody.getString("room_name");
                                    String roomUrl = String.format("%s/%s.flv", Constants.GLCC_VIDEO_PLAYER_BASE_URL, roomName);
                                    Log.d(TAG, "Video File Room Url: " + roomUrl);
                                    playCode = mALivePlayer.checkPlayURL(roomUrl);
                                    if (playCode != V2TXLIVE_OK) {
                                        handler.post(()->{
                                            Toast.makeText(ShowVideo.this, "Invalidate Url", Toast.LENGTH_SHORT).show();
                                        });
                                    } else {
                                        mVideoFileLivePlayer.setRenderView(mVideoFileVideoView);
                                        mVideoFileLivePlayer.setObserver(new MyPlayerObserver());
                                        mVideoFileLivePlayer.setRenderRotation(V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0);
                                        mVideoFileLivePlayer.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFit);
                                        playCode = mVideoFileLivePlayer.startPlay(roomUrl);
                                    }
                                } else {
                                    handler.post(()->{
                                        Toast.makeText(ShowVideo.this, "Detect Video File Request System Error!", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } else {
                                handler.post(()->{
                                    Toast.makeText(ShowVideo.this, "Detect Video File Request Error! Code: " + response.code(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        mIsPlaying = playCode == V2TXLIVE_OK;
                        int finalPlayCode = playCode;
                        handler.post(()->{
                            onPlayStart(finalPlayCode);
                        });

                    }
                }).start();
            }
        }

        protected void onPlayStart(int code) {
            mVideoFilePlayBtn.setClickable(true);
            if (code != V2TXLIVE_OK) {
                mVideoFilePlayBtn.setBackgroundResource(R.drawable.play_button);
                mVideoFilePlayerLayout.setBackgroundResource(R.drawable.bg_video_show_root_layout);
                mVideoFilePlayLogo.setVisibility(View.VISIBLE);
                stopLoadingAnimation();
                mVideoFilePlayToolBar.setVisibility(View.VISIBLE);
                mVideoFilePlayToolBar
                        .animate()
                        .alpha(1f)
                        .setDuration(toolbarFadeTime).setListener(null);
                mVideoFileInfoLayout.setVisibility(View.VISIBLE);
                mVideoFileInfoLayout
                        .animate()
                        .alpha(1f)
                        .setDuration(toolbarFadeTime).setListener(null);
                mVideoFilePlayerLayout.setClickable(false);
            } else {
                mVideoFilePlayBtn.setBackgroundResource(R.drawable.stop_button);
                mVideoFilePlayerLayout.setBackgroundResource(R.drawable.bg_video_show_root_layout2);
                mVideoFilePlayLogo.setVisibility(View.GONE);
                mVideoFilePlayToolBar
                        .animate()
                        .alpha(0f)
                        .setDuration(toolbarFadeTime)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mVideoFilePlayToolBar.setVisibility(View.GONE);
                            }
                        });
                mVideoFileInfoLayout
                        .animate()
                        .alpha(0f)
                        .setDuration(toolbarFadeTime)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mVideoFileInfoLayout.setVisibility(View.GONE);
                            }
                        });
                mVideoFilePlayerLayout.setClickable(true);
            }
        }


        protected void onPlayPause() {
            mVideoFilePlayBtn.setBackgroundResource(R.drawable.play_button);
            mVideoFilePlayToolBar.setVisibility(View.VISIBLE);
            mVideoFilePlayToolBar
                    .animate()
                    .alpha(1f)
                    .setDuration(toolbarFadeTime).setListener(null);
            mVideoFileInfoLayout.setVisibility(View.VISIBLE);
            mVideoFileInfoLayout
                    .animate()
                    .alpha(1f)
                    .setDuration(toolbarFadeTime).setListener(null);
            mVideoShowLayout.setClickable(false);
            stopLoadingAnimation();
        }

        protected void onPlayResume() {
            mVideoFilePlayBtn.setBackgroundResource(R.drawable.stop_button);
            mVideoFilePlayLogo.setVisibility(View.GONE);
            mVideoFilePlayToolBar
                    .animate()
                    .alpha(0f)
                    .setDuration(toolbarFadeTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mVideoFilePlayToolBar.setVisibility(View.GONE);
                        }
                    });
            mVideoFilePlayToolBar
                    .animate()
                    .alpha(0f)
                    .setDuration(toolbarFadeTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mVideoFilePlayToolBar.setVisibility(View.GONE);
                        }
                    });
            mVideoShowLayout.setClickable(true);
        }

        protected void pausePlay() {
            if (!mIsPlaying) {
                return;
            }
            if (mVideoFileLivePlayer != null) {
                mVideoFileLivePlayer.pauseVideo();
            }
            mIsPlaying = false;
            onPlayPause();
        }

        protected void resumePlay() {
            if (mIsPlaying) {
                return;
            }
            int code;
            if (mVideoFileLivePlayer != null) {
                code = mVideoFileLivePlayer.resumeVideo();
            } else {
                code = V2TXLIVE_ERROR_FAILED;
            }
            if (code == V2TXLIVE_OK) {
                mIsPlaying = true;
                onPlayResume();
            }
        }

        protected void playToolAppearing() {
            mIsPlayToolAppearing = true;
            mVideoFilePlayToolBar.setVisibility(View.VISIBLE);
            mVideoFilePlayToolBar
                    .animate()
                    .alpha(1f)
                    .setDuration(toolbarFadeTime).setListener(null);
            mVideoFileInfoLayout.setVisibility(View.VISIBLE);
            mVideoFileInfoLayout
                    .animate()
                    .alpha(1f)
                    .setDuration(toolbarFadeTime).setListener(null);
            mVideoFilePlayerLayout.setClickable(true);
        }

        protected void playToolDisappearing() {
            mIsPlayToolAppearing = false;
            mVideoFilePlayToolBar
                    .animate()
                    .alpha(0f)
                    .setDuration(toolbarFadeTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mVideoFilePlayToolBar.setVisibility(View.GONE);
                        }
                    });
            mVideoFileInfoLayout
                    .animate()
                    .alpha(0f)
                    .setDuration(toolbarFadeTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mVideoFileInfoLayout.setVisibility(View.GONE);
                        }
                    });
            mVideoFilePlayerLayout.setClickable(true);
        }

        protected void startLoadingAnimation() {
            if (mVideoFileLoadingImage != null) {
                mVideoFileLoadingImage.setVisibility(View.VISIBLE);
                mVideoFileLoadingImage.startAnimation(mVideoFileVideoLoadingAnimation);
            }
        }

        private void stopLoadingAnimation() {
            if (mVideoFileLoadingImage.getAnimation() != null) {
                mVideoFileLoadingImage.clearAnimation();
                mVideoFileLoadingImage.setVisibility(View.GONE);
            }
        }

        class VideoOnClick extends ClickUtils.OnDebouncingClickListener {
            public VideoOnClick(final long duration) {
                super(true, duration);
            }
            @Override
            public void onDebouncingClick(View v) {
                switch (v.getId()) {
                    case R.id.video_file_play_reduce: {
                        toggleExit();
                        break;
                    }
                    case R.id.video_file_play_btn: {
                        toggleResume();
                        break;
                    }
                    case R.id.video_file_player_layout: {
                        togglePlayToolAppear();
                        break;
                    }
                    case R.id.video_file_play_fetch: {
                        new Thread(()->{
                            JSONObject reqBody = new JSONObject();
                            reqBody.put("user_name", userModel.getUserName());
                            reqBody.put("user_password", userModel.getPassword());
                            reqBody.put("room_name", roomName);
                            Response response = GLCCClient.doCommonPost(
                                    Constants.GLCC_KICK_DECT_VIDEO_FILE_URL, reqBody.toString());
                            if (ObjectUtils.isEmpty(response)) {
                                Log.d(TAG, "Kick video room_name " + roomName + " fail!");
                            } else {
                                if (response.code() == 200) {
                                    Log.d(TAG, "Kick video room_name " + roomName + " success!");
                                } else {
                                    Log.d(TAG, "Kick video room_name " + roomName + " fail!");
                                }
                            }
                        }).start();
                        mVideoFileLivePlayer.stopPlay();
                        fetchVideo();
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unknown value " + v.getId());
                }
            }
        }

        protected class MyPlayerObserver extends V2TXLivePlayerObserver {
            @Override
            public void onWarning(V2TXLivePlayer player, int code, String msg, Bundle extrainfo) {
                Log.w(TAG, "[Player] onWarning: player-" + player + " code-" + code + " msg-" + msg + " info-" + extrainfo);
                toggleExit();
            }

            @Override
            public void onError(V2TXLivePlayer player, int code, String msg, Bundle extrainfo) {
                Log.e(TAG, "[Player] onError: player-" + player + " code-" + code + " msg-" + msg + " info-" + extrainfo);
                toggleExit();
            }

            @Override
            public void onSnapshotComplete(V2TXLivePlayer v2TXLivePlayer, Bitmap bitmap) {
            }

            @Override
            public void onVideoResolutionChanged(V2TXLivePlayer player, int width, int height) {
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

