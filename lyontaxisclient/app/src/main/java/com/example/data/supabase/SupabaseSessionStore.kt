package com.example.data.supabase

import android.content.Context
import android.util.Base64
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SupabaseSessionStore(context: Context) {
  private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

  fun save(session: SupabaseSessionResponse, identifier: String) {
    val expiresAt = System.currentTimeMillis() + (session.expires_in * 1000L)
    preferences.edit()
      .putString(KEY_IDENTIFIER, identifier)
      .putString(KEY_USER_ID, session.user?.id)
      .putString(KEY_ACCESS_TOKEN, encrypt(session.access_token))
      .putString(KEY_REFRESH_TOKEN, encrypt(session.refresh_token))
      .putLong(KEY_EXPIRES_AT, expiresAt)
      .apply()
  }

  fun read(): StoredSession? {
    val identifier = preferences.getString(KEY_IDENTIFIER, null) ?: return null
    val userId = preferences.getString(KEY_USER_ID, null) ?: return null
    val accessToken = preferences.getString(KEY_ACCESS_TOKEN, null)?.let(::decrypt) ?: return null
    val refreshToken = preferences.getString(KEY_REFRESH_TOKEN, null)?.let(::decrypt) ?: return null
    return StoredSession(userId, identifier, accessToken, refreshToken, preferences.getLong(KEY_EXPIRES_AT, 0L))
  }

  fun clear() {
    preferences.edit().clear().apply()
  }

  private fun encrypt(value: String): String {
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.ENCRYPT_MODE, key())
    val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
    val payload = ByteBuffer.allocate(cipher.iv.size + encrypted.size)
      .put(cipher.iv)
      .put(encrypted)
      .array()
    return Base64.encodeToString(payload, Base64.NO_WRAP)
  }

  private fun decrypt(value: String): String {
    val payload = Base64.decode(value, Base64.NO_WRAP)
    val iv = payload.copyOfRange(0, GCM_IV_LENGTH)
    val encrypted = payload.copyOfRange(GCM_IV_LENGTH, payload.size)
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
    return cipher.doFinal(encrypted).toString(Charsets.UTF_8)
  }

  private fun key(): SecretKey {
    val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
    val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
    if (existing != null) return existing

    val generator = KeyGenerator.getInstance(KEY_ALGORITHM, ANDROID_KEY_STORE)
    generator.init(android.security.keystore.KeyGenParameterSpec.Builder(
      KEY_ALIAS,
      android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
    ).setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
      .build())
    return generator.generateKey()
  }

  data class StoredSession(
    val userId: String,
    val identifier: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long
  )

  companion object {
    private const val PREFERENCES_NAME = "supabase_session"
    private const val KEY_ALIAS = "lyontaxis_supabase_session_key"
    private const val KEY_IDENTIFIER = "identifier"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_EXPIRES_AT = "expires_at"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEY_ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
  }
}
