package com.example.safemode;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Serviço que cria a "cortina" sobre outros apps - VERSÃO ATUALIZADA
 * Agora usa o novo layout que parece uma mensagem de erro do sistema
 */
public class BlockOverlayService extends Service {
    private WindowManager windowManager;
    private View overlayView;
    private String blockedPackageName;
    private Handler timeoutHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        timeoutHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            // Pegar informações sobre qual app foi bloqueado
            if (intent != null) {
                blockedPackageName = intent.getStringExtra("blocked_package");
            }

            // PASSO 1: Aguardar um pouco para o usuário ver o app aberto
            scheduleAppClosure();

        } catch (Exception e) {
            stopSelf();
        }

        return START_NOT_STICKY; // Não reiniciar automaticamente
    }

    /**
     * Cria a tela de bloqueio com o novo layout estilo sistema Android
     */
    private void createOverlay() {
        try {

            // Verificar se já existe uma view
            if (overlayView != null) {
                removeOverlay();
            }

            // Criar o layout da tela de bloqueio
            LayoutInflater inflater = LayoutInflater.from(this);
            overlayView = inflater.inflate(R.layout.overlay_block, null);

            // Configurar o conteúdo
            setupBlockMessage();
            setupButtons();

            // Configurar parâmetros da janela
            WindowManager.LayoutParams params = createWindowParams();

            // Mostrar a janela na tela
            windowManager.addView(overlayView, params);

        } catch (Exception e) {
            stopSelf();
        }
    }

    /**
     * Cria parâmetros de janela para o novo layout
     */
    private WindowManager.LayoutParams createWindowParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        // Tipo de janela apropriado
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // Flags para bloquear TUDO, mas com fundo semi-transparente
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;

        // Formato de pixel
        params.format = PixelFormat.RGBA_8888;

        // Tamanho e posição
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;

        // Posição
        params.x = 0;
        params.y = 0;

        // Para versões mais novas do Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        return params;
    }

    /**
     * Configura a mensagem que será mostrada no novo layout
     */
    private void setupBlockMessage() {
        try {
            // Encontrar o elemento do texto
            TextView textoErro = overlayView.findViewById(R.id.textoErro);

            if (textoErro != null) {
                // Mensagem personalizada baseada no app
                String appName = getAppName(blockedPackageName);
                String message = "O app " + appName + " apresenta falhas contínuas";

                textoErro.setText(message);
            }

        } catch (Exception e) {
        }
    }

    /**
     * Configura os botões do novo layout
     */
    private void setupButtons() {
        try {
            // Botão "Informações do app"
            Button btnInfoApp = overlayView.findViewById(R.id.btnInfoApp);
            if (btnInfoApp != null) {
                btnInfoApp.setOnClickListener(v -> {
                    // Por enquanto, só remove o overlay e vai para home
                    removeOverlay();
                    goToHome();
                });
            }

            // Botão "Fechar app"
            Button btnFecharApp = overlayView.findViewById(R.id.btnFecharApp);
            if (btnFecharApp != null) {
                btnFecharApp.setOnClickListener(v -> {
                    removeOverlay();
                    goToHome();
                });
            }

        } catch (Exception e) {
        }
    }

    /**
     * Programa o fechamento do app após um delay (para usuário ver que abriu)
     */
    private void scheduleAppClosure() {
        try {

            if (timeoutHandler == null) {
                timeoutHandler = new Handler();
            }

            // Aguardar 2-3 segundos para o usuário ver o app aberto, aí fechar
            timeoutHandler.postDelayed(() -> {
                closeBlockedApp();

                // Depois de fechar, aguardar mais um pouco para mostrar a mensagem
                scheduleOverlayCreation();

            }, 350); // 2.5 segundos de delay antes de fechar

        } catch (Exception e) {
            stopSelf();
        }
    }

    /**
     * Fecha o app bloqueado (para parecer que travou)
     */
    private void closeBlockedApp() {
        try {

            // Mandar para home (simula o app "travando" e voltando para tela inicial)
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            startActivity(homeIntent);

        } catch (Exception e) {
        }
    }

    /**
     * Programa a criação do overlay após um delay (para simular processamento)
     */
    private void scheduleOverlayCreation() {
        try {

            // Aguardar 1 segundo depois de fechar o app, aí mostrar a mensagem
            timeoutHandler.postDelayed(() -> {
                createOverlay();
                scheduleRemoval(); // Programar remoção automática
            }, 100); // 1 segundo de delay após fechar

        } catch (Exception e) {
            stopSelf();
        }
    }
    private void scheduleRemoval() {
        try {
            // Cancelar timeout anterior se existir
            if (timeoutHandler != null) {
                timeoutHandler.removeCallbacksAndMessages(null);
            }

            // Remover após 15 segundos (um pouco mais de tempo para parecer real)
            timeoutHandler.postDelayed(() -> {
                removeOverlay();
                goToHome();
            }, 15000); // 15 segundos

        } catch (Exception e) {
        }
    }

    /**
     * Remove a tela de bloqueio de forma segura
     */
    private void removeOverlay() {
        try {

            // Cancelar TODOS os timeouts
            if (timeoutHandler != null) {
                timeoutHandler.removeCallbacksAndMessages(null);
            }

            // Remover view
            if (overlayView != null && windowManager != null) {
                windowManager.removeView(overlayView);
                overlayView = null;
            }

        } catch (Exception e) {
            overlayView = null; // Garantir que seja limpo mesmo com erro
        }

        stopSelf();
    }

    /**
     * Leva o usuário para a tela inicial
     */
    private void goToHome() {
        try {

            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(homeIntent);

        } catch (Exception e) {
        }
    }

    /**
     * Pega o nome amigável do app
     */
    private String getAppName(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return "Sistema Android";
        }

        try {
            android.content.pm.PackageManager pm = getPackageManager();
            android.content.pm.ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            String appName = pm.getApplicationLabel(appInfo).toString();

            // Para deixar mais "convincente", sempre retorna "Sistema Android"
            // independente do app real
            return "Sistema Android";

        } catch (Exception e) {
            return "Sistema Android";
        }
    }

    @Override
    public void onDestroy() {
        try {

            // Cancelar timeout
            if (timeoutHandler != null) {
                timeoutHandler.removeCallbacksAndMessages(null);
            }

            // Remover overlay se ainda existir
            if (overlayView != null) {
                removeOverlay();
            }

        } catch (Exception e) {
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}