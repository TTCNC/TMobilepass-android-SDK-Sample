package kr.co.ttcnc.ucsdk.dccctrllib.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import kr.co.ttcnc.ucsdk.dccctrllib.JNIClass;
import kr.co.ttcnc.ucsdk.dccctrllib.util.TLog;
import kr.co.ttcnc.ucsdk.dccctrllib.util.Utils;

public class PlainTextProcess {
    private final static String TAG = PlainTextProcess.class.getSimpleName();

    private static PlainTextProcess instance;
    private static JNIClass jni;

    private static String sPlainTextToken = null;
    private static byte[] bPlainTextToken = null;

    private static int length = 0;
    private static int index = 0;
    private static int remain = 0;
    private static int writesize = 0;

    //
    public PlainTextProcess() {
        try {
            jni = new JNIClass();
        } catch (Exception e) {
            TLog.i(TAG, "CryptoProcess (Context context) e=" + e.toString());
        }
    }

    public static synchronized PlainTextProcess getInstance() {
        if (instance == null) {
            instance = new PlainTextProcess();
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
        System.arraycopy(bPlainTextToken, index, bBuffer, 0, writesize);

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
    public String putToken(byte[] commandApdu, byte[] bTRN) {
        String sResponseString = null;
        byte[] responseApdu = null;
        /** Temporary Randon Number From Reader */
        byte[] TOKEN = commandApdu;

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

            try {

                byte[] bResponsePacket = TOKEN;

                TLog.i(TAG, "bResponsePacket=" + hexStringFromByteArray(bResponsePacket));
                sResponseString = hexStringFromByteArray(bResponsePacket);
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

        sPlainTextToken = sResponseString + "\r";
        bPlainTextToken = sPlainTextToken.getBytes();
        length = bPlainTextToken.length;
        remain = length;
        writesize = length;

        return sResponseString;
    }

    /**
     * hex String 반환 함수
     * - 주어진 바이트 배열을 hex String으로 변환하여 반환한다.
     *
     * @param bytes
     * @return
     */
    public static String hexStringFromByteArray(byte[] bytes) {
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
    public static byte[] bytesFromHexString(String hex) {
        byte[] result = new byte[hex.length() / 2];
        char[] hexData = hex.toCharArray();
        for (int count = 0, i = 0; count < hexData.length - 1; count += 2) {
            result[i++] = (byte) ((Character.digit(hexData[count], 16) << 4) + Character.digit(hexData[count + 1], 16));
        }
        return result;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 문자열을 바이트 배열로 반환하는 함수
     * - 주어진 문자열을 바이트 배열로 변환하여 반환한다.
     *
     * @param str 바이트 배열로 변환하고자 하는 문자열
     * @return
     */
    public static byte[] bytesFromString(String str) {
        byte[] result = new byte[str.length()];
        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        return result;
    }
}
