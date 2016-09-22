package sto.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

public class PKCS12 {
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	/**
	 * Java密钥库(Java Key Store，JKS)KEY_STORE
	 */
	public static final String KEY_STORE = "PKCS12";

	public static final String X509 = "X.509";
	public static final String certDefaultPassword = "123456";
	
	public static void main(String[] args) throws Exception {
		//（Base64编码后使用3des加密，又Base64编码）
		String p12Cert ="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";
		
		System.err.println(PKCS12.getSignCert(p12Cert));
		System.err.println(PKCS12.getEncryptCert(p12Cert));
		System.err.println(PKCS12.getCertId(p12Cert));
	}
	
	
	/**
	 * 获得签名证书
	 * 
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	public static String getSignCert(String p12Cert)throws Exception {
		byte[] p12Cert_base64_byte = Base64.decode(DES3.decrypt(p12Cert));//b64解码
		KeyStore ks = getKeyStore(new ByteArrayInputStream(p12Cert_base64_byte),certDefaultPassword);
		Enumeration enums = ks.aliases();  
        while (enums.hasMoreElements()) {
            String keyAlias = (String) enums.nextElement();
            X509Certificate	certificate =  (X509Certificate)ks.getCertificate(keyAlias);
            boolean[] usage = certificate.getKeyUsage();
            if(usage[0]){
            	return new String(Base64.encode(certificate.getEncoded()));
            }
        }  
        return null;
	}
	
	/**
	 * 获得加密证书
	 * 
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	public static String getEncryptCert(String p12Cert)throws Exception {
		byte[] p12Cert_base64_byte = Base64.decode(DES3.decrypt(p12Cert));//b64解码
		KeyStore ks = getKeyStore(new ByteArrayInputStream(p12Cert_base64_byte),certDefaultPassword);
		Enumeration enums = ks.aliases();  
        while (enums.hasMoreElements()) {
            String keyAlias = (String) enums.nextElement();
            X509Certificate	certificate =  (X509Certificate)ks.getCertificate(keyAlias);
            boolean[] usage = certificate.getKeyUsage();
            if(usage[3]){
            	return new String(Base64.encode(certificate.getEncoded()));
            }
        }  
        return null;
	}
	/**
	 * 获得cn项
	 * 
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	public static String getCertId(String p12Cert)throws Exception {
		byte[] p12Cert_base64_byte = Base64.decode(DES3.decrypt(p12Cert));//b64解码
		KeyStore ks = getKeyStore(new ByteArrayInputStream(p12Cert_base64_byte), "123456");
		Enumeration enums = ks.aliases();  
        while (enums.hasMoreElements()) {
            String keyAlias = (String) enums.nextElement();
            X509Certificate	certificate =  (X509Certificate)ks.getCertificate(keyAlias);
            String dn = certificate.getSubjectDN().getName();
            String[] givename = dn.split("GIVENNAME=");
            return givename[1].split(",")[0];
        }  
        return null;
	}
	
	/**
	 * 由 KeyStore获得私钥
	 * 
	 * @param keyStorePath
	 * @param keyStorePassword
	 * @param alias
	 * @param aliasPassword
	 * @return
	 * @throws Exception
	 */
	private static PrivateKey getPrivateKey(InputStream is,
			String keyStorePassword, String alias, String aliasPassword)
			throws Exception {
		KeyStore ks = getKeyStore(is, keyStorePassword);
		PrivateKey key = (PrivateKey) ks.getKey(alias,
				aliasPassword.toCharArray());
		return key;
	}

	/**
	 * 由 Certificate获得公钥
	 * 
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	private static PublicKey getPublicKey(InputStream is, String keyStorePassword)
			throws Exception {
		Certificate certificate = getCertificate(is, keyStorePassword);
		PublicKey key = certificate.getPublicKey();
		return key;
	}

	/**
	 * 获得Certificate
	 * 
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	public static Certificate getCertificate(InputStream is, String keyStorePassword)
			throws Exception {
		KeyStore ks = getKeyStore(is, keyStorePassword);
		Enumeration enums = ks.aliases();  
        while (enums.hasMoreElements()) {
            String keyAlias = (String) enums.nextElement();
            System.err.println(keyAlias);
            return ks.getCertificate(keyAlias);
        }  
        return null;
	}
	
	
	
	

	/**
	 * 获得Certificate
	 * 
	 * @param keyStorePath
	 * @param keyStorePassword
	 * @param alias
	 * @return
	 * @throws Exception
	 */
	public static Certificate getCertificate(InputStream is,
			String keyStorePassword, String alias) throws Exception {
		KeyStore ks = getKeyStore(is, keyStorePassword);
		Certificate certificate = ks.getCertificate(alias);

		return certificate;
	}

