package com.xiaweizi.myapplication;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.airbnb.lottie.LottieAnimationView;

/**
 * <pre>
 *     class  : com.xiaweizi.myapplication.TransferProgressView
 *     time   : 2019/07/01
 *     desc   : 传输进度自定义 View
 * </pre>
 */
public class TransferProgressView extends RelativeLayout {

    private static final String TAG = "TransferProgressView::";
    private static final String IMAGE_ASSETS_FOLDER = "images/"; // 存放 lottie 动画图片路径
    private static final String LOTTIE_RECEIVER_FILE_NAME = "receive.json"; // 接收动画 json 文件
    private static final String LOTTIE_RECEIVER_INTERRUPT_FILE_NAME = "receive_interrupt.json"; // 接收打断动画
    private static final String LOTTIE_SEND_FILE_NAME = "send.json"; // 发送动画
    private static final String LOTTIE_SEND_INTERRUPT_FILE_NAME = "send_interrupt.json"; // 发送打断动画

    private static final long RUNNING_ANIMATOR_DURATION = 2500; // 背景动画运动周期
    private static final long INTERRUPT_ANIMATOR_DURATION = 700; // 中断周期
    private static final float ANIMATOR_START_TIME = 0.4f; // 动画真正启动帧
    private static final float INTERRUPT_TIME = 0.18f; // 中断时执行的帧数
    private static final int MAX_PROGRESS = 100; // 最大进度
    private LottieAnimationView mRunningView; // 运行的 view
    private LottieAnimationView mInterruptView; // 中断的 view
    private CircleProgressBar mProgressBar; // 右侧进度 view
    private ValueAnimator mRunningAnimator; // 运行时动画
    private ValueAnimator mInterruptAnimator; // 中断时候的动画
    private ValueAnimator mRecoverAnimator; // 恢复时候的动画

    private boolean mIsInterrupt = false; // 当前是否中断
    private float mStartValue = 0; // 记录中断/恢复时动画值
    private float endValue =0; // 通过计算算出目的动画值

    public TransferProgressView(Context context) {
        this(context, null);
    }

