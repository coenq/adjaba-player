package com.adjaba.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.adjaba.R;
import com.adjaba.activities.viewmodel.DataHolder;
import com.adjaba.models.newmodels.MediaModel;
import com.adjaba.models.newmodels.Root;
import com.adjaba.models.newmodels.VideoImageModel;
import com.adjaba.models.newmodels.WatchingModel;
import com.adjaba.others.TargetHours;
import com.adjaba.room.AdDatabase;
import com.adjaba.room.AdEntity;
import com.adjaba.utilities.AuthManager;
import com.adjaba.utilities.RetrofitBuilder;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectScreens extends AppCompatActivity {

    List<WatchingModel> adList;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static AlertDialog downloadDialog;
    private static ProgressBar dialogProgressBar;
    private ProgressBar progressBar;
    private static TextView dialogProgressText;
    RetrofitBuilder retrofitBuilder;
    Toolbar topAppBar;
    List<MediaModel> mediaModels = new ArrayList<>();
    Context context;
    Activity ac;
    LinearLayout bot_lay;
    Map<String, String> screenPlayerMap, screenLocationMap, screenDeviceMap, screenLocation;
    Map<String, List<String>> screenTags;
    List<TargetHours> targetHoursList;
    RelativeLayout loginrootlayout;
    Spinner spinner1, spinner2, spinnerID;
    ProgressBar loadingBar;
    CheckBox rememberMe, displayText, businessRules;
    ImageView logo, picture;
    List<String> screenOptions1;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    List<String> screenOptions = new ArrayList<>();
    ArrayAdapter<String> spinnerAdapter;
    Button play, logOut;
    String mediaFormat = "";
    String orient, screen_id = "";
    String timeRefresh = "0";
    int[] loadedCount = {0};


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_screen);
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        editor = prefs.edit();
        context = this;
        ac = this;
        loadingBar = findViewById(R.id.loadingBar);
        logOut = findViewById(R.id.logOut);
        screenOptions = new ArrayList<>();
        screenOptions1 = new ArrayList<>();
        retrofitBuilder = new RetrofitBuilder();
        adList = new ArrayList<>();
        targetHoursList = new ArrayList<>();
        findViews();
        screenPlayerMap = new HashMap<>();
        mediaModels = new ArrayList<>();
        screenDeviceMap = new HashMap<>();
        screenLocation = new HashMap<>();
        screenLocationMap = new HashMap<>();
        // في onCreate أو بعد findViews()
        screenTags = new HashMap<>();
        screenOptions.add("Select Screen"); // العنصر الأول الثابت
        //screenOptions1.add("Select Screen");
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, screenOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerID.setAdapter(spinnerAdapter);
        String[] orientationOptions = {"Orientation", "Landscape", "Portrait"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                orientationOptions
        );


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int spinner1Pos = prefs.getInt("spinner1_position", 0);
        int spinner2Pos = prefs.getInt("spinner2_position", 0);
        rememberMe.setChecked(true);
        SharedPreferences prefsw = getSharedPreferences("SpinnerPrefs", MODE_PRIVATE);
        int savedPositionw = prefsw.getInt("spinner2_position", 0);
        getIDs(context, () -> {

            if (rememberMe.isChecked()) {
                spinner1.setSelection(spinner1Pos);
                if (savedPositionw < screenOptions.size()) {
                    spinnerID.setSelection(savedPositionw);
                }
                spinner2.setSelection(spinner2Pos);
            }
        });
