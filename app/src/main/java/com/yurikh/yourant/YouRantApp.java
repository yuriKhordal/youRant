package com.yurikh.yourant;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.yurikh.yourant.model.AuthToken;
import com.yurikh.yourant.model.User;
import com.yurikh.yourant.network.LoggedInParams;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

public class YouRantApp extends Application {
    private final String AUTH_KEY_ALIAS = "auth_key";
    private final int AUTH_KEY_SIZE = 256;
    private final String PREFS_AUTH = "auth";
    private final String PREFS_AUTH_TOKEN_ID = "token_id";
    private final String PREFS_AUTH_TOKEN_KEY = "token_key";
    private final String PREFS_AUTH_TOKEN_EXPIRE = "expire_time";
    private final String PREFS_AUTH_USER_ID = "user_id";

    private static YouRantApp instance;

    private AuthToken authToken;
    private LoggedInParams loggedInParams;
//    private User loggedInUser;
    /**
     * If this is not null, the auth has failed to load, and the first activity should
     * display the error and tell the user logging in is disabled.
     */
    private Exception authLoadError;

    public YouRantApp() {
        super();
        loggedInParams = LoggedInParams.ANONYMOUS;
        authToken = null;
        instance = this;
    }

    public static YouRantApp getInstance() { return instance; }

    @Override
    public void onCreate() {
        super.onCreate();

        // Get auth data:
        try {
            Key key = getOrGenerateKey();
            loadAuthToken(key);
        } catch (Exception e) {
            authLoadError = e;
            loggedInParams = LoggedInParams.ANONYMOUS;
        }
    }

    public LoggedInParams getLoggedInParams() { return loggedInParams; }

//    public User getLoggedInUser() { return  loggedInUser; }

    public boolean isLoggedIn() { return authToken != null; }

    /**
     * Should only be called from an activity, requires UI context!
     * Checks whether there were errors in the initial process of reading, loading, or decrypting
     * authentication data, and if initializing authentication failed, display an alert to the user
     * that logging in has been disabled.
     * After displaying the error the error is cleared.
     */
    public void checkForAuthFail(Context ctx) {
        if (authLoadError == null)
            return;

        Helper.displayError(ctx, authLoadError);
        new AlertDialog.Builder(ctx)
            .setTitle("Credentials Loading Error!")
            .setMessage(
                "Encountered an while error trying to load login authentication. " +
                "Due to the error, youRant won't be able to load nor save login information." +
                "Logging-in in the current session will only persist while the app is open."
            ).show();
        authLoadError = null;
    }

    private Key getOrGenerateKey()
    throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException,
    UnrecoverableKeyException, InvalidAlgorithmParameterException, NoSuchProviderException {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);

        // If key exists, return it:
        if (ks.containsAlias(AUTH_KEY_ALIAS)) {
            return ks.getKey(AUTH_KEY_ALIAS, null);
        }

        // If key doesn't (e.g. fresh install), generate a new key:
        KeyGenerator keygen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        int purpose = KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT;
        KeyGenParameterSpec params = new KeyGenParameterSpec.Builder(AUTH_KEY_ALIAS, purpose)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(AUTH_KEY_SIZE)
                .build();
        keygen.init(params);
        return keygen.generateKey();
    }

    public void login(AuthToken token)
    throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException,
    UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException,
    IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        // Save tokens at least for this session, even if securely saving them fails.
        authToken = token;
        loggedInParams = new LoggedInParams(token.id, token.key, token.user_id);

        // Get key
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        Key key = ks.getKey(AUTH_KEY_ALIAS, null);

        // ENCRYPT
        String encryptedTokenId = Base64.encodeToString(
                Helper.aesEncryptLong(token.id, key), Base64.NO_WRAP
        );
        String encryptedTokenKey = Helper.aesEncryptString(token.key, key);
        String encryptedTokenExpiry = Base64.encodeToString(
                Helper.aesEncryptLong(token.expire_time, key), Base64.NO_WRAP
        );
        String encryptedUserId = Base64.encodeToString(
                Helper.aesEncryptLong(token.user_id, key), Base64.NO_WRAP
        );

        getSharedPreferences(PREFS_AUTH, MODE_PRIVATE).edit()
                .putString(PREFS_AUTH_TOKEN_ID, encryptedTokenId)
                .putString(PREFS_AUTH_TOKEN_KEY, encryptedTokenKey)
                .putString(PREFS_AUTH_TOKEN_EXPIRE, encryptedTokenExpiry)
                .putString(PREFS_AUTH_USER_ID, encryptedUserId)
                .apply();
    }

    public void logout() {
        authToken = null;
        loggedInParams = LoggedInParams.ANONYMOUS;

        getSharedPreferences(PREFS_AUTH, MODE_PRIVATE)
                .edit().clear().apply();
    }

    public void loadAuthToken(Key key)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
    InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SharedPreferences authPrefs = getSharedPreferences(PREFS_AUTH, MODE_PRIVATE);
        // If no auth tokens saved
        if (!authPrefs.contains(PREFS_AUTH_TOKEN_KEY)) {
            authToken = null;
            loggedInParams = LoggedInParams.ANONYMOUS;
            return;
        }

        // Decrypt auth tokens data:
        byte[] encryptedTokenId = Base64.decode(authPrefs.getString(PREFS_AUTH_TOKEN_ID, null), Base64.NO_WRAP);
        String encryptedTokenKey = authPrefs.getString(PREFS_AUTH_TOKEN_KEY, null);
        byte[] encryptedTokenExpiry = Base64.decode(authPrefs.getString(PREFS_AUTH_TOKEN_EXPIRE, null), Base64.NO_WRAP);
        byte[] encryptedUserId = Base64.decode(authPrefs.getString(PREFS_AUTH_USER_ID, null), Base64.NO_WRAP);

        long tokenId = Helper.aesDecryptAsLong(encryptedTokenId, key);
        String tokenKey = Helper.aesDecryptAsString(encryptedTokenKey, key);
        long expireTime = Helper.aesDecryptAsLong(encryptedTokenExpiry, key);
        long userId = Helper.aesDecryptAsLong(encryptedUserId, key);

        authToken = new AuthToken(tokenId, tokenKey, expireTime, userId);
        loggedInParams = new LoggedInParams(tokenId, tokenKey, userId);
    }
}
