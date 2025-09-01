package com.adjaba.others;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adjaba.R;
import com.adjaba.models.newmodels.MediaModel;
import com.bumptech.glide.Glide;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private List<MediaModel> mediaList;

    public MediaAdapter(List<MediaModel> mediaList) {
        this.mediaList = mediaList;
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        VideoView videoView;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.adImageView);
            //videoView = itemView.findViewById(R.id.adVideoView);
        }

        public void bind(MediaModel media) {
            if (media.getType().equals("image")) {
                videoView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext()).load(media.getUrl()).into(imageView);
            } else if (media.getType().equals("video")) {
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(Uri.parse(media.getUrl()));
                videoView.start(); // تشغيل الفيديو تلقائيًا
            }
        }
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ad_container, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        holder.bind(mediaList.get(position));
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }
}
