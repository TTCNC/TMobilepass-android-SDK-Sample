package kr.co.ttcnc.ucsdk.dccctrllib.nfc;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.co.ttcnc.ucsdk.dccctrllib.util.TLog;

/**
 * Created by parkjeongho on 2023-12-19 오전 11:35
 */
class NfcReader implements INfcConstants {
    private static final String TAG = NfcReader.class.getSimpleName();

    private Context context;
    public byte[] applicationID;
    public byte[] serverF53MK;
    public byte[] serverF53CbcIV;
    public String applicationLabel;
    private String currentAppletGubun = APPLET_GUBUN_REAL;

    private byte[] MOBILE_PASS_AID = {
            (byte) 0xF4, (byte) 0x10, (byte) 0x48, (byte) 0x43, (byte) 0x37, (byte) 0x31, (byte) 0x30};
    private byte[] MOBILE_PAYMENT_AID = {
            (byte) 0xF4, (byte) 0x10, (byte) 0x48, (byte) 0x43, (byte) 0x37, (byte) 0x31, (byte) 0x20};

    public NfcReader(Context context) {
        this.context = context;
    }

    /**
     * 어플리케이션 레이블을 설정한다.
     * @param applicationLabel
     */
    public void setApplicationLabel(String applicationLabel) {
        this.applicationLabel = applicationLabel;
    }

    /**
     * 어플리케이션 아이디를 설정한다.
     * @param hex
     */
    public void setAID(String hex) {
        this.applicationID = bytesFromHexString(hex);
    }

    /**
     * 어플리케이션 모드를 설정한다.
     * @param mode
     */
    public void setMode(String mode) {
        currentAppletGubun = mode;
    }

    /**
     * 어플리케이션 모드를 반환한다.
     * @return
     */
    public String getMode() {
        return currentAppletGubun;
    }

    /**
     * 출입관리용 File Control Information 생성한다.
     * @return
     */
    public String getFCI() {
        String aid = hexStringFromByteArray(applicationID);

        StringBuilder sb = new StringBuilder();
        sb.append("6F0084");
        sb.append(bcdToHexString(2, aid.length() / 2));
        sb.append(aid);
        sb.append("A5005010");
        sb.append(eaToHexString(applicationLabel)); // 16 byte Application Label
        sb.append("BF0C08");
        sb.append(currentAppletGubun);
        sb.append(addEmptyHexString(7));
        return sb.toString();
    }

    /**
     * 결제용 File Control Information 생성한다.
     * @return
     */
    public String getPaymentFCI() {
        String aid = hexStringFromByteArray(MOBILE_PAYMENT_AID);

        StringBuilder sb = new StringBuilder();
        sb.append("6F0084");
        sb.append(bcdToHexString(2, aid.length() / 2));
        sb.append(aid);
        sb.append("A5005010");
        sb.append(eaToHexString(applicationLabel)); // 16 byte Application Label
        sb.append("BF0C08");
        sb.append(currentAppletGubun);
        sb.append(addEmptyHexString(7));
        return sb.toString();
    }

    /**
     * 어플리케이션 아이디를 추가한다.
     * @return
     */
    public boolean addAidService() {
        List<String> aidList = new ArrayList<>();
        aidList.add(hexStringFromByteArray(applicationID));
        aidList.add(hexStringFromByteArray(MOBILE_PAYMENT_AID));

        AidService aidservice = new AidService();
        boolean result = aidservice.registAids(context, aidList);
        TLog.i(TAG, "addAidService() result=" + result);

        return result;
    }

    /**
     * 매개 변수로 전달된 데이터를 AES256 암호화 해서 반환한다.
     * @param plainText
     * @return
     * @throws Exception
     */
    public byte[] getServerAes256Cbc(byte[] plainText) throws Exception {
        byte[] EncryptedToken = AesGcmCryptor.encrypt(plainText, serverF53MK, serverF53CbcIV, AesGcmCryptor.WorkType.CBC);
        TLog.i(TAG, "getAes256Cbc Encrypted Token=" + hexStringFromByteArray(EncryptedToken));

        return EncryptedToken;
    }

    /**
     * 매개 변수로 전달된 데이터를 AES256 암호화 해서 반환한다.
     * @param plainText
     * @param option
     * @return
     * @throws Exception
     */
    public byte[] getServerAes256Cbc(byte[] plainText, byte option) throws Exception {
        byte[] EncryptedToken = AesGcmCryptor.encrypt(plainText, serverF53MK, serverF53CbcIV, AesGcmCryptor.WorkType.CBC, option);
        TLog.i(TAG, "getAes256Cbc Encrypted Token=" + hexStringFromByteArray(EncryptedToken));

        return EncryptedToken;
    }

    /**
     * 출입관리용 어플리케이션 아이디가 일치하는지 검사한다.
     * @param data
     * @return
     */
    public boolean isEqualPassAid(byte[] data) {
        if(Arrays.equals(data, applicationID) || Arrays.equals(data, MOBILE_PASS_AID)) {
            return true;
        }
        return false;
    }

    /**
     * 결제용 어플리케이션 아이디가 일치하는지 검사한다.
     * @param data
     * @return
     */
    public boolean isEqualPaymentAid(byte[] data) {
        if(Arrays.equals(data, MOBILE_PAYMENT_AID)) {
            return true;
        }
        return false;
    }

    /**
     * 16진수 문자열을 바이트 배열로 변환하는 함수
     * - 주어진 16진수 문자열을 바이트 배열로 변환하여 반환한다.
     *
     * @param hex 바이트 배열로 변환하고자 하는 16진수 문자열
     * @return
     */
    public byte[] bytesFromHexString(String hex) {
        byte[] result = new byte[hex.length() / 2];
        char[] hexData = hex.toCharArray();
        for (int count = 0, i = 0; count < hexData.length - 1; count += 2) {
            result[i++] = (byte) ((Character.digit(hexData[count], 16) << 4) + Character.digit(hexData[count + 1], 16));
        }
        return result;
    }

    /**
     *
     * @param padLength
     * @param bcd
     * @return
     */
    private String bcdToHexString(int padLength, int bcd) {
        StringBuilder sb = new StringBuilder();
        sb.append("%0");
        sb.append(padLength);
        sb.append("x");
        return String.format(sb.toString(), new Object[]{Integer.valueOf(bcd)});
    }

    /**
     *
     * @param text
     * @return
     */
    private String eaToHexString(String text) {
        return hexStringFromByteArray(text.getBytes());
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
     *
     * @param totalLength
     * @return
     */
    private String addEmptyHexString(int totalLength) {
        StringBuilder sb = new StringBuilder();
        sb.append("%0");
        sb.append(totalLength * 2);
        sb.append("x");
        return String.format(sb.toString(), new Object[]{Integer.valueOf(0)});
    }
}
