package com.glcc.client;

import android.app.Activity;
import android.graphics.CornerPathEffect;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.BarUtils;

import java.util.ArrayList;
import java.util.List;

public class MyUtils {
    public static void setMyStatusBar(@NonNull final Activity activity) {
        BarUtils.transparentStatusBar(activity);
        BarUtils.setStatusBarLightMode(activity, true);
    }

}
class DouglasUtil {

    class Coordinate {
        private double x;
        private double y;
        public Coordinate(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }
    private List<Coordinate> points;

    public DouglasUtil(List<Float> pf) {
        int i = 0;
        float[] point = new float[2];
        for (float p : pf) {
            if (i % 2 == 0) {
                point[0] = p;
            } else {
                point[1] = p;
                points.add(new Coordinate(point[0], point[1]));
            }
            i++;
        }
    }

    public List<Float> douglas(int threshold) {

        // 找到最大阈值点
        double maxH = 0;
        int index = 0;
        int end = points.size();
        for (int i = 1; i < end - 1; i++) {
            // 计算点到起点和终点组成线段的高
            double h = getDistance(points.get(i), points.get(0), points.get(end - 1));
            if (h > maxH) {
                maxH = h;
                index = i;
            }
        }

        // 如果存在最大阈值点，就进行递归遍历出所有最大阈值点
        List<Coordinate> result = getCoordinates(points, threshold, maxH, index, end, new ArrayList<>());
        List<Float> pfList = new ArrayList<>();
        for (Coordinate r : result) {
            pfList.add((float) r.getX());
            pfList.add((float) r.getY());
        }
        return pfList;
    }

    private List<Coordinate> douglas(List<Coordinate> points, int threshold) {
        List<Coordinate> result = new ArrayList<>();

        // 找到最大阈值点
        double maxH = 0;
        int index = 0;
        int end = points.size();
        for (int i = 1; i < end - 1; i++) {
            // 计算点到起点和终点组成线段的高
            double h = getDistance(points.get(i), points.get(0), points.get(end - 1));
            if (h > maxH) {
                maxH = h;
                index = i;
            }
        }
        // 如果存在最大阈值点，就进行递归遍历出所有最大阈值点
        return getCoordinates(points, threshold, maxH, index, end, result);
    }

    private List<Coordinate> getCoordinates(List<Coordinate> points, int epsilon, double maxH, int index, int end, List<Coordinate> result) {
        if (maxH > epsilon) {
            List<Coordinate> leftPoints = new ArrayList<>();
            List<Coordinate> rightPoints = new ArrayList<>();
            // 分成两半 继续找比阈值大的
            for (int i = 0; i < end; i++) {
                if (i < index) {
                    leftPoints.add(points.get(i));
                } else {
                    rightPoints.add(points.get(i));
                }
            }
            List<Coordinate> leftResult = douglas(leftPoints, epsilon);
            List<Coordinate> rightResult = douglas(rightPoints, epsilon);

            rightResult.remove(0);
            leftResult.addAll(rightResult);
            result = leftResult;
        } else {
            result.add(points.get(0));
            result.add(points.get(end - 1));
        }
        return result;
    }

    /**
     * 计算点到直线的距离
     */
    private double getDistance(Coordinate p, Coordinate s, Coordinate e) {
        double AB = distance(s, e);
        double CB = distance(p, s);
        double CA = distance(p, e);
        // 三角形面积
        double S = helen(CB, CA, AB);
        // 三角形面积 = AB（底） * 高 / 2
        // 所以 高 = 2 * 三角形面积 / AB（底）
        return 2 * S / AB;
    }

    /**
     * 计算两点之间的距离
     */
    private double distance(Coordinate p1, Coordinate p2) {
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * 三角形面积
     */
    private double helen(double CB, double CA, double AB) {
        double p = (CB + CA + AB) / 2;
        return Math.sqrt(p * (p - CB) * (p - CA) * (p - AB));
    }
}
