package com.adjaba.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adjaba.R;
import com.adjaba.activities.adapters.MainAdsAdapter;
import com.adjaba.models.newmodels.Root;
import com.adjaba.utilities.AuthManager;
import com.adjaba.utilities.RetrofitBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreviewAds extends AppCompatActivity {
    RecyclerView recyclerView;
    MainAdsAdapter mainAdsAdapter;
    Context context;
    RetrofitBuilder retrofitBuilder;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_ads);
        recyclerView = findViewById(R.id.rcAds);
        FragmentManager fragmentManager=getSupportFragmentManager();
        int spanCount;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 2; // 2 columns
        } else {
            spanCount = 1; // 1 column
        }

        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        context = this;
        retrofitBuilder = new RetrofitBuilder();
        retrofitBuilder.apiCalls().getScreenResponse("Bearer " + AuthManager.getToken(this)).enqueue(new Callback<List<Root>>() {
            @Override
            public void onResponse(Call<List<Root>> call, Response<List<Root>> response) {
                if (response.isSuccessful()) {
                    mainAdsAdapter = new MainAdsAdapter(response.body(), context,fragmentManager);
                    recyclerView.setAdapter(mainAdsAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Root>> call, Throwable t) {

            }
        });

    }
}

