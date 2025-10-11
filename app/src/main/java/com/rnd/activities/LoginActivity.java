package com.rnd.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import com.rnd.R;
import com.rnd.newmodels.LoginRequest;
import com.rnd.newmodels.LoginResponse;
import com.rnd.utilities.ApiCalls;
import com.rnd.utilities.AuthManager;
import com.rnd.utilities.RetrofitBuilder;

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
    TextView termsTv;
    CheckBox checkBox;
    String token = "null";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login1);
        etPassword = findViewById(R.id.edt_password);
        etEmail = findViewById(R.id.edt_email);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBarLogin);
        termsTv=findViewById(R.id.terms_tv);
        checkBox = findViewById(R.id.rememberMeCheckBox);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.colorRed),
                PorterDuff.Mode.SRC_IN
        );

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
        termsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TermsActivity.class);
                startActivity(intent);
            }
        });
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
        /*if (!isInternetAvailable()) {
            Log.d("sayed", AuthManager.getToken(this));
        }*/
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    Log.d("LOGIN_SUCCESS", "Token: " + data.loginToken);
                    SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                    prefs.edit().putString("token", data.loginToken).apply();
                    intentToPreview();

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Failed Data!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
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

}