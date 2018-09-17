package cn.xxyangyoulin.leafloading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LeafLoadingView extends View {

    private int mWidth;
    private int mHeight;

    private Paint mBgPaint;
    private Paint mProgressPaint;
    private Paint mFanPaint;
    private Paint mFanFillPaint;
    private Paint mTextPaint;

    private Random mRandom = new Random();

    /**
     * 进度条距离背景边缘
     */
    private int mProgressPadding = 18;
    /**
     * 总进度
     */
    private int mTotalProgress = 100;
    /**
     * 当前进度
     */
    private int mCurrentProgress = 5;
    /**
     * 是否结束
     */
    private boolean mIsFinish = false;
    /**
     * 背景条颜色
     */
    private int mBackgroundColor = 0xFFFCE49B;
    /**
     * 进度条颜色
     */
    private int mProgressColor = 0xFFFFA800;
    /**
     * 风扇叶颜色
     */
    private int mFanColor = 0xFFFFFFFF;
    /**
     * 风扇内部填充颜色
     */
    private int mFanInColor = 0xFFFDCA48;
    /**
     * 背景矩形
     */
    private RectF mBgRect;
    /**
     * 进度条矩形
     */
    private RectF mProgressRectF;
    /**
     * 左边半圆矩形 用来画弧
     */
    private RectF mSemiCircleRectF;
    /**
     * 进度条总宽度
     */
    private int mProgressBarWidth;
    /**
     * 半圆半径
     */
    private int mSemicircleRadius;

    /**
     * 风扇叶
     */
    private Path mFanLeafPath;
    /**
     * 叶子
     */
    private Path mLeafPath;
    private List<Leaf> mLeafPathArray;
    /**
     * 叶子宽度
     */
    private int mLeafWidth = 66;

    private int mFanCenterRadius = 4;/*风扇中心圆点半径*/
    private int fanLeafInMargin = 10;/*风扇叶距离内部*/
    private int fanLeafOutMargin = 20;/*风扇叶距离外部*/
    /**
     * 风扇旋转方向
     */
    private int mFanRotateDirection = 1;
    /**
     * 风扇叶外圆边宽度
     */
    private int mFanCircleWidth = 8;
    /**
     * 叶子风行周期
     */
    private int mLeafOnceCycleTime = 1500;
    /**
     * 叶子数量
     */
    private int mLeafCount = 7;
    /**
     * 100% 字体大小
     */
    private int mTextMaxSize;

    private int mMiddleAmplitude = 18;/*中等振幅大小*/

    private int mAmplitudeDisparity = 8;/*振幅差*/

    private int fanRotateAngel = 30;/*风扇当前的旋转角度*/
    private int FanScaleTime = 150;/*风扇及其字体缩放动画时间*/
    private float mFanLeafScaleValue = 1;/*风扇缩放比例*/
    private int mTextBaseLineY;


    public LeafLoadingView(Context context) {
        super(context);
        init();
    }

    public LeafLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        initPaint();
    }

    private void initPaint() {
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(mBackgroundColor);

        mFanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFanPaint.setStyle(Paint.Style.FILL);
        mFanPaint.setColor(mFanColor);

        mFanFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFanFillPaint.setStyle(Paint.Style.FILL);
        mFanFillPaint.setColor(mFanInColor);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.FILL);
        mProgressPaint.setColor(mProgressColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mFanColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        /*根据view的高度来计算其他值*/
        mProgressPadding = (int) (mHeight * 1f / 8 + 0.5f);
        mFanCircleWidth = (int) (mHeight * 1f / 16 + 0.5f);
        mTextMaxSize = (int) (mHeight * 21f / 64 + 0.5f);
        mLeafWidth = (int) (mHeight * 3f / 8 + 0.5f);


        double leftMargin = Math.sqrt(Math.pow(mHeight / 2, 2) - Math.pow(mHeight / 2 - mProgressPadding, 2));

        mSemicircleRadius = (int) ((mHeight - mProgressPadding * 2f) / 2);
        mProgressBarWidth = (int) (mWidth - mProgressPadding - mHeight / 2 - leftMargin);

        initShape();
        initLeafArray();
    }

    private void initShape() {
        /*我们将会把画布移动到view的中心去画背景矩形*/
        mBgRect = new RectF(-mWidth / 2, -mHeight / 2, mWidth / 2, mHeight / 2);

        mProgressRectF = new RectF(0, -mSemicircleRadius, 0, mSemicircleRadius);

        mSemiCircleRectF = new RectF(-mSemicircleRadius, -mSemicircleRadius, mSemicircleRadius, mSemicircleRadius);

        initFanLeafPath();
        initLeafPath();

    }

    /**
     * 初始化叶子的路径
     */
    private void initLeafPath() {
        mLeafPath = new Path();
        mLeafPath.moveTo(-1 / 20f * mLeafWidth, 4 / 10f * mLeafWidth);
        mLeafPath.lineTo(1 / 40f * mLeafWidth, 4 / 10f * mLeafWidth);
        mLeafPath.lineTo(1 / 20f * mLeafWidth, 2 / 10f * mLeafWidth);
        mLeafPath.cubicTo(
                1 / 3f * mLeafWidth, 0,
                1 / 4f * mLeafWidth, -2 / 5f * mLeafWidth,
                0, -1 / 2f * mLeafWidth);

        mLeafPath.cubicTo(
                -1 / 4f * mLeafWidth, -2 / 5f * mLeafWidth,
                -1 / 3f * mLeafWidth, 0,
                -1 / 20f * mLeafWidth, 2 / 10f * mLeafWidth);

        mLeafPath.close();
    }

    /**
     * 初始化风扇叶的路径 只包含一个风扇叶的路径
     */
    private void initFanLeafPath() {
        /*风扇叶距离中心的高度*/
        int fanLeafTop = mHeight / 2 - mFanCircleWidth - fanLeafOutMargin / 2;
        int fanLeafRectWidth = mHeight / 2 - mFanCircleWidth;

        mFanLeafPath = new Path();
        mFanLeafPath.moveTo(0, -fanLeafInMargin);
        mFanLeafPath.cubicTo(fanLeafRectWidth / 4f, -fanLeafRectWidth / 3f,
                fanLeafRectWidth / 2f, -fanLeafRectWidth + fanLeafOutMargin / 2,
                0, -fanLeafTop);
        mFanLeafPath.cubicTo(-fanLeafRectWidth / 2f, -fanLeafRectWidth + fanLeafOutMargin / 2,
                -fanLeafRectWidth / 4f, -fanLeafRectWidth / 3f,
                0, -fanLeafInMargin);

        mFanLeafPath.close();
    }

    private void initLeafArray() {
        if (mLeafPathArray == null) {
            mLeafPathArray = new ArrayList<>();
        } else {
            mLeafPathArray.clear();
        }

        for (int i = 0; i < mLeafCount; i++) {
            Leaf leaf = new Leaf();

            leaf.angle = mRandom.nextInt(360);
            leaf.direction = mRandom.nextInt(2);
            int randomType = mRandom.nextInt(3);

            /*随时类型－ 随机振幅*/
            StartType type = StartType.MIDDLE;
            switch (randomType) {
                case 0:
                    break;
                case 1:
                    type = StartType.LITTLE;
                    break;
                case 2:
                    type = StartType.BIG;
                    break;
                default:
                    break;
            }
            leaf.type = type;

            leaf.startTime = System.currentTimeMillis() + mRandom.nextInt(mLeafOnceCycleTime);

            mLeafPathArray.add(leaf);
        }
    }

    static class Leaf {
        int x;
        int y;
        int angle;
        long startTime;
        int direction;/*旋转方向*/
        StartType type;
    }

    private enum StartType {
        LITTLE, MIDDLE, BIG
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        System.out.println("绘制中    ");

        drawBackground(canvas);
        drawLeaf(canvas);
        drawProgress(canvas);

        if (mIsFinish) {
            drawFan(canvas, true);
        } else {
            drawFan(canvas, false);
            invalidate();
        }
    }

    private void drawLeaf(Canvas canvas) {
        canvas.save();
        canvas.translate(mWidth - mSemicircleRadius, mHeight / 2);
        for (Leaf leaf : mLeafPathArray) {

            canvas.save();
            setLeafLocation(leaf);
            canvas.translate(leaf.x, leaf.y);
            /*旋转叶子的角度*/
            canvas.rotate(leaf.angle);

            canvas.drawPath(mLeafPath, mProgressPaint);
            canvas.restore();
        }

        canvas.restore();
    }

    /**
     * 设置叶子的坐标
     */
    private void setLeafLocation(Leaf leaf) {
        /*根据叶子的旋转方向，修改旋转度数*/
        leaf.angle += ((leaf.direction == 0) ? 5 : -5);

        long currentTimeMillis = System.currentTimeMillis();
        /*计算当前时间和叶子出场时间的差值*/
        long timeDiff = currentTimeMillis - leaf.startTime;

        /*1. 未到出场时间*/
        if (timeDiff < 0) {
            return;
        }

        /*2. 到达终点*/
        if (timeDiff > mLeafOnceCycleTime) {
            leaf.x = 0;
            leaf.y = 0;
            /*重置坐标到原点，并且把开始时间加上一个周期，再加一个随机值避免每个周期出场时间都一样*/
            leaf.startTime += mLeafOnceCycleTime + mRandom.nextInt(1000);
            return;
        }

        /*3. 在飞行途中*/
        /*按照时间比例，计算x*/
        leaf.x = -(int) ((mWidth - mProgressPadding - mLeafWidth / 2 - mSemicircleRadius)
                * timeDiff * 1f / mLeafOnceCycleTime);

        leaf.y = getLocationY(leaf) - mHeight / 4;

    }

    /*通过叶子信息获取当前叶子的Y值*/
    private int getLocationY(Leaf leaf) {
        // y = A(wx+Q)+h
        float w = (float) ((float) 2 * Math.PI / mProgressBarWidth);
        float a = mMiddleAmplitude;
        switch (leaf.type) {
            case LITTLE:
                /*小振幅 ＝ 中等振幅 － 振幅差*/
                a = mMiddleAmplitude - mAmplitudeDisparity;
                break;
            case MIDDLE:
                a = mMiddleAmplitude;
                break;
            case BIG:
                /*小振幅 ＝ 中等振幅 + 振幅差*/
                a = mMiddleAmplitude + mAmplitudeDisparity;
                break;
            default:
                break;
        }
        return (int) (a * Math.sin(w * leaf.x)) + mSemicircleRadius * 3 / 4;
    }


    private void drawFan(Canvas canvas, boolean scale) {
        canvas.save();
        /*移动到风扇中心*/
        canvas.translate(mWidth - mHeight / 2, mHeight / 2);
        /*绘制外圆*/
        canvas.drawCircle(0, 0, mHeight / 2, mFanPaint);
        /*绘制内圆*/
        canvas.drawCircle(0, 0, mHeight / 2 - mFanCircleWidth, mFanFillPaint);

        /*在执行缩放动画*/
        if (scale) {
            canvas.save(); /*@1*/
            /*缩放画布*/
            canvas.scale(mFanLeafScaleValue, mFanLeafScaleValue);
        }

        /*风扇缩放比例小于30%的时候，不进行绘制*/
        if (mFanLeafScaleValue > 0.3) {
            canvas.drawCircle(0, 0, mFanCenterRadius, mFanPaint);
            canvas.rotate(-fanRotateAngel);
            for (int i = 0; i < 4; i++) {
                canvas.drawPath(mFanLeafPath, mFanPaint);
                canvas.rotate(90);
            }
        }

        /*在执行缩放动画*/
        if (scale) {
            canvas.restore();/*还原@1处的画布状态*/

            /*缩放比例小于0.5的时候，开始绘制100%文字*/
            if (mFanLeafScaleValue < 0.5f) {
                draw100Text(canvas);
            }

            /*比例未达到0的时候，更新画布 继续动画*/
            if (mFanLeafScaleValue > 0) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        invalidate();
                    }
                }, FanScaleTime / 10);
                mFanLeafScaleValue -= 0.05;
            }
        }

        /*更新风扇旋转值*/
        updateFanRotate(1);
        canvas.restore();
    }

    /**
     * 字体居中绘制处理
     */
    private void initTextBaseLine() {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float top = fontMetrics.top;/*为基线到字体上边框的距离*/
        float bottom = fontMetrics.bottom;/*为基线到字体下边框的距离*/

        /*基线中间点的y轴计算公式*/
        mTextBaseLineY = (int) (mSemiCircleRectF.centerY() - top / 2 - bottom / 2);
    }

    private void draw100Text(Canvas canvas) {
        /*设置字体的大小为： （1-风扇的缩放比例）*/
        mTextPaint.setTextSize(mTextMaxSize * (1 - mFanLeafScaleValue));
        initTextBaseLine();
        canvas.drawText("100%", mSemiCircleRectF.centerX(), mTextBaseLineY, mTextPaint);
    }

    /**
     * 绘制进度条
     */
    private void drawProgress(Canvas canvas) {
        /*获取当前进度条的宽度*/
        float currentProgressWidth = (mProgressBarWidth * mCurrentProgress * 1.0f / mTotalProgress);

        canvas.save();
        /*移动到左边半圆的圆心*/
        canvas.translate(mSemicircleRadius + mProgressPadding, mHeight / 2);

        /*进度还在半圆里面的时候，只花半圆*/
        if (currentProgressWidth > 0 && currentProgressWidth < mSemicircleRadius) {
            /*计算弧度夹角*/
            float degrees = (float) Math.toDegrees(Math.acos((
                    mSemicircleRadius - currentProgressWidth) * 1f / mSemicircleRadius));

            canvas.drawArc(mSemiCircleRectF, 180 - degrees, 2 * degrees,
                    false, mProgressPaint);
        } else if (currentProgressWidth >= mSemicircleRadius) {
            /*进度条大于半圆的时候，需要绘制半圆加矩形*/
            canvas.drawArc(mSemiCircleRectF, 90, 180, false, mProgressPaint);

            mProgressRectF.right = currentProgressWidth - mSemicircleRadius;
            canvas.drawRect(mProgressRectF, mProgressPaint);
        }

        canvas.restore();
    }

    /**
     * 绘制背景圆角矩形
     */
    private void drawBackground(Canvas canvas) {
        canvas.save();
        canvas.translate(mWidth / 2, mHeight / 2);
        canvas.drawRoundRect(mBgRect, mHeight / 2, mHeight / 2, mBgPaint);

        canvas.restore();
    }

    /**
     * 设置当前进度
     */
    public void setCurrentProgress(int currentProgress) {
        /*进度达到最大值的时候，标记完成，启动风扇缩小动画*/
        if (currentProgress >= mTotalProgress) {
            mIsFinish = true;
        } else {
            mIsFinish = false;
            mFanLeafScaleValue = 1f;
        }

        /*有进度的时候，加快风扇的旋转*/
        updateFanRotate(7);
        mCurrentProgress = currentProgress;

        postInvalidate();
    }

    /**
     * 更新风扇旋转角度
     */
    private void updateFanRotate(int margin) {
        fanRotateAngel += (margin * mFanRotateDirection);
        if (fanRotateAngel == 360) {
            fanRotateAngel = 0;
        }
    }

    /*-------------set/get---------------*/
    public int getTotalProgress() {
        return mTotalProgress;
    }

    public void setTotalProgress(int totalProgress) {
        mTotalProgress = totalProgress;
        postInvalidate();
    }

    public int getBgRectColor() {
        return mBackgroundColor;
    }

    public void setBgRectColor(int bgColor) {
        mBackgroundColor = bgColor;
        mBgPaint.setColor(mBackgroundColor);
        postInvalidate();
    }

    public int getProgressColor() {
        return mProgressColor;
    }

    public void setProgressColor(int progressColor) {
        mProgressColor = progressColor;
        mProgressPaint.setColor(mProgressColor);
        postInvalidate();
    }

    public int getFanColor() {
        return mFanColor;
    }

    public void setFanColor(int fanColor) {
        mFanColor = fanColor;
        mFanPaint.setColor(mFanColor);
        postInvalidate();
    }

    public int getFanInColor() {
        return mFanInColor;
    }

    public void setFanInColor(int fanInColor) {
        mFanInColor = fanInColor;
        mFanFillPaint.setColor(mFanInColor);
        postInvalidate();
    }

    public int getLeafOnceCycleTime() {
        return mLeafOnceCycleTime;
    }

    public void setLeafOnceCycleTime(int leafOnceCycleTime) {
        mLeafOnceCycleTime = leafOnceCycleTime;
        postInvalidate();
    }

    public int getLeafCount() {
        return mLeafCount;
    }

    public void setLeafCount(int leafCount) {
        mLeafCount = leafCount;
        initLeafArray();
        postInvalidate();
    }
}
