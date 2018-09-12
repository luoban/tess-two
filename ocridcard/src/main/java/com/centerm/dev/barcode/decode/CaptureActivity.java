package com.centerm.dev.barcode.decode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centerm.dev.barcode.camera.CameraManager;
import com.centerm.dev.barcode.camera.CameraView;
import com.centerm.lib.DecodeResult;
import com.lb.ocridcard.R;

public class CaptureActivity extends Activity implements OnClickListener, CaptureActivityHandler.DecodeListener {

    private RelativeLayout mContainer = null;
    private RelativeLayout mCropLayout = null;
    private ImageView mLightIcon;
    private ImageView mSettings;
    private TextView mLightDescription;
    private int mMaskTop = 217;
    private int mMaskWidth = 400;
    private double mScaleX;
    private double mScaleY;
    private CameraManager mCameraManager;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        mCameraManager = CameraManager.get();
        findViewById(R.id.switch_camera).setOnClickListener(this);
        findViewById(R.id.light_control_layout).setOnClickListener(this);
        mContainer = (RelativeLayout) findViewById(R.id.capture_containter);
        mCropLayout = (RelativeLayout) findViewById(R.id.capture_crop_layout);
        ImageView topMask = (ImageView) findViewById(R.id.top_mask);
        mLightIcon = (ImageView) findViewById(R.id.light_control_img);
        mSettings = (ImageView) findViewById(R.id.settings);
        mSettings.setOnClickListener(this);
        mLightDescription = (TextView) findViewById(R.id.light_control_tv);
        CameraView mCameraView = (CameraView) findViewById(R.id.capture_preview);
        mCameraManager.bindCameraView(mCameraView);
        mCameraManager.bindDecodeCallback(this);

        updateTorchState();
        initMaskScale();
        RelativeLayout.LayoutParams topMaskParams = (RelativeLayout.LayoutParams) topMask
                .getLayoutParams(); // 取控件mGrid当前的布局参数
        topMaskParams.height = (int) (mMaskTop * mScaleY);
        topMask.setLayoutParams(topMaskParams); // 使设置好的布局参数应用到上方阴影图片

        RelativeLayout.LayoutParams cropLayoutParams = (RelativeLayout.LayoutParams) mCropLayout
                .getLayoutParams();
        cropLayoutParams.height = (int) (mMaskWidth * mScaleY);
        cropLayoutParams.width = (int) (mMaskWidth * mScaleX);
        mCropLayout.setLayoutParams(cropLayoutParams);
        ImageView mQrLineView = (ImageView) findViewById(R.id.capture_scan_line);
        TranslateAnimation mAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,
                0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.9f);
        mAnimation.setDuration(1500);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        mQrLineView.setAnimation(mAnimation);
    }


    private void initMaskScale() {
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if (width > height) {//960F
            mMaskWidth = 600;
            mMaskTop = 50;
            mScaleX = width / 1280d;
            mScaleY = height / 720d;
        } else {//V8
            mMaskWidth = 400;
            mMaskTop = 200;
            mScaleX = width / 540d;
            mScaleY = height / 960d;
        }

    }

    private void updateTorchState() {
        if (mCameraManager.getFlashMode() != 0) {
            mLightDescription.setText(R.string.tap_close_flashlight);
            mLightIcon.setImageResource(R.drawable.closelight);
        } else {
            mLightDescription.setText(R.string.flashlight_on);
            mLightIcon.setImageResource(R.drawable.openlight);
        }
    }


    private void stopPreview() {
        mCameraManager.stop();
    }


    private void startPreview() {
        mCameraManager.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraManager.release();
    }

    private void playBeepSoundAndVibrate() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == 211
                || event.getKeyCode() == KeyEvent.KEYCODE_MENU)
            return true;

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.light_control_layout:
                switchLight();
                break;
            case R.id.switch_camera:
                switchCamera();
                break;
        }
    }


    private void switchCamera() {
        stopPreview();
        mCameraManager.switchCamera();
        startPreview();
    }

    private void switchLight() {
        mCameraManager.switchFlashLight();
        updateTorchState();
    }

    @Override
    public void onDecode(int ret, DecodeResult result) {
        if (ret == 0) {
            Bundle b = new Bundle();
            b.putBoolean("result", true);
            b.putString("txtResult", result.getContent());
            playBeepSoundAndVibrate();
            Intent intent = new Intent();
            intent.putExtras(b);
            setResult(RESULT_OK, intent);
        }
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //cancel
        mCameraManager.onCanceled();
    }
}
