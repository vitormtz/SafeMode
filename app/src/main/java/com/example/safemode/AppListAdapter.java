package com.example.safemode;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {

    private List<AppInfo> appList;
    private OnAppToggleListener listener;

    public interface OnAppToggleListener {
        void onAppToggled(AppInfo appInfo, boolean isBlocked);
    }

    public AppListAdapter(List<AppInfo> appList, OnAppToggleListener listener) {
        this.appList = appList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo app = appList.get(position);
        holder.bind(app);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public class AppViewHolder extends RecyclerView.ViewHolder {

        private ImageView iconImageView;
        private TextView nameTextView;
        private TextView packageTextView;
        private CheckBox blockCheckBox;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);

            iconImageView = itemView.findViewById(R.id.app_icon);
            nameTextView = itemView.findViewById(R.id.app_name);
            packageTextView = itemView.findViewById(R.id.app_package);
            blockCheckBox = itemView.findViewById(R.id.checkbox_block);
        }

        public void bind(AppInfo app) {

            iconImageView.setImageDrawable(app.icon);
            nameTextView.setText(app.appName);
            packageTextView.setText(app.packageName);
            packageTextView.setVisibility(View.GONE);
            blockCheckBox.setOnCheckedChangeListener(null);
            blockCheckBox.setChecked(app.isBlocked);
            blockCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                app.isBlocked = isChecked;
                if (listener != null) {
                    listener.onAppToggled(app, isChecked);
                }
            });
            itemView.setOnClickListener(v -> {
                blockCheckBox.setChecked(!blockCheckBox.isChecked());
            });
        }
    }
}
