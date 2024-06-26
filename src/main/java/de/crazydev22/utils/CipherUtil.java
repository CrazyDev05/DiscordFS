package de.crazydev22.utils;

import de.crazydev22.utils.container.Pair;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class CipherUtil {

	public static Pair<byte[], byte[]> encrypt(byte[] clean, byte[] key) {
		byte[] iv = createIV();
		return new Pair<>(iv, encrypt(clean, key, iv));
	}

	@SneakyThrows
	public static byte[] encrypt(byte[] clean, byte [] key, byte[] iv) {
		return create(Cipher.ENCRYPT_MODE, key, iv).doFinal(clean);
	}

	@SneakyThrows
	public static byte[] decrypt(byte[] encrypted, String key, byte[] iv) {
		return create(Cipher.DECRYPT_MODE, key, iv).doFinal(encrypted);
	}

	public static Pair<byte[], CipherInputStream> decrypt(InputStream is, String key) {
		byte[] iv = createIV();
		return new Pair<>(iv, new CipherInputStream(is, create(Cipher.DECRYPT_MODE, key, iv)));
	}

	public static CipherInputStream decrypt(InputStream is, byte[] key, byte[] iv) {
		return new CipherInputStream(is, create(Cipher.DECRYPT_MODE, key, iv));
	}

	public static Pair<byte[], CipherOutputStream> encrypt(OutputStream os, byte[] key) {
		byte[] iv = createIV();
		return new Pair<>(iv, new CipherOutputStream(os, create(Cipher.ENCRYPT_MODE, key, iv)));
	}

	public static Pair<byte[], CipherInputStream> encrypt(InputStream in, byte[] key) {
		byte[] iv = createIV();
		return new Pair<>(iv, new CipherInputStream(in, create(Cipher.ENCRYPT_MODE, key, iv)));
	}

	private static byte[] createIV() {
		// Generating IV.
		int ivSize = 16;
		byte[] iv = new byte[ivSize];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);

		return iv;
	}

	@SneakyThrows
	public static byte[] createHash(String key, int length) {
		byte[] keyBytes = new byte[length];
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(key.getBytes());
		System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.length);
		return keyBytes;
	}

	@SneakyThrows
	public static Cipher create(int mode, String key, byte[] iv) {
		SecretKeySpec secretKeySpec = new SecretKeySpec(createHash(key, 16), "AES");

		// init Cipher
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(mode, secretKeySpec, new IvParameterSpec(iv));
		return cipher;
	}

	@SneakyThrows
	public static Cipher create(int mode, byte[] key, byte[] iv) {
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(mode, secretKeySpec, new IvParameterSpec(iv));
		return cipher;
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
}