// تحميل البيانات من السيرف);

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // عرض مؤشر تحميل على الـ UI (اختياري)
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // example: showProgress(true);
                        progressBar.setVisibility(view.VISIBLE);

                    });

                    dbExecutor.execute(() -> {
                        try {
                            AdDatabase adDatabase = AdDatabase.getInstance(view.getContext().getApplicationContext());

                            while (adDatabase.adDao().getAll().size() > 0) {
                                adDatabase.adDao().deleteAllAds();
                            }
                            adDatabase.adDao().deleteAllAds();

                            // لو أردت مسح ملفات cache إضافية:
                            // clearLocalMediaFiles();

                            new Handler(Looper.getMainLooper()).post(() -> {
                                progressBar.setVisibility(View.GONE);
                                // إخفاء مؤشر التحميل
                                // example: showProgress(false);
                                //Log.d("LOGOUT_SAYED", "DataBase Size : " + adDatabase.adDao().getAll().size());
                                // افتح LoginActivity
                                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                                // لو عايز تمنع الرجوع للشاشة السابقة:
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                view.getContext().startActivity(intent);
                                // لو في Activity وتريد إغلاقها:
                                if (view.getContext() instanceof Activity) {
                                    ((Activity) view.getContext()).finish();
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            new Handler(Looper.getMainLooper()).post(() -> {
                                // show error to user
                                Toast.makeText(view.getContext(), "Error!", Toast.LENGTH_SHORT).show();
                                // hideProgress if needed
                            });
                        }


                    });
                } catch (Exception e) {
                    Intent intent = new Intent(view.getContext(), LoginActivity.class);
                    // لو عايز تمنع الرجوع للشاشة السابقة:
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    view.getContext().startActivity(intent);
                }
            }
        });
        spinner1.setAdapter(adapter);

// استرجاع آخر اختيار لكل Spinner


// تعيين القيم المسترجعة


// Spinner 1
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (rememberMe.isChecked()) {
                    editor.putInt("spinner1_position", position);
                    editor.apply();
                }
                orient = orientationOptions[position];

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

// Spinner 2
        spinnerID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //screen_id = screenOptions.get(position);
//                Log.d("sayed_sppp", screen_id);
                //if (position > 0 && position < screenOptions.size()) {
                // أول عنصر (position = 0) هو "Select Screen" فلا نخزنه كـ ID

                // حفظ القيمة لو Remember Me مفعّل
                if (rememberMe.isChecked()) {
                    getSharedPreferences("SpinnerPrefs", MODE_PRIVATE)
                            .edit()
                            .putInt("spinner2_position", position)
                            .apply();
                }
                screen_id = screenOptions.get(position);
                Log.d("sayed_sppp", screen_id + " " + position);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

