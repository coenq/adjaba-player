package com.rnd.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rnd.R;
import com.rnd.camera.CameraSource;
import com.rnd.camera.CameraSourcePreview;
import com.rnd.face_detection.FaceRecognitionProcessor;
import com.rnd.others.DataHolder;
import com.rnd.others.GraphicOverlay;
import com.rnd.utilities.TinyDB;

import java.io.IOException;

import static com.rnd.face_detection.FaceRecognitionProcessor.handler;
import static com.rnd.face_detection.FaceRecognitionProcessor.runnable;

public class TestCamera extends AppCompatActivity {


    public static CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private static String TAG = "Test Camera";
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    boolean back_camera = false;
    int orientation;
    public static TestCamera instance;
    public static LinearLayout container;

    public static Activity ac;
    TinyDB tinyDb;
    public static TextView key_age, value_age;
    public static TextView key_object;
    public static TextView value_object;
    public static TextView value_txt, mood_txt;
    FaceRecognitionProcessor faceRecognitionProcessor;
    public static String orient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        instance = this;
        ac = this;
        tinyDb = new TinyDB(ac);
        container=findViewById(R.id.ageContainer);
        orientation = tinyDb.getInt("Orientation");
        back_camera = tinyDb.getBoolean("BackCamera");
        if (orientation == 0) {
            orient = "portrait";
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            orient = "landscape";
        }
        preview = findViewById(R.id.camera_source_preview);


        key_age = findViewById(R.id.key_age);
        value_age = findViewById(R.id.value_age);

        key_object = findViewById(R.id.key_object);
        value_object = findViewById(R.id.value_object);

        value_txt = findViewById(R.id.value_txt);
        mood_txt = findViewById(R.id.mood_tv);
        faceRecognitionProcessor = new FaceRecognitionProcessor(getAssets(), false, this);
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
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }

        if (cameraSource == null) {
            createCameraSource();
        }
        startCameraSource(); // ✅ نشغل الكاميرا كل مرة نرجع للشاشة
    }


    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (cameraSource != null) {
                cameraSource.release(); // يقفل الكاميرا والـ Threadات
                cameraSource = null;
            }
            if (preview != null) {
                preview.stop();
                preview.release();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void createCameraSource() {
        createCameraSource(false);
    }

    private void createCameraSource(boolean frontFacingCamera) {

        if (cameraSource != null) {
            cameraSource.stop();
            cameraSource.release();
        }
        cameraSource = new CameraSource(this, graphicOverlay);
        if (back_camera) {
            cameraSource.setFacing(frontFacingCamera ? CameraSource.CAMERA_FACING_BACK : CameraSource.CAMERA_FACING_BACK);
        } else {
            cameraSource.setFacing(frontFacingCamera ? CameraSource.CAMERA_FACING_FRONT : CameraSource.CAMERA_FACING_FRONT);
        }
        cameraSource.setMachineLearningFrameProcessor(faceRecognitionProcessor);
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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createCameraSource();
                    startCameraSource();
                } else {
                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCameraAndProcessor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCameraAndProcessor();
    }

    @Override
    public void onBackPressed() {
        stopCameraAndProcessor();
        finish();
        super.onBackPressed();
    }

    private void stopCameraAndProcessor() {
        try {
            // أوقف الـ faceRecognitionProcessor لو شغال
            if (faceRecognitionProcessor != null) {
                faceRecognitionProcessor.stop1();

                if (handler != null && faceRecognitionProcessor.runnableCode != null) {
                    handler.removeCallbacks(faceRecognitionProcessor.runnableCode);
                    faceRecognitionProcessor.runnableCode = null;
                }

                faceRecognitionProcessor.stop();  // لو عندك stop عام
                faceRecognitionProcessor = null;  // مهم عشان ما يتندهش عليه تاني
            }

            // أوقف الـ Runnable العام (لو فيه غير اللي في faceRecognitionProcessor)
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
                runnable = null;
            }

            // أوقف الكاميرا نفسها
            if (cameraSource != null) {
                cameraSource.stop();     // يوقف الـ preview
                cameraSource.release();  // يحرر الموارد
                cameraSource = null;
            }

            // أوقف الـ Preview (SurfaceView/TextureView)
            if (preview != null) {
                preview.stop();
                preview = null;
            }

            Log.d("CameraDebug", "Camera + Processor stopped successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
