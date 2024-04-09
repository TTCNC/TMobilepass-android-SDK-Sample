package kr.co.ttcnc.ucsdk.dccctrllib.ble;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import kr.co.ttcnc.ucsdk.dccctrllib.util.TLog;
import kr.co.ttcnc.ucsdk.dccctrllib.util.Utils;

class TripleDES {
    private static final String TAG = TripleDES.class.getSimpleName();

    public enum WorkType {
        CBC,
        ECB
    }

    /**
     *
     * @param plainTextBytes
     * @param keyBytes
     * @param type
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] plainTextBytes, byte[] keyBytes, WorkType type) throws Exception {
        byte[] tripleKeyBytes = Arrays.copyOf(keyBytes, 24);

        byte[] bIV = {(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07};
        // T-DES Key Size 16 and 24 Support
        // Key Size 가 16 이면 시작부터 8 바이트의 키값을 16 byte 부터 24 byte 위치에 복사하여
        // 24 byte 키를 생성하여 사용한다.
        if (keyBytes.length < 24) {
            int j = 0;
            int k = 16;
            while (j < 8) {
                int k2 = k + 1;
                int j2 = j + 1;
                tripleKeyBytes[k] = tripleKeyBytes[j];
                k = k2;
                j = j2;
            }
        }
        TLog.d("TripleKeyBytes", Utils.hexToString(tripleKeyBytes));

        SecretKey key = new SecretKeySpec(tripleKeyBytes, "DESede");
        IvParameterSpec iv = new IvParameterSpec(bIV);
        Cipher cipher = null;
        if (type == WorkType.CBC) {
            cipher = Cipher.getInstance("DESede/CBC/NoPadding");
            cipher.init(1, key, iv);
        } else if (type == WorkType.ECB) {
            cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(1, key);
        }
        return cipher.doFinal(plainTextBytes);
    }

    /**
     *
     * @param message
     * @param keyBytes
     * @param type
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] message, byte[] keyBytes, WorkType type) throws Exception {
        byte[] TripleKeyBytes = Arrays.copyOf(keyBytes, 24);
        byte[] bIV = {(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07};
        // T-DES Key Size 16 and 24 Support
        // Key Size 가 16 이면 시작부터 8 바이트의 키값을 16 byte 부터 24 byte 위치에 복사하여
        // 24 byte 키를 생성하여 사용한다.
        if (keyBytes.length < 24) {
            int j = 0;
            int k = 16;
            while (j < 8) {
                int k2 = k + 1;
                int j2 = j + 1;
                TripleKeyBytes[k] = TripleKeyBytes[j];
                k = k2;
                j = j2;
            }
        }

        TLog.d("TripleKeyBytes", Utils.hexToString(TripleKeyBytes));

        try {
            SecretKey key = new SecretKeySpec(TripleKeyBytes, "DESede");
            IvParameterSpec iv = new IvParameterSpec(bIV);
            Cipher decipher = null;
            if (type == WorkType.CBC) {
                decipher = Cipher.getInstance("DESede/CBC/NoPadding");
                decipher.init(2, key, iv);
            } else if (type == WorkType.ECB) {
                decipher = Cipher.getInstance("DESede/ECB/NoPadding");
                decipher.init(2, key);
            }

            return decipher.doFinal(message);

        } catch (InvalidKeyException e) {
            TLog.d("TripleKeyBytes", "InvalidKeyException");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            TLog.d("TripleKeyBytes", "NoSuchAlgorithmException");
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            TLog.d("TripleKeyBytes", "NoSuchPaddingException");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            TLog.d("TripleKeyBytes", "IllegalStateException");
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @param first
     * @param rest
     * @return
     */
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

    /**
     *
     * @param b
     * @return
     */
    public static String byteArrayToBinaryString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte byteToBinaryString : b) {
            sb.append(byteToBinaryString(byteToBinaryString));
        }
        return sb.toString();
    }

    /**
     *
     * @param n
     * @return
     */
    public static String byteToBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
    }

    /**
     *
     * @param hexStr
     * @return
     */
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
