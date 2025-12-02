package com.example.safemode;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Gerenciador de PINs do aplicativo SafeMode.
 * Responsável por armazenar, verificar e validar PINs de forma segura usando hash SHA-256
 * com salt aleatório. Gerencia tanto o PIN principal quanto o PIN secundário.
 */
public class PinManager {

    private static final String PREF_NAME = "LockScreenPrefs";
    private static final String KEY_PIN_HASH = "pin_hash";
    private static final String KEY_PIN_SALT = "pin_salt";
    private static final String KEY_SECONDARY_PIN_HASH = "secondary_pin_hash";
    private static final String KEY_SECONDARY_PIN_SALT = "secondary_pin_salt";
    private final SharedPreferences preferences;

    // Inicializa o gerenciador de PINs com contexto e SharedPreferences
    public PinManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Define o PIN principal de 4 dígitos com hash seguro usando SHA-256
    public boolean setPin(String pin) {
        try {
            if (pin == null || pin.length() != 4) {
                return false;
            }

            byte[] salt = generateSalt();
            String hash = hashPin(pin, salt);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_PIN_HASH, hash);
            editor.putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP));
            return editor.commit();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Verifica se o PIN principal informado corresponde ao armazenado
    public boolean verifyPin(String pin) {
        try {
            if (pin == null || pin.length() != 4) {
                return false;
            }

            String storedHash = preferences.getString(KEY_PIN_HASH, null);
            String storedSaltString = preferences.getString(KEY_PIN_SALT, null);

            if (storedHash == null || storedSaltString == null) {
                return true;
            }

            byte[] salt = Base64.decode(storedSaltString, Base64.NO_WRAP);
            String hash = hashPin(pin, salt);

            return hash.equals(storedHash);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Verifica se existe um PIN principal configurado
    public boolean hasPin() {
        String storedHash = preferences.getString(KEY_PIN_HASH, null);
        return storedHash != null && !storedHash.isEmpty();
    }

    // Remove o PIN principal armazenado e seu salt
    public void clearPin() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_PIN_HASH);
        editor.remove(KEY_PIN_SALT);
        editor.apply();
    }

    // Define o PIN secundário de 4 dígitos com hash seguro usando SHA-256
    public boolean setSecondaryPin(String pin) {
        try {
            if (pin == null || pin.length() != 4) {
                return false;
            }

            byte[] salt = generateSalt();
            String hash = hashPin(pin, salt);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_SECONDARY_PIN_HASH, hash);
            editor.putString(KEY_SECONDARY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP));
            return editor.commit();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Verifica se o PIN secundário informado corresponde ao armazenado
    public boolean verifySecondaryPin(String pin) {
        try {
            if (pin == null || pin.length() != 4) {
                return false;
            }

            String storedHash = preferences.getString(KEY_SECONDARY_PIN_HASH, null);
            String storedSaltString = preferences.getString(KEY_SECONDARY_PIN_SALT, null);

            if (storedHash == null || storedSaltString == null) {
                return false;
            }

            byte[] salt = Base64.decode(storedSaltString, Base64.NO_WRAP);
            String hash = hashPin(pin, salt);

            return hash.equals(storedHash);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Verifica se existe um PIN secundário configurado
    public boolean hasSecondaryPin() {
        String storedHash = preferences.getString(KEY_SECONDARY_PIN_HASH, null);
        return storedHash != null && !storedHash.isEmpty();
    }

    // Verifica o tipo de PIN informado: 1 para principal, 2 para secundário, 0 para inválido
    public int verifyPinType(String pin) {
        if (verifyPin(pin)) {
            return 1;
        } else if (verifySecondaryPin(pin)) {
            return 2;
        }
        return 0;
    }

    // Gera um salt aleatório de 16 bytes para aumentar a segurança do hash
    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    // Gera o hash SHA-256 do PIN combinado com o salt
    private String hashPin(String pin, byte[] salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.reset();
        digest.update(salt);
        byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }
}