	/**
	 * 获得KeyStore
	 * 
	 * @param keyStorePath
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static KeyStore getKeyStore(InputStream is, String password)
			throws Exception {
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(is, password.toCharArray());
		is.close();
		return ks;
	}
	
	/**
	 * 私钥加密
	 * 
	 * @param data
	 * @param keyStorePath
	 * @param keyStorePassword
	 * @param alias
	 * @param aliasPassword
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPrivateKey(byte[] data, InputStream is,
			String keyStorePassword, String alias, String aliasPassword)
			throws Exception {
		// 取得私钥
		PrivateKey privateKey = getPrivateKey(is, keyStorePassword,
				alias, aliasPassword);

		// 对数据加密
		Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);

		return cipher.doFinal(data);

	}

	/**
	 * 私钥解密
	 * 
	 * @param data
	 * @param keyStorePath
	 * @param alias
	 * @param keyStorePassword
	 * @param aliasPassword
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] data, InputStream is,
			String alias, String keyStorePassword, String aliasPassword)
			throws Exception {
		// 取得私钥
		PrivateKey privateKey = getPrivateKey(is, keyStorePassword,
				alias, aliasPassword);

		// 对数据加密
		Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);

		return cipher.doFinal(data);

	}

	/**
	 * 公钥加密
	 * 
	 * @param data
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPublicKey(byte[] data, InputStream is, String keyStorePassword)
			throws Exception {

		// 取得公钥
		PublicKey publicKey = getPublicKey(is, keyStorePassword);
		// 对数据加密
		Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);

		return cipher.doFinal(data);

	}

	/**
	 * 公钥解密
	 * 
	 * @param data
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPublicKey(byte[] data, InputStream is, String keyStorePassword)
			throws Exception {
		// 取得公钥
		PublicKey publicKey = getPublicKey(is, keyStorePassword);

		// 对数据加密
		Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicKey);

		return cipher.doFinal(data);

	}

	/**
	 * 验证Certificate
	 * 
	 * @param certificatePath
	 * @return
	 */
	public static boolean verifyCertificate(InputStream is, String keyStorePassword) {
		return verifyCertificate(new Date(), is, keyStorePassword);
	}

	/**
	 * 验证Certificate是否过期或无效
	 * 
	 * @param date
	 * @param certificatePath
	 * @return
	 */
	public static boolean verifyCertificate(Date date,  InputStream is, String keyStorePassword) {
		boolean status = true;
		try {
			// 取得证书
			Certificate certificate = getCertificate(is, keyStorePassword);
			// 验证证书是否过期或无效
			status = verifyCertificate(date, certificate);
		} catch (Exception e) {
			status = false;
		}
		return status;
	}

	/**
	 * 验证证书是否过期或无效
	 * 
	 * @param date
	 * @param certificate
	 * @return
	 */
	private static boolean verifyCertificate(Date date, Certificate certificate) {
		boolean status = true;
		try {
			X509Certificate x509Certificate = (X509Certificate) certificate;
			x509Certificate.checkValidity(date);
		} catch (Exception e) {
			status = false;
		}
		return status;
	}

	/**
	 * 签名
	 * 
	 * @param keyStorePath
	 * @param alias
	 * @param keyStorePassword
	 * @param aliasPassword
	 * @return
	 * @throws Exception
	 */
	public static byte[] sign(byte[] sign, InputStream is,
			String keyStorePassword, String aliasPassword) throws Exception {
		KeyStore ks = getKeyStore(is, keyStorePassword);
		Enumeration enums = ks.aliases(); 
		String alias = "";
        while (enums.hasMoreElements()) {
        	 alias = (String) enums.nextElement();
        }  
		
		// 获得证书
        X509Certificate x509Certificate = (X509Certificate) ks.getCertificate(alias);

		// 取得私钥
        PrivateKey key = (PrivateKey) ks.getKey(alias, aliasPassword.toCharArray());

		// 构建签名
		Signature signature = Signature.getInstance(x509Certificate
				.getSigAlgName());
		signature.initSign(key);
		signature.update(sign);
		return signature.sign();
	}

	/**
	 * 验证签名
	 * 
	 * @param data
	 * @param sign
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	public static boolean verify(byte[] data, byte[] sign,
			InputStream is, String keyStorePassword) throws Exception {
		// 获得证书
		X509Certificate x509Certificate = (X509Certificate) getCertificate(is, keyStorePassword);
		// 获得公钥
		PublicKey publicKey = x509Certificate.getPublicKey();
		// 构建签名
		Signature signature = Signature.getInstance(x509Certificate
				.getSigAlgName());
		signature.initVerify(publicKey);
		signature.update(data);

		return signature.verify(sign);

	}

	/**
	 * 验证Certificate
	 * 
	 * @param keyStorePath
	 * @param keyStorePassword
	 * @param alias
	 * @return
	 */
	public static boolean verifyCertificate(Date date, InputStream is,
			String keyStorePassword, String alias) {
		boolean status = true;
		try {
			Certificate certificate = getCertificate(is,
					keyStorePassword, alias);
			status = verifyCertificate(date, certificate);
		} catch (Exception e) {
			status = false;
		}
		return status;
	}

	/**
	 * 验证Certificate
	 * 
	 * @param keyStorePath
	 * @param keyStorePassword
	 * @param alias
	 * @return
	 */
	public static boolean verifyCertificate(InputStream is,
			String keyStorePassword, String alias) {
		return verifyCertificate(new Date(), is, keyStorePassword,
				alias);
	}
	
	
	
}
