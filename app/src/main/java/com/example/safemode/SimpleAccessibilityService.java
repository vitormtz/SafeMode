package com.example.safemode;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/**
 * Serviço de acessibilidade simples e vazio.
 * Criado apenas para permitir que o usuário habilite o serviço de acessibilidade
 * nas configurações do sistema sem executar nenhuma funcionalidade específica.
 */
public class SimpleAccessibilityService extends AccessibilityService {

    // Método chamado quando eventos de acessibilidade são detectados (sem implementação)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    // Método chamado quando o serviço é interrompido (sem implementação)
    @Override
    public void onInterrupt() {}

    // Método chamado quando o serviço é conectado ao sistema (sem implementação)
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }
}