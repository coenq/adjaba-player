package com.adjaba.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.adjaba.R;
import com.adjaba.models.newmodels.TvPollAuthResponse;
import com.adjaba.models.newmodels.TvStartAuthResponse;
import com.adjaba.utilities.AuthManager;
import com.adjaba.utilities.RetrofitBuilder;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TvLoginActivity extends AppCompatActivity {

    private ImageView qrCodeImage;
    private TextView tvUserCode;
    private TextView tvCountdown;
    private TextView tvStatus;
    private TextView tvQrUrl;
    private Button btnBack;

    private RetrofitBuilder retrofitBuilder;
    private String currentDeviceCode;
    private int currentPollInterval = 5;
    private Handler pollHandler;
    private Runnable pollRunnable;
    private CountDownTimer countDownTimer;
    private boolean isPolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_login);

        qrCodeImage = findViewById(R.id.qr_code_image);
        tvUserCode = findViewById(R.id.tv_user_code);
        tvCountdown = findViewById(R.id.tv_countdown);
        tvStatus = findViewById(R.id.tv_status);
        tvQrUrl = findViewById(R.id.tv_qr_url);
        btnBack = findViewById(R.id.btn_back_to_login);

        retrofitBuilder = new RetrofitBuilder();
        pollHandler = new Handler(Looper.getMainLooper());

        btnBack.setOnClickListener(v -> finish());

        startAuthFlow();
    }

    private void startAuthFlow() {
        stopPolling();
        if (countDownTimer != null) countDownTimer.cancel();

        tvStatus.setText("Getting your code...");
        tvStatus.setVisibility(View.VISIBLE);
        tvUserCode.setVisibility(View.GONE);
        qrCodeImage.setVisibility(View.GONE);
        tvQrUrl.setVisibility(View.GONE);
        tvCountdown.setText("");

        retrofitBuilder.apiCalls().tvStartAuth().enqueue(new Callback<TvStartAuthResponse>() {
            @Override
            public void onResponse(Call<TvStartAuthResponse> call, Response<TvStartAuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TvStartAuthResponse data = response.body();
                    currentDeviceCode = data.deviceCode;
                    currentPollInterval = data.pollInterval > 0 ? data.pollInterval : 5;
                    showCodeAndQr(data);
                    startCountdown(data.expiresIn > 0 ? data.expiresIn : 600);
                    startPolling();
                } else {
                    tvStatus.setText("Failed to get code. Retrying...");
                    pollHandler.postDelayed(TvLoginActivity.this::startAuthFlow, 3000);
                }
            }

            @Override
            public void onFailure(Call<TvStartAuthResponse> call, Throwable t) {
                tvStatus.setText("Connection error. Retrying...");
                pollHandler.postDelayed(TvLoginActivity.this::startAuthFlow, 3000);
            }
        });
    }

    private void showCodeAndQr(TvStartAuthResponse data) {
        tvStatus.setVisibility(View.GONE);
        tvUserCode.setText(data.userCode);
        tvUserCode.setVisibility(View.VISIBLE);
        qrCodeImage.setVisibility(View.VISIBLE);

        // Strip the scheme so the printed URL is shorter (adjaba.in/tv?code=…)
        String displayUrl = data.qrUrl.replaceFirst("https?://", "");
        tvQrUrl.setText(displayUrl);
        tvQrUrl.setVisibility(View.VISIBLE);

        try {
            int qrSize = (int) (300 * getResources().getDisplayMetrics().density);
            QRGEncoder qrgEncoder = new QRGEncoder(data.qrUrl, null, QRGContents.Type.TEXT, qrSize);
            qrgEncoder.setColorBlack(Color.WHITE);
            qrgEncoder.setColorWhite(Color.BLACK);
            Bitmap bitmap = qrgEncoder.getBitmap();
            qrCodeImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            // QR generation failed; user code is still shown
        }
    }

    private void startCountdown(int totalSeconds) {
        countDownTimer = new CountDownTimer(totalSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secs = millisUntilFinished / 1000;
                tvCountdown.setText(String.format("Expires in %d:%02d", secs / 60, secs % 60));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("Expired");
            }
        }.start();
    }

    private void startPolling() {
        isPolling = true;
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPolling || currentDeviceCode == null) return;

                retrofitBuilder.apiCalls().tvPollAuth(currentDeviceCode).enqueue(new Callback<TvPollAuthResponse>() {
                    @Override
                    public void onResponse(Call<TvPollAuthResponse> call, Response<TvPollAuthResponse> response) {
                        if (!isPolling) return;

                        if (response.isSuccessful() && response.body() != null) {
                            String status = response.body().status;
                            if ("approved".equals(status)) {
                                stopPolling();
                                onAuthApproved(response.body());
                            } else if ("expired".equals(status) || "denied".equals(status)) {
                                stopPolling();
                                onAuthExpiredOrDenied();
                            } else {
                                pollHandler.postDelayed(pollRunnable, currentPollInterval * 1000L);
                            }
                        } else {
                            pollHandler.postDelayed(pollRunnable, currentPollInterval * 1000L);
                        }
                    }

                    @Override
                    public void onFailure(Call<TvPollAuthResponse> call, Throwable t) {
                        if (isPolling) {
                            pollHandler.postDelayed(pollRunnable, currentPollInterval * 1000L);
                        }
                    }
                });
            }
        };
        pollHandler.postDelayed(pollRunnable, currentPollInterval * 1000L);
    }

    private void stopPolling() {
        isPolling = false;
        if (pollHandler != null && pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }

    private void onAuthApproved(TvPollAuthResponse data) {
        if (countDownTimer != null) countDownTimer.cancel();
        AuthManager.saveToken(getApplicationContext(), data.token);
        startActivity(new Intent(this, SelectScreens.class));
        finish();
    }

    private void onAuthExpiredOrDenied() {
        if (countDownTimer != null) countDownTimer.cancel();
        tvStatus.setText("Code expired. Getting a new code...");
        tvStatus.setVisibility(View.VISIBLE);
        tvUserCode.setVisibility(View.GONE);
        qrCodeImage.setVisibility(View.GONE);
        tvQrUrl.setVisibility(View.GONE);
        pollHandler.postDelayed(this::startAuthFlow, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
