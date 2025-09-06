package com.rnd.activities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.rnd.R;
import com.rnd.camera.CameraSource;
import com.rnd.camera.CameraSourcePreview;
import com.rnd.face_detection.FaceRecognitionProcessor;
import com.rnd.others.GraphicOverlay;
import com.rnd.utilities.TinyDB;

import java.io.IOException;

public class TestCamera extends AppCompatActivity {


    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private static String TAG = "Test Camera";
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    boolean back_camera = false;
    int orientation;

    public static Activity ac;
    TinyDB tinyDb;
    public static TextView key_age, value_age;
    public static TextView key_object;
    public static TextView value_object;
    public static TextView value_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        ac = this;
        tinyDb = new TinyDB(ac);

        orientation = tinyDb.getInt("Orientation");
        back_camera = tinyDb.getBoolean("BackCamera");
        if (orientation == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        preview = findViewById(R.id.camera_source_preview);


        key_age = findViewById(R.id.key_age);
        value_age = findViewById(R.id.value_age);

        key_object = findViewById(R.id.key_object);
        value_object = findViewById(R.id.value_object);

        value_txt = findViewById(R.id.value_txt);

        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.graphics_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            createCameraSource();
            startCameraSource();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cameraSource != null) {
            try {
                cameraSource.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            cameraSource = null;
        }
    }


    private void createCameraSource() {
        createCameraSource(false);
    }

    private void createCameraSource(boolean frontFacingCamera) {

        if (cameraSource != null) {
            onPause();
            cameraSource.release();
        }
        cameraSource = new CameraSource(this, graphicOverlay);
        if (back_camera) {
            cameraSource.setFacing(frontFacingCamera ? CameraSource.CAMERA_FACING_BACK : CameraSource.CAMERA_FACING_BACK);
        } else {
            cameraSource.setFacing(frontFacingCamera ? CameraSource.CAMERA_FACING_FRONT : CameraSource.CAMERA_FACING_FRONT);
        }
        cameraSource.setMachineLearningFrameProcessor(new FaceRecognitionProcessor(getAssets(), frontFacingCamera, this));
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createCameraSource();
                    startCameraSource();
                } else {}
                return;
            }
        }
    }
}
