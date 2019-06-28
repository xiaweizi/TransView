package com.xiaweizi.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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
    private int mCurrentProgress;
    private int mTotalProgress = TOTAL_PROGRESS;
    private Paint mBottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTopPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mHeight;
    private int mWidth;
    private float mRadius = 20;

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

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        mBottomPaint.setStyle(Paint.Style.STROKE);
        mBottomPaint.setStrokeWidth(20);
        mBottomPaint.setColor(getResources().getColor(R.color.bottom_color));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
        Log.i("xiaweizi::", "mWidth: " + mWidth + " mHeight: " + mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


}
