package com.glcc.client.manager;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.SPUtils;
import com.tencent.liteav.videoproducer.producer.VideoProducerDef;

import java.io.Serializable;


public class UserModel implements Serializable {
    private String userName;
    private String password;
    private String nickName;
    private String userAvatar;
    private static final String TAG = UserModel.class.getName();

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserModel{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", nickName='" + nickName + '\'' +
                ", userAvatar='" + userAvatar + '\'' +
                '}';
    }

    public static boolean containUser(String userName) {
        return SPUtils.getInstance(TAG).contains(userName);
    }

    public boolean containUser() {
        return SPUtils.getInstance(TAG).contains(userName);
    }

    public boolean saveUserModel() {
        try {
            SPUtils.getInstance(TAG).put(userName, GsonUtils.toJson(this));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isEmpty() {
        return !ObjectUtils.isEmpty(userName) || !ObjectUtils.isEmpty(password) || !ObjectUtils.isEmpty(nickName);
    }

    public static boolean isEmpty(UserModel userModel) {
        return !ObjectUtils.isEmpty(userModel.userName) || !ObjectUtils.isEmpty(userModel.password) || !ObjectUtils.isEmpty(userModel.nickName);
    }

    public static boolean saveUserModel(UserModel userModel) {
        try {
            SPUtils.getInstance(TAG).put(userModel.userName, GsonUtils.toJson(userModel));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static UserModel loadUserModel(String userName) {
        try {
            String json = SPUtils.getInstance(TAG).getString(userName);
            return GsonUtils.fromJson(json, UserModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
