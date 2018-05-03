package com.seckawijoki.progressingbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by 灵灯引芒 on 2017/3/9.
 */

public class ProgressingBar extends View {
  private OnClickListener mOnClickListener;
  private Paint mBottomBarPaint;
  private Paint mTopBarPaint;
  private Paint mPercentagePaint;
  private Rect mPercentageRect;
  private int mBottomBarColor;
  private int mTopBarColor;
  private int mPercentageColor;
  private float mPercentageSize;
  private float mRadius;
  private final float MAX_PROGRESS = 1000;
  private LinearGradient mLinearGradient;
  private float mProgress;
  private float mCurrentProgress;
  public ProgressingBar(Context context) {
    this(context, null);
  }

  public ProgressingBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ProgressingBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressingBar, defStyleAttr, 0);
    mBottomBarColor = ta.getColor(R.styleable.ProgressingBar_bottomBarColor, 0xFFC8E6C9);
    mTopBarColor = ta.getColor(R.styleable.ProgressingBar_topBarColor, 0xFFACE25C);
    mPercentageColor = ta.getColor(R.styleable.ProgressingBar_percentageColor, Color.WHITE);
    mPercentageSize = ta.getDimensionPixelSize(R.styleable.ProgressingBar_percentageSize, sp2px(context, 30));

    mBottomBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mBottomBarPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    mBottomBarPaint.setStrokeWidth(getHeight());
    mBottomBarPaint.setColor(mBottomBarColor);
    mBottomBarPaint.setStrokeCap(Paint.Cap.ROUND);

    mTopBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTopBarPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    mTopBarPaint.setStrokeWidth(getHeight());
    mTopBarPaint.setColor(mTopBarColor);
    mTopBarPaint.setStrokeCap(Paint.Cap.ROUND);

    mPercentagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPercentagePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    mPercentagePaint.setTextSize(mPercentageSize);
    mPercentagePaint.setColor(mPercentageColor);

    mPercentageRect = new Rect();
    mOnClickListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        animateProgress(mProgress);
      }
    };

    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mOnClickListener != null) {
          mOnClickListener.onClick(ProgressingBar.this);
        }
      }
    });
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int measureWidth;
    int measureHeight;
    int specMode = MeasureSpec.getMode(widthMeasureSpec);
    int specSize = MeasureSpec.getSize(widthMeasureSpec);
    if (specMode == MeasureSpec.EXACTLY){
      measureWidth = getWidth();
    } else {
      measureWidth = dp2px(getContext(), 489);
      if (specMode == MeasureSpec.AT_MOST){
        measureWidth = Math.min(measureWidth, specSize);
      }
    }

    specMode = MeasureSpec.getMode(heightMeasureSpec);
    specSize = MeasureSpec.getSize(widthMeasureSpec);
    if (specMode == MeasureSpec.EXACTLY){
      measureHeight = getHeight();
    } else {
      measureHeight = dp2px(getContext(), 46);
      if (specMode == MeasureSpec.AT_MOST){
        measureHeight = Math.min(measureHeight, specSize);
      }
    }
    setMeasuredDimension(measureWidth, measureHeight);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    mRadius = getHeight()/2;
    float progressWith = (getWidth()-getHeight()) * mCurrentProgress / MAX_PROGRESS;
    mLinearGradient = new LinearGradient(0,0,getWidth(),0,mTopBarColor, 0xFF73C12F, Shader.TileMode.CLAMP);
    mTopBarPaint.setShader(mLinearGradient);
    canvas.drawCircle(mRadius, getHeight()/2, mRadius, mBottomBarPaint);
    canvas.drawCircle(getWidth()-mRadius, getHeight()/2, mRadius, mBottomBarPaint);
    canvas.drawRect(mRadius, 0, getWidth()-mRadius, getHeight(), mBottomBarPaint);

    canvas.drawCircle(mRadius, getHeight()/2, mRadius, mTopBarPaint);
    canvas.drawRect(mRadius, 0, mRadius+progressWith, getHeight(), mTopBarPaint);
    canvas.drawCircle(mRadius + progressWith, getHeight() / 2, mRadius, mTopBarPaint);
    String percentage = (int)(mCurrentProgress /10) + "%";
    mPercentagePaint.getTextBounds(percentage, 0, String.valueOf(percentage).length(), mPercentageRect);
    float x = progressWith - mPercentageRect.width() + getHeight() - mRadius/2;
    if (x < mRadius/2)x = mRadius/2;
    canvas.drawText(percentage,
        x,
        getHeight()/2 + mPercentageRect.height()/2,
        mPercentagePaint);
  }

  public void setProgress(float progress) {
    ValueAnimator animator = ValueAnimator.ofFloat(mCurrentProgress, progress);
    animator.setInterpolator(new FastOutSlowInInterpolator());
    animator.setDuration((long) Math.abs(mCurrentProgress - progress));
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();
        mCurrentProgress = Math.round(value * 10) / 10;
        invalidate();
      }
    });
    animator.start();
  }

  public void animateProgress(float progress){
    mProgress = progress;
    mCurrentProgress = 0;
    setProgress(progress);
  }

  private static int sp2px(Context context, float spVal) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
            spVal, context.getResources().getDisplayMetrics());
  }

  private static int dp2px(Context context, float dpVal) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            dpVal, context.getResources().getDisplayMetrics());
  }
}
