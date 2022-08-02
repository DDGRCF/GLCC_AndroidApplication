package com.glcc.client;

import android.annotation.SuppressLint;

public class Constants {
    // Login
    static int SPLASH_SCREEN = 5000;
    static int DISSPLASH_SCREEN = 2500;
    // Server
    public static final String GLCC_SERVER_HOST = "https://192.168.199.120";
    public static final int GLCC_SERVER_PORT = 9999;
    @SuppressLint("DefaultLocale")
    public static final String GLCC_SERVER_URL = String.format("%s:%d", GLCC_SERVER_HOST, GLCC_SERVER_PORT);

    public static final String GLCC_HELLO_URI = "/hello_world";
    public static final String GLCC_HELLO_URL = GLCC_SERVER_URL + GLCC_HELLO_URI;

    public static final String GLCC_LOGIN_URI = "/login";
    public static final String GLCC_LOGIN_URL = GLCC_SERVER_URL + GLCC_LOGIN_URI;

    public static final String GLCC_REGISTER_URI = "/register";
    public static final String GLCC_REGISTER_URL = GLCC_SERVER_URL + GLCC_REGISTER_URI;

    public static final String GLCC_DECT_VIDEO_URI = GLCC_LOGIN_URI + "/dect_video";
    public static final String GLCC_DECT_VIDEO_URL = GLCC_SERVER_URL + GLCC_DECT_VIDEO_URI;

    public static final String GLCC_DISDECT_VIDEO_URI = GLCC_LOGIN_URI + "/disdect_video";
    public static final String GLCC_DISDECT_VIDEO_URL = GLCC_SERVER_URL + GLCC_DISDECT_VIDEO_URI;

    public static final boolean USE_TEMPLATE_VIDEO_URL = true;
    public static final String GLCC_DEFAULT_VIDEO_NAME = "test";
    public static final String GLCC_PULL_VIDEO_BASE_URL = "http://192.168.199.120:7001/live";

    // Video URL Status
    public static final int PLAY_STATUS_SUCCESS                 = 0;
    public static final int PLAY_STATUS_EMPTY_URL               = -1;
    public static final int PLAY_STATUS_INVALID_URL             = -2;
    public static final int play_status_invalid_play_type       = -4;



}
