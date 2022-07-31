package com.glcc.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ContentInfoCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.*;
import net.sf.json.JSONObject;

import com.google.android.material.textfield.TextInputEditText;
import com.tencent.rtmp.TXLivePlayer;

import java.util.Objects;

public class Login extends AppCompatActivity {
    static SQLiteDatabase db;
    static MDBOpenHelper MDBOpenHelper;
    private ImageView mImageViewLog;
    private TextView mTxtViewAppWeclome;
    private TextView mTxtViewAppContinue;
    private TextInputEditText mTxtEditUsername;
    private TextInputEditText mTxtEditPassword;
    private Button mTxtBtnLoginGO;
    private TextView mTxtBtnForget;
    private TextView mTxtBtnRegister;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        mImageViewLog = findViewById(R.id.login_logo);
        mTxtViewAppWeclome = findViewById(R.id.weclome_back_txt);
        mTxtViewAppContinue = findViewById(R.id.continue_back_txt);
        mTxtEditUsername = findViewById(R.id.username_input_edit);
        mTxtEditPassword = findViewById(R.id.password_input_edit);
        mTxtBtnLoginGO = findViewById(R.id.go_login_btn);
        mTxtBtnForget = findViewById(R.id.forget_password_btn);
        mTxtBtnRegister = findViewById(R.id.go_sign_up_btn);
        MDBOpenHelper = new MDBOpenHelper(Login.this,
                MDBConstants.mdb_name, null, MDBConstants.mdb_version);
        db = MDBOpenHelper.getWritableDatabase();
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

    protected class OnClick extends MUtils.NoShakeListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onSingleClick(@NonNull View view) {
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
                        Response response = GLCCClient.doCommonPost(Constants.GLCC_LOGIN_URL, login_infos.toString());
                        mTxtBtnLoginGO.setClickable(false);
                        if (response == null) {
                            handler.post(()->{
                                mTxtEditPassword.setText("");
                                Toast.makeText(Login.this, "Request Error", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            if (response.code() == 200) {
                                handler.post(()->{
                                    mTxtEditUsername.setText("");
                                    mTxtEditPassword.setText("");
                                    Intent intent = new Intent(Login.this, ShowVideo.class);
                                    startActivity(intent);
                                });
                                Log.d("Login", response.body().toString());
                            } else {
                                handler.post(()->{
                                    mTxtEditPassword.setText("");
                                    Toast.makeText(Login.this, "Failed! Please check your username or password!", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        mTxtBtnLoginGO.setClickable(true);
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