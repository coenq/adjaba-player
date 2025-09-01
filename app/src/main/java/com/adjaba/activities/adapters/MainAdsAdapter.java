package com.adjaba.activities.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adjaba.R;
import com.adjaba.activities.AdvertWatching;
import com.adjaba.models.newmodels.Root;
import com.bumptech.glide.Glide;

import java.util.List;

public class MainAdsAdapter extends RecyclerView.Adapter<MainAdsAdapter.AdViewHolder> {

    private final List<Root> adList;
    private final Context context;
    private final FragmentManager fragmentManager;

    public MainAdsAdapter(List<Root> adList, Context context, FragmentManager fragmentManager) {
        this.adList = adList;
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.main_ads_custom, parent, false);
        return new AdViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
        Root ad = adList.get(position);

        // Set data
        holder.about.setText(ad.getAboutScreen());
        holder.screenID.setText("Screen ID : " + ad.getScreenId());
        holder.location.setText(ad.getLocation());

        // Format location types
        StringBuilder locBuilder = new StringBuilder();
        for (int i = 0; i < ad.getLocationType().size(); i++) {
            locBuilder.append(ad.getLocationType().get(i));
            if (i != ad.getLocationType().size() - 1) {
                locBuilder.append(" | ");
            }
        }
        holder.locationType.setText(locBuilder.toString());

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            Intent intent=new Intent(context,AdvertWatching.class);
            intent.putExtra("screen_id",ad.getScreenId());
            intent.putExtra("screen_loc",ad.getLocation());
            context.startActivity(intent);


        });
    }

    @Override
    public int getItemCount() {
        return adList.size();
    }

    public static class AdViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAd;
        TextView about, locationType, location, screenID;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            screenID = itemView.findViewById(R.id.screen_id);
            about = itemView.findViewById(R.id.about_screen);
            locationType = itemView.findViewById(R.id.locationType);
            location = itemView.findViewById(R.id.location);
           // imageAd = itemView.findViewById(R.id.imageView); // Ensure this ID exists in layout
        }
    }
}
