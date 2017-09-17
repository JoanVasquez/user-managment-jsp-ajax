package userprocesses.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

public class AES256 {
	/** The Constant alg. */
	private final static String alg = "AES";

	/** The Constant keyValue. */
	private static final byte[] keyValue = new byte[] { '9', '2', 'A', 'E', '3', '1', 'A', '7', '9', 'F', 'E', 'E', 'B',
			'2', 'A', '3' };

	/** The Constant cI. */
	private final static String cI = "AES/CBC/PKCS5Padding";

	/** The Constant iv. */
	private static final String iv = "0123456789ABCDEF";

	/**
	 * Encryption.
	 *
	 * @param data
	 *            the data
	 * @return the byte[]
	 */
	public static byte[] encryption(String data) {
		try {

			Cipher cipher = Cipher.getInstance(cI);
			Key key = generateKey();
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
			byte[] encrypted = cipher.doFinal(data.getBytes());
			return encodeBase64(encrypted);

		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException | InvalidAlgorithmParameterException ex) {
			Logger.getLogger(AES256.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Decryption.
	 *
	 * @param encryptedData
	 *            the encrypted data
	 * @return the string
	 */
	public static String decryption(String encryptedData) {
		try {

			Cipher cipher = Cipher.getInstance(cI);
			Key key = generateKey();
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());

			byte[] decodedValue = decodeBase64(encryptedData);
			cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
			byte[] decrypted = cipher.doFinal(decodedValue);
			return new String(decrypted);

		} catch (NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException
				| NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
			Logger.getLogger(AES256.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Generate key.
	 *
	 * @return the key
	 */
	private static Key generateKey() {
		Key key = new SecretKeySpec(keyValue, alg);
		return key;
	}

}
