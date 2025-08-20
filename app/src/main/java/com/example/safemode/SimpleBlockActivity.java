package com.example.safemode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Activity de bloqueio - VERSÃO ATUALIZADA
 * Agora usa o novo layout que parece uma mensagem de erro do sistema
 */
public class SimpleBlockActivity extends Activity {

    private static final String TAG = "SimpleBlockActivity";
    private Handler autoCloseHandler;
    private static boolean isCurrentlyShowing = false; // Flag para evitar duplicatas
    private static long lastCloseTime = 0; // Tempo da última vez que fechou
    private static SimpleBlockActivity currentInstance = null; // ✅ NOVO: Referência da instância atual

    /**
     * ✅ NOVO: Método estático para verificar se há uma instância ativa
     */
    public static boolean isCurrentlyActive() {
        return isCurrentlyShowing && currentInstance != null;
    }

    /**
     * ✅ NOVO: Método estático para obter a instância atual
     */
    public static SimpleBlockActivity getCurrentInstance() {
        return currentInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ PROTEÇÃO MELHORADA: Só bloquear se realmente está rodando E foi recente
        long currentTime = System.currentTimeMillis();
        long timeSinceLastClose = currentTime - lastCloseTime;

        if (isCurrentlyShowing && timeSinceLastClose > 1000) {
            isCurrentlyShowing = false;
        }

        if (isCurrentlyShowing) {
            finish();
            return;
        }

        // ✅ Marcar que agora está rodando e salvar referência
        isCurrentlyShowing = true;
        currentInstance = this;

        try {
            // ✅ PASSO 1: Configurar janela invisível primeiro (app "trava")
            setupInvisibleWindow();

            // ✅ PASSO 2: Travar o app imediatamente (overlay transparente total)
            createInvisibleOverlay();

            // ✅ PASSO 3: Aguardar 2 segundos, depois mostrar a mensagem
            scheduleErrorMessage();

        } catch (Exception e) {
            finish();
        }
    }