// Spinner 3
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (rememberMe.isChecked()) {
                    editor.putInt("spinner2_position", position);
                    editor.apply();
                }
                timeRefresh = position + "";

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingBar.setVisibility(View.VISIBLE);
                Log.d("sayed_scre", screen_id);
                if (!screen_id.equals("Select Screen") && !orient.equals("Orientation")) {
                    DataHolder.getInstance().screenID = screen_id;
                    DataHolder.getInstance().screenDevice = screenDeviceMap.get(screen_id);
                    DataHolder.getInstance().screenPlayer = screenPlayerMap.get(screen_id);
                    DataHolder.getInstance().locationTypes = screenLocationMap.get(screen_id);
                    DataHolder.getInstance().location = screenLocation.get(screen_id);
                    DataHolder.getInstance().tags = screenTags.get(screen_id);

                    DataHolder.getInstance().orient = orient;
                    DataHolder.getInstance().time = timeRefresh;
                    if (displayText.isChecked()) {
                        DataHolder.getInstance().displayFlag = 1;
                    }
                    if (!displayText.isChecked()) {
                        DataHolder.getInstance().displayFlag = 0;
                    }
                    if (businessRules.isChecked()) {
                        DataHolder.getInstance().targetHoursFlag = 1;
                    }
                    if (!businessRules.isChecked()) {
                        DataHolder.getInstance().targetHoursFlag = 0;
                    }
                    Executors.newSingleThreadExecutor().execute(() -> {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            getAds(0);
                        });
                    });
                } else {
                    Toast.makeText(context, "Please select orientation and screen id", Toast.LENGTH_LONG).show();
                    loadingBar.setVisibility(View.GONE);
                }
            }


        });
        rememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
           /* SharedPreferences prefsw = getSharedPreferences("SpinnerPrefs", MODE_PRIVATE);
            int savedPositionw = prefsw.getInt("spinner2_position", 0);*/


            if (isChecked) {
                if (savedPositionw < screenOptions.size()) {
                    spinnerID.setSelection(savedPositionw);
                }
                spinner1.setSelection(spinner1Pos);
                spinner2.setSelection(spinner2Pos);
            } else {
                spinner1.setSelection(0);
                spinner2.setSelection(0);
                spinnerID.setSelection(0);
            }
        });
    }

    void getAds(int flag) {
        AdDatabase adDatabase = AdDatabase.getInstance(context);

        // نشتغل على Background Thread
        new Thread(() -> {
            // 1️⃣ مسح قاعدة البيانات
            adDatabase.adDao().deleteAllAds().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
            Log.d("sayed_finalD", adDatabase.adDao().getAll().size() + "");

            // 2️⃣ مسح البيانات من الذاكرة
            if (adList != null) {
                adList.clear();
            }
            DataHolder.getInstance().advertIds.clear();

            Log.d("DB_Clear", "All ads deleted from DB and memory");

            // 3️⃣ نكمل تحميل الإعلانات بعد المسح
            new Handler(Looper.getMainLooper()).post(() -> {
                Log.d("API_Request", "Requesting ads for screen: " + screen_id + " with token: " + AuthManager.getToken(this));

                retrofitBuilder.apiCalls()
                        .getAdsByScreen(screen_id.split("/")[0], "Bearer " + AuthManager.getToken(this))
                        .enqueue(new Callback<List<WatchingModel>>() {
                            @Override
                            public void onResponse(Call<List<WatchingModel>> call, Response<List<WatchingModel>> response) {
                                if (response.code() == 200) {
                                    adList = response.body();
                                    Log.d("sayed_ssiizz", adList.size()+"");

                                    if (adList == null || adList.isEmpty() || adList.size() == 0) {
                                        Log.d("sayed_scre", screen_id);
                                        if (!screen_id.equals("Select Screen") && !orient.equals("Orientation")) {
                                            DataHolder.getInstance().screenID = screen_id;
                                            DataHolder.getInstance().screenDevice = screenDeviceMap.get(screen_id);
                                            DataHolder.getInstance().screenPlayer = screenPlayerMap.get(screen_id);
                                            DataHolder.getInstance().locationTypes = screenLocationMap.get(screen_id);
                                            DataHolder.getInstance().location = screenLocation.get(screen_id);
                                            DataHolder.getInstance().tags = screenTags.get(screen_id);
                                            DataHolder.getInstance().isData = 5;
                                            DataHolder.getInstance().orient = orient;
                                            DataHolder.getInstance().time = timeRefresh;
                                            if (displayText.isChecked()) {
                                                DataHolder.getInstance().displayFlag = 1;
                                            }
                                            if (!displayText.isChecked()) {
                                                DataHolder.getInstance().displayFlag = 0;
                                            }
                                            if (businessRules.isChecked()) {
                                                DataHolder.getInstance().targetHoursFlag = 1;
                                            }
                                            if (!businessRules.isChecked()) {
                                                DataHolder.getInstance().targetHoursFlag = 0;
                                            }
                                            Executors.newSingleThreadExecutor().execute(() -> {
                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    loadingBar.setVisibility(View.GONE);
                                                    startActivity(new Intent(context, AdvertWatching.class));
                                                });
                                            });
                                        } else {
                                            Toast.makeText(context, "Please select orientation and screen id", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    //Log.d("sayed_size", adList.size() + "");
                                    else {


                                        if (executorService == null || executorService.isShutdown()) {
                                            executorService = Executors.newSingleThreadExecutor();
                                        }
                                        executorService.execute(() -> {
                                            for (int i = 0; i < adList.size(); i++) {
                                                DataHolder.getInstance().advertIds.add(adList.get(i).adContractData.advertId);
                                                String format = adList.get(i).adContractData.format.toLowerCase();
                                                String videoUrl = adList.get(i).adContractData.videoUrl;
                                                int duration = adList.get(i).duration;

                                                getUrl(
                                                        adList.get(i).contractId,
                                                        adList.get(i).currency,
                                                        adList.get(i).maxBid,
                                                        adList.get(i).adContractData.targetHours,
                                                        adList.get(i).adContractData.textTop,
                                                        adList.get(i).adContractData.textRight,
                                                        adList.get(i).adContractData.textLeft,
                                                        adList.get(i).adContractData.textBottom,
                                                        adList.get(i).adContractData.advertId,
                                                        adList.get(i).screenId,
                                                        videoUrl,
                                                        format,
                                                        loadedCount,
                                                        adList.size(),
                                                        duration,
                                                        context,
                                                        flag,
                                                        i
                                                );
                                            }
                                        });
                                    }
                                } else {
                                    //Toast.makeText(context, "Error : " + response.code(), Toast.LENGTH_LONG).show();
                                    loadingBar.setVisibility(View.GONE);
                                    if (!screen_id.equals("Select Screen") && !orient.equals("Orientation")) {
                                        DataHolder.getInstance().screenID = screen_id;
                                        DataHolder.getInstance().screenDevice = screenDeviceMap.get(screen_id);
                                        DataHolder.getInstance().screenPlayer = screenPlayerMap.get(screen_id);
                                        DataHolder.getInstance().locationTypes = screenLocationMap.get(screen_id);
                                        DataHolder.getInstance().location = screenLocation.get(screen_id);
                                        DataHolder.getInstance().tags = screenTags.get(screen_id);
                                        DataHolder.getInstance().isData = 5;
                                        DataHolder.getInstance().orient = orient;
                                        DataHolder.getInstance().time = timeRefresh;
                                        if (displayText.isChecked()) {
                                            DataHolder.getInstance().displayFlag = 1;
                                        }
                                        if (!displayText.isChecked()) {
                                            DataHolder.getInstance().displayFlag = 0;
                                        }
                                        if (businessRules.isChecked()) {
                                            DataHolder.getInstance().targetHoursFlag = 1;
                                        }
                                        if (!businessRules.isChecked()) {
                                            DataHolder.getInstance().targetHoursFlag = 0;
                                        }
                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                loadingBar.setVisibility(View.GONE);
                                                startActivity(new Intent(context, AdvertWatching.class));
                                            });
                                        });
                                    } else {
                                        Toast.makeText(context, "Please select orientation and screen id", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<List<WatchingModel>> call, Throwable t) {
                                Log.e("getAds_error", t.getMessage());
                            }
                        });
            });
        }).start();
    }

    private void getUrl(String currency, String contractId, int maxBid, List<Integer> targetHours, String txtTop, String txtRight, String txtLeft, String info, String advertId, String screenId, String path, String type, int[] loadedCount, int totalCount, int duration, Context context, int flag, int serverOrder) {
        if (path == null || path.isEmpty()) {
            return;
        }
        // لو هذه هي بداية السلسلة، اعرض الـ dialog
        // (بافتراض أن المستدعي ينادي getUrl عدة مرات ويعطي totalCount الإجمالي)
        // new Handler(Looper.getMainLooper()).post(() -> showDownloadDialog(context, totalCount));

        retrofitBuilder.apiCalls().getUrl("Bearer " + AuthManager.getToken(this), path).enqueue(new Callback<VideoImageModel>() {
            @Override
            public void onResponse(Call<VideoImageModel> call, Response<VideoImageModel> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String resolvedUrl = response.body().url;
                    String extension;
                    try {
                        int start = resolvedUrl.lastIndexOf('.') + 1;
                        int end = resolvedUrl.contains("?") ? resolvedUrl.indexOf("?") : resolvedUrl.length();
                        extension = resolvedUrl.substring(start, end);
                    } catch (Exception e) {
                        extension = "mp4";
                    }
                    Log.d("sayed_extentaion", extension);

                    String fileName = UUID.randomUUID().toString() + "." + extension;

                    Executors.newSingleThreadExecutor().execute(() -> {
                        // استخدم النسخة المعدلة مع callback
                        String localPath = downloadFileToInternalStorage(context, resolvedUrl, fileName);
                        long lastReportedPercent = -1;

                            /*@Override
                            public void onProgress(long downloadedBytes, long totalBytes) {
                                int percent;
                                if (totalBytes > 0) {
                                    percent = (int) ((downloadedBytes * 100) / totalBytes);
                                } else {
                                    percent = 0;
                                }
                                // لتقليل عدد الرفع على الـ UI فقط نحدث لو تغيرت النسبة
                                *//*if (percent != lastReportedPercent) {
                                    lastReportedPercent = percent;
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        updateDownloadDialogProgress(percent, loadedCount[0], totalCount);
                                    });
                                }*//*
                            }*/

                          /*  @Override
                            public void onComplete(String path) {
                                // تم تحميل الملف فعليًا — لكن هنا أيضاً سنعالج بعد خروج الـ download function
                            }*/

                           /* @Override
                            public void onError(Exception e) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    Toast.makeText(context, "Download error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }*/

                        if (localPath != null) {
                            if (Objects.equals(path, "") || isImage(path)) {
                                mediaFormat = "Image";
                            } else if (isVideo(path)) {
                                mediaFormat = "Video";
                            }
                            Log.d("sayyyy", mediaFormat);
                            AdEntity ad = new AdEntity(
                                    advertId,
                                    mediaFormat.toUpperCase(),
                                    localPath,
                                    txtTop, info, txtLeft, txtRight,
                                    duration * 1000,
                                    "Landscape",
                                    screenId,
                                    contractId, listToString(targetHours), serverOrder, currency, maxBid
                            );
                            AdDatabase db = AdDatabase.getInstance(context);
                            db.adDao().insertAd(ad);

                            // زيادة العداد على thread الخلفي
                            loadedCount[0]++;

                            // حدّث الـ dialog بعد كل ملف ليُظهر عدد الملفات المحفوظة
                            int currentLoaded = loadedCount[0];
                           /* new Handler(Looper.getMainLooper()).post(() -> {
                                // نحدد نسبة عامة تقريبية بين الملفات (مثال بسيط: 100 * loaded/total)
                                int overallPercent = (int) ((currentLoaded * 100.0f) / totalCount);
                                //updateDownloadDialogProgress(overallPercent, currentLoaded, totalCount);
                            });*/
                            targetHoursList.add(new TargetHours(advertId, targetHours));
                            if (loadedCount[0] == totalCount) {
                                // تم الانتهاء من كل الملفات — اغلق الـ dialog وابدأ الـ Activity
                                List<AdEntity> ads = db.adDao().getAllAds(screenId);
                                /*Log.d("sayed++", ads.get(0).advertId +
                                        " " + ads.get(1).advertId + " " +
                                        ads.get(2).advertId + " " +
                                        ads.get(3).advertId);*/
                                mediaModels.clear();
                                for (AdEntity ada : ads) {
                                    mediaModels.add(new MediaModel(contractId, currency, maxBid, ada.format, ada.localPath, ada.duration, ada.textBottom, ada.textTop, "", ada.targetHours, ada.advertId));

                                }
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    //Log.d("in__for", mediaModels.get(0).getAdvertId() + " " + mediaModels.get(1).getAdvertId());

                                    DataHolder.getInstance().targetHours = targetHoursList;
                                    //dismissDownloadDialog();
                                    loadingBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(context, AdvertWatching.class);
                                    DataHolder.getInstance().allAds = mediaModels;
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                });
                            }
                        } else {
                            // فشل التحميل
                            loadedCount[0]++;
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(context, "Failed to download media: " + path, Toast.LENGTH_SHORT).show();
                                // ممكن تحويّل السلوك: حاول إعادة المحاولة أو تتابع بدون الملف
                                if (loadedCount[0] == totalCount) {
                                    // dismissDownloadDialog();
                                    // تعامل كما في حالة الاكتمال
                                }
                            });
                        }
                    });
                } else {
                    Log.e("media_error", "Failed to get media URL for path: " + path + " | Response code: " + response.code());
                    // تعامل مع الفشل
                    loadedCount[0]++;
                }
            }

            @Override
            public void onFailure(Call<VideoImageModel> call, Throwable t) {
                Log.e("sayyed_error", t.getMessage());
                loadedCount[0]++;
                new Handler(Looper.getMainLooper()).post(() -> {
                    //Toast.makeText(context, "Request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    if (loadedCount[0] == totalCount) {
                        //dismissDownloadDialog();
                    }
                });
            }
        });
    }

    public List<Integer> stringToList(String str) {
        List<Integer> list = new ArrayList<>();
        if (str == null || str.isEmpty()) return list;

        String[] parts = str.split("/");
        for (String part : parts) {
            try {
                list.add(Integer.parseInt(part));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public String listToString(List<Integer> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) {
                sb.append("/");
            }
        }
        return sb.toString();
    }

    public String listStringToString(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) {
                sb.append("/");
            }
        }
        return sb.toString();
    }

    List<String> getIDs(Context context, Runnable onFinish) {
        retrofitBuilder.apiCalls().getScreenResponse("Bearer " + AuthManager.getToken(this)).enqueue(new Callback<List<Root>>() {
            @Override
            public void onResponse(Call<List<Root>> call, Response<List<Root>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (int screen = 0; screen < response.body().size(); screen++) {
                        //screenOptions1.add(response.body().get(screen).screenId);
                        screenPlayerMap.put(response.body().get(screen).screenId, response.body().get(screen).screenPlayer);
                        screenDeviceMap.put(response.body().get(screen).screenId, response.body().get(screen).screenDevice);
                        screenLocationMap.put(response.body().get(screen).screenId, listStringToString(response.body().get(screen).locationType));
                        screenTags.put(response.body().get(screen).screenId, response.body().get(screen).screenTags);
                        screenLocation.put(response.body().get(screen).screenId, response.body().get(screen).location);

                        screenOptions.add(response.body().get(screen).getScreenId());
                    }
                    spinnerAdapter.notifyDataSetChanged();

                    // تحميل القيمة المحفوظة لو موجودة
                    SharedPreferences prefs = getSharedPreferences("SpinnerPrefs", MODE_PRIVATE);
                    boolean rememberState = prefs.getBoolean("remember_state", false);
                    if (rememberState) {
                        int savedPosition = prefs.getInt("spinner2_position", 0);
                        if (savedPosition < screenOptions.size()) {
                            if (rememberMe.isChecked()) {
                                spinnerID.setSelection(savedPosition);

                            }
                        }
                    }
                }
                onFinish.run();
            }

            @Override
            public void onFailure(Call<List<Root>> call, Throwable t) {
                onFinish.run();
            }
        });
        return screenOptions;
    }

    public interface ProgressCallback {
        void onProgress(long downloadedBytes, long totalBytes);

        void onComplete(String localPath);

        void onError(Exception e);
    }

    private String downloadFileToInternalStorage(Context context, String fileUrl, String fileName) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }

            long fileLength = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileLength = connection.getContentLengthLong();
            }

            input = connection.getInputStream();
            File dir = context.getFilesDir(); // أو أي مكان تفضله
            File outFile = new File(dir, fileName);
            output = new FileOutputStream(outFile);

            byte[] data = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);

                // نبلّغ عن التقدّم
               /* if (callback != null) {
                    callback.onProgress(total, fileLength);
                }*/
            }

            output.flush();

           /* if (callback != null) {
                callback.onComplete(outFile.getAbsolutePath());
            }*/
            return outFile.getAbsolutePath();

        } catch (Exception e) {
           /* if (callback != null) {
                callback.onError(e);
            }*/
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException ignored) {
            }
            if (connection != null) connection.disconnect();
        }
    }

    /*public static String getFilePath() {

        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "CrashReports" + File.separator);
        if (dir.exists()) {
        } else {
            dir.mkdir();
        }
        return dir.getAbsolutePath();
    }*/
    static void showDownloadDialog(Context context, int totalFiles) {
        if (downloadDialog != null && downloadDialog.isShowing()) return;

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);
        dialogProgressBar = dialogView.findViewById(R.id.dialogProgressBar);
        dialogProgressText = dialogView.findViewById(R.id.dialogProgressText);

        dialogProgressBar.setMax(100); // سنعرض نسبة مئوية
        dialogProgressBar.setProgress(0);
        dialogProgressText.setText("0% - 0 / " + totalFiles);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setCancelable(false); // منع الإغلاق أثناء التحميل
        downloadDialog = builder.create();
        downloadDialog.show();
    }

    static void updateDownloadDialogProgress(int percent, int loadedFiles, int totalFiles) {
        if (downloadDialog == null || !downloadDialog.isShowing()) return;
        dialogProgressBar.setProgress(percent);
        dialogProgressText.setText(percent + "% - " + loadedFiles + " / " + totalFiles);
    }

    static void dismissDownloadDialog() {
        if (downloadDialog != null && downloadDialog.isShowing()) {
            downloadDialog.dismiss();
        }
    }

    private void findViews() {
        businessRules = findViewById(R.id.business_rule);
        displayText = findViewById(R.id.text_display);
        rememberMe = findViewById(R.id.rememberMeCh);
        topAppBar = findViewById(R.id.topAppBar);
        bot_lay = findViewById(R.id.bot_lay);
        spinner1 = findViewById(R.id.spinner1);
        spinnerID = findViewById(R.id.spinnerID);
        progressBar = findViewById(R.id.progressSignOut);
        spinner2 = findViewById(R.id.spinner2);
//        spinner_select_screen=findViewById( R.id.spinner_select_screen);
        play = findViewById(R.id.loginbtn);
        loginrootlayout = findViewById(R.id.loginrootlayout);

        //logo = findViewById(R.id.logo);
       /* logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (doubleBackToExitPressedOnce) {
                    debug.setVisibility(View.VISIBLE);
                    return;
                }
                doubleBackToExitPressedOnce = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        });
*/
        picture = findViewById(R.id.picture);
        /*picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (doubleBackToExitPressedOnce) {

                    Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);

                    return;
                }
                doubleBackToExitPressedOnce = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        });*/
    }


    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            int i = 0;
            while (i < children.length) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
                i++;
            }
        }
        assert dir != null;
        return dir.delete();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adList.clear();
        DataHolder.getInstance().isData=0;
        Arrays.fill(loadedCount, 0);

    }

    public static String getFileExtensionFromUrl(String url) {
        if (url != null) {
            return MimeTypeMap.getFileExtensionFromUrl(url);
        }
        return null;
    }

    public static boolean isImage(String url) {
        String ext = getFileExtensionFromUrl(url);
        return ext != null && (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") ||
                ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("gif") ||
                ext.equalsIgnoreCase("bmp") || ext.equalsIgnoreCase("webp"));
    }

    public static boolean isVideo(String url) {
        String ext = getFileExtensionFromUrl(url);
        return ext != null && (ext.equalsIgnoreCase("mp4") || ext.equalsIgnoreCase("mkv") ||
                ext.equalsIgnoreCase("3gp") || ext.equalsIgnoreCase("avi") ||
                ext.equalsIgnoreCase("mov"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdownNow();

    }


}
