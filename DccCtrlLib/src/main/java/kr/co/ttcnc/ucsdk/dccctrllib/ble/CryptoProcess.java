package kr.co.ttcnc.ucsdk.dccctrllib.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import static kr.co.ttcnc.ucsdk.dccctrllib.ble.TripleDES.ConcatArrays;

import java.util.Random;

import kr.co.ttcnc.ucsdk.dccctrllib.JNIClass;
import kr.co.ttcnc.ucsdk.dccctrllib.util.TLog;
import kr.co.ttcnc.ucsdk.dccctrllib.util.Utils;

public class CryptoProcess {
    private final static String TAG = CryptoProcess.class.getSimpleName();

    private static CryptoProcess instance;
    private static JNIClass jni;
    private static final int TDES_TRN_LENGTH = 8;
    private static final int TDES_HRN_LENGTH = 8;

    private static final int TOKEN_LENGTH = 16;
    private static final int E_TOKEN_LENGTH = 24;

    private static String sCryptedToken = null;
    private static byte[] bCryptedToken = null;

    private static int length = 0;
    private static int index = 0;
    private static int remain = 0;
    private static int writesize = 0;

    private static final byte[] SW1SW2_SUCCESS = {(byte) 0x90, (byte) 0x00};

    //
    public CryptoProcess() {
        try {
            jni = new JNIClass();
        } catch (Exception e) {
            TLog.i(TAG, "CryptoProcess (Context context) e=" + e.toString());
        }
    }

    public static synchronized CryptoProcess getInstance() {
        if (instance == null) {
            instance = new CryptoProcess();
        }

        return instance;
    }

    /*
     * Ble Messages  Write
     */
    public static boolean bleWriteMessages(BluetoothGatt mGatt) {
        if (remain == 0) {
            length = 0;
            index = 0;
            remain = 0;
            writesize = 0;
            return false;
        }

        if (length < 21) {
            writesize = length;
            remain = 0;
        } else
            writesize = 20;

        BluetoothGattCharacteristic characteristic = BluetoothUtils.findWriteCharacteristic(mGatt);
        if (characteristic == null) {
            TLog.i(TAG, "Unable to find echo characteristic.");
            return false;
        }

        TLog.i(TAG, "writesize : " + writesize);
        TLog.i(TAG, "index : " + index);
        TLog.i(TAG, "remain : " + remain);

        byte[] bBuffer = new byte[writesize];
        System.arraycopy(bCryptedToken, index, bBuffer, 0, writesize);

        characteristic.setValue(bBuffer);
        @SuppressLint("MissingPermission") boolean success = mGatt.writeCharacteristic(characteristic);
        if (success) {
            TLog.i(TAG, "Wrote: " + Utils.hexToString(bBuffer));
        } else {
            TLog.i(TAG, "Failed to write data");
        }
        length -= writesize;
        index += writesize;

        return true;
    }

    /**
     * putToken 처리 함수
     * - MobilePass 인증 처리  리더기에 인증 정보를 전송한다.
     *
     * @param commandApdu
     * @return
     */
    public String putCryptoToken(byte[] commandApdu, byte[] bTRN) {
        String sResponseString = null;
        byte[] responseApdu = null;
        /** Temporary Randon Number From Reader */
        byte[] TOKEN = commandApdu;

        byte[] TRN = ConcatArrays(bTRN, "00".getBytes());       // HOME RANDON NUMBER
        byte[] HRN = new byte[TDES_HRN_LENGTH];       // HOME RANDON NUMBER

        String cardNumber = null;

        if (commandApdu == null) {
            return null;
        }

        length = 0;
        index = 0;
        remain = 0;
        writesize = 0;

        TLog.i(TAG, "TOKEN=" + hexStringFromByteArray(TOKEN));

        new Random().nextBytes(HRN);    // Fill RANDOM BYTEs
        TLog.i(TAG, "TRN=" + hexStringFromByteArray(TRN));
        TLog.i(TAG, "HRN=" + hexStringFromByteArray(HRN));
        /* 출입통제용 토클 암호화 처리
         * TK = AES192_CBC_ENC(TRN[16] || HRN[16], MK)
         * E_TOKEN = AES_GCM256_ENC(TOKEN[32] , TK)
         */
        Boolean bresult = false;

        try {

            byte[] f53MK_from = jni.getf53MKValue();

            TLog.i(TAG, "===TripleDES.f53MK_from=" + hexStringFromByteArray(f53MK_from));
            byte[] TK = TripleDES.encrypt(ConcatArrays(TRN, HRN), f53MK_from, TripleDES.WorkType.CBC);
            TLog.i(TAG, "TK=" + hexStringFromByteArray(TK));
            byte[] E_TOKEN = TripleDES.encrypt(ConcatArrays(TOKEN, hexStringToByteArray("8000000000000000")), TK, TripleDES.WorkType.CBC);
            TLog.i(TAG, "E_TOKEN=" + hexStringFromByteArray(E_TOKEN));


            byte[] bResponsePacket = ConcatArrays(HRN, E_TOKEN);
            TLog.i(TAG, "bResponsePacket=" + hexStringFromByteArray(bResponsePacket));

            try {
                responseApdu = new byte[TDES_HRN_LENGTH + E_TOKEN_LENGTH + SW1SW2_SUCCESS.length];
                System.arraycopy(bResponsePacket, 0, responseApdu, 0, TDES_HRN_LENGTH + E_TOKEN_LENGTH);
                System.arraycopy(SW1SW2_SUCCESS, 0, responseApdu, TDES_HRN_LENGTH + E_TOKEN_LENGTH, SW1SW2_SUCCESS.length);

                TLog.i(TAG, "responseApdu=" + hexStringFromByteArray(responseApdu));
                sResponseString = hexStringFromByteArray(responseApdu);
                bresult = true;

            } catch (Exception e2) {
                TLog.i(TAG, "Exception e2=" + e2.toString());
                bresult = false;
            }

        } catch (Exception e3) {
            TLog.i(TAG, "Exception e3=" + e3.toString());
            bresult = false;
        }
        if (!bresult) {
            return null;
        }

        sCryptedToken = sResponseString + "\r";
        bCryptedToken = sCryptedToken.getBytes();
        length = bCryptedToken.length;
        remain = length;
        writesize = length;

        return sResponseString;
    }

