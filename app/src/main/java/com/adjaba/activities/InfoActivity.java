package com.adjaba.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adjaba.R;
import com.adjaba.activities.adapters.InfoAdapter;
import com.adjaba.room.AdDatabase;
import com.adjaba.room.InfoEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends AppCompatActivity {
    RecyclerView infoRecyclerView;
    Context context;
    FloatingActionButton clearData;
    private List<InfoEntity> infoEntityList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        context = this;
        infoEntityList=new ArrayList<>();
        clearData=findViewById(R.id.clearInfo);
        infoRecyclerView = findViewById(R.id.infoRecycler);
        infoRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        new Thread(new Runnable() {
            @Override
            public void run() {
                AdDatabase adDatabase = AdDatabase.getInstance(context);
                infoEntityList=adDatabase.infoDao().getAllInfo();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        infoRecyclerView.setAdapter(new InfoAdapter(infoEntityList, context));
                    }
                });

            }
        }).start();
        clearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AdDatabase adDatabase = AdDatabase.getInstance(context);
                        infoEntityList.clear();
                        adDatabase.infoDao().clearInfo();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                infoRecyclerView.setAdapter(new InfoAdapter(infoEntityList, context));
                            }
                        });

                    }
                }).start();
            }
        });
    }
}