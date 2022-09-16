package com.glcc.client.manager;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;

import java.util.HashMap;
import java.util.Map;

public class VideoRecorderModel {
    private String imageUrl;
    private String videoUrl;
    private String startTime;
    private String endTime;
    private String message;
    private Bitmap bitMap;

    private static final String TAG = VideoRecorderModel.class.getName();


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Bitmap getBitMap() {
        return bitMap;
    }

    public void setBitMap(Bitmap bitMap) {
        this.bitMap = bitMap;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public static void removeRecorderModel(String userName, String videoName, String videoUrl) {
        SPUtils.getInstance(TAG).getInstance(userName).getInstance(videoName).remove(videoUrl);
    }

    public static void removeAllRecorderModel(String userName, String videoName) {
        SPUtils.getInstance(TAG).getInstance(userName).getInstance(videoName).clear();
    }

    public static void clearRecorderModel(String userName) {
        SPUtils.getInstance(TAG).getInstance(userName).clear();
    }

    public boolean saveVideoRecorderModel(String userName, String videoName) {
        try {
            SPUtils.getInstance(TAG)
                    .getInstance(userName)
                    .getInstance(videoName)
                    .put(videoUrl, GsonUtils.toJson(this));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static VideoRecorderModel loadVideoRecorderModel(String userName, String videoName, String videoUrl) {
        try {
            String json = SPUtils.getInstance(TAG)
                    .getInstance(userName)
                    .getInstance(videoName)
                    .getString(videoUrl);
            return GsonUtils.fromJson(json, VideoRecorderModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, VideoRecorderModel> loadAllVideoRecorderModel(String userName, String videoName) {
        Map<String, String> SPUtilsMap = (Map<String, String>) SPUtils
                .getInstance(TAG)
                .getInstance(userName)
                .getInstance(videoName)
                .getAll();
        Map<String, VideoRecorderModel> SPUtilsMapModel = new HashMap<>();
        for (Map.Entry<String, String> item : SPUtilsMap.entrySet()) {
            try {
                SPUtilsMapModel.put(item.getKey(), GsonUtils.fromJson(item.getValue(), VideoRecorderModel.class));
            } catch (Exception e) {
                e.printStackTrace();
                SPUtilsMapModel.put(item.getKey(), new VideoRecorderModel());
            }
        }
        return SPUtilsMapModel;
    }

    @NonNull
    @Override
    public String toString() {
        return "VideoRecorderModel{" +
                "imageUrl='" + imageUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", start_time='" + startTime + '\'' +
                ", end_time='" + endTime + '\'' +
                ", message='" + message + '\'' +
                ", bitMap=" + bitMap +
                '}';
    }
}
