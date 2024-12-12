package com.example.doodler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Stack;

public class DoodleView extends View {
    public static final float TOUCH_TOLERANCE = 10;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paintScreen;
    private Paint paintLine;
    private HashMap<Integer, Path> pathMap;
    private HashMap<Integer, Point> previousPointMap;
    private boolean isErasing = false;
    private int previousColor;
    private float previousWidth;

    private Stack<Path> undoStack;
    private Stack<Path> redoStack;

    public DoodleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        paintScreen = new Paint();
        paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.BLACK);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(10);
        paintLine.setStrokeCap(Paint.Cap.ROUND);
        pathMap = new HashMap<>();
        previousPointMap = new HashMap<>();

        undoStack = new Stack<>();
        redoStack = new Stack<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);

        for (Integer key : pathMap.keySet()) {
            canvas.drawPath(pathMap.get(key), paintLine);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex), event.getPointerId(actionIndex));
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            touchEnded(event.getPointerId(actionIndex));
        } else {
            touchMoved(event);
        }

        invalidate();

        return true;
    }

    private void touchStarted(float x, float y, int pointerId) {
        Path path;
        Point point;

        if (pathMap.containsKey(pointerId)) {
            path = pathMap.get(pointerId);
            point = previousPointMap.get(pointerId);
        } else {
            path = new Path();
            pathMap.put(pointerId, path);
            point = new Point();
            previousPointMap.put(pointerId, point);
        }

        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;
    }

    private void touchMoved(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId);

            if (pathMap.containsKey(pointerId)) {
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = pathMap.get(pointerId);
                Point point = previousPointMap.get(pointerId);

                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                    path.quadTo(point.x, point.y, (newX + point.x) / 2, (newY + point.y) / 2);

                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    private void touchEnded(int pointerId) {
        Path path = pathMap.get(pointerId);
        if (path != null) {
            bitmapCanvas.drawPath(path, paintLine);
            undoStack.push(new Path(path));
            redoStack.clear(); // Clear redo stack after a new stroke
            path.reset();
        }
    }

    public void clear() {
        pathMap.clear();
        previousPointMap.clear();
        undoStack.clear();
        redoStack.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    public void setEraserMode(boolean erasing) {
        if (erasing) {
            previousColor = paintLine.getColor();
            previousWidth = paintLine.getStrokeWidth();

            paintLine.setColor(Color.WHITE);
            paintLine.setStrokeWidth(100);
        } else {
            paintLine.setColor(previousColor);
            paintLine.setStrokeWidth(previousWidth);
        }

        isErasing = erasing;
    }

    public void setDrawingColor(int color) {
        paintLine.setColor(color);
    }

    public int getDrawingColor() {
        return paintLine.getColor();
    }

    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }

    public int getLineWidth() {
        return (int) paintLine.getStrokeWidth();
    }

    public void undoStroke() {
        if (!undoStack.isEmpty()) {
            Path undonePath = undoStack.pop();
            redoStack.push(undonePath);
            redrawBitmap();
        }
    }

    public void redoStroke() {
        if (!redoStack.isEmpty()) {
            Path redonePath = redoStack.pop();
            undoStack.push(redonePath);
            bitmapCanvas.drawPath(redonePath, paintLine);
            invalidate();
        }
    }

    private void redrawBitmap() {
        bitmap.eraseColor(Color.WHITE);
        for (Path path : undoStack) {
            bitmapCanvas.drawPath(path, paintLine);
        }
        invalidate();
    }
}

