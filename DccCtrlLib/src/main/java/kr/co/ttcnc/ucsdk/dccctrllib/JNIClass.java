package kr.co.ttcnc.ucsdk.dccctrllib;

public class JNIClass {
    static {
        System.loadLibrary("dcc-mobilepass-f53mk");
    }

    public native byte[] getf53MKValue();

    public native byte[] getAes192MKValue();

    public native byte[] getAes256MKValue();

    public native byte[] getf53TokenIVValue();
}