    public TransferProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransferProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        View.inflate(context, R.layout.view_transfer_progress, this);
        initView();
        initData();
    }

    private void initView() {
        mRunningView = findViewById(R.id.lottie_view_running);
        mInterruptView = findViewById(R.id.lottie_view_interrupt);
        mProgressBar = findViewById(R.id.progress_bar);

        ViewGroup.LayoutParams layoutParams = mRunningView.getLayoutParams();
        layoutParams.height = (int) getActualValue(getContext(), 410);
        mRunningView.setLayoutParams(layoutParams);
        mInterruptView.setLayoutParams(layoutParams);
    }

    private void initData() {
        mRunningView.setImageAssetsFolder(IMAGE_ASSETS_FOLDER);
        mInterruptView.setImageAssetsFolder(IMAGE_ASSETS_FOLDER);
        initRunningAnimator(0, 1);
    }

    /**
     * 初始化运行时动画
     */
    private void initRunningAnimator(float... values) {
        if (mRunningAnimator == null) {
            mRunningAnimator = ValueAnimator.ofFloat().setDuration(RUNNING_ANIMATOR_DURATION);
            mRunningAnimator.setInterpolator(new LinearInterpolator());
            mRunningAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mRunningAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    value = value > 1 ? value - 1 : value;
                    refreshValue(value);
                    if (!mIsInterrupt) {
                        mRunningView.setProgress(value);
                    } else {
                        mInterruptView.setProgress(value);
                    }
                }
            });
        }
        mRunningAnimator.setFloatValues(values);
    }

    /**
     * 初始化中断动画
     */
    private void initInterruptAnimator() {
        if (mRunningAnimator == null) return;
        mStartValue = (float) mRunningAnimator.getAnimatedValue();
        endValue = mStartValue + INTERRUPT_TIME;
        if (mInterruptAnimator == null) {
            mInterruptAnimator = ValueAnimator.ofFloat();
            mInterruptAnimator.setInterpolator(new DecelerateInterpolator());
            mInterruptAnimator.setDuration(INTERRUPT_ANIMATOR_DURATION);
            mInterruptAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mRunningView.setProgress(value > 1 ? value - 1 : value);
                    mInterruptView.setProgress(value > 1 ? value - 1 : value);
                    float ratio = (value - mStartValue) / INTERRUPT_TIME;
                    mInterruptView.setAlpha(ratio);
                    mProgressBar.refreshBgColor(ratio);
                }
            });
        }
        mInterruptAnimator.setFloatValues(mStartValue, endValue);
    }

    /**
     * 初始化恢复动画
     */
    private void initRecoverAnimator() {
        if (mInterruptAnimator == null) return;
        mStartValue = (float) mInterruptAnimator.getAnimatedValue();
        endValue = mStartValue + INTERRUPT_TIME;
        if (mRecoverAnimator == null) {
            mRecoverAnimator = ValueAnimator.ofFloat();
            mRecoverAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mRecoverAnimator.setDuration(INTERRUPT_ANIMATOR_DURATION);
            mRecoverAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mRunningView.setProgress(value > 1 ? value - 1 : value);
                    mInterruptView.setProgress(value > 1 ? value - 1 : value);
                    float ratio = (value - mStartValue) / INTERRUPT_TIME;
                    mInterruptView.setAlpha(1 - ratio);
                    mProgressBar.refreshBgColor(1 - ratio);
                    if (value >= endValue - 0.02f && !mRunningAnimator.isRunning()) {
                        float startValue = value > 1 ? value - 1 : value;
                        startValue = startValue > 1 ? startValue - 1 : startValue;
                        float endValue = startValue + 1;
                        initRunningAnimator(startValue, endValue);
                        mRunningAnimator.start();
                    }
                }
            });
        }
        mRecoverAnimator.setFloatValues(mStartValue, endValue);
    }

    /**
     * 根据背景动画刷新，刷新进度的脉冲进度
     */
    private void refreshValue(float value) {
        float animatedValue = value;
        if (animatedValue <= ANIMATOR_START_TIME) {
            animatedValue = animatedValue + 1;
        }
        animatedValue = Math.abs(animatedValue - ANIMATOR_START_TIME);
        mProgressBar.refreshValue(animatedValue);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 动态适配 view 的高度
        int screenWidth = getScreenWidth(getContext());
        int height = (int) getActualValue(getContext(), 410);
        setMeasuredDimension(screenWidth, height);
    }

    ///////////////////////////////////////////////////////////////////////////////
    // 暴露给调用者使用的方法
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * 设置当前模式，切换 lottie 动画资源文件
     * @param isSendMode 发送 or 接收
     */
    public void initMode(boolean isSendMode) {
        mProgressBar.setSend(isSendMode);
        if (isSendMode) {
            mRunningView.setAnimation(LOTTIE_SEND_FILE_NAME);
            mInterruptView.setAnimation(LOTTIE_SEND_INTERRUPT_FILE_NAME);
        } else {
            mRunningView.setAnimation(LOTTIE_RECEIVER_FILE_NAME);
            mInterruptView.setAnimation(LOTTIE_RECEIVER_INTERRUPT_FILE_NAME);
        }
    }

    /** 更新进度 */
    public void setProgress(int progress) {
        if (mProgressBar != null && progress >= 0 && progress <= MAX_PROGRESS) {
           mProgressBar.setCurrentProgress(progress);
        }
    }

    /** 开始动画 */
    public void start() {
        if (mRunningAnimator != null && !mRunningAnimator.isRunning()) {
            mRunningAnimator.start();
        }
    }

    /** 由暂停状态切换到开始状态 */
    public void resume() {
        if (mRunningAnimator != null) {
            mRunningAnimator.resume();
        }
    }

    /** 暂停动画 */
    public void pause() {
        if (mRunningAnimator != null) {
            mRunningAnimator.pause();
        }
    }

    /** 动画意外终止 */
    public void interrupt() {
        if (mRunningAnimator == null) return;
        mIsInterrupt = true;
        initInterruptAnimator();
        mRunningAnimator.end();
        mInterruptAnimator.start();
    }

    /** 恢复 */
    public void recover() {
        mIsInterrupt = false;
        initRecoverAnimator();
        mRecoverAnimator.start();
    }

    /** 动画结束完成 */
    public void complete() {
        if (mInterruptAnimator != null) {
            mInterruptAnimator.removeAllUpdateListeners();
            mInterruptAnimator = null;
        }
        if (mRunningAnimator != null) {
            mRunningAnimator.removeAllUpdateListeners();
            mRunningAnimator = null;
        }
        if (mRecoverAnimator != null) {
            mRecoverAnimator.removeAllUpdateListeners();
            mRecoverAnimator = null;
        }
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point outSize = new Point();
        wm.getDefaultDisplay().getSize(outSize);
        return outSize.x;
    }

    public static float getActualValue(Context context, float srcValue) {
        return srcValue * getScreenWidth(context) / 1080;
    }
}
