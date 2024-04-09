package kr.co.ttcnc.ucsdk.dccctrllib.nfc;

import android.content.Context;

/**
 * Created by parkjeongho on 2023-12-19 오후 12:15
 */
class NC900CMReader extends NfcReader {

    public NC900CMReader(Context context) {
        super(context);

        applicationLabel = "TMOBILEPASS_CARD";
        applicationID = new byte[] {
                (byte)0xF4, (byte)0x10, (byte)0x48, (byte)0x43, (byte)0x67, (byte)0x31, (byte)0x30};

        serverF53MK = bytesFromHexString("fc3e1d346c3d896a2e56013d77f1ceb3214512edc3459f3d452d89bced336f2c");
        serverF53CbcIV = bytesFromHexString("3f4d45673bac2d6fc455898f1a6d78ed");
    }
}
