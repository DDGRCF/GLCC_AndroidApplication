package com.glcc.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import android.graphics.Path;

import com.blankj.utilcode.util.ObjectUtils;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.glcc.client.manager.ContourModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class DrawView extends View {
    private float preX;
    private float preY;
    private Path path;
    private Paint mPaint = null;
    private static final int defaultInterval = 5;
    private int []mCanvasRealSize = new int[] {-1, -1};
    private List<Path> mShowPathList = new CopyOnWriteArrayList<>(); // 在画的
    private List<Path> mRecyclePathList = new CopyOnWriteArrayList<>(); // 丢弃的
    private Map<String, List<Path>> mConfirmPathMap = new ConcurrentHashMap<>();

    private com.github.clans.fab.FloatingActionButton mDrawViewUndoBtn;
    private com.github.clans.fab.FloatingActionButton mDrawViewRedoBtn;
    private com.github.clans.fab.FloatingActionButton mDrawViewFindBtn;
    private com.github.clans.fab.FloatingActionButton mDrawViewClearBtn;
    private com.github.clans.fab.FloatingActionButton mDrawViewConfirmBtn;
    private com.github.clans.fab.FloatingActionMenu mDrawViewMenuBtn;

    public DrawView(Context context) {
        super(context);
        mPaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6);
        mPaint.setDither(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6);
        mPaint.setDither(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setmDrawViewMenuBtn(FloatingActionMenu mDrawViewMenuBtn) {
        this.mDrawViewMenuBtn = mDrawViewMenuBtn;
    }

    public void setmDrawViewUndoBtn(FloatingActionButton mDrawViewUndoBtn) {
        this.mDrawViewUndoBtn = mDrawViewUndoBtn;
    }

    public void setmDrawViewRedoBtn(FloatingActionButton mDrawViewRedoBtn) {
        this.mDrawViewRedoBtn = mDrawViewRedoBtn;
    }

    public void setmDrawViewFindBtn(FloatingActionButton mDrawViewFindBtn) {
        this.mDrawViewFindBtn = mDrawViewFindBtn;
    }

    public void setmDrawViewClearBtn(FloatingActionButton mDrawViewClearBtn) {
        this.mDrawViewClearBtn = mDrawViewClearBtn;
    }

    public void setmDrawViewConfirmBtn(FloatingActionButton mDrawViewConfirmBtn) {
        this.mDrawViewConfirmBtn = mDrawViewConfirmBtn;
    }

    public void setmCanvasRealSize(int height, int width) {
        mCanvasRealSize[0] = height;
        mCanvasRealSize[1] = width;
    }

    public int[] getmCanvasRealSize() {
        return mCanvasRealSize;
    }

    public float[] getmCanvasRatio() {
        float[] ratio = new float[2];
        Log.d("mDraw", DrawView.this.getHeight() + "*" + DrawView.this.getWidth());
        ratio[0] = mCanvasRealSize[0] / (float)getHeight();
        ratio[1] = mCanvasRealSize[1] / (float)getWidth();
//        ratio[0] = 1;
//        ratio[1] = 1;
        return ratio;
    }


    public Map<String, List<Path>> getmConfirmPathMap() {
        return mConfirmPathMap;
    }

    public List<Path> getmShowPathList() {
        return mShowPathList;
    }

    public List<Path> getmRecyclePathList() {
        return mRecyclePathList;
    }


    public void insertConfirmPathMap(String contourName, Path path) {
        List<Path> pathList = new ArrayList<>();
        pathList.add(path);
        mConfirmPathMap.put(contourName, pathList);
    }

    public void insertConfirmPathMap(String contourName, List<Float> points) {
        Path path = points2Path(points, getmCanvasRatio());
        List<Path> pathList = new ArrayList<>();
        pathList.add(path);
        mConfirmPathMap.put(contourName, pathList);
    }

    public boolean removeConfirmPathMapRecycle(String contourName) {
        if (mConfirmPathMap.containsKey(contourName)) {
            List<Path> pathList = mConfirmPathMap.get(contourName);
            mRecyclePathList.addAll(pathList);
            mConfirmPathMap.remove(contourName);
            return true;
        } else {
            return false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                path = new Path();
                mShowPathList.add(path);
                mRecyclePathList.clear();
                preX = x;
                preY = y;
                path.moveTo(x, y);
                if (!ObjectUtils.isEmpty(mDrawViewMenuBtn)) {
                    if (mDrawViewMenuBtn.getVisibility() == View.VISIBLE) {
                        mDrawViewMenuBtn.setVisibility(View.GONE);
                        mDrawViewConfirmBtn.setAlpha(0.5F);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - preX);
                float dy = Math.abs(y - preY);
                if(dx >= 2.0 || dy >= 2.0){
                    path.quadTo(preX, preY, (preX + x)/2, (preY + y)/2);
                    preX = x;
                    preY = y;
                }
                break;

            case MotionEvent.ACTION_UP:
                path.lineTo(x, y);
                if (!ObjectUtils.isEmpty(mDrawViewMenuBtn)) {
                    mDrawViewMenuBtn.setVisibility(View.VISIBLE);
                    mDrawViewConfirmBtn.setAlpha(1.0F);
                }
                break;
        }
        invalidate();
        return true;
    }

    @Override
    public void onDraw(Canvas canvas){
        if (!ObjectUtils.isEmpty(mDrawViewConfirmBtn)) {
            if (canConfirm()) {
                if (mDrawViewConfirmBtn.getVisibility() != View.VISIBLE) {
                    mDrawViewConfirmBtn.setVisibility(View.VISIBLE);
                }
            } else {
                if (mDrawViewConfirmBtn.getVisibility() != View.GONE) {
                    mDrawViewConfirmBtn.setVisibility(View.GONE);
                }
            }
        }

        for(Path path : mShowPathList){
            canvas.drawPath(path, mPaint);
        }

        for (Map.Entry<String, List<Path>> path_entry: mConfirmPathMap.entrySet()) {
            for (Path path : path_entry.getValue()) {
                canvas.drawPath(path, mPaint);
            }
        }
    }

    public boolean canConfirm() {
        return mShowPathList.size() > 0;
    }

    public boolean canGetConfirmContour(String contourName) {
        List<Path> pathList = mConfirmPathMap.get(contourName);
        return pathList != null && pathList.size() > 0;
    }

    /**
     * 能否撤销
     */
    public boolean canUndo(){
        return mShowPathList.size() > 0;
    }

    /**
     * 能否恢复
     */
    public boolean canRedo(){
        return mRecyclePathList.size() > 0;
    }

    public boolean canClear() {
        return mShowPathList.size() > 0;
    }

    /**
     * 撤销
     */
    public boolean undo() {
        if(canUndo()){
            mRecyclePathList.add(mShowPathList.remove(mShowPathList.size() - 1));
            invalidate();
            return true;
        } else {
            return false;
        }
    }


    /**
     * 恢复
     */
    public boolean redo(){
        if(canRedo()){
            mShowPathList.add(mRecyclePathList.remove(mRecyclePathList.size() - 1));
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 清除全部
     */
    public boolean clear() {
        mRecyclePathList.clear();
        if(canClear()) {
            mShowPathList.clear();
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    public boolean confirm(String contourName) {
        if (canConfirm()) {
            mConfirmPathMap.put(contourName, new ArrayList<>(mShowPathList));
            return true;
        } else {
            return false;
        }
    }

    public boolean undoSingleConfirm(String pathListName) {
        if (mConfirmPathMap.containsKey(pathListName)) {
            mRecyclePathList.addAll(Objects.requireNonNull(mConfirmPathMap.remove(pathListName)));
            return true;
        } else {
            return false;
        }
    }

    public boolean undoAllConfirm() {
        if (mConfirmPathMap.size() > 0) {
            mConfirmPathMap.clear();
            return true;
        } else {
            return false;
        }
    }

    public List<Float> getPointsOfShowPath(int interval) {
        if (mShowPathList.size() > 0) {
            int height = DrawView.this.getHeight();
            int width = DrawView.this.getWidth();
            Log.d("mDraw", height + "*" + width);
            List<Float> pointList = path2Points(mShowPathList, interval, height, width, getmCanvasRatio());
            return pointList;
        } else {
            return null;
        }
    }

    public List<Float> getPointsOfShowPath() {
        return getPointsOfShowPath(defaultInterval);
    }

    public List<Float> getPointsOfSingleConfirmPath(int interval, String pathListName) {
        if (canGetConfirmContour(pathListName)) {
            List<Path> pathList = mConfirmPathMap.get(pathListName);
            int height = DrawView.this.getHeight();
            int width = DrawView.this.getWidth();
            List<Float> pointList = path2Points(pathList, interval, height, width, getmCanvasRatio());
            Log.d("mDraw", pointList.toString());
            return pointList;
        } else {
            return null;
        }
    }

    public Map<String, List<Float>> getPointsOfAllConfirmPath(int interval, String... names) {
        Map<String, List<Float>> mPointsOfAllConfirmPath = new HashMap<>();
        for (String name : names) {
            List<Float> pointList = getPointsOfSingleConfirmPath(interval, name);
            if (!ObjectUtils.isEmpty(pointList)) {
                mPointsOfAllConfirmPath.put(name, getPointsOfSingleConfirmPath(interval, name));
            }
        }
        return mPointsOfAllConfirmPath;
    }

    public boolean loadAllContourModel(String userName, String videoName) {
        Map<String, ContourModel> allContourModel = ContourModel.loadAllContourModel(userName, videoName);
        if (allContourModel.size() > 0) {
            mConfirmPathMap.clear();
            for (Map.Entry<String, ContourModel> item : allContourModel.entrySet()) {
                List<Float> pathOfPoints = item.getValue().getContourPath();
                Path path = points2Path(pathOfPoints, getmCanvasRatio());
                List<Path> pathList = new ArrayList<>();
                pathList.add(path);
                mConfirmPathMap.put(item.getValue().getContourName(), pathList);
            }
            return true;
        } else {
            return false;
        }
    }

    public static Path points2Path(List<Float> points) {
        return points2Path(points, new float[] {1, 1});
    }

    public static Path points2Path(List<Float> points, float[] ratio) {
        int i = 0;
        float[] point = new float[2];
        Path path = new Path();
        for (float p : points) {
            // y
            if (i % 2 == 0) {
                point[0] = p / ratio[1];
            } else if (i % 2 == 1) {
                point[1] = p / ratio[0];
                if (i - 1 == 0) {
                    path.moveTo(point[0], point[1]);
                } else {
                    path.lineTo(point[0], point[1]);
                }
            }
            i++;
        }
        path.close();
        return path;
    }


    public static List<Float> path2Points(List<Path> pathList, int interval, int height, int width) {
        return path2Points(pathList, interval, height, width, new float[]{1, 1});
    }

    public static List<Float> path2Points(List<Path> pathList, int interval, int height, int width, float[] ratio) {
        if (pathList.size() > 0) {
            List<Float> pointList = new ArrayList<>();
            for (Path path : pathList) {
                float[] point = new float[2];
                PathMeasure pathMeasure = new PathMeasure(path, false);
                for (int i = 0; i < pathMeasure.getLength(); i+=interval){
                    pathMeasure.getPosTan(i, point, null);
                    if (point[0] < 0 || point[0] > width - 1) {
                        continue;
                    }
                    if (point[1] < 0 || point[1] > height - 1) {
                        continue;
                    }
                    pointList.add(point[0] * ratio[1]);
                    pointList.add(point[1] * ratio[0]);
                }
            }
            return pointList;
        } else {
            return null;
        }
    }

    public List<Float> getPointsOfSingleConfirmPath(String pathListName) {
        return getPointsOfSingleConfirmPath(defaultInterval, pathListName);
    }

    public Map<String, List<Float>> getPointsOfAllConfirmPath(String... names) {
        return getPointsOfAllConfirmPath(defaultInterval, names);
    }

    public Map<String, List<Float>> getPointsOfAllConfirmPath(int interval) {
        return getPointsOfAllConfirmPath(interval, mConfirmPathMap.keySet().toArray(new String[0]));
    }

    public Map<String, List<Float>> getPointsOfAllConfirmPath() {
        return getPointsOfAllConfirmPath(defaultInterval);
    }

    /**
     * 设置画笔大小
     */
    public void setPaintWidth(int width) {
        mPaint.setStrokeWidth(width);
    }

    /**
     * 设置画笔颜色
     */
    public void setPaintColor(int color) {
        mPaint.setColor(color);
    }

    public void updateCanvas() {
        invalidate();
    }
}