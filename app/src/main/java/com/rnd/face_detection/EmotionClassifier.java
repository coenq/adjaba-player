package com.rnd.face_detection;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class EmotionClassifier {
    private Interpreter interpreter;

    public EmotionClassifier(AssetManager assetManager, String modelPath) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        interpreter = new Interpreter(loadModelFile(assetManager, modelPath), options);
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public String predictEmotion(Bitmap faceBitmap) {
        // Resize الوجه للمقاس المطلوب (64x64)
        Bitmap resized = Bitmap.createScaledBitmap(faceBitmap, 64, 64, false);

        // input: [1,64,64,3]
        float[][][][] input = new float[1][64][64][3];
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int pixel = resized.getPixel(x, y);

                float r = (pixel >> 16 & 0xFF) / 255.0f;
                float g = (pixel >> 8 & 0xFF) / 255.0f;
                float b = (pixel & 0xFF) / 255.0f;

                input[0][y][x][0] = r;
                input[0][y][x][1] = g;
                input[0][y][x][2] = b;
            }
        }

        // output: [1,7]
        float[][] output = new float[1][7];
        interpreter.run(input, output);

        // دور على أعلى احتمال
        int maxIndex = 0;
        for (int i = 1; i < 7; i++) {
            if (output[0][i] > output[0][maxIndex]) {
                maxIndex = i;
            }
        }

        // نفس ترتيب الكلاسات اللي الموديل مدرّب عليها
        String[] emotions = {"Angry", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral"};

        return emotions[maxIndex];
    }
}
