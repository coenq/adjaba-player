package com.adjaba.social;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adjaba.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SocialPostAdapter extends RecyclerView.Adapter<SocialPostAdapter.PostViewHolder> {

    private final List<SocialMediaManager.SocialPost> posts;
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public SocialPostAdapter(List<SocialMediaManager.SocialPost> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.social_post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        SocialMediaManager.SocialPost post = posts.get(position);
        holder.username.setText(post.username != null ? post.username : "");
        holder.text.setText(post.text != null ? post.text : "");
        holder.timestamp.setText(post.timestamp != null ? TIME_FMT.format(post.timestamp) : "");
        String stats = "♥ " + post.likes;
        if (post.platform == SocialMediaManager.SocialPlatform.TWITTER && post.retweets > 0) {
            stats += "   ⟳ " + post.retweets;
        }
        holder.stats.setText(stats);
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView username, text, timestamp, stats;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.postUsername);
            text = itemView.findViewById(R.id.postText);
            timestamp = itemView.findViewById(R.id.postTimestamp);
            stats = itemView.findViewById(R.id.postStats);
        }
    }
}
