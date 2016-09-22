package sto.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;

public final class DES3 {

	private final static String DES = "DESede";
	private final static byte[] DEFAULT_KEY_BYTE = {0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,(byte) 0xAA,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,(byte) 0xBB,0x01,0x02,0x03,0x04};


	private static byte[] encrypt(byte[] src, byte[] key) throws Exception {
		SecretKey securekey = new SecretKeySpec(key, DES);
		byte[] keyiv = {0,0,0,0,0,0,0,0};
		IvParameterSpec ips = new IvParameterSpec(keyiv);
		Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, securekey, ips);
		return cipher.doFinal(src);

	}

	private static byte[] decrypt(byte[] src, byte[] key) throws Exception {
		SecretKey securekey = new SecretKeySpec(key, DES);
		byte[] keyiv = {0,0,0,0,0,0,0,0};
		IvParameterSpec ips = new IvParameterSpec(keyiv);
		Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, securekey, ips);
		return cipher.doFinal(src);
	}

	private static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs.toUpperCase();
	}

	private static byte[] hex2byte(String hex) {

		if (hex.length() % 2 != 0)
			return null;

		byte[] ret = new byte[hex.length() / 2];
		for (int i = 0; i < hex.length(); i = i + 2) {
			ret[i / 2] = (byte) (Integer.parseInt(hex.substring(i, i + 2), 16));
		}
		return ret;
	}

	private static String padTo8(String key) {
		while (key.length() % 8 != 0) {
			key += "=";
		}
		return key;
	}

	/**
	 * 加密数据算法
	 * 
	 * @param src
	 *            明文内容
	 * @return 密文
	 * @throws Exception
	 *             错误
	 */
	public static String encrypt(String src) throws Exception {

		return new String(Base64.encode(encrypt(src.getBytes(), DEFAULT_KEY_BYTE)));
	}

	/**
	 * 解密密文
	 * 
	 * @param src
	 *            密文
	 * @return 明文数据
	 * @throws Exception
	 *             错误
	 */
	public static String decrypt(String src) throws Exception {

		return new String(
				decrypt(Base64.decode(src), DEFAULT_KEY_BYTE));
	}

	/**
	 * 加密数据算法
	 * 
	 * @param src
	 *            明文内容
	 * @return 密文
	 * @throws Exception
	 *             错误
	 */
	public static String encrypt(String src, String key) throws Exception {

		return byte2hex(encrypt(src.getBytes(), padTo8(key).getBytes()));
	}

	/**
	 * 解密密文
	 * 
	 * @param src
	 *            密文
	 * @return 明文数据
	 * @throws Exception
	 *             错误
	 */
	public static String decrypt(String src, String key) throws Exception {
		return new String(decrypt(hex2byte(src), padTo8(key).getBytes()));
	}

	public static void main(String[] args) throws Exception {
//		String key = "aGViY2E=";
		String source = "xFDM8PsIaTZTZYOnz05MWg==";
//		String source = "12345678";
		System.out.println(decrypt(source));

	}

}
