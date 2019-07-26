package com.xiaweizi.myapplication;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.airbnb.lottie.LottieAnimationView;

import java.io.IOException;
import java.io.InputStream;

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
    private static final String LOTTIE_SEND_FACE_FILE_NAME = "send_complete.json"; // 发送结束动画
    private static final String LOTTIE_RECEIVER_FACE_FILE_NAME = "receiver_complete.json"; // 接收结束动画
    private static final String LOTTIE_RECEIVER_BG_FILE_NAME = "images/receiver_1.png"; // 接收背景图片文件
    private static final String LOTTIE_SEND_BG_FILE_NAME = "images/send_3.png"; // 接收背景图片文件


    private static final long RUNNING_ANIMATOR_DURATION = 2500; // 背景动画运动周期
    private static final long INTERRUPT_ANIMATOR_DURATION = 700; // 中断周期
    private static final long TRANS_X_ANIMATOR_DURATION = 800; // 偏移动画时长
    private static final float ANIMATOR_START_TIME = 0.4f; // 动画真正启动帧
    private static final float INTERRUPT_TIME = 0.18f; // 中断时执行的帧数
    private static final int MAX_PROGRESS = 100; // 最大进度
    private LottieAnimationView mRunningView; // 运行的 view
    private LottieAnimationView mInterruptView; // 中断的 view
    private LottieAnimationView mFaceView; // 中断的 view
    private CircleProgressBar mProgressBar; // 右侧进度 view
    private ImageView mIvBg; // 背景图片
    private ValueAnimator mRunningAnimator; // 运行时动画
    private ValueAnimator mInterruptAnimator; // 中断时候的动画
    private ValueAnimator mRecoverAnimator; // 恢复时候的动画
    private ObjectAnimator mTransXAnimator; // 开始和结束时的偏移动画

    private int mScreenWidth; // 屏幕宽度
    private float mTransXValue = 0; // 偏移量
    private boolean mIsInterrupt = false; // 当前是否中断
    private float mStartValue = 0; // 记录中断/恢复时动画值
    private float mEndValue =0; // 通过计算算出目的动画值
    private boolean mSendMode = false; // 是否是发送模式
    private int mSendColor; // 发送的颜色
    private int mReceiverColor; // 接收的颜色
    private int mInterruptColor; // 中断的颜色
    private boolean mHasCompleted = false; // 是否完成


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
        mScreenWidth = getScreenWidth();
        mSendColor = getResources().getColor(R.color.transfer_send_color);
        mReceiverColor = getResources().getColor(R.color.transfer_receiver_color);
        mInterruptColor = getResources().getColor(R.color.transfer_interrupt);

        mRunningView = findViewById(R.id.lottie_view_running);
        mInterruptView = findViewById(R.id.lottie_view_interrupt);
        mFaceView = findViewById(R.id.lottie_view_face);
        mProgressBar = findViewById(R.id.progress_bar);
        mIvBg = findViewById(R.id.iv_bg);

        ViewGroup.LayoutParams layoutParams = mRunningView.getLayoutParams();
        layoutParams.height = (int) getActualValue(410);
        mRunningView.setLayoutParams(layoutParams);
        mInterruptView.setLayoutParams(layoutParams);
        mIvBg.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams faceLayoutParams = (LayoutParams) mFaceView.getLayoutParams();
        faceLayoutParams.width = (int) getActualValue(220);
        faceLayoutParams.height = (int) getActualValue(220);
        faceLayoutParams.topMargin = (int) getActualValue(140);
        faceLayoutParams.rightMargin = (int) getActualValue(40);
        mFaceView.setLayoutParams(faceLayoutParams);

    }

    private void initData() {
        mRunningView.setImageAssetsFolder(IMAGE_ASSETS_FOLDER);
        mInterruptView.setImageAssetsFolder(IMAGE_ASSETS_FOLDER);
        mFaceView.setImageAssetsFolder(IMAGE_ASSETS_FOLDER);
        initRunningAnimator(0, 1);
        mTransXValue = getActualValue(240);
        setTranslationX(mTransXValue);
    }

    private void setImageBackground() {
        AssetManager am = getContext().getAssets();
        try {
            InputStream open = am.open(mSendMode ? LOTTIE_SEND_BG_FILE_NAME : LOTTIE_RECEIVER_BG_FILE_NAME);
            Bitmap bitmap = BitmapFactory.decodeStream(open);
            if (bitmap != null) {
                mIvBg.setImageBitmap(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    if (mIsInterrupt) return;
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
        mRunningAnimator.setStartDelay(200);
        mRunningAnimator.setFloatValues(values);
    }

    /**
     * 初始化中断动画
     */
    private void initInterruptAnimator() {
        if (mRunningAnimator == null) return;
        mStartValue = (float) mRunningAnimator.getAnimatedValue();
        mEndValue = mStartValue + INTERRUPT_TIME;
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
                    setWindowBackGround(ratio);
                }
            });
        }
        mInterruptAnimator.setFloatValues(mStartValue, mEndValue);
    }

    /**
     * 初始化恢复动画
     */
    private void initRecoverAnimator() {
        if (mInterruptAnimator == null) return;
        mStartValue = (float) mInterruptAnimator.getAnimatedValue();
        mEndValue = mStartValue + INTERRUPT_TIME;
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
                    setWindowBackGround(1 - ratio);
                    if (value >= mEndValue - 0.02f && !mRunningAnimator.isRunning()) {
                        float startValue = value > 1 ? value - 1 : value;
                        startValue = startValue > 1 ? startValue - 1 : startValue;
                        float endValue = startValue + 1;
                        initRunningAnimator(startValue, endValue);
                        mRunningAnimator.setStartDelay(0);
                        mRunningAnimator.start();
                    }
                }
            });
        }
        mRecoverAnimator.setFloatValues(mStartValue, mEndValue);
    }

    private void startTransXAnimator(float... values) {
        if (mTransXAnimator == null) {
            mTransXAnimator = ObjectAnimator.ofFloat(this, "translationX", values);
            mTransXAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mTransXAnimator.setDuration(TRANS_X_ANIMATOR_DURATION);
        } else {
            mTransXAnimator.setFloatValues(values);
        }
        mTransXAnimator.start();
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
        mProgressBar.refreshValue(animatedValue, !mHasCompleted);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 动态适配 view 的高度
        mScreenWidth = getScreenWidth();
        int height = (int) getActualValue(410);
        setMeasuredDimension(mScreenWidth, height);
    }

    ///////////////////////////////////////////////////////////////////////////////
    // 暴露给调用者使用的方法
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * 设置当前模式，切换 lottie 动画资源文件
     * @param isSendMode 发送 or 接收
     */
    public void initMode(boolean isSendMode) {
        mSendMode = isSendMode;
        mProgressBar.setSend(isSendMode);
        setImageBackground();
        setWindowBackGround(0);
        if (isSendMode) {
            mRunningView.setAnimation(LOTTIE_SEND_FILE_NAME);
            mInterruptView.setAnimation(LOTTIE_SEND_INTERRUPT_FILE_NAME);
            mFaceView.setAnimation(LOTTIE_SEND_FACE_FILE_NAME);
        } else {
            mRunningView.setAnimation(LOTTIE_RECEIVER_FILE_NAME);
            mInterruptView.setAnimation(LOTTIE_RECEIVER_INTERRUPT_FILE_NAME);
            mFaceView.setAnimation(LOTTIE_RECEIVER_FACE_FILE_NAME);
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
        mHasCompleted = false;
        mRunningView.animate().alpha(1).setDuration(600).start();
        startTransXAnimator(mTransXValue, 0);
        if (mFaceView != null && mFaceView.getVisibility() == View.VISIBLE) {
            mFaceView.setVisibility(GONE);
        }
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
        if (mRecoverAnimator != null) {
            mRecoverAnimator.start();
        }
    }

    /** 动画结束完成 */
    public void complete() {
        mHasCompleted = true;
        mFaceView.setVisibility(VISIBLE);
        mFaceView.playAnimation();
        mFaceView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                // 笑脸动画结束，进行偏移动画
                mRunningView.animate().alpha(0).setDuration(600).start();
                startTransXAnimator(0, mTransXValue);
            }
        });
    }

    public void release() {
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
        if (mRunningView != null) {
            mRunningView.removeAllUpdateListeners();
        }
        if (mInterruptView != null) {
            mInterruptView.removeAllUpdateListeners();
        }
        if (mFaceView != null) {
            mFaceView.removeAllUpdateListeners();
        }
        if (mTransXAnimator != null) {
            mTransXAnimator.removeAllListeners();
            mTransXAnimator = null;
        }
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

    private void setWindowBackGround(float radio) {
//        if (getContext() != null && getContext() instanceof Activity) {
//            Window window = ((Activity) getContext()).getWindow();
//            if (window != null) {
//                window.setBackgroundDrawable(new ColorDrawable(getBgColor(radio)));
//            }
//        }
    }

    private int getBgColor(float radio) {
        int startColor = mSendMode ? mSendColor : mReceiverColor;
        int redStart = Color.red(startColor);
        int blueStart = Color.blue(startColor);
        int greenStart = Color.green(startColor);
        int redEnd = Color.red(mInterruptColor);
        int blueEnd = Color.blue(mInterruptColor);
        int greenEnd = Color.green(mInterruptColor);

        int red = (int) (redStart + ((redEnd - redStart) * radio + 0.5));
        int greed = (int) (greenStart + ((greenEnd - greenStart) * radio + 0.5));
        int blue = (int) (blueStart + ((blueEnd - blueStart) * radio + 0.5));
        return Color.argb(255, red, greed, blue);
    }
}
