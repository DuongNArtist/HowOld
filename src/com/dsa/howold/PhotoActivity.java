package com.dsa.howold;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class PhotoActivity extends Activity implements OnClickListener {

    private static String TAG = PhotoActivity.class.getSimpleName();

    private static final int MAX_FACES = 20;
    private static final int UPDATE = 999;

    public static final int REQUIRE_WIDTH = 480;
    public static final int REQUIRE_HEIGHT = 800;

    public static int screenWidth;
    public static int screenHeight;

    private FaceView mFaceView;
    private ProcessingDialog mProcessingDialog;
    private Bitmap mFaceBitmap;

    private int mFaceWidth = REQUIRE_WIDTH;
    private int mFaceHeight = REQUIRE_HEIGHT;

    private Button mbtSave;
    private Button mbtShare;
    private Button mbtRepick;
    private FrameLayout mflPhoto;

    private Handler mHandler = new Handler() {

        public void handleMessage(Message message) {
            mFaceView.invalidate();
            mProcessingDialog.cancel();
            MainActivity.showAd();
            super.handleMessage(message);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        mflPhoto = (FrameLayout) findViewById(R.id.fl_photo);
        mFaceView = new FaceView(this);
        mflPhoto.addView(mFaceView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mbtSave = (Button) findViewById(R.id.bt_save);
        mbtShare = (Button) findViewById(R.id.bt_share);
        mbtRepick = (Button) findViewById(R.id.bt_repick);
        mbtSave.setOnClickListener(this);
        mbtShare.setOnClickListener(this);
        mbtRepick.setOnClickListener(this);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        mProcessingDialog = new ProcessingDialog(this);
        mProcessingDialog.show();
        String path = getIntent().getStringExtra(MainActivity.PATH);
        mFaceBitmap = decodeSampledBitmapFromFile(path, REQUIRE_WIDTH,
                REQUIRE_HEIGHT);
        mFaceWidth = mFaceBitmap.getWidth();
        mFaceHeight = mFaceBitmap.getHeight();
        mFaceView.setImageBitmap(mFaceBitmap);
        mFaceView.invalidate();
        calculateAge();
    }

    public void setFace() {
        FaceDetector faceDetector;
        FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
        PointF eyesCenter = new PointF();
        int[] eyesX = null;
        int[] eyesY = null;
        float[] eyesDistances = null;
        int numberOfFaces = 0;
        try {
            faceDetector = new FaceDetector(mFaceWidth, mFaceHeight, MAX_FACES);
            numberOfFaces = faceDetector.findFaces(mFaceBitmap, faces);
            Log.i(TAG, "Number Of Faces = " + numberOfFaces);
        } catch (Exception e) {
            Log.i(TAG, "Number Of Faces: " + e.toString());
            return;
        }
        if (numberOfFaces > 0) {
            eyesX = new int[numberOfFaces * 2];
            eyesY = new int[numberOfFaces * 2];
            eyesDistances = new float[numberOfFaces];
            for (int i = 0; i < numberOfFaces; i++) {
                try {
                    faces[i].getMidPoint(eyesCenter);
                    eyesDistances[i] = faces[i].eyesDistance();
                    eyesX[2 * i] = (int) (eyesCenter.x - eyesDistances[i] / 2);
                    eyesY[2 * i] = (int) eyesCenter.y;
                    eyesX[2 * i + 1] = (int) (eyesCenter.x + eyesDistances[i] / 2);
                    eyesY[2 * i + 1] = (int) eyesCenter.y;
                } catch (Exception e) {
                    Log.i(TAG, "Set Face " + i + ": " + e.toString());
                }
            }
        }
        mFaceView.setDisplayPoints(eyesX, eyesY, eyesDistances,
                numberOfFaces * 2);
    }

    private void calculateAge() {
        Thread thread = new Thread() {
            Message message = new Message();

            @Override
            public void run() {
                try {
                    final int delayMillis = 2000;
                    setFace();
                    message.what = PhotoActivity.UPDATE;
                    PhotoActivity.this.mHandler.sendMessageDelayed(message,
                            delayMillis);
                } catch (Exception e) {
                    Log.e(TAG, "Calculate Age: " + e.toString());
                }
            }
        };
        thread.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.bt_save:
            save();
            break;

        case R.id.bt_share:
            share();
            break;

        case R.id.bt_repick:
            finish();
            break;

        default:
            break;
        }
    }

    public void share() {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("image/png");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + save()));
        try {
            String message = getResources().getString(R.string.post);
            startActivity(Intent.createChooser(share, message));
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
    }

    public File save() {
        Bitmap bitmap = mFaceView.getBitmap();
        String root = Environment.getExternalStorageDirectory().toString();
        File newDir = new File(root + "/dsa");
        newDir.mkdirs();
        String photoName = "dsa_" + System.currentTimeMillis() + ".png";
        File file = new File(newDir, photoName);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            String message = getResources().getString(R.string.saved);
            Toast.makeText(this, message + "\n" + file.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            String message = getResources().getString(R.string.unsaved);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        return file;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth,
            int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;
        if (height > reqHeight) {
            inSampleSize = Math.round((float) height / (float) reqHeight);
        }
        int expectedWidth = width / inSampleSize;
        if (expectedWidth > reqWidth) {
            inSampleSize = Math.round((float) width / (float) reqWidth);
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
}