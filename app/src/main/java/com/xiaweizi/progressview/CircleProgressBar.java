package com.xiaweizi.progressview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

/**
 * <pre>
 *     class  : com.xiaweizi.myapplication.CircleProgressBar
 *     time   : 2019/06/28
 *     desc   : 右上角进度
 * </pre>
 */
public class CircleProgressBar extends View {

    private static final String TAG = "CircleProgressBar::";

    private static final int TOTAL_PROGRESS = 100; // 总进度
    private static final int MASK_DURATION = 1000; // 发光动画展示时长
    private static final int MASK_SHOW_VALUE_SEND = 65; // 发送触发发光的阈值
    private static final int MASK_SHOW_VALUE_RECEIVER = 80; // 接收触发发光的阈值
    private int mCurrentProgress = 0; // 当前进度
    private Paint mBottomPaint; // 底部环
    private Paint mCirclePaint; // 底部圆圈背景
    private Paint mProgressPaint; // 进度环
    private Paint mTextPaint; // 中间文本
    private Paint mMaskPaint; // 发光环
    private float mRadius; // 半径
    private float mMarginRight; // 距离右边的 margin
    private float mMarginTop; // 距离顶部的 margin
    private float mStrokeWidth; // 边缘宽度
    private RectF mRectF = new RectF(); // 确定绘制的区域
    private SweepGradient mGradient; // 脉冲渐变实现类
    private Matrix mLocalM; // 脉冲辅助矩阵
    private ValueAnimator mMaskAlphaAnimator; // 脉冲闪烁动画
    private float mCenterX; // 圆心点x
    private float mCenterY; // 圆心点y
    private float mPulseValue; // 控制脉冲旋转的值
    private float mMaskAlphaValue; // 控制闪光 view 的透明度
    private Bitmap mMaskBitmap; // 发光的 bitmap
    private int mRecoverColor; // 接收背景颜色
    private int mInterruptColor; // 发送背景颜色
    private float mRatio = 0; // 颜色过渡渐变的比例，用来展示背景的颜色
    private boolean mSendMode = false; // 是否是发送模式
    private PorterDuffXfermode mXfermode; // 重叠模仿，避免在 onDraw 中频繁创建对象
    private int mScreenWidth; // 屏幕宽度

    public void setCurrentProgress(int progress) {
        this.mCurrentProgress = progress;
        invalidate();
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
    }

    private void initAnimator() {
        mMaskAlphaAnimator = ValueAnimator.ofFloat(0, 0.3f, 0.8f, 1, 0.95f, 0.9f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.1f, 0.08f, 0.04f, 0);
        mMaskAlphaAnimator.setDuration(MASK_DURATION);
        mMaskAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMaskAlphaValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    public void setSend(boolean isSend) {
        mSendMode = isSend;
        if (isSend) {
            mRecoverColor = getResources().getColor(R.color.background_send);
        } else {
            mRecoverColor = getResources().getColor(R.color.background_receiver);
        }
    }

    /**
     * @param value 脉冲进度值
     * @param isShowMask 是否展示发光图片
     */
    public void refreshValue(float value, boolean isShowMask) {
        this.mPulseValue = value;
        int temValue = (int) (value * 100);
        invalidate();
        // 如果 (sendMode && >= 65) || (!sendMode && >= 80) 则展示发光圆环
        if (isShowMask && !mMaskAlphaAnimator.isRunning()) {
            if (mSendMode && temValue >= MASK_SHOW_VALUE_SEND) {
                mMaskAlphaAnimator.start();
            } else if (!mSendMode && temValue >= MASK_SHOW_VALUE_RECEIVER) {
                mMaskAlphaAnimator.start();
            }
        }
    }

    public void refreshBgColor(float radio) {
        this.mRatio = radio;
        invalidate();
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        mScreenWidth = getScreenWidth();
        int startColor = getResources().getColor(R.color.transfer_start_color);
        int endColor = getResources().getColor(R.color.transfer_end_color);
        mRecoverColor = getResources().getColor(R.color.background_receiver);
        mInterruptColor = getResources().getColor(R.color.background_pause);
        int[] colors = new int[]{startColor, endColor, startColor};
        mLocalM = new Matrix();
        float[] positions = new float[]{0.3f, 0.7f, 1};
        mGradient = new SweepGradient(0, 0, colors, positions);
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        mMarginRight = getActualValue(80);
        mMarginTop = getActualValue(180);
        mRadius = getActualValue(70);
        mStrokeWidth = getActualValue(20);
        initPaint();
        initAnimator();
    }

    private void initPaint() {
        mBottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCirclePaint.setColor(getResources().getColor(R.color.background_receiver));
        mCirclePaint.setStrokeWidth(mStrokeWidth);

        mBottomPaint.setStyle(Paint.Style.STROKE);
        mBottomPaint.setStrokeWidth(mStrokeWidth);
        mBottomPaint.setColor(getResources().getColor(R.color.bottom_ring_color));
        mBottomPaint.setStrokeCap(Paint.Cap.ROUND);

        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mStrokeWidth);
        mProgressPaint.setShader(mGradient);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(getActualValue(36));
    }

