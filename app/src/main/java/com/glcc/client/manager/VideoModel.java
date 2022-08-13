package com.glcc.client.manager;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;

import java.util.HashMap;
import java.util.Map;

public class VideoModel {
    private String videoName;
    private String videoUrl;
    private String roomUrl;
    private static final String TAG = VideoModel.class.getName();

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getRoomUrl() {
        return roomUrl;
    }

    public void setRoomUrl(String roomUrl) {
        this.roomUrl = roomUrl;
    }

    public boolean saveVideoModel(String userName) {
        try {
            SPUtils.getInstance(TAG).getInstance(userName).put(videoName, GsonUtils.toJson(this));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static VideoModel loadVideoModel(String userName, String videoName) {
        try {
            String json = SPUtils.getInstance(TAG).getInstance(userName).getString(videoName);
            return GsonUtils.fromJson(json, VideoModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void removeVideoModel(String userName, String videoName) {
        SPUtils.getInstance(TAG).getInstance(userName).remove(videoName);
    }

    public static void removeAllVideoModel(String userName) {
        SPUtils.getInstance(TAG).getInstance(userName).clear();
    }

    public static Map<String, VideoModel> loadAllVideoModel(String userName) {
        Map<String, String> SPUtilsMap = (Map<String, String>) SPUtils.getInstance(TAG).getInstance(userName).getAll();
        Map<String, VideoModel> SPUtilsMapModel = new HashMap<>();
        for (Map.Entry<String, String> item : SPUtilsMap.entrySet()) {
            try {
                SPUtilsMapModel.put(item.getKey(), GsonUtils.fromJson(item.getValue(), VideoModel.class));
            } catch (Exception e) {
                e.printStackTrace();
                SPUtilsMapModel.put(item.getKey(), new VideoModel());
            }
        }
        return SPUtilsMapModel;
    }


    public static boolean containVideoModel(String userName, String videoName) {
        return SPUtils.getInstance(TAG).getInstance(userName).contains(videoName);
    }


    @NonNull
    @Override
    public String toString() {
        return "VideoModel{" +
                "videoName='" + videoName + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", roomUrl='" + roomUrl + '\'' +
                '}';
    }
}

