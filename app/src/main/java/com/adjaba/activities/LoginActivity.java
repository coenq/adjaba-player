package com.adjaba.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.adjaba.Interface.ApiCalls;
import com.adjaba.R;
import com.adjaba.models.newmodels.LoginRequest;
import com.adjaba.models.newmodels.LoginResponse;
import com.adjaba.room.AdDatabase;
import com.adjaba.utilities.AuthManager;
import com.adjaba.utilities.RetrofitBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    RetrofitBuilder retrofitBuilder;
    EditText etEmail, etPassword;
    Button btnLogin;
    ProgressBar progressBar;
    CheckBox checkBox;
    String token = "null";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etPassword = findViewById(R.id.edt_password);
        etEmail = findViewById(R.id.edt_email);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBarLogin);
        checkBox = findViewById(R.id.rememberMeCheckBox);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.colorRed),
                PorterDuff.Mode.SRC_IN
        );
        Executors.newSingleThreadExecutor().execute(() -> {
            AdDatabase adDatabase = AdDatabase.getInstance(this);
            adDatabase.adDao().deleteAllAds();
        });
        // Access SharedPreferences
        checkBox.setChecked(true);
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (checkBox.isChecked()) {
            String savedEmail = sharedPreferences.getString("email", "");
            String savedPassword = sharedPreferences.getString("password", "");

            etEmail.setText(savedEmail);
            etPassword.setText(savedPassword);
        } else {
            etEmail.setText("");
            etPassword.setText("");
        }
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String savedEmail = sharedPreferences.getString("email", "");
                String savedPassword = sharedPreferences.getString("password", "");
                etEmail.setText(savedEmail);
                etPassword.setText(savedPassword);
            } else {
                etEmail.setText("");
                etPassword.setText("");
            }
        });

        retrofitBuilder = new RetrofitBuilder();
        if (Objects.equals(token, "null")) {
            token = AuthManager.getToken(this);
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnLogin.setVisibility(View.GONE);
                attemptLogin(editor);
            }
        });

    }

    void startLogin(String userId, String password, ApiCalls apiCalls, SharedPreferences.Editor editor) {
        progressBar.setVisibility(View.VISIBLE);
        LoginRequest request = new LoginRequest(userId, password);
        apiCalls.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    if (checkBox.isChecked()) {
                        editor.putString("email", etEmail.getText().toString());
                        editor.putString("password", etPassword.getText().toString());
                        editor.apply();
                    }
                    progressBar.setVisibility(View.GONE);
                    LoginResponse data = response.body();
                    MasterKey masterKey = null;
                    try {
                        masterKey = new MasterKey.Builder(getApplicationContext())
                                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                                .build();
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    SharedPreferences securePrefs = null;
                    try {
                        securePrefs = EncryptedSharedPreferences.create(
                                getApplicationContext(),
                                "auth",
                                masterKey,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        );
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    securePrefs.edit()
                            .putString("token", data.loginToken)
                            .apply();
                    intentToPreview();

                } else {
                    btnLogin.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Failed Data!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Connection Error!", Toast.LENGTH_LONG).show();
            }
        });
    }

    void intentToPreview() {
        startActivity(new Intent(this, SelectScreens.class));
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            @SuppressLint({"NewApi", "LocalSuppress"})
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
        return false;
    }

    private void attemptLogin(
            SharedPreferences.Editor editor) {
        String userId = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!isValidInput(userId, password)) {
            btnLogin.setVisibility(View.VISIBLE);
            return;
        }

        startLogin(userId, password, retrofitBuilder.apiCalls(), editor);
    }

    private boolean isValidInput(String userId, String password) {
        if (userId.isEmpty()) {
            etEmail.setError("Username is required");
            etEmail.requestFocus();
            return false;
        }

        if (userId.length() < 2) {
            etEmail.setError("Username is too short");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnLogin.setVisibility(View.VISIBLE);
    }
}