    /**
     * Configura janela invisível para travar o app imediatamente (MANTENDO barras)
     */
    private void setupInvisibleWindow() {
        try {
            // Remover apenas barra de título do app
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            // Configurações da janela
            Window window = getWindow();

            // ✅ FLAGS SEM FULLSCREEN para manter barras do sistema
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            );

            // ✅ Manter barras do sistema visíveis (não forçar transparência total)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Manter cor padrão das barras
                window.setStatusBarColor(getResources().getColor(android.R.color.black));
                window.setNavigationBarColor(getResources().getColor(android.R.color.black));
            }

        } catch (Exception e) {
        }
    }

    /**
     * Cria overlay invisível que trava o app (SEM afetar barras do sistema)
     */
    private void createInvisibleOverlay() {
        try {
            // ✅ Layout transparente que só bloqueia a área do app
            android.widget.FrameLayout invisibleLayout = new android.widget.FrameLayout(this);
            invisibleLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            invisibleLayout.setClickable(true); // Captura cliques do app
            invisibleLayout.setFocusable(true);

            // ✅ IMPORTANTE: Definir altura para não cobrir barras do sistema
            android.view.ViewGroup.LayoutParams params = new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
            invisibleLayout.setLayoutParams(params);

            setContentView(invisibleLayout);

        } catch (Exception e) {
        }
    }

    /**
     * Programa comportamentos da mensagem e timeout de segurança
     */
    private void scheduleErrorMessage() {
        try {
            autoCloseHandler = new Handler();

            // ✅ Aguardar 2 segundos, depois mostrar a mensagem de erro
            autoCloseHandler.postDelayed(() -> {
                showErrorMessage();
            }, 2000); // 2 segundos de delay

            // ✅ NOVO: Timeout de segurança - se algo der errado, limpar flag após 30s
            autoCloseHandler.postDelayed(() -> {
                isCurrentlyShowing = false;
                lastCloseTime = System.currentTimeMillis();
            }, 30000); // 30 segundos
        } catch (Exception e) {
        }
    }

    /**
     * Mostra a mensagem de erro por cima do app travado
     */
    private void showErrorMessage() {
        try {
            // ✅ Agora sim, mostrar o layout da mensagem
            setContentView(R.layout.overlay_block);

            // Configurar interface da mensagem
            setupUI();

        } catch (Exception e) {
        }
    }

    /**
     * Configura a janela para aparecer como overlay SUPER PERSISTENTE
     */
    private void setupWindow() {
        try {
            // Remover barra de título
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            // Configurações da janela
            Window window = getWindow();

            // ✅ FLAGS SUPER PERSISTENTES - nunca sair da tela
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            );

            // ✅ Configurar fundo transparente
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
            window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);

            // Para Android 6.0+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                window.setFlags(
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                );
            }

        } catch (Exception e) {
        }
    }

    /**
     * Configura a interface do usuário com o novo layout
     */
    private void setupUI() {
        try {
            // Pegar informações do app bloqueado
            String blockedPackage = getIntent().getStringExtra("blocked_package");
            String appName = getAppName(blockedPackage);

            // Configurar texto principal com nome real do app
            TextView textoErro = findViewById(R.id.textoErro);
            if (textoErro != null) {
                // ✅ MUDANÇA: Mostrar nome real do app
                String message = "O app " + appName + " apresenta falhas contínuas";
                textoErro.setText(message);
            }

            // Configurar botão "Informações do app"
            Button btnInfoApp = findViewById(R.id.btnInfoApp);
            if (btnInfoApp != null) {
                btnInfoApp.setOnClickListener(v -> {
                    openAppInfo(blockedPackage);
                });
            }

            // Configurar botão "Fechar app"
            Button btnFecharApp = findViewById(R.id.btnFecharApp);
            if (btnFecharApp != null) {
                btnFecharApp.setOnClickListener(v -> {
                    goHome();
                });
            }

        } catch (Exception e) {
        }
    }

    /**
     * Cancela o fechamento automático
     */
    private void cancelAutoClose() {
        if (autoCloseHandler != null) {
            autoCloseHandler.removeCallbacksAndMessages(null);
            autoCloseHandler = null;
        }
    }

    /**
     * Pega nome real do aplicativo bloqueado
     */
    private String getAppName(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return "este aplicativo";
        }

        try {
            android.content.pm.PackageManager pm = getPackageManager();
            android.content.pm.ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            String realAppName = pm.getApplicationLabel(appInfo).toString();

            // ✅ MUDANÇA: Agora retorna o nome real do app
            return realAppName;

        } catch (Exception e) {

            // ✅ Fallback melhorado: extrair nome do package
            return getAppNameFromPackage(packageName);
        }
    }

    /**
     * Extrai nome aproximado do package (fallback)
     */
    private String getAppNameFromPackage(String packageName) {
        try {
            // Exemplos de conversão:
            // com.whatsapp → WhatsApp
            // com.instagram.android → Instagram
            // com.google.android.youtube → YouTube

            if (packageName.contains("whatsapp")) return "WhatsApp";
            if (packageName.contains("instagram")) return "Instagram";
            if (packageName.contains("youtube")) return "YouTube";
            if (packageName.contains("facebook")) return "Facebook";
            if (packageName.contains("twitter")) return "Twitter";
            if (packageName.contains("tiktok")) return "TikTok";
            if (packageName.contains("spotify")) return "Spotify";
            if (packageName.contains("netflix")) return "Netflix";
            if (packageName.contains("gmail")) return "Gmail";
            if (packageName.contains("chrome")) return "Chrome";
            if (packageName.contains("gallery")) return "Galeria";
            if (packageName.contains("camera")) return "Câmera";
            if (packageName.contains("phone")) return "Telefone";
            if (packageName.contains("message")) return "Mensagens";

            // Se não encontrar, usar última parte do package
            String[] parts = packageName.split("\\.");
            if (parts.length > 0) {
                String lastPart = parts[parts.length - 1];
                // Capitalizar primeira letra
                return lastPart.substring(0, 1).toUpperCase() + lastPart.substring(1);
            }

            return "este aplicativo";

        } catch (Exception e) {
            return "este aplicativo";
        }
    }

    /**
     * Abre a tela de informações do aplicativo bloqueado
     */
    private void openAppInfo(String packageName) {
        try {

            if (packageName == null || packageName.isEmpty()) {
                goHome();
                return;
            }

            // ✅ Intent para abrir as configurações do app específico
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + packageName));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);

            // ✅ Fechar nossa mensagem após abrir as configurações
            new Handler().postDelayed(() -> {
                forceClose();
            }, 500); // Pequeno delay para suavizar

        } catch (Exception e) {
            // ✅ Fallback: se não conseguir abrir configurações, tentar configurações gerais
            try {
                Intent fallbackIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                fallbackIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(fallbackIntent);

                new Handler().postDelayed(() -> forceClose(), 500);

            } catch (Exception e2) {
                goHome();
            }
        }
    }

    /**
     * Fecha a mensagem APENAS quando usuário clicar nos botões
     */
    private void forceClose() {
        try {
            cancelAutoClose();

            // ✅ Registrar tempo de fechamento e limpar referências
            lastCloseTime = System.currentTimeMillis();
            isCurrentlyShowing = false;
            currentInstance = null;

            // ✅ Fechar de verdade (chamada manual)
            super.finish();

        } catch (Exception e) {
            // Se der erro, forçar limpeza e saída extrema
            lastCloseTime = System.currentTimeMillis();
            isCurrentlyShowing = false;
            System.exit(0);
        }
    }

    /**
     * Volta para a tela inicial (APENAS quando usuário clicar)
     */
    private void goHome() {
        try {

            // Primeiro ir para home
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(homeIntent);

            // Depois fechar nossa mensagem
            new Handler().postDelayed(() -> forceClose(), 200);

        } catch (Exception e) {
            forceClose(); // Fechar mesmo assim
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean isFinishing() {
        return false;
    }

    @Override
    protected void onDestroy() {
        try {
            cancelAutoClose();

            // ✅ Registrar tempo e limpar referências quando Activity for destruída
            lastCloseTime = System.currentTimeMillis();
            isCurrentlyShowing = false;
            currentInstance = null;

        } catch (Exception e) {
        }
        super.onDestroy();
    }
}