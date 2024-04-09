package kr.co.ttcnc.ucsdk.dccctrllib.nfc;

import android.content.Context;

/**
 * Created by parkjeongho on 2023-12-19 오후 12:15
 */
class NC400LSTReader extends NfcReader {

    public NC400LSTReader(Context context) {
        super(context);

        applicationLabel = "TMOBILEPASS_CARD";
        applicationID = new byte[] {
                (byte)0xF4, (byte)0x10, (byte)0x48, (byte)0x43, (byte)0x47, (byte)0x31, (byte)0x30};

        serverF53MK = bytesFromHexString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbcccc");
        serverF53CbcIV = bytesFromHexString("ccccccccccccccccdddddddddddddddf");
    }
}
