package kr.co.ttcnc.ucsdk.dccctrllib.nfc;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import kr.co.ttcnc.ucsdk.dccctrllib.util.TLog;
import kr.co.ttcnc.ucsdk.dccctrllib.util.Utils;

class AesGcmCryptor {
    private static final String TAG = AesGcmCryptor.class.getSimpleName();

    public enum WorkType {
        GCM,
        CBC,
        CCM,
        CFB,
        CMAC,
        CTR,
        OFB,
        KEYWARP
    }

    private static final String GCM_CRYPTO_NAME = "AES/GCM/NoPadding";
    private static final String CBC_CRYPTO_NAME = "AES/CBC/NoPadding";

    private static final byte VERSION_BYTE = 0x01;
    private static final int VERSION_BYTE_LENGTH = 1;
    private static final int CBC_IV_BYTES_LENGTH = 16;
    private static final int GCM_IV_BYTES_LENGTH = 12;
    private static final int GCM_TAG_BYTES_LENGTH = 16;

    // AES secret key
    public static SecretKey getAESKey(byte[] keyBytes) throws NoSuchAlgorithmException {
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        return key;
    }

    // AES generate IV (initialisation vector)
    public static byte[] getAESIV() throws NoSuchAlgorithmException {

        final byte[] iv = new byte[GCM_IV_BYTES_LENGTH];
        new Random().nextBytes(iv);    // Fill RANDON BYTEs

        return iv;
    }

    /**
     * Encrypts a plaintext with a password.
     * <p>
     * The encryption provides the following security properties:
     * Confidentiality + Integrity
     * <p>
     * This is achieved my using the AES-GCM AEAD blockmode with a randomized IV.
     * <p>
     * The tag is calculated over the version byte, the IV as well as the ciphertext.
     * <p>
     * Finally the encrypted bytes have the following structure:
     * <pre>
     *          +------------------------------------------------------------+
     *          |                         |           |            |         |
     *          | ciphertext bytes        |    tag    |  IV bytes  | version |
     *          |                         |           |            |         |
     *          +------------------------------------------------------------+
     * Length:     len(32) bytes             16B        12B          1B
     * </pre>
     * Note: There is no padding required for AES-GCM, but this also implies that
     * the exact plaintext length is revealed.
     *
     * @param Crypto_Key  password to use for encryption
     * @param plainTextBytes plaintext to encrypt
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeySpecException
     */
    public static byte[] encrypt(byte[] plainTextBytes, byte[] Crypto_Key, byte[] aCrypto_bIV, WorkType type) throws Exception {
        TLog.d("AliceAesGcm encrypt" , "encrypt Crypto_Key =" + Utils.hexToString(Crypto_Key));
        TLog.d("AliceAesGcm encrypt" ,"plainTextBytes =" + Utils.bytesToHex(plainTextBytes));

        Cipher cipher;
        if (type == WorkType.GCM) {

            // initialise random
            SecretKey key = getAESKey(Crypto_Key);

            // GCM Mode IV Length 12
            final byte[] iv = new byte[GCM_IV_BYTES_LENGTH];
            System.arraycopy(aCrypto_bIV, 0, iv , 0, GCM_IV_BYTES_LENGTH);

            // encrypt
            cipher = Cipher.getInstance(GCM_CRYPTO_NAME);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BYTES_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);


            // add IV to MAC
            final byte[] versionBytes = new byte[]{VERSION_BYTE};

            final byte[] bAAD = aCrypto_bIV;
            cipher.updateAAD(bAAD);

            // encrypt and MAC plaintext
            byte[] ciphertext = cipher.doFinal(plainTextBytes);


            // frepend IV and VERSION to ciphertext
            byte[] encrypted = new byte[ciphertext.length + GCM_IV_BYTES_LENGTH + VERSION_BYTE_LENGTH];
            int pos = 0;
            System.arraycopy(ciphertext, 0, encrypted, pos, ciphertext.length);
            pos += ciphertext.length;
            System.arraycopy(aCrypto_bIV, 0, encrypted, pos, GCM_IV_BYTES_LENGTH);
            pos += GCM_IV_BYTES_LENGTH;
            System.arraycopy(versionBytes, 0, encrypted, pos, VERSION_BYTE_LENGTH);

            return encrypted;
        } else if (type == WorkType.CBC) {
            // CBC Mode IV Length 16
            cipher = Cipher.getInstance(CBC_CRYPTO_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, getAESKey(Crypto_Key), new IvParameterSpec(aCrypto_bIV));

            byte[] cipherFullText = cipher.doFinal(plainTextBytes);
            TLog.d("AliceAesGcm encrypt", "cipherFullText =" + Utils.hexToString(cipherFullText));

            return cipherFullText;
        }

