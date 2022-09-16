package com.glcc.client.manager;

import android.graphics.Path;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContourModel {
    private String contourName;
    private List<Float> contourPath;
    private static final String TAG = ContourModel.class.getName();

    public String getContourName() {
        return contourName;
    }

    public void setContourName(String contourName) {
        this.contourName = contourName;
    }

    public List<Float> getContourPath() {
        return contourPath;
    }

    public void setContourPath(List<Float> contourPath) {
        this.contourPath = contourPath;
    }

    public static void removeAllContourModel(String userName, String videoName) {
        SPUtils.getInstance(TAG).getInstance(userName).getInstance(videoName).clear();
    }

    public static void clearContourModel(String userName) {
        SPUtils.getInstance(TAG).getInstance(userName).clear();
    }

    public static void removeContourModel(String userName, String videoName, String contourName) {
        SPUtils.getInstance(TAG).getInstance(userName).getInstance(videoName).remove(contourName);
    }

    public boolean saveContourModel(String userName, String videoName) {
        try {
            SPUtils.getInstance(TAG)
                    .getInstance(userName)
                    .getInstance(videoName)
                    .put(contourName, GsonUtils.toJson(this));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ContourModel loadContourModel(String userName, String videoName, String contourName) {
        try {
            String json = SPUtils.getInstance(TAG)
                    .getInstance(userName)
                    .getInstance(videoName)
                    .getString(contourName);
            return GsonUtils.fromJson(json, ContourModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, ContourModel> loadAllContourModel(String userName, String videoName) {
        Map<String, String> SPUtilsMap = (Map<String, String>) SPUtils
                .getInstance(TAG)
                .getInstance(userName)
                .getInstance(videoName)
                .getAll();
        Map<String, ContourModel> SPUtilsMapModel = new HashMap<>();
        for (Map.Entry<String, String> item : SPUtilsMap.entrySet()) {
            try {
                SPUtilsMapModel.put(item.getKey(), GsonUtils.fromJson(item.getValue(), ContourModel.class));
            } catch (Exception e) {
                e.printStackTrace();
                SPUtilsMapModel.put(item.getKey(), new ContourModel());
            }
        }
        return SPUtilsMapModel;
    }

    @NonNull
    @Override
    public String toString() {
        return "ContourModel{" +
                "contourName='" + contourName + '\'' +
                ", contourPath=" + contourPath +
                '}';
    }
}
