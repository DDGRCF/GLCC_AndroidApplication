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
    public static String GLCC_SERVER_IP = "172.20.10.3";
    public static int GLCC_PLAYER_PORT = 8080;

    public static String GLCC_SERVER_HOST = "https://" + GLCC_SERVER_IP;
    public static int GLCC_SERVER_PORT = 9999;

//    public static final String GLCC_VIDEO_PLAYER_BASE_URL = "http://172.20.10.3:8080/live";
    public static String GLCC_VIDEO_PLAYER_BASE_URL = "http://" + GLCC_SERVER_IP + ":" + GLCC_PLAYER_PORT + "/live";
    public static final String GLCC_DEFAULT_VIDEO_NAME = "test";

    @SuppressLint("DefaultLocale")
    public static String GLCC_SERVER_URL = String.format("%s:%d", GLCC_SERVER_HOST, GLCC_SERVER_PORT);

    public static String GLCC_HELLO_URI = "/hello_world";
    public static String GLCC_HELLO_URL = GLCC_SERVER_URL + GLCC_HELLO_URI;

    public static String GLCC_LOGIN_URI = "/login";
    public static String GLCC_LOGIN_URL = GLCC_SERVER_URL + GLCC_LOGIN_URI;

    public static String GLCC_REGISTER_URI = "/register";
    public static String GLCC_REGISTER_URL = GLCC_SERVER_URL + GLCC_REGISTER_URI;

    public static String GLCC_REGISTER_VIDEO_URI = GLCC_LOGIN_URI + "/register_video";
    public static String GLCC_REGISTER_VIDEO_URL = GLCC_SERVER_URL + GLCC_REGISTER_VIDEO_URI;

    public static String GLCC_DELETE_VIDEO_URI = GLCC_LOGIN_URI + "/delete_video";
    public static String GLCC_DELETE_VIDEO_URL = GLCC_SERVER_URL + GLCC_DELETE_VIDEO_URI;

    public static String GLCC_DECT_VIDEO_URI = GLCC_LOGIN_URI + "/dect_video";
    public static String GLCC_DECT_VIDEO_URL = GLCC_SERVER_URL + GLCC_DECT_VIDEO_URI;

    public static String GLCC_DISDECT_VIDEO_URI = GLCC_LOGIN_URI + "/disdect_video";
    public static String GLCC_DISDECT_VIDEO_URL = GLCC_SERVER_URL + GLCC_DISDECT_VIDEO_URI;

    public static String GLCC_PUT_LATTICE_URI = GLCC_LOGIN_URI + "/put_lattice";
    public static String GLCC_PUT_LATTICE_URL = GLCC_SERVER_URL + GLCC_PUT_LATTICE_URI;

    public static String GLCC_DISPUT_LATTICE_URI = GLCC_LOGIN_URI + "/disput_lattice";
    public static String GLCC_DISPUT_LATTICE_URL = GLCC_SERVER_URL + GLCC_DISPUT_LATTICE_URI;

    public static String GLCC_DECT_VIDEO_FILE_URI = GLCC_LOGIN_URI + "/dect_video_file";
    public static String GLCC_DECT_VIDEO_FILE_URL = GLCC_SERVER_URL + GLCC_DECT_VIDEO_FILE_URI;

    public static String GLCC_FETCH_VIDEO_FILE_URI = GLCC_LOGIN_URI + "/fetch_video_file";
    public static String GLCC_FETCH_VIDEO_FILE_URL = GLCC_SERVER_URL + GLCC_FETCH_VIDEO_FILE_URI;

    public static String GLCC_KICK_DECT_VIDEO_FILE_URI = GLCC_LOGIN_URI + "/kick_dect_video_file";
    public static String GLCC_KICK_DECT_VIDEO_FILE_URL = GLCC_SERVER_URL + GLCC_KICK_DECT_VIDEO_FILE_URI;

    public static String GLCC_TRANSMISS_VIDEO_FILE_URI = GLCC_LOGIN_URI + "/transmiss_video_file";
    public static String GLCC_TRANSMISS_VIDEO_FILE_URL = GLCC_SERVER_URL + GLCC_TRANSMISS_VIDEO_FILE_URI;


    public static final String GLCC_NOTIFICATION_TAG = "GLCC_NOTIFICATION";
    public static final String GLCC_SERVER_SETTING_TAG = "GLCC_SERVER_SET";

    // Video Type
    public static final Set<String> SUPPORT_VIDEO_PREFIX = new HashSet<>(Arrays.asList("rtmp://", "http://"));
    public static final Set<String> SUPPORT_VIDEO_SUFFIX = new HashSet<>(Arrays.asList("flv"));

    protected static void reLoadData() {
        GLCC_SERVER_HOST = "https://" + GLCC_SERVER_IP;
        GLCC_VIDEO_PLAYER_BASE_URL = "http://" + GLCC_SERVER_IP + ":" + GLCC_PLAYER_PORT + "/live";
        GLCC_SERVER_URL = String.format("%s:%d", GLCC_SERVER_HOST, GLCC_SERVER_PORT);
        GLCC_HELLO_URL = GLCC_SERVER_URL + GLCC_HELLO_URI;
        GLCC_LOGIN_URL = GLCC_SERVER_URL + GLCC_LOGIN_URI;
        GLCC_REGISTER_URL = GLCC_SERVER_URL + GLCC_REGISTER_URI;
        GLCC_REGISTER_VIDEO_URL = GLCC_SERVER_URL + GLCC_REGISTER_VIDEO_URI;
        GLCC_DELETE_VIDEO_URL = GLCC_SERVER_URL + GLCC_DELETE_VIDEO_URI;
        GLCC_DECT_VIDEO_URL = GLCC_SERVER_URL + GLCC_DECT_VIDEO_URI;
        GLCC_DISDECT_VIDEO_URL = GLCC_SERVER_URL + GLCC_DISDECT_VIDEO_URI;
        GLCC_PUT_LATTICE_URL = GLCC_SERVER_URL + GLCC_PUT_LATTICE_URI;
        GLCC_DISPUT_LATTICE_URL = GLCC_SERVER_URL + GLCC_DISPUT_LATTICE_URI;
        GLCC_DECT_VIDEO_FILE_URL = GLCC_SERVER_URL + GLCC_DECT_VIDEO_FILE_URI;
        GLCC_FETCH_VIDEO_FILE_URL = GLCC_SERVER_URL + GLCC_FETCH_VIDEO_FILE_URI;
        GLCC_KICK_DECT_VIDEO_FILE_URL = GLCC_SERVER_URL + GLCC_KICK_DECT_VIDEO_FILE_URI;
        GLCC_TRANSMISS_VIDEO_FILE_URL = GLCC_SERVER_URL + GLCC_TRANSMISS_VIDEO_FILE_URI;
    }
}
