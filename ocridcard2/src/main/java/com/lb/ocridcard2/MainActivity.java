package com.lb.ocridcard2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.ocr.ui.camera.CameraActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CAMERA = 102;
    private static   String FILE_PATH =  "/mnt/sdcard/ocr/1.jpg";
    private TextView mTipView;
    private ImageView mIDCardView;
    private OCRThread mOCRThread;
    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTipView = findViewById(R.id.tips_view);
        mIDCardView = findViewById(R.id.id_card_image);
        mProgress = new ProgressDialog(this);
        // 正面(手动)
        findViewById(R.id.scan_idcard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                        FILE_PATH);
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            }
        });

        HandlerThread thread = new HandlerThread("ocr_thread");
        thread.start();
        mOCRThread = new OCRThread(thread.getLooper());
        mOCRThread.doInit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mOCRThread.decode();
            }
        }
    }

    private void showProgress(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress.setMessage(msg);
                mProgress.show();
            }
        });
    }

    private void dissmissProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress.dismiss();
            }
        });
    }


    private void updateView(final IDCardInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIDCardView.setImageBitmap(info.getBitmap());
                mTipView.setText(info.toString());
            }
        });
    }


    private class OCRThread extends Handler {
        private static final int INIT = 0;
        private static final int DECODE = 1;

        public OCRThread(Looper looper) {
            super(looper);
        }

        public void doInit() {
            sendEmptyMessage(INIT);
        }

        public void decode() {
            sendEmptyMessage(DECODE);
        }

        private void onInit() {
            showProgress("On initializing");
            OCRManager.get().init();
            dissmissProgress();
        }

        private void onDecode() {
            try {
                showProgress("Parsing");
                IDCardRecognition idr = new IDCardRecognition(FILE_PATH);
                IDCardInfo info = idr.recognize();
                dissmissProgress();
                updateView(info);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT:
                    onInit();
                    break;
                case DECODE:
                    onDecode();
                    break;
            }
        }
    }

}
