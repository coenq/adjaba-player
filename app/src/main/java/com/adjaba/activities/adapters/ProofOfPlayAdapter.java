package com.adjaba.activities.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adjaba.R;
import com.adjaba.room.ImpressionDao;

import java.util.List;

/** Renders ImpressionDao.AdPlaySummary rows for ProofOfPlayActivity. */
public class ProofOfPlayAdapter extends RecyclerView.Adapter<ProofOfPlayAdapter.ViewHolder> {

    private final List<ImpressionDao.AdPlaySummary> rows;

    public ProofOfPlayAdapter(List<ImpressionDao.AdPlaySummary> rows) {
        this.rows = rows;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_proof_of_play, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImpressionDao.AdPlaySummary row = rows.get(position);
        holder.advertId.setText(row.advertId != null ? row.advertId : "(unknown ad)");
        holder.format.setText(row.format != null ? row.format : "—");
        holder.playCount.setText(row.playCount + "×");
        holder.totalDuration.setText(formatDuration(row.totalSeconds));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes > 0 ? (minutes + "m " + seconds + "s") : (seconds + "s");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView advertId, format, playCount, totalDuration;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            advertId = itemView.findViewById(R.id.rowAdvertId);
            format = itemView.findViewById(R.id.rowFormat);
            playCount = itemView.findViewById(R.id.rowPlayCount);
            totalDuration = itemView.findViewById(R.id.rowTotalDuration);
        }
    }
}
