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

/**
 * Adaptador para mostrar a lista de apps na tela
 * É como um assistente que organiza os apps na lista
 */
public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {

    private List<AppInfo> appList;
    private OnAppToggleListener listener;

    // Interface para avisar quando um app é marcado/desmarcado
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

    /**
     * Classe que representa cada item na lista
     * É como um cartãozinho para cada app
     */
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

        /**
         * Preenche os dados do app no cartãozinho
         */
        public void bind(AppInfo app) {

            // Mostrar ícone do app
            iconImageView.setImageDrawable(app.icon);

            // Mostrar nome do app
            nameTextView.setText(app.appName);

            // Mostrar nome técnico (opcional, pode ser escondido)
            packageTextView.setText(app.packageName);
            packageTextView.setVisibility(View.GONE); // Esconder por padrão

            // Configurar checkbox
            blockCheckBox.setOnCheckedChangeListener(null); // Limpar listener anterior
            blockCheckBox.setChecked(app.isBlocked);

            // Configurar o que acontece quando marca/desmarca
            blockCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                app.isBlocked = isChecked;
                if (listener != null) {
                    listener.onAppToggled(app, isChecked);
                }
            });

            // Permitir clicar no item inteiro para marcar/desmarcar
            itemView.setOnClickListener(v -> {
                blockCheckBox.setChecked(!blockCheckBox.isChecked());
            });
        }
    }
}
