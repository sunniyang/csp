package com.suomee.csp.lib.security;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	private static final String ALGORITHM = "AES";
	private static final String DEFAULT_SECRET = "2018010120181231";
	private static Map<String, AES> instances = new HashMap<String, AES>();
	
	public static AES getDefault() {
		return AES.getInstance(DEFAULT_SECRET);
	}
	
	public static AES getInstance(String secret) {
		if (secret == null || secret.isEmpty()) {
			return null;
		}
		AES aes = instances.get(secret);
		if (aes == null) {
			synchronized (instances) {
				aes = instances.get(secret);
				if (aes == null) {
					aes = new AES(secret);
					instances.put(secret, aes);
				}
			}
		}
		return aes;
	}
	
	private Charset charset;
	//Cipher负责完成加密或解密工作
	private Cipher encryptor;
	private Cipher decryptor;
	
	private AES(String secret) {
		try {
			this.charset = Charset.forName("UTF-8");
			//密钥
			SecretKeySpec key = new SecretKeySpec(secret.getBytes("UTF-8"), ALGORITHM);
			//生成Cipher对象,指定其支持的AES算法；根据密钥，对Cipher对象进行初始化，ENCRYPT_MODE表示加密模式
			this.encryptor = Cipher.getInstance(ALGORITHM);
			this.encryptor.init(Cipher.ENCRYPT_MODE, key);
			this.decryptor = Cipher.getInstance(ALGORITHM);
			this.decryptor.init(Cipher.DECRYPT_MODE, key);
		}
		catch (Exception e) {}
	}
	
	public String encrypt(String content) throws Exception {
		if (content == null || content.length() == 0) {
			return content;
		}
		byte[] buffer = this.encryptor.doFinal(content.getBytes(this.charset));
		return Base64.encode(buffer);
	}
	
	public String decrypt(String content) throws Exception {
		if (content == null || content.length() == 0) {
			return content;
		}
		byte[] buffer = this.decryptor.doFinal(Base64.decode(content));
		return new String(buffer, this.charset);
	}
}
