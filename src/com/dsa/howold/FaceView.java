package com.dsa.howold;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.widget.ImageView;

class FaceView extends ImageView {

    public static final String TAG = FaceView.class.getSimpleName();
    public static final int EYES_SIZE = 10;
    public static final int TEXT_SIZE = 40;

    private Bitmap mBox;
    private int mBoxWidth;
    private int mBoxHeight;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Rect mSrc;
    private Rect mDst;
    private int mWidth = PhotoActivity.screenWidth;
    private int mHeight = PhotoActivity.screenHeight;

    private Paint mRectPaint;
    private int[] mMidPointX = null;
    private int[] mMidPointY = null;
    private float[] mEyesDistances;
    private int[] mAges;

    private Rect mAgeBounds;
    private Paint mAgePaint;
    private String mMessage;
    private int mNumberOfFaces;
    private boolean mDetected;

    public FaceView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mDetected = false;
        mNumberOfFaces = 0;
        mMessage = getResources().getString(R.string.unage);
        mAgeBounds = new Rect();
        mBox = BitmapFactory.decodeResource(getResources(), R.drawable.sex_gay
                + new Random().nextInt(4));
        mBoxWidth = mBox.getWidth();
        mBoxHeight = mBox.getHeight();
        mWidth = PhotoActivity.REQUIRE_WIDTH;
        mHeight = PhotoActivity.REQUIRE_HEIGHT;
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
        mCanvas = new Canvas(mBitmap);
        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeCap(Paint.Cap.ROUND);
        mRectPaint.setColor(Color.WHITE);
        mRectPaint.setStrokeWidth(2);
        mAgePaint = new Paint();
        mAgePaint.setAntiAlias(true);
        mAgePaint.setStyle(Paint.Style.FILL);
        mAgePaint.setColor(Color.BLACK);
        mAgePaint.setTextSize(TEXT_SIZE);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            mWidth = bitmap.getWidth();
            mHeight = bitmap.getHeight();
            mSrc = new Rect(0, 0, mWidth, mHeight);
            int dstHeight = PhotoActivity.screenWidth * mHeight / mWidth;
            mDst = new Rect(0, (PhotoActivity.screenHeight - dstHeight) / 2,
                    PhotoActivity.screenWidth,
                    (PhotoActivity.screenHeight + dstHeight) / 2);
            mBitmap = Bitmap.createBitmap(mWidth, mHeight,
                    Bitmap.Config.RGB_565);
            mCanvas = new Canvas();
            mCanvas.setBitmap(mBitmap);
            mCanvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth,
            int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        mWidth = (mBitmap != null) ? mBitmap.getWidth() : 0;
        mHeight = (mBitmap != null) ? mBitmap.getHeight() : 0;
        if (mWidth == width && mHeight == height) {
            return;
        }
        if (mWidth < width) {
            mWidth = width;
        }
        if (mHeight < height) {
            mHeight = height;
        }
    }

    public void setDisplayPoints(int[] midPointX, int[] midPointY,
            float[] eyesDistances, int total) {
        mNumberOfFaces = total / 2;
        mDetected = true;
        if (mNumberOfFaces == 0) {
            mAgePaint.setTextSize(2 * TEXT_SIZE);
            mAgePaint.setColor(Color.WHITE);
        } else {
            mAgePaint.setTextSize(TEXT_SIZE);
            mAgePaint.setColor(Color.BLACK);
        }
        mMidPointX = null;
        mMidPointY = null;
        mEyesDistances = null;
        mAges = null;
        if (midPointX != null && midPointY != null && eyesDistances != null
                && total > 0) {
            mMidPointX = new int[total];
            mMidPointY = new int[total];
            mEyesDistances = new float[total / 2];
            mAges = new int[total / 2];
            for (int index = 0; index < total; index++) {
                mMidPointX[index] = midPointX[index];
                mMidPointY[index] = midPointY[index];
                if (index < total / 2) {
                    mEyesDistances[index] = eyesDistances[index];
                    int start = (int) mEyesDistances[index];
                    mAges[index] = start / 5 + start / 8;
                    Log.i(TAG, "distance = " + mEyesDistances[index / 2]);
                    Log.i(TAG, "age = " + mEyesDistances[index]);
                }
                Log.i(TAG, "left = " + mMidPointX[index]);
                Log.i(TAG, "right = " + mMidPointY[index]);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap != null) {
            if (mMidPointX != null && mMidPointY != null && mNumberOfFaces > 0) {
                for (int index = 0; index < mMidPointX.length; index++) {
                    if (index % 2 == 0) {
                        int eyesIndex = index / 2;
                        mCanvas.drawRect(mMidPointX[index] - 0.618f
                                * mEyesDistances[eyesIndex], mMidPointY[index]
                                - 0.618f * mEyesDistances[eyesIndex],
                                mMidPointX[index] + 1.618f
                                        * mEyesDistances[eyesIndex],
                                mMidPointY[index] + 1.618f
                                        * mEyesDistances[eyesIndex], mRectPaint);
                        mCanvas.drawBitmap(mBox, mMidPointX[index]
                                + (mEyesDistances[eyesIndex] - mBoxWidth) / 2,
                                mMidPointY[index] - mEyesDistances[eyesIndex]
                                        - 0.75f * mBoxHeight, null);
                        mMessage = mAges[index / 2] + "";
                        mAgePaint.getTextBounds(mMessage, 0, mMessage.length(),
                                mAgeBounds);
                        mCanvas.drawText(mMessage, mMidPointX[index]
                                + mEyesDistances[eyesIndex] / 2,
                                mMidPointY[index] - mEyesDistances[eyesIndex]
                                        - mAgePaint.getTextSize() / 2,
                                mAgePaint);
                    }
                }
            }
            if (mNumberOfFaces == 0 && mDetected) {
                mAgePaint.getTextBounds(mMessage, 0, mMessage.length(),
                        mAgeBounds);
                Log.i(TAG, "size = " + mAgeBounds.right + " - "
                        + ((mSrc.right - mAgeBounds.right) / 2));
                mCanvas.drawText(mMessage, (mSrc.right - mAgeBounds.right) / 2,
                        mSrc.top + mAgePaint.getTextSize(), mAgePaint);
            }
            canvas.drawBitmap(mBitmap, mSrc, mDst, null);
        }
    }
}