    public byte[] getClientAes256Cbc(byte[] EncryptedToken) throws Exception {
        TLog.i(TAG, "getClientAes256Cbc()");
        TLog.i(TAG, "getClientAes256Cbc EncryptedToken=" + hexStringFromByteArray(EncryptedToken));

        byte[] Server_f53MK_from = bytesFromHexString("fa3e1d346c3d896a2e56013d77f1ceb3214512edc3459f3d452d89bced336f2c");;
        byte[] Server_f53CbcIV_from = bytesFromHexString("1f4d45673bac2d6fc455898f1a6d78ed");

        byte[] plainText = AesGcmCryptor.decrypt(EncryptedToken, Server_f53MK_from, Server_f53CbcIV_from, AesGcmCryptor.WorkType.CBC);
        TLog.i(TAG, "getAes256Cbc plainText Token=" + hexStringFromByteArray(plainText));

        return plainText;
    }

    /**
     * putToken 처리 함수
     * - MobilePass 인증 처리  리더기에 인증 정보를 전송한다.
     *
     * @param commandApdu
     * @return
     */
    public String putPlaintextToken(byte[] commandApdu, byte[] bTRN) {
        String sResponseString = null;
        byte[] responseApdu = null;
        /** Temporary Random Number From Reader */
        byte[] TOKEN = commandApdu;

        byte[] TRN = ConcatArrays(bTRN, "00".getBytes());       // HOME RANDON NUMBER
        byte[] HRN = new byte[TDES_HRN_LENGTH];       // HOME RANDON NUMBER

        String cardNumber = null;

        if (commandApdu == null) {
            return null;
        }

        length = 0;
        index = 0;
        remain = 0;
        writesize = 0;

        TLog.i(TAG, "TOKEN=" + hexStringFromByteArray(TOKEN));

        Boolean bresult = false;

        try {
            TLog.i(TAG, "responseApdu=" + hexStringFromByteArray(responseApdu));
            sResponseString = hexStringFromByteArray(responseApdu);
            bresult = true;
        } catch (Exception e3) {
            TLog.i(TAG, "Exception e3=" + e3);
            bresult = false;
        }
        if (!bresult) {
            return null;
        }

        sCryptedToken = sResponseString + "\r";
        bCryptedToken = sCryptedToken.getBytes();
        length = bCryptedToken.length;
        remain = length;
        writesize = length;

        return sResponseString;
    }

    /**
     * hex String 반환 함수
     * - 주어진 바이트 배열을 hex String 으로 변환하여 반환한다.
     *
     * @param bytes
     * @return
     */
    private String hexStringFromByteArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        StringBuffer buffer = new StringBuffer();
        for (byte b : bytes) {
            buffer.append(String.format("%02X", b));
        }

        return buffer.toString();
    }

    /**
     * 16진수 문자열을 바이트 배열로 변환하는 함수
     * - 주어진 16진수 문자열을 바이트 배열로 변환하여 반환한다.
     *
     * @param hex 바이트 배열로 변환하고자 하는 16진수 문자열
     * @return
     */
    private byte[] bytesFromHexString(String hex) {
        byte[] result = new byte[hex.length() / 2];
        char[] hexData = hex.toCharArray();
        for (int count = 0, i = 0; count < hexData.length - 1; count += 2) {
            result[i++] = (byte) ((Character.digit(hexData[count], 16) << 4) + Character.digit(hexData[count + 1], 16));
        }
        return result;
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
