package com.example.safemode;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class PinManager {

    private static final String PREF_NAME = "LockScreenPrefs";
    private static final String KEY_PIN_HASH = "pin_hash";
    private static final String KEY_PIN_SALT = "pin_salt";

    private SharedPreferences preferences;

    public PinManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

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

    public boolean hasPin() {
        String storedHash = preferences.getString(KEY_PIN_HASH, null);
        return storedHash != null && !storedHash.isEmpty();
    }

    public void clearPin() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_PIN_HASH);
        editor.remove(KEY_PIN_SALT);
        editor.apply();
    }

    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private String hashPin(String pin, byte[] salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.reset();
        digest.update(salt);
        byte[] hash = digest.digest(pin.getBytes("UTF-8"));
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }
}
