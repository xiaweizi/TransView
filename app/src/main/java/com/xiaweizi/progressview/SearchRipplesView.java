package com.xiaweizi.progressview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * <pre>
 *     class  : com.xiaweizi.myapplication.SearchRipplesView
 *     time   : 2019/07/02
 *     desc   : 搜索水波纹 view
 * </pre>
 */
public class SearchRipplesView extends View {

    private static final long ANIMATOR_DURATION = 2500;
    private static final float SINGLE_DURATION = ANIMATOR_DURATION * 1.0f / 4;
    private Paint mPaint;
    private int mCenterX;
    private int mCenterY;
    private float mRadius;
    private float value1;
    private float value2;
    private float value3;
    private float value4;
    private ValueAnimator valueAnimator1;
    private ValueAnimator valueAnimator2;
    private ValueAnimator valueAnimator3;
    private ValueAnimator valueAnimator4;

    public SearchRipplesView(Context context) {
        this(context, null);
    }

    public SearchRipplesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchRipplesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs, defStyle);
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        int rippleColor = getResources().getColor(R.color.ripple_color);
        initPaint(rippleColor);
    }

    private void initPaint(int rippleColor) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(rippleColor);
        mPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 初始化动画，使用三个动画循环执行
     */
    private void initAnimator() {
        if (valueAnimator1 == null) {
            valueAnimator1 = ValueAnimator.ofFloat(0, ANIMATOR_DURATION);
            valueAnimator1.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator1.setDuration(ANIMATOR_DURATION);
            valueAnimator1.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    value1 = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
        }
        if (valueAnimator2 == null) {
            valueAnimator2 = ValueAnimator.ofFloat(0, ANIMATOR_DURATION);
            valueAnimator2.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator2.setDuration(ANIMATOR_DURATION);
            valueAnimator2.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    value2 = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
        }
        if (valueAnimator3 == null) {
            valueAnimator3 = ValueAnimator.ofFloat(0, ANIMATOR_DURATION);
            valueAnimator3.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator3.setDuration(ANIMATOR_DURATION);
            valueAnimator3.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    value3 = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
        }
        if (valueAnimator4 == null) {
            valueAnimator4 = ValueAnimator.ofFloat(0, ANIMATOR_DURATION);
            valueAnimator4.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator4.setDuration(ANIMATOR_DURATION);
            valueAnimator4.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    value4 = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredWidth();
        mCenterX = getMeasuredWidth() >> 1;
        mCenterY = height >> 1;
        mRadius = getMeasuredWidth() >> 1;
        setMeasuredDimension(getMeasuredWidth(), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 透明度从 100% 到 0%，缩放比例
        float radius = value1 / ANIMATOR_DURATION * mRadius;
        int alpha = (int) ((1 - value1 / ANIMATOR_DURATION) * 255 * 0.15f);
        mPaint.setAlpha(alpha);
        canvas.drawCircle(mCenterX, mCenterY, radius, mPaint);

        radius = value2 / ANIMATOR_DURATION * mRadius;
        alpha = (int) ((1 - value2 / ANIMATOR_DURATION) * 255 * 0.15f);
        mPaint.setAlpha(alpha);
        canvas.drawCircle(mCenterX, mCenterY, radius, mPaint);

        radius = value3 / ANIMATOR_DURATION * mRadius;
        alpha = (int) ((1 - value3 / ANIMATOR_DURATION) * 255 * 0.15f);
        mPaint.setAlpha(alpha);
        canvas.drawCircle(mCenterX, mCenterY, radius, mPaint);

        radius = value4 / ANIMATOR_DURATION * mRadius;
        alpha = (int) ((1 - value4 / ANIMATOR_DURATION) * 255 * 0.15f);
        mPaint.setAlpha(alpha);
        canvas.drawCircle(mCenterX, mCenterY, radius, mPaint);
    }

    public void start() {
        initAnimator();
        valueAnimator1.start();
        valueAnimator2.setStartDelay((long) SINGLE_DURATION);
        valueAnimator2.start();
        valueAnimator3.setStartDelay((long) SINGLE_DURATION * 2);
        valueAnimator3.start();
        valueAnimator4.setStartDelay((long) SINGLE_DURATION * 3);
        valueAnimator4.start();
    }

    public void stop() {
        initAnimator();
        valueAnimator1.end();
        valueAnimator2.end();
        valueAnimator3.end();
        valueAnimator4.end();
    }

    public void resume() {
        valueAnimator1.resume();
        valueAnimator2.resume();
        valueAnimator3.resume();
        valueAnimator4.resume();
    }

    public void pause() {
        valueAnimator1.pause();
        valueAnimator2.pause();
        valueAnimator3.pause();
        valueAnimator4.pause();
    }

    public void release() {
        if (valueAnimator1 != null) {
            valueAnimator1.end();
            valueAnimator1.removeAllUpdateListeners();
            valueAnimator1 = null;
        }
        if (valueAnimator2 != null) {
            valueAnimator2.end();
            valueAnimator2.removeAllUpdateListeners();
            valueAnimator2 = null;
        }
        if (valueAnimator3 != null) {
            valueAnimator3.end();
            valueAnimator3.removeAllUpdateListeners();
            valueAnimator3 = null;
        }
        if (valueAnimator4 != null) {
            valueAnimator4.end();
            valueAnimator4.removeAllUpdateListeners();
            valueAnimator4 = null;
        }
    }

}
