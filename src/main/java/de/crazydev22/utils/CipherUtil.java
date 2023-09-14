package de.crazydev22.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class CipherUtil {

	public static Pair encrypt(byte[] clean, String key) {

		// Generating IV.
		int ivSize = 16;
		byte[] iv = new byte[ivSize];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);

		return new Pair(iv, encrypt(clean, iv, key));
	}

	public static byte[] encrypt(byte[] clean, byte[] iv, String key) {
		try {
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			// Hashing key.
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(key.getBytes(StandardCharsets.UTF_8));
			byte[] keyBytes = new byte[16];
			System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
			SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

			// Encrypt.
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

			return cipher.doFinal(clean);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] decrypt(byte[] encrypted, byte[] iv, String key) {
		try {
			int keySize = 16;
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			// Hash key.
			byte[] keyBytes = new byte[keySize];
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(key.getBytes());
			System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.length);
			SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

			// Decrypt.
			Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
			return cipherDecrypt.doFinal(encrypted);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String toString(byte[] bytes) {
		String[] strings = new String[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			strings[i] = String.valueOf(bytes[i]);
		}

		return String.join("|", strings);
	}

	public static byte[] toBytes(String string) {
		var split = string.split("\\|");
		byte[] bytes = new byte[split.length];

		for (int i = 0; i < split.length; i++) {
			bytes[i] = Byte.parseByte(split[i]);
		}

		return bytes;
	}

	public record Pair(byte[] iv, byte[] encrypted){}
}
