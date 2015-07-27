package com.example.testvistousview.Utils;

import android.animation.FloatEvaluator;
import android.graphics.PointF;

/**
 * 定义几何图形工具
 */
public class GeometryUtils {


    /**
     * 计算两点之间的距离
     *
     * @param p0 第一个点的坐标值
     * @param p1 第二个点的坐标值
     * @return 两点之间的距离
     */
    public static float getDistanceBetween2Point(PointF p0, PointF p1) {
        float yOffset = p0.y - p1.y;
        float xOffect = p0.x - p1.x;
        float distance = (float) Math.sqrt(Math.pow(yOffset, 2) + Math.pow(xOffect, 2));
        return distance;
    }

    /**
     * 计算两点连线的的中心坐标
     *
     * @param p0
     * @param p1
     * @return
     */
    public static PointF getMiddleBetween2Point(PointF p0, PointF p1) {
        float yOffset = p0.y - p1.y;
        float xOffect = p0.x - p1.x;
        return new PointF(xOffect / 2.0f, yOffset / 2.0f);
    }

    /**
     * 根据百分比获得两点连线上的某个位置的坐标
     *
     * @param p0
     * @param p1
     * @param percent
     * @return
     */
    public static PointF getPointByPercent(PointF p0, PointF p1, float percent) {
        FloatEvaluator mFloatEvaluator = new FloatEvaluator();
        Float xPoint = mFloatEvaluator.evaluate(percent, p0.x, p1.x);
        Float yPoint = mFloatEvaluator.evaluate(percent, p0.y, p1.y);
        return new PointF(xPoint, yPoint);
    }

    /**
     * 获取通过指定圆心且斜率为lineK的直线与圆的交点
     *
     * @param pMiddle
     * @param radius
     * @param lineK
     * @return
     */
    public static PointF[] getIntersectionPoints(PointF pMiddle, float radius, Double lineK) {
        PointF[] pointFs = new PointF[2];

        float radian, xOffset = 0, yOffset = 0;
        if (lineK != null) {
            radian = (float) Math.atan(lineK);
            xOffset = (float) (Math.sin(radian) * radius);
            yOffset = (float) (Math.cos(radian) * radius);

        } else {
            xOffset = radius;
            yOffset = 0;
        }

        pointFs[0] = new PointF(pMiddle.x + xOffset, pMiddle.y - yOffset);
        pointFs[1] = new PointF(pMiddle.x - xOffset, pMiddle.y + yOffset);

        return pointFs;
    }
}
