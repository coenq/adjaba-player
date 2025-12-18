package com.adjaba.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adjaba.R;
//import com.adjaba.room.InfoDao_Impl;
import com.adjaba.room.InfoEntity;

import java.util.ArrayList;
import java.util.List;

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> {
    List<InfoEntity> infoEntityList;
    Context context;

    public InfoAdapter(List<InfoEntity> infoEntityList, Context context) {
        this.infoEntityList = infoEntityList;
        this.context = context;
    }

    @NonNull
    @Override
    public InfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_custom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoAdapter.ViewHolder holder, int position) {
        holder.infoTv.setText(infoEntityList.get(position).info);
    }

    @Override
    public int getItemCount() {
        return infoEntityList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView infoTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            infoTv = itemView.findViewById(R.id.text_info);
        }
    }
}
