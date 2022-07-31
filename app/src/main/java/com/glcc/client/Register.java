package com.glcc.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import net.sf.json.JSONObject;

import org.w3c.dom.Text;

import java.util.Objects;

import okhttp3.Response;

public class Register extends AppCompatActivity {
    private ImageView mImageViewLog;
    private TextView mTxtViewAppWeclome;
    private TextView mTxtViewAppContinue;
    private TextInputEditText mTxtEditUsername;
    private TextInputEditText mTxtEditNickname;
    private TextInputEditText mTxtEditPassword;
    private TextInputEditText mTxtEditRePassword;

    private Button mBtnRegister;
    private TextView mBtnGoBackSigIn;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mTxtEditUsername = findViewById(R.id.register_username_edit);
        mTxtEditNickname = findViewById(R.id.register_nickname_edit);
        mTxtEditPassword = findViewById(R.id.register_password_edit);
        mTxtEditRePassword = findViewById(R.id.register_repassword_edit);
        mImageViewLog = findViewById(R.id.register_login_logo);
        mTxtViewAppWeclome = findViewById(R.id.register_weclome);
        mTxtViewAppContinue = findViewById(R.id.register_continue);
        mBtnRegister = findViewById(R.id.register_btn);
        mBtnGoBackSigIn = findViewById(R.id.goback_signin_btn);
        InitRegister();
    }

    private void mTransitionAnimation(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        Pair[] pairs = new Pair[7];
        pairs[0] = new Pair<>(mImageViewLog, getResources().getString(R.string.logo_image_transition_name));
        pairs[1] = new Pair<>(mTxtViewAppWeclome, getResources().getString(R.string.logo_text_transition_name));
        pairs[2] = new Pair<>(mTxtViewAppContinue, getResources().getString(R.string.desc_text_transition_name));
        pairs[3] = new Pair<>(mTxtEditUsername, getResources().getString(R.string.username_text_transition_name));
        pairs[4] = new Pair<>(mTxtEditPassword, getResources().getString(R.string.password_text_transition_name));
        pairs[5] = new Pair<>(mBtnRegister, getResources().getString(R.string.go_btn_transition_name));
        pairs[6] = new Pair<>(mBtnGoBackSigIn, getResources().getString(R.string.register_text_transition_name));
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity)context, pairs);
        startActivity(intent, options.toBundle());
    }


    protected void InitRegister() {
        handler = new Handler();
        setListener();
    }

    protected void setListener() {
        OnClick onclick = new OnClick();
        mBtnRegister.setOnClickListener(onclick);
        mBtnGoBackSigIn.setOnClickListener(onclick);
    }

    protected class OnClick extends MUtils.NoShakeListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onSingleClick(@NonNull View view) {
            switch (view.getId()) {
                case R.id.register_btn: {
                    String register_username = Objects.requireNonNull(mTxtEditUsername.getText()).toString();
                    String register_nickname = Objects.requireNonNull(mTxtEditNickname.getText()).toString();
                    String register_password = Objects.requireNonNull(mTxtEditPassword.getText()).toString();
                    String register_repassword = Objects.requireNonNull(mTxtEditRePassword.getText()).toString();
                    JSONObject register_infos = new JSONObject();
                    register_infos.put("user_name", register_username);
                    register_infos.put("user_password", register_password);
                    register_infos.put("user_nickname", register_nickname);
                    Log.d("Register", register_infos.toString());
                    if (register_username.length() == 0) {
                        handler.post(()->{
                            Toast.makeText(Register.this, "Username can't be empty", Toast.LENGTH_SHORT).show();
                        });
                        break;
                    }
                    if (register_password.length() == 0) {
                        handler.post(()->{
                            Toast.makeText(Register.this, "Password can't be empty", Toast.LENGTH_SHORT).show();
                        });
                        break;
                    }
                    if (register_repassword.length() == 0) {
                        handler.post(()->{
                            Toast.makeText(Register.this, "Please input password again", Toast.LENGTH_SHORT).show();
                        });
                        break;
                    }
                    if (register_nickname.length() == 0) {
                        handler.post(()->{
                            Toast.makeText(Register.this, "Nickname can't be empty", Toast.LENGTH_SHORT).show();
                        });
                        break;
                    }

                    if (!register_password.equals(register_repassword)) {
                        handler.post(()->{
                            mTxtEditPassword.requestFocus();
                            mTxtEditPassword.setText("");
                            mTxtEditRePassword.setText("");
                            Toast.makeText(Register.this, "Twice input password is not same", Toast.LENGTH_SHORT).show();
                        });
                        break;
                    }

                    new Thread(()->{
                        Response response = GLCCClient.doCommonPost(Constants.GLCC_REGISTER_URL, register_infos.toString());
                        mBtnRegister.setClickable(false);
                        if (response == null) {
                            handler.post(()->{
                                Toast.makeText(Register.this, "Request Error", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            if (response.code() == 200) {
                                handler.post(()->{
                                    mTxtEditUsername.requestFocus();
                                    mTxtEditUsername.setText("");
                                    mTxtEditNickname.setText("");
                                    mTxtEditPassword.setText("");
                                    mTxtEditRePassword.setText("");
                                    Toast.makeText(Register.this, "Register Success", Toast.LENGTH_SHORT).show();
                                });
                                String sql_mysql = String.format("INSERT INTO User (username, password, nickname) VALUES (\"%s\", \"%s\", \"%s\");",
                                        register_username, register_password, register_nickname);
                                Login.db.execSQL(sql_mysql);
                            } else {
                                handler.post(()->{
                                    mTxtEditUsername.requestFocus();
                                    mTxtEditPassword.setText("");
                                    mTxtEditRePassword.setText("");
                                    Toast.makeText(Register.this, "Failed! Please check your information", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        mBtnRegister.setClickable(true);
                    }).start();
                    break;

                } case R.id.goback_signin_btn: {
                    handler.post(()->{
//                        Intent intent = new Intent(Register.this, Login.class);
//                        startActivity(intent);
                        mTransitionAnimation(Register.this, Login.class);
                    });
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + view.getId());
            }

        }
    }
}