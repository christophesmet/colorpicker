package com.christophesmet.android.views.colorpicker;

import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.christophesmet.colorpicker.lib.R;


/**
 * Created by christophesmet on 24/10/15.
 */

public class ColorPickerView extends FrameLayout {

    //Debug props
    private boolean mDrawDebug = false;
    private Paint mDebugPaint = null;
    private Point mLastSelectedColorPoint = null;

    private int mLastSelectedColor;

    //Views
    private ImageView mImgWheel;
    private ImageView mImgThumb;

    //Drawable references
    @Nullable
    private Drawable mWheelDrawable;
    private Drawable mThumbDrawable;

    //Path of the color range
    @NonNull
    private Path mThumbWheelPath;

    //Center
    private float mCenterX = 0;
    private float mCenterY = 0;
    private float mRadius = 0;
    private float mRadiusOffset = 0;

    //Listeners
    @Nullable
    protected ColorListener mColorListener;

    public ColorPickerView(Context context) {
        super(context);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initAttributes(attrs);
        initViews();
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initAttributes(attrs);
        initViews();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        initAttributes(attrs);
        initViews();
    }

    private void init() {
        //register first measture
        registerMeasure();
    }

    private void initAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.colorpicker);

        try {
            if (a.hasValue(R.styleable.colorpicker_radiusOffset)) {
                mRadiusOffset = a.getDimension(R.styleable.colorpicker_radiusOffset, 0);
            }
            if (a.hasValue(R.styleable.colorpicker_thumbDrawable)) {
                mThumbDrawable = a.getDrawable(R.styleable.colorpicker_thumbDrawable);
            }
            if (a.hasValue(R.styleable.colorpicker_wheelDrawable)) {
                mWheelDrawable = a.getDrawable(R.styleable.colorpicker_wheelDrawable);
            }
        } finally {
            a.recycle();
        }
    }

    private void initViews() {

        mImgWheel = new ImageView(getContext());
        if (mWheelDrawable != null) {
            mImgWheel.setImageDrawable(mWheelDrawable);
        }
        FrameLayout.LayoutParams wheelParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wheelParams.leftMargin= (int) dpToPx(getContext(),16);
        wheelParams.topMargin= (int) dpToPx(getContext(),16);
        wheelParams.rightMargin= (int) dpToPx(getContext(),16);
        wheelParams.bottomMargin= (int) dpToPx(getContext(),16);

        wheelParams.gravity = Gravity.CENTER;
        addView(mImgWheel, wheelParams);

        mImgThumb = new ImageView(getContext());
        if (mThumbDrawable != null) {
            mImgThumb.setImageDrawable(mThumbDrawable);
        }
        FrameLayout.LayoutParams thumbParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        thumbParams.gravity = Gravity.CENTER;
        addView(mImgThumb, thumbParams);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mImgThumb.setStateListAnimator(AnimatorInflater.loadStateListAnimator(getContext(), R.anim.raise));
            mImgThumb.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, mImgThumb.getMeasuredWidth(), mImgThumb.getMeasuredHeight());
                }
            });
        }
    }

    private void loadListeners() {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mImgThumb.setPressed(true);
                        return onTouchReceived(event);
                    case MotionEvent.ACTION_MOVE:
                        mImgThumb.setPressed(true);
                        return onTouchReceived(event);
                    default:
                        mImgThumb.setPressed(false);
                        return false;
                }
            }
        });
    }

    public int getColor() {
        return mLastSelectedColor;
    }

    //return true if consumed
    //Check for the closes point on the circle
    //The move the thumb image to that spot.
    private boolean onTouchReceived(@NonNull MotionEvent event) {
        Point snapPoint = getClosestPoint(event.getX(), event.getY());

        //We have the closes point on the circle.
        //Now adjust the postion to center the thumb in the middle of the point
        mImgThumb.setX(snapPoint.x - (mImgThumb.getMeasuredWidth() / 2));
        mImgThumb.setY(snapPoint.y - (mImgThumb.getMeasuredHeight() / 2));
        return true;
    }

    /**
     * This will return the closes point on the circle relative to the touch event
     *
     * @param touchX
     * @param touchY
     * @return
     */
    private Point getClosestPoint(float touchX, float touchY) {
        //Todo: find if there is a faster way.
        //This is for center 0,0
        double angle = Math.atan2(touchY - getCenterYInParent(), touchX - getCenterXInParent());

        double onCircleX = Math.cos(angle) * mRadius;
        double onCircleY = Math.sin(angle) * mRadius;

        //fetch the selected color from the drawable
        mLastSelectedColor = getColorFromColorRing((float) onCircleX + mCenterX, (float) onCircleY + mCenterY);
        Log.d("colorpicker", "Selected color: " + mLastSelectedColor);
        fireColorListener(getColor());

        //The circle is on an offset, not on 0,0 but centered and in a viewgroup (our parent framelayout)
        onCircleX += getCenterXInParent();
        onCircleY += getCenterYInParent();

        return new Point((int) onCircleX, (int) onCircleY);
    }

    private int getColorFromColorRing(float x, float y) {
        if (mWheelDrawable == null) {
            return 0;
        }
        Log.d("colorpicker", "touch x: " + x + " y: " + y);
        Matrix invertMatrix = new Matrix();
        mImgWheel.getImageMatrix().invert(invertMatrix);

        float[] mappedPoints = new float[]{x, y};
        Log.d("colorpicker", "mapped touch x: " + mappedPoints[0] + " y: " + mappedPoints[1]);

        invertMatrix.mapPoints(mappedPoints);

        if (mImgWheel.getDrawable() != null && mImgWheel.getDrawable() instanceof BitmapDrawable &&
                mappedPoints[0] > 0 && mappedPoints[1] > 0 &&
                mappedPoints[0] < mImgWheel.getDrawable().getIntrinsicWidth() && mappedPoints[1] < mImgWheel.getDrawable().getIntrinsicHeight()) {

            mLastSelectedColorPoint = new Point((int) mappedPoints[0], (int) mappedPoints[1]);
            invalidate();
            return ((BitmapDrawable) mImgWheel.getDrawable()).getBitmap().getPixel((int) mappedPoints[0], (int) mappedPoints[1]);
        }
        return 0;
    }

    private void onFirstLayout() {
        //First layout, lets grab the size and generate the path for
        mThumbWheelPath = generateThumbWheelPath(mImgWheel.getMeasuredWidth(), mImgWheel.getMeasuredHeight());
        //Fake a touch top center
        onTouchReceived(
                MotionEvent.obtain(System.currentTimeMillis(),
                        System.currentTimeMillis() + 100,
                        MotionEvent.ACTION_UP,
                        getMeasuredWidth() / 2,
                        0,
                        0)
        );
        //Setup done, register listeners
        loadListeners();
    }

    public float getRadius(float side) {
        //Offset the entire circle
        //Radius is the smallest side - the offset to center the circle in the center of the color circle.
        float radius = ((side) - mRadiusOffset) / 2;
        return radius;
    }

    @NonNull
    private Path generateThumbWheelPath(int measuredWidth, int measuredHeight) {
        //By default we just make a circle. default scaletype of an imageview is fitCenter
        //Lets calculate the square size of the imageview's content
        int side = Math.min(measuredHeight, measuredWidth);
        //Lets create the path
        Path output = new Path();

        mRadius = getRadius(side);
        mCenterX = (float) measuredWidth / 2;
        mCenterY = (float) measuredHeight / 2;

        //add the offset of the imageview wheel in this viewgroup
        output.addCircle(getCenterXInParent(), getCenterYInParent(), mRadius, Path.Direction.CW);
        return output;
    }


    private void registerMeasure() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                onFirstLayout();
            }
        });
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mDrawDebug && mThumbWheelPath != null) {
            checkDebugPaint();
            //Draw the path to see while debugging
            canvas.drawPath(mThumbWheelPath, mDebugPaint);
            //cross in the center of the parent
            canvas.drawLine(getCenterXInParent(), getCenterYInParent() - 20, getCenterXInParent(), getCenterYInParent() + 20, mDebugPaint);
            canvas.drawLine(getCenterXInParent() - 20, getCenterYInParent(), getCenterXInParent() + 20, getCenterYInParent(), mDebugPaint);
            if (mLastSelectedColorPoint != null) {
                canvas.drawCircle(mImgWheel.getX() + mLastSelectedColorPoint.x, mImgWheel.getY() + mLastSelectedColorPoint.y, 14, mDebugPaint);
            }
        }
    }

    private float getCenterXInParent() {
        return mCenterX + mImgWheel.getX();
    }

    private float getCenterYInParent() {
        return mCenterY + mImgWheel.getY();
    }

    private void checkDebugPaint() {
        //Allocations during a draw phase are a big no-no. But will happen only once, and it's for debugging anyway.
        if (mDebugPaint == null) {
            mDebugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mDebugPaint.setStrokeWidth(5f);
            mDebugPaint.setStyle(Paint.Style.STROKE);
            mDebugPaint.setColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    public static float pxToDp(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float dpToPx(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public void setDrawDebug(boolean drawDebug) {
        mDrawDebug = drawDebug;
        //Debug draw enabled.
        //request a draw
        invalidate();
    }

    private void fireColorListener(int color) {
        if (mColorListener != null) {
            mColorListener.onColorSelected(color);
        }
    }

    public void setColorListener(@Nullable ColorListener colorListener) {
        mColorListener = colorListener;
    }

    public interface ColorListener {
        void onColorSelected(int color);
    }
}
