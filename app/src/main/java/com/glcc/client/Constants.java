package com.glcc.client;

import android.annotation.SuppressLint;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constants {
    // Login
    static int SPLASH_SCREEN = 6000;
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

    public static final String GLCC_REGISTER_VIDEO_URI = GLCC_LOGIN_URI + "/register_video";
    public static final String GLCC_REGISTER_VIDEO_URL = GLCC_SERVER_URL + GLCC_REGISTER_VIDEO_URI;

    public static final String GLCC_DELETE_VIDEO_URI = GLCC_LOGIN_URI + "/delete_video";
    public static final String GLCC_DELETE_VIDEO_URL = GLCC_SERVER_URL + GLCC_DELETE_VIDEO_URI;

    public static final String GLCC_DECT_VIDEO_URI = GLCC_LOGIN_URI + "/dect_video";
    public static final String GLCC_DECT_VIDEO_URL = GLCC_SERVER_URL + GLCC_DECT_VIDEO_URI;

    public static final String GLCC_DISDECT_VIDEO_URI = GLCC_LOGIN_URI + "/disdect_video";
    public static final String GLCC_DISDECT_VIDEO_URL = GLCC_SERVER_URL + GLCC_DISDECT_VIDEO_URI;


    public static final String GLCC_PUT_LATTICE_URI = GLCC_LOGIN_URI + "/put_lattice";
    public static final String GLCC_PUT_LATTICE_URL = GLCC_SERVER_URL + GLCC_PUT_LATTICE_URI;

    public static final String GLCC_DISPUT_LATTICE_URI = GLCC_LOGIN_URI + "/disput_lattice";
    public static final String GLCC_DISPUT_LATTICE_URL = GLCC_SERVER_URL + GLCC_DISPUT_LATTICE_URI;

    public static final boolean USE_TEMPLATE_VIDEO_URL = true;
    public static final String GLCC_DEFAULT_VIDEO_NAME = "test";
    public static final String GLCC_VIDEO_PLAYER_BASE_URL = "http://192.168.199.120:8080/live";


    // Video Type
    public static final Set<String> SUPPORT_VIDEO_PREFIX = new HashSet<>(Arrays.asList("rtmp://", "http://"));
    public static final Set<String> SUPPORT_VIDEO_SUFFIX = new HashSet<>(Arrays.asList("flv"));
}
