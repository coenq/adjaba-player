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

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.tensorflow.lite.Interpreter;

import static com.rnd.activities.TestCamera.key_age;
import static com.rnd.activities.TestCamera.key_object;
import static com.rnd.activities.TestCamera.value_age;
import static com.rnd.activities.TestCamera.value_object;
import static com.rnd.activities.TestCamera.value_txt;

public class FaceRecognitionProcessor {

	private ByteBuffer imgData;
    private Interpreter tflite;
    private int[] intValuesDetection;
	private float[][][] outputLocations;
	private float[][] outputClasses;
	private float[][] outputScores;
	private float[] numDetections;
    private Vector<String> labels = new Vector<String>();

	private static final String TAG = "TextRecProc";

	private final FirebaseVisionFaceDetector detector;

	private TensorFlowInferenceInterface genderIinferenceInterface;
	private TensorFlowInferenceInterface ageInferenceInterface;
	private final int CARNIE_DIM = 227;
	private float[] floatValues = new float[64 * 64 * 3];
	private int[] intValues = new int[64*64];
	private float[] floatValuesCarnie = new float[CARNIE_DIM*CARNIE_DIM*3];
	private int[] intValuesCarnie = new int[CARNIE_DIM*CARNIE_DIM];
	private final List<String> AGE_LIST = Arrays.asList("(0, 2)","(4, 6)","(8, 12)","(15, 20)","(25, 32)","(38, 43)","(48, 53)","(60, 100)");
	private final int[] ageListMapping = {0, 0, 0, 0, 1, 2, 3, 4};
	private final List<String> AGE_LIST_2 = Arrays.asList("(0, 20)","(20, 32)","(32, 43)","(43, 53)","(53, 100)");

	private float[] genderOutputs = new float[1];
	private float[] ageOutputs = new float[8];
	private boolean frontFacingCamera;
	public static String detectedGender = "N/A";
	public static  String detectedAgeRange = "N/A";
	private List<String> viewList = new ArrayList<>();

	public  static  List<String> viewCountList = new ArrayList<>();
	 String textFound = "";


	// Whether we should ignore process(). This is usually caused by feeding input data faster than
	// the model can handle.
	private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);
	private Context context;

	public FaceRecognitionProcessor(AssetManager assetManager, boolean frontFacingCamera, Context mainActivity){
		this.genderIinferenceInterface = new TensorFlowInferenceInterface(assetManager, "gender_model.pb");
		this.ageInferenceInterface = new TensorFlowInferenceInterface(assetManager, "rude_carnie_age_model.pb");
		try {
			this.tflite = new Interpreter(loadModelFile(assetManager, "detect.tflite"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.frontFacingCamera = frontFacingCamera;

		detector = FirebaseVision.getInstance().getVisionFaceDetector();
		this.imgData = ByteBuffer.allocateDirect(300 * 300 * 3);
		this.imgData.order(ByteOrder.nativeOrder());
		this.intValuesDetection = new int[300*300];
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
			Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
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


	protected void onSuccess(@NonNull List<FirebaseVisionFace> results, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay, @NonNull Bitmap bitmap) {

		graphicOverlay.clear();

		try {
			key_age.setText("");
			value_age.setText("");
			value_object.setText("");
			value_txt.setText("");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Bitmap scaledBitmap;

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

			if(rect.left + rect.width() >= bitmap.getWidth()) {
				rect.right = bitmap.getWidth();
			}
			if(rect.top + rect.height() >= bitmap.getHeight()) {
				rect.bottom = bitmap.getHeight();
			}
			if(rect.top < 0) {
				rect.top = 0;
			}
			if(rect.left < 0) {
				rect.left = 0;
			}

			Bitmap resultBmp = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
			// process bitmap here
			scaledBitmap = Bitmap.createScaledBitmap(resultBmp, 64, 64, false);

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

			// Copy the input data into TensorFlow.
			genderIinferenceInterface.feed(inputName, floatValues, 1, 64, 64, 3);

			// Run the inference call.
			genderIinferenceInterface.run(new String[]{outputName});

			// Copy the output Tensor back into the output array.
			genderIinferenceInterface.fetch(outputName, genderOutputs);

			if(genderOutputs[0] <= 0.72) {
				detectedGender = "F";
			} else {
				detectedGender = "M";
			}

			/*** Run Age Detection Model ***/
			inputName = "Placeholder";
			outputName = "output/output";

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
			ageInferenceInterface.feed(inputName, floatValuesCarnie, 1, CARNIE_DIM, CARNIE_DIM, 3);

			// Run the inference call.
			ageInferenceInterface.run(new String[]{outputName});

			// Copy the output Tensor back into the output array.
			ageInferenceInterface.fetch(outputName, ageOutputs);

			
			int maxAt = 0;

			for (int i = 0; i < ageOutputs.length; i++) {
				maxAt = ageOutputs[i] > ageOutputs[maxAt] ? i : maxAt;
			}

			detectedAgeRange = AGE_LIST_2.get(ageListMapping[maxAt]);


			System.out.println("AgeRange"+detectedAgeRange+" Gender"+detectedGender);
			try {
				key_age.setText("AgeRange: "+detectedAgeRange);
				value_age.setText(String.valueOf("Gender: "+detectedGender));
			} catch (Exception e) {
				try {
					key_age.setText("");
					value_age.setText("");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			viewList.add(detectedAgeRange+"#"+detectedGender);


			try {
				// filter the distinct
				Set<String> setWithUniqueValues = new HashSet<>(viewList);
				viewCountList = new ArrayList<>(setWithUniqueValues);

//				System.out.println("Face Reco data "+MainActivity.viewCountList);

			} catch (Exception e) {
				e.printStackTrace();
			}

			// add graphic overlay
			GraphicOverlay.Graphic faceGraphic = new FaceGraphic(graphicOverlay, result, scaledBitmap, detectedGender, detectedAgeRange, frontFacingCamera);
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
                    detectedText.append("\n");
                }
            }
           textFound = detectedText.toString();
			try {
				value_txt.setText(String.valueOf(textFound));
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.d(TAG, detectedText.toString());
        }
        finally {
            textRecognizer.release();
        }


		/*
			* This is Object Detection Section
			* */

			scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
			scaledBitmap.getPixels(intValuesDetection, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());

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

            for(Recognition r : recognitions){
                final RectF location = r.getLocation();
                if (location != null && r.getConfidence() >= 0.5f){
                    if(counts.containsKey(r.getTitle())){
                        counts.put(r.getTitle(), counts.get(r.getTitle()) + 1);
                    }else{
                        counts.put(r.getTitle(), 1);
                    }
                }
            }

            //Just for Debugging, Printing list of counts
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                String key = entry.getKey();
                Integer count = entry.getValue();
                Log.d(TAG, key + " : " + count);
//				Toast.makeText(ac, key + " : " + count, Toast.LENGTH_SHORT).show();
				try {
					key_object.setText(key);
					value_object.setText(String.valueOf(count));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			/*
			Till Here

			key_age, value_age;
    static TextView key_object, value_object;
    static TextView key_txt, value_txt
			 */
	}

	protected void onFailure(@NonNull Exception e) {
		Log.w(TAG, "Face detection failed." + e);
	}

	private void detectInVisionImage( FirebaseVisionImage image, final FrameMetadata metadata, final GraphicOverlay graphicOverlay, final ByteBuffer data) {
		viewList.clear();

		final Bitmap bm = image.getBitmap();
		detectInImage(image)
				.addOnSuccessListener(
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

}