    private void createBitmap() {
        if (mMaskBitmap == null) {
            mMaskBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.transfer_mask_image);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int width = getMeasuredWidth();
        mRectF.set(width - mRadius * 2 - mMarginRight, mMarginTop, width - mMarginRight, mMarginTop + mRadius * 2);
        mCenterX = width - mRadius - mMarginRight;
        mCenterY = mMarginTop + mRadius;
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
        mCirclePaint.setColor(getBgColor(mRatio));
        canvas.drawCircle(mCenterX, mCenterY, mRadius + getActualValue(12), mCirclePaint);
        canvas.drawArc(mRectF, 360, 360, false, mBottomPaint);
    }

    /**
     * 绘制高亮
     */
    private void drawMaskCircle(Canvas canvas) {
        mMaskPaint.setAlpha((int) (255 * mMaskAlphaValue));
        createBitmap();
        canvas.save();
        Matrix m = new Matrix();
        float scale = (mRadius * 2 + getActualValue(70)) / mMaskBitmap.getWidth();
        m.setScale(scale, scale);
        canvas.translate(mCenterX - (mMaskBitmap.getWidth() >> 1) * scale, mCenterY - (mMaskBitmap.getHeight() >> 1) * scale);
        canvas.drawBitmap(mMaskBitmap, m, mMaskPaint);
        canvas.restore();
    }


    /**
     * 绘制进度圆环
     */
    private void drawProgressRing(Canvas canvas) {
        int id = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        mProgressPaint.setShader(null);
        mProgressPaint.setStrokeWidth(mStrokeWidth);
        float sweepAngle = mCurrentProgress * 1f / TOTAL_PROGRESS * 360;
        canvas.drawArc(mRectF, -90, sweepAngle, false, mProgressPaint);
        canvas.save();
        // 通过控制 mPulseValue 的值，来完成脉冲的旋转
        canvas.rotate(360 * mPulseValue, mCenterX, mCenterY);
        mProgressPaint.setShader(mGradient);
        mLocalM.setTranslate(mCenterX, mCenterY);
        mGradient.setLocalMatrix(mLocalM);
        mPath.reset();
        mPath.addCircle(mCenterX, mCenterY, mRadius, Path.Direction.CCW);
        mProgressPaint.setXfermode(mXfermode);
        mProgressPaint.setStrokeWidth(mStrokeWidth + 5); // 防止看到黑色边框
        canvas.drawPath(mPath, mProgressPaint);
        mProgressPaint.setXfermode(null);
        canvas.restore();
        canvas.restoreToCount(id);
    }

    /**
     * 绘制中心文字
     */
    private void drawCenterText(Canvas canvas) {
        canvas.drawText(mCurrentProgress + " %", mCenterX, mCenterY - ((mTextPaint.descent() + mTextPaint.ascent()) / 2), mTextPaint);
    }

    private int getBgColor(float radio) {
        int redStart = Color.red(mRecoverColor);
        int blueStart = Color.blue(mRecoverColor);
        int greenStart = Color.green(mRecoverColor);
        int redEnd = Color.red(mInterruptColor);
        int blueEnd = Color.blue(mInterruptColor);
        int greenEnd = Color.green(mInterruptColor);

        int red = (int) (redStart + ((redEnd - redStart) * radio + 0.5));
        int greed = (int) (greenStart + ((greenEnd - greenStart) * radio + 0.5));
        int blue = (int) (blueStart + ((blueEnd - blueStart) * radio + 0.5));
        return Color.argb(255,red, greed, blue);
    }

    public int getScreenWidth() {
        if (mScreenWidth != 0) return mScreenWidth;
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point outSize = new Point();
        if (wm == null) return 0;
        wm.getDefaultDisplay().getSize(outSize);
        return outSize.x;
    }

    public float getActualValue(float srcValue) {
        return srcValue * mScreenWidth / 1080;
    }
}
