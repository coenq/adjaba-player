// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.rnd.face_detection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.rnd.newmodels.ScreenRecord;
import com.rnd.others.APIImpression;
import com.rnd.others.DataHolder;
import com.rnd.others.FrameMetadata;
import com.rnd.others.GraphicOverlay;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.rnd.report.ReportAPI;
import com.rnd.room.AdDatabase;
import com.rnd.room.ImpressionEntity;
import com.rnd.room.ReportDataBase;
import com.rnd.room.ReportEntity;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.tensorflow.lite.Interpreter;

import static com.rnd.activities.TestCamera.ac;
import static com.rnd.activities.TestCamera.cameraSource;
import static com.rnd.activities.TestCamera.female_number;
import static com.rnd.activities.TestCamera.all_number;
import static com.rnd.activities.TestCamera.sadPeople;
import static com.rnd.activities.TestCamera.key_age;
import static com.rnd.activities.TestCamera.key_object;
import static com.rnd.activities.TestCamera.maleNum;
import static com.rnd.activities.TestCamera.femaleNum;
import static com.rnd.activities.TestCamera.value_age;
import static com.rnd.activities.TestCamera.value_object;
import static com.rnd.activities.TestCamera.value_txt;
import static com.rnd.activities.TestCamera.mood_txt;
import static com.rnd.activities.TestCamera.orient;
import static com.rnd.activities.TestCamera.happyPeople;
import static com.rnd.activities.TestCamera.neutralPeople;

public class FaceRecognitionProcessor {

    private ByteBuffer imgData;
    private Interpreter tflite;
    private int[] intValuesDetection;
    private float[][][] outputLocations;
    public Runnable runnableCode;
    private float[][] outputClasses;
    public int female20 = 0;
    public int phone = 0;
    public int watch = 0;
    public int laptop = 0;
    public int people = 0;
    public List<String> sentimentCount = new ArrayList<>();
    public int female32 = 0;
    public Set<String> detectedObjects = new HashSet<>();
    public List<String> tags = new ArrayList<>();
    public Set<String> detecteddText = new HashSet<>();
    public int female40 = 0;
    public int female50 = 0;
    public int female50plus = 0;
    public int male20 = 0;
    public int male32 = 0;
    public int male40 = 0;
    public int male50 = 0;
    public int male50plus = 0;
    int maxAt = 0;
    String screenViewId = "view-" + String.format("%03d", new Random().nextInt(100000));
    private float[][] outputScores;
    private float[] numDetections;
    private final List<float[]> uniqueFaces = new ArrayList<>();
    private static final float MATCH_THRESHOLD = 0.91f;

    public static Handler handler = new Handler(Looper.getMainLooper());
    public static Runnable runnable;
    private Vector<String> labels = new Vector<String>();

    private static final String TAG = "TextRecProc";
    private long lastUploadTime = 0;
    private long lastUploadTime1 = 0;
    int i = 0;
    int angrySM, disgustSM, fearSM, happySM, sadSM, surpriseSM, neutralSM = 0;
    int angrySF, disgustSF, fearSF, happySF, sadSF, surpriseSF, neutralSF = 0;

    private final FirebaseVisionFaceDetector detector;
    private EmotionClassifier emotionClassifier;
    private String detectedMood = "N/A";
    long child = 0, teen = 0, adult = 0, senior = 0;
    private TensorFlowInferenceInterface genderIinferenceInterface;
    private TensorFlowInferenceInterface ageInferenceInterface;
    private final int CARNIE_DIM = 227;
    private float[] floatValues = new float[64 * 64 * 3];
    private int[] intValues = new int[64 * 64];
    private float[] floatValuesCarnie = new float[CARNIE_DIM * CARNIE_DIM * 3];
    private int[] intValuesCarnie = new int[CARNIE_DIM * CARNIE_DIM];
    private final List<String> AGE_LIST = Arrays.asList("(0, 2)", "(4, 6)", "(8, 12)", "(15, 20)", "(25, 32)", "(38, 43)", "(48, 53)", "(60, 100)");
    private final int[] ageListMapping = {0, 0, 0, 0, 1, 2, 3, 4};
    private final List<String> AGE_LIST_2 = Arrays.asList("(0, 20)", "(20, 32)", "(32, 43)", "(43, 53)", "(53, 100)");
    private static final int EMBEDDING_SIZE = 192;
    long lastUpdateTime = System.currentTimeMillis();
    long lastUpdateTime1 = System.currentTimeMillis();
    long lastUpdateTime3 = System.currentTimeMillis();
    public Set<String> objects = new HashSet<>();
    public int male = 0, female = 0, allPeople = 0;
    private FaceNetModel faceNetModel;
    String angryMale, angryFemale, disgustMale, disgustFemale, fearMale, fearFemale, happyMale, happyFemale, sadMale, sadFemale, surpriseMale, surpriseFemale, neutralMale, neutralFemale;
    int angryM, disgustM, fearM, happyM, sadM, surpriseM, neutralM = 0;
    int angryF, disgustF, fearF, happyF, sadF, surpriseF, neutralF = 0;

