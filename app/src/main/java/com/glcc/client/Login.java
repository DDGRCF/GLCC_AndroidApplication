package com.glcc.client;

import static com.glcc.client.manager.VideoModel.removeVideoModel;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.blankj.utilcode.util.BarUtils;
import com.glcc.client.manager.ContourModel;
import com.glcc.client.manager.UserModel;
import com.glcc.client.manager.VideoModel;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ClickUtils;
import com.kongzue.dialogx.dialogs.WaitDialog;


public class Login extends AppCompatActivity {
    private ImageView mImageViewLog;
    private TextView mTxtViewAppWeclome;
    private TextView mTxtViewAppContinue;
    private TextInputEditText mTxtEditUsername;
    private TextInputEditText mTxtEditPassword;
    private Button mTxtBtnLoginGO;
    private TextView mTxtBtnForget;
    private TextView mTxtBtnRegister;
    private Handler handler;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.setMyStatusBar(Login.this);
        setContentView(R.layout.activity_login);
        mImageViewLog = findViewById(R.id.login_logo);
        mTxtViewAppWeclome = findViewById(R.id.weclome_back_txt);
        mTxtViewAppContinue = findViewById(R.id.continue_back_txt);
        mTxtEditUsername = findViewById(R.id.username_input_edit);
        mTxtEditPassword = findViewById(R.id.password_input_edit);
        mTxtBtnLoginGO = findViewById(R.id.go_login_btn);
        mTxtBtnForget = findViewById(R.id.forget_password_btn);
        mTxtBtnRegister = findViewById(R.id.go_sign_up_btn);
        BarUtils.addMarginTopEqualStatusBarHeight(mImageViewLog);
        InitLogin();
    }

    protected void InitLogin() {
        handler = new Handler();
        setListener();
    }

    protected void setListener() {
        OnClick onclick = new OnClick();
        mTxtBtnLoginGO.setOnClickListener(onclick);
        mTxtBtnRegister.setOnClickListener(onclick);
        mTxtBtnForget.setOnClickListener(onclick);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    private void mTransitionAnimation(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        Pair[] pairs = new Pair[7];
        pairs[0] = new Pair<>(mImageViewLog, getResources().getString(R.string.logo_image_transition_name));
        pairs[1] = new Pair<>(mTxtViewAppWeclome, getResources().getString(R.string.logo_text_transition_name));
        pairs[2] = new Pair<>(mTxtViewAppContinue, getResources().getString(R.string.desc_text_transition_name));
        pairs[3] = new Pair<>(mTxtEditUsername, getResources().getString(R.string.username_text_transition_name));
        pairs[4] = new Pair<>(mTxtEditPassword, getResources().getString(R.string.password_text_transition_name));
        pairs[5] = new Pair<>(mTxtBtnLoginGO, getResources().getString(R.string.go_btn_transition_name));
        pairs[6] = new Pair<>(mTxtBtnRegister, getResources().getString(R.string.register_text_transition_name));
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity)context, pairs);
        startActivity(intent, options.toBundle());
    }

    protected class OnClick extends ClickUtils.OnDebouncingClickListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onDebouncingClick(@NonNull View view) {
            switch (view.getId()) {
                case R.id.go_login_btn: {
                    String login_username = Objects.requireNonNull(mTxtEditUsername.getText()).toString();
                    String login_password = Objects.requireNonNull(mTxtEditPassword.getText()).toString();
                    JSONObject login_infos = new JSONObject();
                    login_infos.put("user_name", login_username);
                    login_infos.put("user_password", login_password);
                    if (login_username.length() == 0) {
                        handler.post(()->{
                            Toast.makeText(Login.this, "Username can't be empty", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                    if (login_password.length() == 0) {
                        handler.post(()->{
                            Toast.makeText(Login.this, "Password can't be empty", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                    Log.d("Login", login_infos.toString());
                    new Thread(()->{
                        WaitDialog.show("Please wait!");
                        Response response = GLCCClient.doCommonPost(Constants.GLCC_LOGIN_URL, login_infos.toString());
                        WaitDialog.dismiss();
                        if (ObjectUtils.isEmpty(response)) {
                            handler.post(()->{
                                mTxtEditPassword.setText("");
                                Toast.makeText(Login.this, "Request Error!", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            if (response.code() == 200) {
                                String body = null;
                                try {
                                    body = response.body().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                UserModel userModel = UserModel.loadUserModel(login_username);
                                if (ObjectUtils.isEmpty(userModel)) {
                                    userModel = new UserModel();
                                    userModel.setUserName(login_username);
                                    userModel.setPassword(login_password);
                                    userModel.saveUserModel();
                                }
                                Map<String, VideoModel> videoModelMap = VideoModel.loadAllVideoModel(login_username);
                                if (!ObjectUtils.isEmpty(body)) {
                                    JSONObject json = JSONObject.fromObject(body);
                                    if (json.containsKey("video_name") && json.containsKey("video_url")) {
                                        JSONArray video_names = json.getJSONArray("video_name");
                                        JSONArray video_urls = json.getJSONArray("video_url");
                                        for (int i = 0; i < video_names.size(); i++) {
                                            String video_name = video_names.getString(i);
                                            String video_url = video_urls.getString(i);
                                            if (videoModelMap.containsKey(video_name)) {
                                                VideoModel oldVideModel = Objects.requireNonNull(videoModelMap.get(video_name));
                                                oldVideModel.setVideoName(video_name);
                                                oldVideModel.setVideoUrl(video_url);
                                                oldVideModel.saveVideoModel(login_username);
                                            } else {
                                                VideoModel newVideoModel = new VideoModel();
                                                newVideoModel.setVideoName(video_name);
                                                newVideoModel.setVideoUrl(video_url);
                                                newVideoModel.saveVideoModel(login_username);
                                            }
                                        }
                                        Log.d("Login", "video_names" + video_names.toString());
                                        for (Map.Entry<String, VideoModel> elem : videoModelMap.entrySet()) {
                                            if (!video_names.contains(elem.getKey())) {
                                                VideoModel.removeVideoModel(login_username, elem.getKey());
                                            }
                                        }
                                        if (json.containsKey("contour_name") && json.containsKey("contour_path")
                                            && json.containsKey("contour_video_name")) {
                                            JSONArray contour_names = json.getJSONArray("contour_name");
                                            JSONArray contour_paths = json.getJSONArray("contour_path");
                                            JSONArray contour_video_names = json.getJSONArray("contour_video_name");
                                            String video_name = null;
                                            String contour_name = null;
                                            List<Float> contour_path = new ArrayList<>();
                                            for (int i = 0; i < contour_video_names.size(); i++) {
                                                video_name = contour_video_names.getString(i);
                                                contour_name = contour_names.getString(i);
                                                contour_path = JSONArray.toList(contour_paths.getJSONArray(i), Float.class);
                                                ContourModel contourModel = new ContourModel();
                                                contourModel.setContourName(contour_name);
                                                contourModel.setContourPath(contour_path);
                                                contourModel.saveContourModel(login_username, video_name);
                                                Log.d("LoginAllContourModel", ContourModel.loadAllContourModel(login_username, video_name).keySet().toString());
                                                for (Map.Entry<String, ContourModel> elem : ContourModel.loadAllContourModel(login_username, video_name).entrySet()) {
                                                    if (!contour_names.contains(elem.getKey())) {
                                                        ContourModel.removeContourModel(login_username, video_name, elem.getKey());
                                                    }
                                                }
                                            }
                                        } else {
                                            videoModelMap = VideoModel.loadAllVideoModel(login_username);
                                            for (Map.Entry<String, VideoModel> elem : videoModelMap.entrySet()) {
                                                ContourModel.removeAllContourModel(login_username, elem.getKey());
                                            }
                                        }
                                    } else {
                                        VideoModel.removeAllVideoModel(login_username);
                                    }
                                } else {
                                    VideoModel.removeAllVideoModel(login_username);
                                }
                                handler.post(()->{
                                    mTxtEditUsername.setText("");
                                    mTxtEditPassword.setText("");
                                    Intent intent = new Intent(Login.this, ShowVideo.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("login_username", login_username);
                                    intent.putExtra("bundle", bundle);
                                    startActivity(intent);
                                });
                            } else {
                                handler.post(()->{
                                    mTxtEditPassword.setText("");
                                    Toast.makeText(Login.this, "Failed! Please check your username or password!", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    }).start();
                    break;
                }
                case R.id.go_sign_up_btn: {
                    handler.post(()->{
//                        Intent intent = new Intent(Login.this, Register.class);
//                        startActivity(intent);
                        mTransitionAnimation(Login.this, Register.class);
                    });
                    break;
                }
                case R.id.forget_password_btn: {
                    handler.post(()->{
                        Intent intent = new Intent(Login.this, SeekAcounts.class);
                        startActivity(intent);
                    });
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + view.getId());
            }
        }
    }
}