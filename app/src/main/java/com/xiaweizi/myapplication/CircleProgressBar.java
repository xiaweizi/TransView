package com.xiaweizi.myapplication;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * <pre>
 *     author : xiaweizi
 *     class  : com.xiaweizi.myapplication.CircleProgressBar
 *     e-mail : 1012126908@qq.com
 *     time   : 2019/06/28
 *     desc   :
 * </pre>
 */
public class CircleProgressBar extends View {

    private static final String TAG = "CircleProgressBar::";

    private static final int TOTAL_PROGRESS = 100;
    private static final int MASK_DURATION = 1000; // 发光动画展示时长
    private static final int PULSE_DURATION = 2333; // 脉冲动画时长
    private int mCurrentProgress;
    private Paint mBottomPaint;
    private Paint mProgressPaint;
    private Paint mTextPaint;
    private Paint mMaskPaint;
    private float mRadius = 55;
    private float mMarginRight = 60;
    private float mMarginTop = 70;
    private RectF mRectF = new RectF();
    private SweepGradient mGradient;
    private Matrix localM;
    private ValueAnimator mMaskAlphaAnimator;
    private ValueAnimator mPulseAnimator;
    private float centerX;
    private float centerY;
    private float value;
    private float mMaskAlphaValue;

    public void setCurrentProgress(int progress) {
        this.mCurrentProgress = progress;
        invalidate();
        Log.i(TAG, "setCurrentProgress: " + progress);
    }

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs, defStyle);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private void initAnimator() {
        mMaskAlphaAnimator = ValueAnimator.ofFloat(0, 0.3f, 0.8f, 1, 0.95f, 0.9f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.1f, 0.08f, 0.04f, 0).setDuration(MASK_DURATION);
        mMaskAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMaskAlphaValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        // 脉冲动画
        mPulseAnimator = ValueAnimator.ofFloat(0, 1).setDuration(PULSE_DURATION);
        mPulseAnimator.setRepeatMode(ValueAnimator.RESTART);
        mPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mPulseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                value = (float) animation.getAnimatedValue();
                int temValue = (int) (value * 100);
                invalidate();
                // 如果脉冲值到达 80% 则展示发光圆环
                if (temValue == 80 && !mMaskAlphaAnimator.isRunning()) {
                    mMaskAlphaAnimator.start();
                }
            }
        });
        mPulseAnimator.start();
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        int mStartColor = getResources().getColor(R.color.start_color);
        int mEndColor = getResources().getColor(R.color.end_color);
        int[] colors = new int[]{mStartColor, mEndColor, mStartColor};
        localM = new Matrix();
        float[] positions = new float[]{0.3f, 0.7f, 1};
        mGradient = new SweepGradient(0, 0, colors, positions);
        mMarginRight = 60;
        mMarginTop = 70;
        initPaint();
        initAnimator();
    }

    private void initPaint() {
        mBottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mBottomPaint.setStyle(Paint.Style.STROKE);
        mBottomPaint.setStrokeWidth(15);
        mBottomPaint.setColor(getResources().getColor(R.color.bottom_color));
        mBottomPaint.setStrokeCap(Paint.Cap.ROUND);

        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(15);
        mProgressPaint.setShader(mGradient);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(26);

        mMaskPaint.setStyle(Paint.Style.STROKE);
        mMaskPaint.setStrokeWidth(15);
        mMaskPaint.setColor(getResources().getColor(R.color.mask_color));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        Log.i(TAG, "mWidth: " + width + " mHeight: " + height);
        mRectF.set(width - mRadius * 2 - mMarginRight, mMarginTop, width - mMarginRight, mMarginTop + mRadius * 2);
        centerX = width - mRadius - mMarginRight;
        centerY = mMarginTop + mRadius;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBottomRing(canvas);
        drawProgressRing(canvas);
        drawCenterText(canvas);
        drawMaskCircle(canvas);
    }

    private Path mPath = new Path();

    /**
     * 绘制底部基础的圆环
     */
    private void drawBottomRing(Canvas canvas) {
        canvas.drawArc(mRectF, 360, 360, false, mBottomPaint);
    }

    /**
     * 绘制高亮
     */
    private void drawMaskCircle(Canvas canvas) {
        mMaskPaint.setAlpha((int) (255 * mMaskAlphaValue));
        mMaskPaint.setShadowLayer(12, 0, 0, Color.WHITE);
        canvas.drawCircle(centerX, centerY, mRadius, mMaskPaint);
    }


    /**
     * 绘制进度圆环
     */
    private void drawProgressRing(Canvas canvas) {
        int id = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        mProgressPaint.setShader(null);
        mProgressPaint.setStrokeWidth(15);
        float sweepAngle = mCurrentProgress * 1f / TOTAL_PROGRESS * 360;
        canvas.drawArc(mRectF, -90, sweepAngle, false, mProgressPaint);
        mPath.reset();
        canvas.save();
        canvas.rotate(360 * value, centerX, centerY);
        mProgressPaint.setShader(mGradient);
        localM.setTranslate(centerX, centerY);
        mGradient.setLocalMatrix(localM);
        mPath.addCircle(centerX, centerY, mRadius, Path.Direction.CCW);
        mProgressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        mProgressPaint.setStrokeWidth(20);
        canvas.drawPath(mPath, mProgressPaint);
        mProgressPaint.setXfermode(null);
        canvas.restore();
        canvas.restoreToCount(id);
    }

    /**
     * 绘制中心文字
     */
    private void drawCenterText(Canvas canvas) {
        canvas.drawText(mCurrentProgress + " %", centerX, centerY - ((mTextPaint.descent() + mTextPaint.ascent()) / 2), mTextPaint);
    }

}
