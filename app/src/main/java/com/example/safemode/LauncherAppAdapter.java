package com.example.safemode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LauncherAppAdapter extends RecyclerView.Adapter<LauncherAppAdapter.AppViewHolder> {

    private Context context;
    private List<LauncherAppInfo> apps;
    private OnAppClickListener listener;

    public interface OnAppClickListener {
        void onAppClick(LauncherAppInfo app);
    }

    public LauncherAppAdapter(Context context, List<LauncherAppInfo> apps, OnAppClickListener listener) {
        this.context = context;
        this.apps = apps;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_launcher_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        LauncherAppInfo app = apps.get(position);
        holder.bind(app);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public void updateApps(List<LauncherAppInfo> newApps) {
        this.apps.clear();
        this.apps.addAll(newApps);
        notifyDataSetChanged();
    }

    class AppViewHolder extends RecyclerView.ViewHolder {

        private ImageView iconView;
        private TextView nameView;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.app_icon);
            nameView = itemView.findViewById(R.id.app_name);
        }

        public void bind(LauncherAppInfo app) {
            iconView.setImageDrawable(app.icon);
            nameView.setText(app.appName);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAppClick(app);
                }
            });
        }
    }
}