    private float[] genderOutputs = new float[1];
    private float[] ageOutputs = new float[8];
    private boolean frontFacingCamera;
    public static String detectedGender = "N/A";
    public static String detectedAgeRange = "N/A";
    private List<String> viewList = new ArrayList<>();

    public static List<String> viewCountList = new ArrayList<>();
    String textFound = "";


    // Whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle.
    private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);
    private Context context;

    public FaceRecognitionProcessor(AssetManager assetManager, boolean frontFacingCamera, Context mainActivity) {
        this.genderIinferenceInterface = new TensorFlowInferenceInterface(assetManager, "gender_model.pb");
        this.ageInferenceInterface = new TensorFlowInferenceInterface(assetManager, "rude_carnie_age_model.pb");
        try {
            this.tflite = new Interpreter(loadModelFile(assetManager, "detect.tflite"));
            faceNetModel = new FaceNetModel(assetManager, "mobilefacenet.tflite");

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.emotionClassifier = new EmotionClassifier(assetManager, "model.tflite");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.frontFacingCamera = frontFacingCamera;

        detector = FirebaseVision.getInstance().getVisionFaceDetector();
        this.imgData = ByteBuffer.allocateDirect(300 * 300 * 3);
        this.imgData.order(ByteOrder.nativeOrder());
        this.intValuesDetection = new int[300 * 300];
        this.outputLocations = new float[1][10][4];
        this.outputClasses = new float[1][10];
        this.outputScores = new float[1][10];
        this.numDetections = new float[1];
        this.context = mainActivity;

        InputStream labelsInput = null;
        String actualFilename = "labelmap.txt";
        try {
            labelsInput = assetManager.open(actualFilename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String line = "";
        BufferedReader br = null;
        br = new BufferedReader(new InputStreamReader(labelsInput));
        while (true) {
            try {
                if (!((line = br.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.labels.add(line);
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            detector.close();

        } catch (IOException e) {
        }
    }

    public void process(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) {

        if (shouldThrottle.get()) {
            return;
        }
        FirebaseVisionImageMetadata metadata =
                new FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(frameMetadata.getWidth())
                        .setHeight(frameMetadata.getHeight())
                        .setRotation(frameMetadata.getRotation())
                        .build();

        detectInVisionImage(FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata, graphicOverlay, data);
    }

    //endregion

    //region ----- Helper Methods -----

    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    int frame = 0;
    private long lastProcessedTime = 0;
    private final long PROCESS_INTERVAL = 200;

    @SuppressLint("SetTextI18n")
    protected void onSuccess(@NonNull List<FirebaseVisionFace> results, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay, @NonNull Bitmap bitmap) {
       /* long currentTime1 = System.currentTimeMillis();

        // لو لسه الوقت ماكملش — متعالجش الفريم
        if (currentTime1 - lastProcessedTime < PROCESS_INTERVAL) {
            return;
        }

        // سجّل آخر وقت تمت فيه المعالجة
        lastProcessedTime = currentTime1;*/
        graphicOverlay.clear();
        //container.setOrientation(LinearLayout.VERTICAL);
        try {
            key_age.setText("");
            value_age.setText("");
            value_object.setText("");
            value_txt.setText("");
            mood_txt.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        for (FirebaseVisionFace result : results) {
            // crop face bitmap
            Rect rect = result.getBoundingBox();

//			// Crop even more off (the api returns a bounding box that encompasses more than just face)
//			int dw = rect.width()/12;
//			int dh = rect.height()/12;
//
//			rect.left = rect.left + dw;
//			rect.right = rect.right - dw;
//
//			rect.top = rect.top + dh;
//			rect.bottom = rect.bottom - dh;

            if (rect.left + rect.width() >= bitmap.getWidth()) {
                rect.right = bitmap.getWidth();
            }
            if (rect.top + rect.height() >= bitmap.getHeight()) {
                rect.bottom = bitmap.getHeight();
            }
            if (rect.top < 0) {
                rect.top = 0;
            }
            if (rect.left < 0) {
                rect.left = 0;
            }

            Bitmap resultBmp = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());

            // process bitmap here
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(resultBmp, 64, 64, false);

            scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
            for (int i = 0; i < intValues.length; ++i) {
                final int val = intValues[i];
                floatValues[i * 3 + 2] = (((val >> 16) & 0xFF));
                floatValues[i * 3 + 1] = (((val >> 8) & 0xFF));
                floatValues[i * 3 + 0] = ((val & 0xFF));
            }

            String inputName = "input_2";
            String outputName = "pred/mul_33";

            /*** Run Gender Detection Model First ***/
            if (emotionClassifier != null) {
                detectedMood = emotionClassifier.predictEmotion(scaledBitmap);
                mood_txt.setText(detectedMood);
            }
            // Copy the input data into TensorFlow.
            genderIinferenceInterface.feed(inputName, floatValues, 1, 64, 64, 3);

            // Run the inference call.
            genderIinferenceInterface.run(new String[]{outputName});

            // Copy the output Tensor back into the output array.
            genderIinferenceInterface.fetch(outputName, genderOutputs);
            if (genderOutputs[0] <= 0.72) {
                detectedGender = "F";

            } else {
                detectedGender = "M";

            }

            /*** Run Age Detection Model ***/
            String inputName1 = "Placeholder";
            String outputName1 = "output/output";

            // Rude carnie expects different input dimensions
            scaledBitmap = Bitmap.createScaledBitmap(resultBmp, CARNIE_DIM, CARNIE_DIM, false);

            scaledBitmap.getPixels(intValuesCarnie, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
            for (int i = 0; i < intValuesCarnie.length; ++i) {
                final int val = intValuesCarnie[i];
                // Rude Carnie is in RGB Format
                floatValuesCarnie[i * 3 + 0] = (((val >> 16) & 0xFF));
                floatValuesCarnie[i * 3 + 1] = (((val >> 8) & 0xFF));
                floatValuesCarnie[i * 3 + 2] = ((val & 0xFF));
            }

            // Copy the input data into TensorFlow.
            ageInferenceInterface.feed(inputName1, floatValuesCarnie, 1, CARNIE_DIM, CARNIE_DIM, 3);

            // Run the inference call.
            ageInferenceInterface.run(new String[]{outputName1});

            // Copy the output Tensor back into the output array.
            ageInferenceInterface.fetch(outputName1, ageOutputs);


            maxAt = 0;
            for (int i = 0; i < ageOutputs.length; i++) {
                maxAt = ageOutputs[i] > ageOutputs[maxAt] ? i : maxAt;
            }

            detectedAgeRange = AGE_LIST_2.get(ageListMapping[maxAt]);


            try {
                key_age.setText(detectedAgeRange);
                value_age.setText(String.valueOf(detectedGender));
            } catch (Exception e) {
                try {
                    key_age.setText("");
                    value_age.setText("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            viewList.add(detectedAgeRange + "#" + detectedGender);


            try {
                // filter the distinct
                Set<String> setWithUniqueValues = new HashSet<>(viewList);
                viewCountList = new ArrayList<>(setWithUniqueValues);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // add graphic overlay
            GraphicOverlay.Graphic faceGraphic = new FaceGraphic(graphicOverlay, result, scaledBitmap, detectedGender, detectedAgeRange, frontFacingCamera, detectedMood);
            graphicOverlay.add(faceGraphic);

            try {
//				cameraSource.release();
            } catch (Exception e) {
                try {
//					TestCamera.cameraSource.release();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }



            /* * This is Text Recognition Section
             */
            TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
            try {
                if (!textRecognizer.isOperational()) {
                    new AlertDialog.
                            Builder(context).
                            setMessage("Text recognizer could not be set up on your device").show();
                    return;
                }

                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> origTextBlocks = textRecognizer.detect(frame);
                List<TextBlock> textBlocks = new ArrayList<>();
                for (int i = 0; i < origTextBlocks.size(); i++) {
                    TextBlock textBlock = origTextBlocks.valueAt(i);
                    textBlocks.add(textBlock);
                }
                Collections.sort(textBlocks, new Comparator<TextBlock>() {
                    @Override
                    public int compare(TextBlock o1, TextBlock o2) {
                        int diffOfTops = o1.getBoundingBox().top - o2.getBoundingBox().top;
                        int diffOfLefts = o1.getBoundingBox().left - o2.getBoundingBox().left;
                        if (diffOfTops != 0) {
                            return diffOfTops;
                        }
                        return diffOfLefts;
                    }
                });

                StringBuilder detectedText = new StringBuilder();
                for (TextBlock textBlock : textBlocks) {
                    if (textBlock != null && textBlock.getValue() != null) {
                        detectedText.append(textBlock.getValue());
                        detecteddText.add(textBlock.getValue());
                        detectedText.append("\n");
                    }
                }
                textFound = detectedText.toString();
                try {
                    value_txt.setText(String.valueOf(textFound));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } finally {
                textRecognizer.release();
            }


            /*
             * This is Object Detection Section
             * */


            try {

                Bitmap scaledFace = Bitmap.createScaledBitmap(resultBmp, 112, 112, true);
                float[] embedding = faceNetModel.getEmbedding(scaledFace);

                if (embedding != null && embedding.length == EMBEDDING_SIZE) {

                    if (!isSamePerson(embedding)) {
                        i++;
                        if (i < 1) return;
                        i = 0;
                        if (ageListMapping[maxAt] == 0) {
                            child++;
                        } else if (ageListMapping[maxAt] == 1) {
                            adult++;
                        } else if (ageListMapping[maxAt] == 2) {
                            teen++;
                        } else if (ageListMapping[maxAt] == 3 || ageListMapping[maxAt] == 4) {
                            senior++;
                        }
                        if (detectedGender.equals("M")) {
                            male++;
                            switch (detectedMood) {
                                case "Angry":
                                    angryM++;
                                    angrySM++;
                                    break;
                                case "Disgust":
                                    disgustM++;
                                    disgustSM++;
                                    break;
                                case "Fear":
                                    fearM++;
                                    fearSM++;
                                    break;
                                case "Happy":
                                    happyM++;
                                    happySM++;
                                    break;
                                case "Sad":
                                    sadM++;
                                    sadSM++;
                                    break;
                                case "Surprise":
                                    surpriseM++;
                                    surpriseSM++;
                                    break;
                                case "Neutral":
                                    neutralM++;
                                    neutralSM++;
                                    break;
                                default:
                                    break;
                            }
                            if (ageListMapping[maxAt] == 0) male20++;
                            if (ageListMapping[maxAt] == 1) male32++;
                            if (ageListMapping[maxAt] == 2) male40++;
                            if (ageListMapping[maxAt] == 3) male50++;
                            if (ageListMapping[maxAt] == 4) male50plus++;


                        } else {
                            female++;
                            switch (detectedMood) {
                                case "Angry":
                                    angryF++;
                                    angrySF++;
                                    break;
                                case "Disgust":
                                    disgustF++;
                                    disgustSF++;
                                    break;
                                case "Fear":
                                    fearF++;
                                    fearSF++;
                                    break;
                                case "Happy":
                                    happyF++;
                                    happySF++;
                                    break;
                                case "Sad":
                                    sadF++;
                                    sadSF++;
                                    break;
                                case "Surprise":
                                    surpriseF++;
                                    surpriseSF++;
                                    break;
                                case "Neutral":
                                    neutralF++;
                                    neutralSF++;
                                    break;
                                default:
                                    break;
                            }
                            if (ageListMapping[maxAt] == 0) female20++;
                            if (ageListMapping[maxAt] == 1) female32++;
                            if (ageListMapping[maxAt] == 2) female40++;
                            if (ageListMapping[maxAt] == 3) female50++;
                            if (ageListMapping[maxAt] == 4) female50plus++;

                        }
                        uniqueFaces.add(embedding);
                        ac.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (happyF + happyM > 0)
                                    happyPeople.setText(happyF + happyM + "");
                                if (sadF + sadM > 0)
                                    sadPeople.setText(sadF + sadM + "");
                                if (neutralF + neutralM > 0)
                                    neutralPeople.setText(neutralF + neutralM + "");

                                if ((male + female) > 0) {
                                    all_number.setText(uniqueFaces.size() + "");
                                    maleNum.setText((int) Math.ceil((male * 100) / (male + female)) + "% Male");
                                    femaleNum.setText(100 - Integer.parseInt(maleNum.getText().toString().replace("% Male", "")) + "% Female");

                                }
                            }
                        });


                    }
                }
            } catch (Exception e) {

            }

        }
        Bitmap scaledBitmap3 = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
        scaledBitmap3.getPixels(intValuesDetection, 0, scaledBitmap3.getWidth(), 0, 0, scaledBitmap3.getWidth(), scaledBitmap3.getHeight());

        imgData.rewind();
        for (int i = 0; i < 300; ++i) {
            for (int j = 0; j < 300; ++j) {
                int pixelValue = intValuesDetection[i * 300 + j];
                imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                imgData.put((byte) (pixelValue & 0xFF));
            }
        }


        Object[] inputArray = {imgData};
        outputLocations = new float[1][10][4];
        outputClasses = new float[1][10];
        outputScores = new float[1][10];
        numDetections = new float[1];

        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, outputLocations); //You can draw bounding boxes from using these locations
        outputMap.put(1, outputClasses);
        outputMap.put(2, outputScores);
        outputMap.put(3, numDetections);


        // Running Model
        tflite.runForMultipleInputsOutputs(inputArray, outputMap);

        final ArrayList<Recognition> recognitions = new ArrayList<>(10);
        for (int i = 0; i < 10; ++i) {
            final RectF detection =
                    new RectF(
                            outputLocations[0][i][1] * 300,
                            outputLocations[0][i][0] * 300,
                            outputLocations[0][i][3] * 300,
                            outputLocations[0][i][2] * 300);
            // SSD Mobilenet V1 Model assumes class 0 is background class
            // in label file and class labels start from 1 to number_of_classes+1,
            // while outputClasses correspond to class index from 0 to number_of_classes
            int labelOffset = 1;
            recognitions.add(
                    new Recognition(
                            "" + i,
                            labels.get((int) outputClasses[0][i] + labelOffset),
                            outputScores[0][i],
                            detection));
        }

        // List which stores type and number of objects detected
        Map<String, Integer> counts = new HashMap<String, Integer>();

        for (Recognition r : recognitions) {
            final RectF location = r.getLocation();
            if (location != null && r.getConfidence() >= 0.5f) {
                if (counts.containsKey(r.getTitle())) {
                    counts.put(r.getTitle(), counts.get(r.getTitle()) + 1);
                } else {
                    counts.put(r.getTitle(), 1);
                }
            }
        }

        //Just for Debugging, Printing list of counts
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            String key = entry.getKey();
            Integer count = entry.getValue();
            detectedObjects.add(key);
            objects.add(key);

            try {
                key_object.setText(key);
                value_object.setText(String.valueOf(count));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .enableTracking()
                .build();
        FaceDetector detector = FaceDetection.getClient(options);
        detector.process(image)
                .addOnSuccessListener(faces -> {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUpdateTime >= 10000) {
                        lastUpdateTime = currentTime;
                        saveAndSendImpression(context);
                    }

                    if (currentTime - lastUpdateTime1 >= 600000) {
                        lastUpdateTime1 = currentTime;
                        if (isInternetAvailable(context)) {
                            Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show();
                            ReportDataBase adReportDataBase = ReportDataBase.getInstance(context);
                            AdDatabase adDatabase = AdDatabase.getInstance(context);
                            new Thread(() -> {
                                try {
                                    List<ImpressionEntity> impressions = adDatabase.impDao().getAllImpressions();
                                    int nMale20 = 0, nMale32 = 0, nMale40 = 0, nMale50 = 0, nMale50Plus = 0, nFemale20 = 0, nFemale32 = 0, nFemale40 = 0, nFemale50 = 0, nFemale50Plus = 0;
                                    List<ReportEntity> newReport = adReportDataBase.reportDao().getAllReportsByHour(Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault()).format(new Date())), Integer.parseInt(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date())));

                                    for (int i = 0; i < newReport.size(); i++) {
                                        nMale20 += newReport.get(i).male20;
                                        nMale32 += newReport.get(i).male32;
                                        nMale40 += newReport.get(i).male40;
                                        nMale50 += newReport.get(i).male50;
                                        nMale50Plus += newReport.get(i).male50plus;
                                        nFemale20 += newReport.get(i).female20;
                                        nFemale32 += newReport.get(i).female32;
                                        nFemale40 += newReport.get(i).female40;
                                        nFemale50 += newReport.get(i).female50;
                                        nFemale50Plus += newReport.get(i).female50plus;

                                    }
                                    String angryMaleS, angryFemaleS, disgustMaleS, disgustFemaleS, fearMaleS, fearFemaleS, happyMaleS, happyFemaleS, sadMaleS, sadFemaleS, surpriseMaleS, surpriseFemaleS, neutralMaleS, neutralFemaleS;

                                    angryMaleS = "M" + "/" + angrySM + "/" + "Angry";
                                    angryFemaleS = "F" + "/" + angrySF + "/" + "Angry";
                                    happyMaleS = "M" + "/" + happySM + "/" + "Happy";
                                    happyFemaleS = "F" + "/" + happySF + "/" + "Happy";
                                    disgustMaleS = "M" + "/" + disgustSM + "/" + "Disgust";
                                    disgustFemaleS = "F" + "/" + disgustSF + "/" + "Disgust";
                                    fearMaleS = "M" + "/" + fearSM + "/" + "Fear";
                                    fearFemaleS = "F" + "/" + fearSF + "/" + "Fear";
                                    sadMaleS = "M" + "/" + sadSM + "/" + "Sad";
                                    sadFemaleS = "F" + "/" + sadSF + "/" + "Sad";
                                    surpriseMaleS = "M" + "/" + surpriseSM + "/" + "Surprise";
                                    surpriseFemaleS = "F" + "/" + surpriseSF + "/" + "Surprise";
                                    neutralMaleS = "M" + "/" + neutralSM + "/" + "Neutral";
                                    neutralFemaleS = "F" + "/" + neutralSF + "/" + "Neutral";
                                    sentimentCount.add(happyMaleS);
                                    sentimentCount.add(happyFemaleS);
                                    sentimentCount.add(sadMaleS);
                                    sentimentCount.add(sadFemaleS);
                                    sentimentCount.add(disgustMaleS);
                                    sentimentCount.add(disgustFemaleS);
                                    sentimentCount.add(angryMaleS);
                                    sentimentCount.add(angryFemaleS);
                                    sentimentCount.add(fearMaleS);
                                    sentimentCount.add(fearFemaleS);
                                    sentimentCount.add(surpriseMaleS);
                                    sentimentCount.add(surpriseFemaleS);
                                    sentimentCount.add(neutralMaleS);
                                    sentimentCount.add(neutralFemaleS);

                                    int count = nMale20 + nMale32 + nMale40 + nMale50 + nMale50Plus + nFemale20 + nFemale32 + nFemale40 + nFemale50 + nFemale50Plus;
                                    ScreenRecord screenRecord = new ScreenRecord();
                                    screenRecord.setScreenViewId(screenViewId);
                                    screenRecord.setScreenId(DataHolder.getInstance().screenID);
                                    screenRecord.setAmountSettled(true);
                                    screenRecord.setCurrency("USD");
                                    screenRecord.setFormat("HD");
                                    screenRecord.setTextDetected(null);
                                    screenRecord.setDayHour(Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault()).format(new Date())));
                                    screenRecord.setLocationType(DataHolder.getInstance().locationTypes);
                                    screenRecord.setFemale20(nFemale20);
                                    screenRecord.setFemale32(nFemale32);
                                    screenRecord.setFemale40(nFemale40);
                                    screenRecord.setFemale50(nFemale50);
                                    screenRecord.setFemale50plus(nFemale50Plus);
                                    screenRecord.setMale20(nMale20);
                                    screenRecord.setMale32(nMale32);
                                    screenRecord.setMale40(nMale40);
                                    screenRecord.setMale50(nMale50);
                                    screenRecord.setMale50plus(nMale50Plus);
                                    screenRecord.setPlaySec(45);
                                    screenRecord.setImpressionCost(0);
                                    screenRecord.setOrientation(orient);
                                    screenRecord.setViewCount(count);
                                    screenRecord.setObjectDetected(new ArrayList<>(objects));
                                    screenRecord.setRecordDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                                    screenRecord.setRecordTime(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
                                    screenRecord.setScreenPlayer("DD");
                                    screenRecord.setSentiemtCount(sentimentCount);
                                    screenRecord.setScreenDevice("Saa");
                                    // uploadReport(screenRecord, context);
                                    angrySM = 0;
                                    disgustSM = 0;
                                    fearSM = 0;
                                    happySM = 0;
                                    sadSM = 0;
                                    surpriseSM = 0;
                                    neutralSM = 0;
                                    angrySF = 0;
                                    disgustSF = 0;
                                    fearSF = 0;
                                    happySF = 0;
                                    sadSF = 0;
                                    surpriseSF = 0;
                                    neutralF = 0;
                                    uniqueFaces.clear();
                                    objects.clear();
                                    for (ImpressionEntity impression1 : impressions) {

                                        APIImpression.sendImpression(context, impression1);
                                    }
                                    ReportAPI.sendImpression(context, screenRecord);
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                                            sentimentCount.clear();
                                        }
                                    });
                                } catch (Exception e) {
                                }

                            }).start();


                        } else {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Check Internet !", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                });

    }

    protected void onFailure(@NonNull Exception e) {
    }

    private void detectInVisionImage(FirebaseVisionImage image, final FrameMetadata metadata, final GraphicOverlay graphicOverlay, final ByteBuffer data) {
        viewList.clear();

        final Bitmap bm = image.getBitmap();
        detectInImage(image).addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionFace> results) {
                                shouldThrottle.set(false);
                                com.rnd.face_detection.FaceRecognitionProcessor.this.onSuccess(results, metadata, graphicOverlay, bm);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                shouldThrottle.set(false);
                                com.rnd.face_detection.FaceRecognitionProcessor.this.onFailure(e);
                            }
                        });
        // Begin throttling until this frame of input has been processed, either in onSuccess or
        // onFailure.
        shouldThrottle.set(true);

    }

    public void stop1() {
        try {
            if (cameraSource != null) {
                cameraSource.stop();
                cameraSource.release();
                cameraSource = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //endregion

    // Object Detection Utils
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public class Recognition {
        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         */
        private final String id;

        /** Display name for the recognition. */
        private final String title;

        /**
         * A sortable score for how good the recognition is relative to others. Higher should be better.
         */
        private final Float confidence;

        /** Optional location within the source image for the location of the recognized object. */
        private RectF location;

        public Recognition(
                final String id, final String title, final Float confidence, final RectF location) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }
    }

    public void saveAndSendImpression(Context context) {
        angryMale = "M" + "/" + angryM + "/" + "Angry";
        angryFemale = "F" + "/" + angryF + "/" + "Angry";
        happyMale = "M" + "/" + happyM + "/" + "Happy";
        happyFemale = "F" + "/" + happyF + "/" + "Happy";
        disgustMale = "M" + "/" + disgustM + "/" + "Disgust";
        disgustFemale = "F" + "/" + disgustF + "/" + "Disgust";
        fearMale = "M" + "/" + fearM + "/" + "Fear";
        fearFemale = "F" + "/" + fearF + "/" + "Fear";
        sadMale = "M" + "/" + sadM + "/" + "Sad";
        sadFemale = "F" + "/" + sadF + "/" + "Sad";
        surpriseMale = "M" + "/" + surpriseM + "/" + "Surprise";
        surpriseFemale = "F" + "/" + surpriseF + "/" + "Surprise";
        neutralMale = "M" + "/" + neutralM + "/" + "Neutral";
        neutralFemale = "F" + "/" + neutralF + "/" + "Neutral";
        tags.add(happyMale);
        tags.add(happyFemale);
        tags.add(sadMale);
        tags.add(sadFemale);
        tags.add(disgustMale);
        tags.add(disgustFemale);
        tags.add(angryMale);
        tags.add(angryFemale);
        tags.add(fearMale);
        tags.add(fearFemale);
        tags.add(surpriseMale);
        tags.add(surpriseFemale);
        tags.add(neutralMale);
        tags.add(neutralFemale);

        ReportEntity reportEntity = new ReportEntity();
        ImpressionEntity impression = new ImpressionEntity();
        impression.screenViewId = screenViewId;
        impression.amountSettled = true;
        impression.currency = "USD";
        impression.dayHour = Long.parseLong(new SimpleDateFormat("HH", Locale.getDefault()).format(new Date()));
        impression.playSec = 45;
        impression.format = "HD";
        impression.locationType = "Indoor";
        impression.orientation = orient;
        impression.playTimeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
        impression.screenDevice = "Saa";
        impression.screenPlayer = "DD";
        impression.screenId = DataHolder.getInstance().screenID;
        impression.viewCount = male20 + male32 + male40 + male50 + male50plus + female20 + female32 + female40 + female50 + female50plus;
        impression.male20 = male20;
        impression.male32 = male32;
        impression.male40 = male40;
        impression.male50 = male50;
        impression.happy = (long) (happyF + happyM);
        impression.sad = (long) (sadF + sadM);
        impression.neutral = (long) (neutralM + neutralF);
        impression.male50plus = male50plus;
        impression.female20 = female20;
        impression.female32 = female32;
        impression.female40 = female40;
        impression.female50 = female50;
        impression.female50plus = female50plus;
        impression.tags = tags;
        impression.textDetected = new ArrayList<>(detecteddText);
        impression.objectDetected = new ArrayList<>(detectedObjects);
        impression.impressionCost = 0;

        //
        reportEntity.hour = Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault()).format(new Date()));
        reportEntity.day = Integer.parseInt(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()));
        reportEntity.female20 = female20;
        reportEntity.female32 = female32;
        reportEntity.female40 = female40;
        reportEntity.female50 = female50;
        reportEntity.female50plus = female50plus;
        reportEntity.male20 = male20;
        reportEntity.male32 = male20;
        reportEntity.male40 = male40;
        reportEntity.male50 = male50;
        reportEntity.male50plus = male50plus;
        reportEntity.sad = (sadF + sadM);
        reportEntity.happy = happyF + happyM;
        reportEntity.neutral = neutralF + neutralM;
        reportEntity.adult = male32 + male32;
        reportEntity.child = male20 + female20;
        reportEntity.middle = male40 + female40;
        reportEntity.senior = male50 + male50plus + female50plus + female50;
        ReportDataBase reportDataBase = ReportDataBase.getInstance(context);
        AdDatabase db = AdDatabase.getInstance(context);
        new Thread(() -> {
            reportDataBase.reportDao().insertReport(reportEntity);
            db.impDao().insertImpression(impression);

            tags.clear();
            male20 = 0;
            male32 = 0;
            male40 = 0;
            male50 = 0;
            male50plus = 0;
            female20 = 0;
            female32 = 0;
            female40 = 0;
            female50 = 0;
            female50plus = 0;
            //uniqueFaces.clear();
            detecteddText.clear();
            detectedObjects.clear();
            angryM = 0;
            disgustM = 0;
            fearM = 0;
            happyM = 0;
            sadM = 0;
            surpriseM = 0;
            neutralM = 0;
            angryF = 0;
            disgustF = 0;
            fearF = 0;
            happyF = 0;
            sadF = 0;
            surpriseF = 0;
            neutralF = 0;
        }).start();

    }

    private boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private String getCurrentHourFormatted() {
        return new SimpleDateFormat("H", Locale.getDefault()).format(new Date());
    }


    @SuppressLint("SetTextI18n")
    private boolean isSamePerson(float[] newEmbedding) {
        for (float[] saved : uniqueFaces) {
            float distance = calculateDistance(newEmbedding, saved);
            float distance1 = faceNetModel.cosineSimilarity(newEmbedding, saved);
            if (distance1 > 0.6) {
                return true; // نفس الشخص
            }

        }
        return false;
    }

    private float calculateDistance(float[] emb1, float[] emb2) {
        float sum = 0f;
        for (int i = 0; i < emb1.length; i++) {
            float diff = emb1[i] - emb2[i];
            sum += diff * diff;
        }
        return (float) Math.sqrt(sum);
    }

    public void uploadReport(ScreenRecord media, Context context) {

        new Thread(() -> {
            if (isInternetAvailable(context)) {
                ReportAPI.sendImpression(context, media);

            }
        }).start();
    }
}
