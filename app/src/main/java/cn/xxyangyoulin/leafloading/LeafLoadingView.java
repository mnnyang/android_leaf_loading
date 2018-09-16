package cn.xxyangyoulin.leafloading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

    private int mProgressPadding = 18;

    private int mTotalProgress = 100;
    private int mCurrentProgress = 5;
    private boolean mIsFinish = false;


    private int mBackgroundColor = 0xFFFCE49B;
    private int mProgressColor = 0xFFFFA800;
    private int mFanColor = Color.WHITE;
    private int mFanInColor = 0xFFFDCA48;


    private Paint mBgPaint;
    private Paint mProgressPaint;
    private Paint mLinePaint;
    private Paint mFanPaint;
    private Paint mFanFillPaint;

    private RectF mBgRect;
    private RectF mSemiCircleRectF;
    private int mProgressBarWidth;
    private int mSemicircleRadius;
    private RectF mProgressRectF;
    private float mCurrentProgressWidth;

    private Path mFanLeafPath;

    private int mFanCenterRadius = 4;
    private int fanLeafInMargin = 10;
    private int fanLeafOutMargin = 20;

    private int mFanRotateDirection = 1;
    private Path mLeafPath;
    private List<Leaf> mLeafPathArray;

    private int mLeafOnceCycleTime = 2000;

    private Random mRandom = new Random();
    private Paint mTextPaint;
    private int mTextMaxSize = 50;
    private int mFanCircleWidth = 8;

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

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStrokeWidth(1);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mFanColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mProgressPadding = (int) (mHeight * 18f / 144 + 0.5f);
        mFanCircleWidth = (int) (mHeight * 8f / 144 + 0.5f);

        mTextMaxSize = (int) (mHeight * 48f / 144 + 0.5f);

        double v = Math.sqrt(Math.pow(mHeight / 2, 2) - Math.pow(mHeight / 2 - mProgressPadding, 2));
        mSemicircleRadius = (int) ((mHeight - mProgressPadding * 2f) / 2);
        mProgressBarWidth = (int) (mWidth - mProgressPadding - mHeight / 2
                - v);


        System.out.println("----w" + w + "--h" + h + "--" + mSemicircleRadius + "--");
        initShape();
    }

    private void initShape() {
        mBgRect = new RectF(-mWidth / 2, -mHeight / 2, mWidth / 2, mHeight / 2);
        mProgressRectF = new RectF(0, -mSemicircleRadius, 0, mSemicircleRadius);
        mSemiCircleRectF = new RectF(-mSemicircleRadius, -mSemicircleRadius, mSemicircleRadius, mSemicircleRadius);

        mFanLeafPath = new Path();
        mFanLeafPath.moveTo(0, -fanLeafInMargin);
        mFanLeafPath.lineTo(-20, -mHeight / 2 + fanLeafOutMargin);
        mFanLeafPath.lineTo(20, -mHeight / 2 + fanLeafOutMargin);
        mFanLeafPath.close();

        mLeafPath = new Path();
        mLeafPath.addRect(-12, -12, 12, 12, Path.Direction.CCW);

        mLeafPath.close();

        mLeafPathArray = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Leaf e = new Leaf(new Random().nextInt(360));

            int randomType = mRandom.nextInt(3);
            // 随时类型－ 随机振幅
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
            e.type = type;

            e.startTime = System.currentTimeMillis() + mRandom.nextInt(mLeafOnceCycleTime);

            mLeafPathArray.add(e);
        }
    }

    static class Leaf {
        int x;
        int y;
        int angle;
        int startAngle;
        long startTime;
        StartType type;

        Leaf(int startAngle) {
            this.startAngle = startAngle;
        }
    }

    private enum StartType {
        LITTLE, MIDDLE, BIG
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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
            canvas.rotate(leaf.angle);


            canvas.drawPath(mLeafPath, mProgressPaint);
            canvas.restore();
        }

        canvas.restore();
    }

    private void setLeafLocation(Leaf leaf) {

        leaf.angle += 5;


        long currentTimeMillis = System.currentTimeMillis();
        long timeDiff = currentTimeMillis - leaf.startTime;

        System.out.println("差值：" + timeDiff);

        /*未到出场时间*/
        if (timeDiff < 0) {
            return;
        }

        /*到达终点*/
        if (timeDiff > mLeafOnceCycleTime) {
            leaf.x = 0;
            leaf.y = 0;
            leaf.startTime += mLeafOnceCycleTime + mRandom.nextInt(1000);
            return;
        }

        /*0 ~ leafOnceCycleTime 之间 - 在飞行途中*/
        leaf.x = -(int) ((mWidth - mProgressPadding - 25 - mSemicircleRadius) * timeDiff * 1f / mLeafOnceCycleTime);
        leaf.y = getLocationY(leaf) - mHeight / 4;

    }

    // 中等振幅大小
    private int mMiddleAmplitude = 18;
    // 振幅差
    private int mAmplitudeDisparity = 8;

    // 通过叶子信息获取当前叶子的Y值
    private int getLocationY(Leaf leaf) {
        // y = A(wx+Q)+h
        float w = (float) ((float) 2 * Math.PI / mProgressBarWidth);
        float a = mMiddleAmplitude;
        switch (leaf.type) {
            case LITTLE:
                // 小振幅 ＝ 中等振幅 － 振幅差
                a = mMiddleAmplitude - mAmplitudeDisparity;
                break;
            case MIDDLE:
                a = mMiddleAmplitude;
                break;
            case BIG:
                // 小振幅 ＝ 中等振幅 + 振幅差
                a = mMiddleAmplitude + mAmplitudeDisparity;
                break;
            default:
                break;
        }
        return (int) (a * Math.sin(w * leaf.x)) + mSemicircleRadius * 3 / 4;
    }

    int fanRotateAngel = 30;
    private int FanScaleTime = 150;

    private float mFanLeafScaleValue = 1;

    private void drawFan(Canvas canvas, boolean scale) {
        canvas.save();
        canvas.translate(mWidth - mHeight / 2, mHeight / 2);

        canvas.drawCircle(0, 0, mHeight / 2, mFanPaint);
        canvas.drawCircle(0, 0, mHeight / 2 - mFanCircleWidth, mFanFillPaint);

        if (scale) {
            canvas.save();
            canvas.scale(mFanLeafScaleValue, mFanLeafScaleValue);
        }

        if (mFanLeafScaleValue > 0.3) {
            canvas.drawCircle(0, 0, mFanCenterRadius, mFanPaint);
            canvas.rotate(-fanRotateAngel);
            for (int i = 0; i < 4; i++) {
                canvas.drawPath(mFanLeafPath, mFanPaint);
                canvas.rotate(90);
            }
        }

        if (scale) {
            canvas.restore();

            if (mFanLeafScaleValue < 0.5f) {
                draw100Text(canvas);
            }

            if (mFanLeafScaleValue >= 0) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        invalidate();
                    }
                }, FanScaleTime / 10);
            }
            mFanLeafScaleValue -= 0.05;
        }

        updateFanRotate(1);

        canvas.restore();
    }

    private void draw100Text(Canvas canvas) {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom

        int baseLineY = (int) (mSemiCircleRectF.centerY() - top / 2 - bottom / 2);//基线中间点的y轴计算公式

        mTextPaint.setTextSize(mTextMaxSize * (1 - mFanLeafScaleValue));
        canvas.drawText("100%", mSemiCircleRectF.centerX(), baseLineY, mTextPaint);
    }

    private void drawProgress(Canvas canvas) {

        mCurrentProgressWidth = (mProgressBarWidth * mCurrentProgress * 1.0f / mTotalProgress);

        canvas.save();
        canvas.translate(mSemicircleRadius + mProgressPadding, mHeight / 2);

        if (mCurrentProgressWidth > 0 &&
                /*只花半圆*/
                mCurrentProgressWidth < mSemicircleRadius) {

            float degrees = (float) Math.toDegrees(Math.acos((
                    mSemicircleRadius - mCurrentProgressWidth) * 1f / mSemicircleRadius));

            canvas.drawArc(mSemiCircleRectF, 90 + 90 - degrees, 2 * degrees,
                    false, mProgressPaint);
        } else if (mCurrentProgressWidth >= mSemicircleRadius) {
            /*半圆加矩形*/
            canvas.drawArc(mSemiCircleRectF, 90, 180, false, mProgressPaint);

            mProgressRectF.right = mCurrentProgressWidth - mSemicircleRadius;
            canvas.drawRect(mProgressRectF, mProgressPaint);
        }

        canvas.restore();
    }

    private void drawBackground(Canvas canvas) {
        canvas.save();
        canvas.translate(mWidth / 2, mHeight / 2);
        canvas.drawRoundRect(mBgRect, mHeight / 2, mHeight / 2, mBgPaint);

        canvas.restore();
    }


    public void setCurrentProgress(int currentProgress) {
        if (currentProgress >= mTotalProgress) {
            mIsFinish = true;
        } else {
            mIsFinish = false;
            mFanLeafScaleValue = 1f;
        }
        updateFanRotate(6);
        mCurrentProgress = currentProgress;

        postInvalidate();
    }

    private void updateFanRotate(int margin) {
        fanRotateAngel += (margin * mFanRotateDirection);
        if (fanRotateAngel == 360) {
            fanRotateAngel = 0;
        }
    }
}
