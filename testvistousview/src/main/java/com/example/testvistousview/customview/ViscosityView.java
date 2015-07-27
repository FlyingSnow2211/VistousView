package com.example.testvistousview.customview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.OvershootInterpolator;

import com.example.testvistousview.Utils.GeometryUtils;

/**
 * 粘性控件：消息未读提醒
 */
public class ViscosityView extends View {

    private Paint mPaint;
    private PointF[] mDragPoint;
    private PointF[] mStickPoint;
    private PointF mControlPoint;
    private PointF mDragCenterPoint;
    private PointF mStickCenterPoint;
    private float mDragRadius;
    private float mStickRadius;
    private float xOffset;
    private float yOffset;
    private double lineK;
    private int statusHeight;
    float maxDistance = 200.0f;
    private boolean isOutOfRange = false;
    private boolean disappear = false;
    private Paint mTextPaint;

    public interface OnDragEventListener {
        void onDisappear();

        void onReset(boolean isToReset);
    }

    public OnDragEventListener mOnDragEventListener;

    public OnDragEventListener getmOnDragEventListener() {
        return mOnDragEventListener;
    }

    public void setmOnDragEventListener(OnDragEventListener mOnDragEventListener) {
        this.mOnDragEventListener = mOnDragEventListener;
    }

    public ViscosityView(Context context) {
        this(context, null);
    }

    public ViscosityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViscosityView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //ANTI_ALIAS_FLAG抗锯齿
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(24);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mDragPoint = new PointF[2];
        mStickPoint = new PointF[2];
        mControlPoint = new PointF();
        mDragCenterPoint = new PointF(150f, 150f);
        mStickCenterPoint = new PointF(200f, 200f);
        mStickRadius = 30f;
        mDragRadius = 30f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 将画布向上平移状态栏高度的位置
         */
        //保存当前的的状态
        canvas.save();
        canvas.translate(0, -statusHeight);

        //计算连接部分----利用变量
        //计算变量：
        //1.求出固定圆的半径（暂时假设为12f）
        float tempStickRadius = getStickRadius();

        //2.求出四个附着点的坐标
        xOffset = mStickCenterPoint.x - mDragCenterPoint.x;
        yOffset = mStickCenterPoint.y - mDragCenterPoint.y;
        if (xOffset != 0) {
            lineK = yOffset / xOffset;
        }
        mStickPoint = GeometryUtils.getIntersectionPoints(mStickCenterPoint, tempStickRadius, lineK);
        mDragPoint = GeometryUtils.getIntersectionPoints(mDragCenterPoint, mDragRadius, lineK);

