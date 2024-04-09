package kr.co.ttcnc.ucsdk.dccctrllib.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import androidx.annotation.Nullable;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class Utils {
    public static final String TAG = "Utils";

    private static String byteToHex(byte b) {
        char char1 = Character.forDigit((b & 0xF0) >> 4, 16);
        char char2 = Character.forDigit((b & 0x0F), 16);

        return String.format("0x%1$s%2$s", char1, char2);
    }

    public static String byteArrayInHexFormat(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{ ");
        for (int i = 0; i < byteArray.length; i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            String hexString = byteToHex(byteArray[i]);
            stringBuilder.append(hexString);
        }
        stringBuilder.append(" }");

        return stringBuilder.toString();
    }

    public static byte[] bytesFromStringUTF8(String string) {
        byte[] stringBytes = new byte[0];
        try {
            stringBytes = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            TLog.e(TAG, "Failed to convert message string to byte array");
        }

        return stringBytes;
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

    @Nullable
    public static String stringFromBytes(byte[] bytes) {
        String byteString = null;
        try {
            byteString = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            TLog.e(TAG, "Unable to convert message bytes to string");
        }
        return byteString;
    }

    public static String stringToHexString(String s) {
        String result = "";

        for (int i = 0; i < s.length(); i++) {
            result += String.format("%02X", (int) s.charAt(i));
        }

        return result;
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

    public static String hexStringToString(String hex) {
        StringBuilder sb = new StringBuilder();
        char[] hexData = hex.toCharArray();
        for (int count = 0; count < hexData.length - 1; count += 2) {
            int firstDigit = Character.digit(hexData[count], 16);
            int lastDigit = Character.digit(hexData[count + 1], 16);
            int decimal = firstDigit * 16 + lastDigit;
            sb.append((char) decimal);
        }
        return sb.toString();
    }

    public static byte[] stringToHex(String hex) {
        byte[] result = new byte[hex.length() / 2];
        char[] hexData = hex.toCharArray();
        for (int count = 0, i = 0; count < hexData.length - 1; count += 2) {
            result[i++] = (byte) ((Character.digit(hexData[count], 16) << 4) + Character.digit(hexData[count + 1], 16));
        }
        return result;
    }

    public static String hexToString(byte[] hexData) {
        String result = "";
        for (int count = 0; count < hexData.length; count++) {
            result += String.format("%02X", hexData[count]);
        }
        return result;
    }

    public static byte[] stringToByte(String str) {
        byte[] result = new byte[str.length()];
        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        return result;
    }

    /**
     * 랜덤 바이트 배열 생성 함수
     * - 주어진 길이의 랜덤 바이트를 생성하여 반환한다.
     *
     * @param length
     * @return
     */
    public static byte[] randomBytes(int length) {
        byte[] randomBytes = new byte[length];

        Random random = new Random();
        int k = 0;
        for (int i = 0; i < length; i++) {
            k = random.nextInt();
            randomBytes[i] = (byte) k;
        }
        TLog.i(TAG, "randomBytes " + hexStringFromByteArray(randomBytes));

        return randomBytes;
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
     * 복호 처리 함수
     * - 주어진 데이터를 주어진 키를 이용해 복호화하여 반환한다.
     *
     * @param key 키
     * @param data 데이터
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] key, byte[] data) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decryptedData = cipher.doFinal(data);

       return decryptedData;
    }

    //***********************************************************************/
    //* the function Convert a "4-char String" to a two bytes format
    //* Example : "0F43" -> { 0X0F ; 0X43 }
    //***********************************************************************/
    public static byte[] ConvertStringToHexBytes(String StringToConvert) {
        StringToConvert = StringToConvert.toUpperCase();
        StringToConvert = StringToConvert.replaceAll(" ", "");
        char[] CharArray = StringToConvert.toCharArray();
        char[] Char = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int result = 0;
        byte[] ConvertedString = new byte[]{(byte) 0x00, (byte) 0x00};
        for (int i = 0; i <= 1; i++) {

            for (int j = 0; j <= 15; j++) {
                if (CharArray[i] == Char[j]) {
                    if (i == 1) {
                        result = result + j;
                        j = 15;
                    } else if (i == 0) {
                        result = result + j * 16;
                        j = 15;
                    }

                }
            }
        }
        ConvertedString[0] = (byte) result;

        result = 0;
        for (int i = 2; i <= 3; i++) {
            for (int j = 0; j <= 15; j++) {
                if (CharArray[i] == Char[j]) {
                    if (i == 3) {
                        result = result + j;
                        j = 15;
                    } else if (i == 2) {
                        result = result + j * 16;
                        j = 15;
                    }

                }
            }
        }
        ConvertedString[1] = (byte) result;

        return ConvertedString;
    }

    /**
     * 바이트 배열 반환 함수
     * - 주어진 int 값을 바이트 배열로 변환하여 반환한다.
     *
     * @param value
     * @return
     */
    public static byte[] bytesFromInt(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    /**
     *
     * @param value
     * @return
     */
    public static byte[] reverse(byte[] value) {
        int length = value.length;
        byte[] reversed = new byte[length];
        for (int i = 0; i < length; i++) {
            reversed[i] = value[length - (i + 1)];
        }
        return reversed;
    }

    /**
     * 암호 처리 함수
     * - 주어진 데이터를 주어진 키를 이용해 암호화하여 반환한다.
     *
     * @param key
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] key, byte[] data) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");    //
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encryptedData = cipher.doFinal(data);

        return encryptedData;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        is.close();

        return sb.toString();
    }

    public static int BigToInt(String big) {
        byte[] data = stringToHex(big);
        int ret = 0;

        for (int i = 0; i < data.length; ++i) {
            ret <<= 8;
            ret |= (data[i] & 0x0FF);
        }

        return ret;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static int LittleToInt(byte[] data, int index, int len) {
        int ret = 0;
        index += len - 1;
        for (int i = 0; i < len; ++i, --index) {
            ret <<= 8;
            ret |= (data[index] & 0x0FF);
        }

        return ret;
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }

        return bitmap;
    }

    /**
     * 현재 시간 정보 반환 함수
     * - 폰의 현재 시간 정보를 읽어와 반환한다.
     *
     * @return sCurrentTime
     */
    public static String getCurrentTime() {
        String sCurrentTime = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = new Date();
        sCurrentTime = dateFormat.format(today.getTime());

        return sCurrentTime;
    }


    /**
     * 명령어 SET을 NDEF MESSAGE FORMAT으로 저장하여 송신한다.
     * bCommand : 명령어 코드
     * HCE_COMMAND_MODE_SET_REAL_TIME : 5 // 핸드폰의 현재 시간 정보를 설정함
     *
     * @return
     */
    public static NdefMessage makeSendCommandMessage(byte bCommand, String CommandBodyMessage) {
        TLog.i(TAG, "makeNDEF");

        NdefMessage ndefMessage = null;
        byte[] Command = {bCommand};
        byte[] bCommandBodyMessage = CommandBodyMessage.getBytes();
        byte[] bFullCommandofBodyMessage = new byte[Command.length + 1 + bCommandBodyMessage.length];

        System.arraycopy(Command, 0, bFullCommandofBodyMessage, 0, Command.length);
        System.arraycopy(bCommandBodyMessage, 0, bFullCommandofBodyMessage, 2, bCommandBodyMessage.length);
        bFullCommandofBodyMessage[1] = (byte) bCommandBodyMessage.length;
        TLog.i(TAG, "bFullCommandofBodyMessage " + hexStringFromByteArray(bFullCommandofBodyMessage));
        try {
            ndefMessage = new NdefMessage(
                    new NdefRecord[]{
                            createTextRecord(bFullCommandofBodyMessage, Locale.US, true)
                    });
            TLog.d(TAG, "NdefMessage message = " + ndefMessage.toString());
        } catch (Exception e) {
            // ups, illegal ndef message payload
            TLog.d(TAG, "NdefMessage Exception = " + e.toString());
        }

        TLog.i(TAG, "makeSendCommandMessage " + hexStringFromByteArray(ndefMessage.toByteArray()));

        return ndefMessage;
    }


    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }
    /**
     * Creates an NDEF record of well known type URI.
     */
    /**
     * NFC Forum "URI Record Type Definition"
     * <p>
     * This is a mapping of "URI Identifier Codes" to URI string prefixes,
     * per section 3.2.2 of the NFC Forum URI Record Type Definition document.
     */
    private static final String[] URI_PREFIX_MAP = new String[]{
            "", // 0x00
            "http://www.", // 0x01
            "https://www.", // 0x02
            "http://", // 0x03
            "https://", // 0x04
            "tel:", // 0x05
            "mailto:", // 0x06
            "ftp://anonymous:anonymous@", // 0x07
            "ftp://ftp.", // 0x08
            "ftps://", // 0x09
            "sftp://", // 0x0A
            "smb://", // 0x0B
            "nfs://", // 0x0C
            "ftp://", // 0x0D
            "dav://", // 0x0E
            "news:", // 0x0F
            "telnet://", // 0x10
            "imap:", // 0x11
            "rtsp://", // 0x12
            "urn:", // 0x13
            "pop:", // 0x14
            "sip:", // 0x15
            "sips:", // 0x16
            "tftp:", // 0x17
            "btspp://", // 0x18
            "btl2cap://", // 0x19
            "btgoep://", // 0x1A
            "tcpobex://", // 0x1B
            "irdaobex://", // 0x1C
            "file://", // 0x1D
            "urn:epc:id:", // 0x1E
            "urn:epc:tag:", // 0x1F
            "urn:epc:pat:", // 0x20
            "urn:epc:raw:", // 0x21
            "urn:epc:", // 0x22
    };
    /**
     * RTD URI type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_URI = {0x55};   // "U"
    public static final byte[] UNKNOWN_RTD_URI = {};   // "U"
    /**
     * Indicates the type field uses the RTD type name format.
     * <p>
     * Use this TNF with RTD types such as RTD_TEXT, RTD_URI.
     */
    public static final short TNF_WELL_KNOWN = 0x01;
    public static final short TNF_UN_KNOWN = 0x05;


    public static NdefRecord createUri(String uriString) {
        byte prefix = 0x0;
        for (int i = 1; i < URI_PREFIX_MAP.length; i++) {
            if (uriString.startsWith(URI_PREFIX_MAP[i])) {
                prefix = (byte) i;
                uriString = uriString.substring(URI_PREFIX_MAP[i].length());
                break;
            }
        }
        byte[] uriBytes = uriString.getBytes(Charset.forName("US-ASCII"));
        byte[] recordBytes = new byte[uriBytes.length + 1];
        recordBytes[0] = prefix;
        System.arraycopy(uriBytes, 0, recordBytes, 1, uriBytes.length);
        return new NdefRecord(TNF_WELL_KNOWN, RTD_URI, new byte[0], recordBytes);
    }

    /**
     * RTD Android app type. For use with TNF_EXTERNAL.
     * <p>
     * The payload of a record with type RTD_ANDROID_APP
     * should be the package name identifying an application.
     * Multiple RTD_ANDROID_APP records may be included
     * in a single {@link NdefMessage}.
     * <p>
     * Use {@link #createApplicationRecord(String)} to create
     * RTD_ANDROID_APP records.
     *
     * @hide
     */
    public static final byte[] RTD_ANDROID_APP = "android.com:pkg".getBytes();
    public static final short TNF_EXTERNAL_TYPE = 0x04;

    public static NdefRecord createApplicationRecord(String packageName) {
        return new NdefRecord(TNF_EXTERNAL_TYPE, RTD_ANDROID_APP, new byte[]{},
                packageName.getBytes(Charset.forName("US-ASCII")));
    }

    public static NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payload.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }

    public static NdefRecord createTextRecord(byte[] textBytes, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }
}
