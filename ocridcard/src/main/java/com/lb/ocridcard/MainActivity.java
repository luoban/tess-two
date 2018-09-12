package com.lb.ocridcard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.centerm.dev.barcode.decode.CaptureActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private OCRManager mOcrManager;
    private TextView mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResult = findViewById(R.id.result_text);
        findViewById(R.id.start_decode).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_decode:
                startDecode();
                break;
        }
    }

    private void startDecode() {
        startActivityForResult(new Intent(this, CaptureActivity.class), 1024);
    }
}