        //3.求出控制点的坐标
        mControlPoint = GeometryUtils.getPointByPercent(mDragCenterPoint, mStickCenterPoint, 0.618f);

        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mStickCenterPoint.x, mStickCenterPoint.y, maxDistance, mPaint);
        mPaint.setStyle(Paint.Style.FILL);

        if (!disappear) {
            if (!isOutOfRange) {
                //绘制两圆的连接部分采用贝塞尔曲线
                //绘制路径
                Path path = new Path();
                path.moveTo(mStickPoint[0].x, mStickPoint[0].y);
                path.quadTo(mControlPoint.x, mControlPoint.y, mDragPoint[0].x, mDragPoint[0].y);
                path.lineTo(mDragPoint[1].x, mDragPoint[1].y);
                path.quadTo(mControlPoint.x, mControlPoint.y, mStickPoint[1].x, mStickPoint[1].y);
                path.close();

                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);//设置抗锯齿的属性
                int[] colors = new int[]{
                        Color.RED,
                        Color.GRAY,
                        Color.YELLOW,
                        Color.GREEN,
                        Color.BLUE
                };
                Shader shader = new LinearGradient(0, 0, 10, 10, colors, null, Shader.TileMode.REPEAT);
                paint.setShader(shader);

                canvas.drawCircle(mDragPoint[0].x, mDragPoint[0].y, 3.0f, paint);
                canvas.drawCircle(mDragPoint[1].x, mDragPoint[1].y, 3.0f, paint);
                canvas.drawCircle(mStickPoint[0].x, mStickPoint[0].y, 3.0f, paint);
                canvas.drawCircle(mStickPoint[1].x, mStickPoint[1].y, 3.0f, paint);

                //绘制贝塞尔曲线
                canvas.drawPath(path, mPaint);

                //绘制gudingyu固定圆
                canvas.drawCircle(mStickCenterPoint.x, mStickCenterPoint.y, tempStickRadius, mPaint);
            }
            //绘制拖拽圆
            canvas.drawCircle(mDragCenterPoint.x, mDragCenterPoint.y, mDragRadius, mPaint);
            canvas.drawText("23", mDragCenterPoint.x, mDragCenterPoint.y + mDragRadius / 3.0f, mTextPaint);
        }
        //恢复到画布刚刚保存的状态
        canvas.restore();
    }

    private float getStickRadius() {
        float distance = GeometryUtils.getDistanceBetween2Point(mStickCenterPoint, mDragCenterPoint);
        distance = Math.min(distance, maxDistance);
        float percent = distance / maxDistance;
        FloatEvaluator evaluator = new FloatEvaluator();
        Float evaluate = evaluator.evaluate(percent, 1.0f, 0.2f);
        System.out.println("percent------------------------" + percent);
        return evaluate * mStickRadius;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                isOutOfRange = false;
                disappear = false;
                float downX = event.getRawX();
                float downY = event.getRawY();
                updateDragCenter(downX, downY);

                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getRawX();
                float moveY = event.getRawY();
                updateDragCenter(moveX, moveY);

                //实时判断拖拽圆和固定圆两圆的的圆心距与最大距离的比较
                if (GeometryUtils.getDistanceBetween2Point(mDragCenterPoint, mStickCenterPoint) > maxDistance) {
                    isOutOfRange = true;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                //表示已经超出范围了
                if (isOutOfRange) {
                    //实时的判断是否超出范围
                    if (GeometryUtils.getDistanceBetween2Point(mDragCenterPoint, mStickCenterPoint) > maxDistance) {
                        disappear = true;

                        if (mOnDragEventListener != null) {
                            mOnDragEventListener.onDisappear();
                        }
                        invalidate();
                    } else {
                        updateDragCenter(mStickCenterPoint.x, mStickCenterPoint.y);
                        if (mOnDragEventListener != null) {
                            mOnDragEventListener.onReset(true);
                        }
                    }
                } else {
                    final PointF lastPointCenter = new PointF(mDragCenterPoint.x, mDragCenterPoint.y);
                    //updateDragCenter(mStickCenterPoint.x,mStickCenterPoint.y);
                    ValueAnimator mValueAnimator = ValueAnimator.ofFloat(1.0f);
                    mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float fraction = animation.getAnimatedFraction();
                            PointF pointF = GeometryUtils.getPointByPercent(lastPointCenter, mStickCenterPoint, fraction);
                            updateDragCenter(pointF.x, pointF.y);
                        }
                    });
                    mValueAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                            if (mOnDragEventListener != null) {
                                mOnDragEventListener.onReset(true);
                            }
                        }
                    });
                    //mValueAnimator.setInterpolator(new CycleInterpolator(4));
                    mValueAnimator.setInterpolator(new OvershootInterpolator());
                    mValueAnimator.setDuration(200);
                    mValueAnimator.start();
                }
                break;
        }
        return true;
    }

    /**
     * 更新拖拽圆的圆心坐标
     *
     * @param x
     * @param y
     */
    private void updateDragCenter(float x, float y) {
        mDragCenterPoint.set(x, y);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Rect rect = new Rect();
        this.getWindowVisibleDisplayFrame(rect);
        statusHeight = rect.top;
        System.out.println("statusHeight-------------------------" + statusHeight);
    }
}
