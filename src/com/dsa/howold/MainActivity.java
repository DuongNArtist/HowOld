package com.dsa.howold;

import java.io.File;

import com.google.ads.AdRequest;
import com.google.ads.InterstitialAd;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

    public static final int REQUEST_CAMERA = 2358;
    public static final int REQUEST_GALLERY = 8532;

    public static final String PATH = "path";

    public static InterstitialAd mInterstitialAd;

    private Button mbtCamera;
    private Button mbtGallery;
    private Button mbtRate;
    private Button mbtOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mbtCamera = (Button) findViewById(R.id.bt_camera);
        mbtGallery = (Button) findViewById(R.id.bt_gallery);
        mbtRate = (Button) findViewById(R.id.bt_rate);
        mbtOther = (Button) findViewById(R.id.bt_other);
        mbtCamera.setOnClickListener(this);
        mbtGallery.setOnClickListener(this);
        mbtRate.setOnClickListener(this);
        mbtOther.setOnClickListener(this);
        loadAd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAd();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.bt_camera:
            handleClickCamera();
            break;

        case R.id.bt_gallery:
            handleClickGallery();
            break;

        case R.id.bt_rate:
            handleClickRate();
            break;

        case R.id.bt_other:
            handleClickOther();
            break;

        default:
            break;
        }
    }

    private void loadAd() {
        String key = getResources().getString(R.string.admob_full);
        mInterstitialAd = new InterstitialAd(this, key);
        mInterstitialAd.loadAd(new AdRequest());
    }

    public static void showAd() {
        if (mInterstitialAd.isReady()) {
            mInterstitialAd.show();
        }
    }

    private void handleClickOther() {
        goToMyStore("https://play.google.com/store/apps/developer?id=Dsa+Inc+app");
    }

    private void handleClickRate() {
        goToMyStore("market://details?id="
                + getApplicationContext().getPackageName());
    }

    private void goToMyStore(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(path));
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String path = "";
            if (requestCode == REQUEST_CAMERA) {
                path = Environment.getExternalStorageDirectory()
                        + File.separator + "dsa/dsa.png";
            }
            if (requestCode == REQUEST_GALLERY) {
                Uri uri = data.getData();
                String[] projection = { MediaColumns.DATA };
                Cursor cursor = managedQuery(uri, projection, null, null, null);
                int index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
                cursor.moveToFirst();
                path = cursor.getString(index);
            }
            Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
            intent.putExtra(PATH, path);
            startActivity(intent);
        }
    }

    private void handleClickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"),
                REQUEST_GALLERY);
    }

    private void handleClickCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + "dsa/dsa.png");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, REQUEST_CAMERA);
    }
}