        return null;
    }


    /**
     * Encrypts a plaintext with a password.
     * <p>
     * The encryption provides the following security properties:
     * Confidentiality + Integrity
     * <p>
     * This is achieved my using the AES-GCM AEAD blockmode with a randomized IV.
     * <p>
     * The tag is calculated over the version byte, the IV as well as the ciphertext.
     * <p>
     * Finally the encrypted bytes have the following structure:
     * <pre>
     *          +------------------------------------------------------------+
     *          |                         |           |            |         |
     *          | ciphertext bytes        |    tag    |  IV bytes  | version |
     *          |                         |           |            |         |
     *          +--------------------en(32) bytes     ----------------------------------------+
     *      * Length:     l        16B        12B          1B
     * </pre>
     * Note: There is no padding required for AES-GCM, but this also implies that
     * the exact plaintext length is revealed.
     *
     * @param Crypto_Key     password to use for encryption
     * @param plainTextBytes plaintext to encrypt
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeySpecException
     */
    public static byte[] encrypt(byte[] plainTextBytes, byte[] Crypto_Key, byte[] aCrypto_bIV, WorkType type, byte gOption) throws Exception {
        TLog.d("AliceAesGcm encrypt" , "encrypt Crypto_Key =" + Utils.hexToString(Crypto_Key));
        TLog.d("AliceAesGcm encrypt" ,"plainTextBytes =" + bytesToHex(plainTextBytes));

        Cipher cipher = null;
        if (type == WorkType.GCM) {

            // initialise random
            SecretKey key = getAESKey(Crypto_Key);

            // GCM Mode IV Length 12
            final byte[] iv = new byte[GCM_IV_BYTES_LENGTH];
            System.arraycopy(aCrypto_bIV, 0, iv, 0, GCM_IV_BYTES_LENGTH);

            // encrypt
            cipher = Cipher.getInstance(GCM_CRYPTO_NAME);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BYTES_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);


            // add IV to MAC
            final byte[] versionBytes = new byte[]{gOption};
            final byte[] bAAD = aCrypto_bIV;
            cipher.updateAAD(bAAD);

            // encrypt and MAC plaintext
            byte[] ciphertext = cipher.doFinal(plainTextBytes);


            // frepend IV and VERSION to ciphertext
            byte[] encrypted = new byte[ciphertext.length + GCM_IV_BYTES_LENGTH + VERSION_BYTE_LENGTH];
            int pos = 0;
            System.arraycopy(ciphertext, 0, encrypted, pos, ciphertext.length);
            pos += ciphertext.length;
            System.arraycopy(aCrypto_bIV, 0, encrypted, pos, GCM_IV_BYTES_LENGTH);
            pos += GCM_IV_BYTES_LENGTH;
            System.arraycopy(versionBytes, 0, encrypted, pos, VERSION_BYTE_LENGTH);

            return encrypted;
        } else if (type == WorkType.CBC) {


            // CBC Mode IV Length 16
            cipher = Cipher.getInstance(CBC_CRYPTO_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, getAESKey(Crypto_Key), new IvParameterSpec(aCrypto_bIV));

            byte[] cipherFullText = cipher.doFinal(plainTextBytes);
            //TLog.d("AliceAesGcm encrypt", "cipherFullText =" + Utils.hexToString(cipherFullText));

            return cipherFullText;
        }

        return null;
    }

    public static byte[] decrypt(byte[] ciphertext, byte[] Crypto_Key, byte[] aCrypto_bIV, WorkType type) throws Exception {
        TLog.d("AliceAesGcmdecrypt Crypto_Key", Utils.hexToString(Crypto_Key));

        TLog.d("AliceAesGcm decrypt" , "Crypto_Key =" + Utils.hexToString(Crypto_Key));
        TLog.d("AliceAesGcm decrypt" ,"ciphertext =" + bytesToHex(ciphertext));

        // input validation
        if (ciphertext == null) {
            throw new IllegalArgumentException("ciphertext cannot be null");
        }

        Cipher cipher = null;
        if (type == WorkType.GCM) {

            if (ciphertext.length <= VERSION_BYTE_LENGTH + GCM_IV_BYTES_LENGTH + GCM_TAG_BYTES_LENGTH) {
                throw new IllegalArgumentException("ciphertext too short");
            }

            // initialise random
            SecretKey key = getAESKey(Crypto_Key);

            final byte[] iv = aCrypto_bIV;


            // init cipher
            cipher = Cipher.getInstance(GCM_CRYPTO_NAME);
            GCMParameterSpec params = new GCMParameterSpec(GCM_TAG_BYTES_LENGTH * 8,
                    ciphertext,
                    GCM_IV_BYTES_LENGTH,
                    VERSION_BYTE_LENGTH
            );
            cipher.init(Cipher.DECRYPT_MODE, key, params);

            final int ciphertextOffset = GCM_IV_BYTES_LENGTH;

            // add IV to MAC
            cipher.updateAAD(ciphertext, 0, ciphertextOffset);

            // decipher and check MAC
            return cipher.doFinal(ciphertext, ciphertextOffset, ciphertext.length - ciphertextOffset);
        } else if (type == WorkType.CBC) {
            cipher = Cipher.getInstance(CBC_CRYPTO_NAME);
            cipher.init(Cipher.DECRYPT_MODE, getAESKey(Crypto_Key), new IvParameterSpec(aCrypto_bIV));

            byte[] Decryptedplaintext = cipher.doFinal(ciphertext);
            TLog.d("AliceAesGcm decrypt", "Decryptedplaintext =" + Utils.hexToString(Decryptedplaintext));

            return Decryptedplaintext;
        }
        return null;
    }

    public static byte[] ConcatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        int offset2 = offset;
        for (byte[] array2 : rest) {
            System.arraycopy(array2, 0, result, offset2, array2.length);
            offset2 += array2.length;
        }
        return result;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String byteArrayToBinaryString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte byteToBinaryString : b) {
            sb.append(byteToBinaryString(byteToBinaryString));
        }
        return sb.toString();
    }

    public static String byteToBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String hexStr) {
        byte[] result = null;
        if (hexStr != null) {
            result = new byte[(hexStr.length() / 2)];
            for (int i = 0; i < result.length; i++) {
                result[i] = (byte) Integer.parseInt(hexStr.substring(i * 2, (i * 2) + 2), 16);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("hexStringToByteArray ");
        sb.append(hexStr);
        TLog.d("dukduk", sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("hexStringToByteArray ");
        sb2.append(result.length);
        TLog.d("dukduk", sb2.toString());
        return result;
    }
}
