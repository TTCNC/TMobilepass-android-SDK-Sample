package kr.co.ttcnc.ucsdk.dccctrllib.nfc;

import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Build;
import android.os.Bundle;
import kr.co.ttcnc.ucsdk.dccctrllib.util.TLog;
import kr.co.ttcnc.ucsdk.dccctrllib.util.Utils;

/**
 * HostApduService 상속 클래스
 * - NFC 컨트롤러가 수신한 apdu 명령어를 처리하는 서비스 클래스
 */
public class DccHostApduService extends HostApduService {
    private static final String TAG = DccHostApduService.class.getSimpleName();

    /**
     * apdu 명령어 처리기
     */
    DccApduManager apduManager = null;

    public DccHostApduService() {
        TLog.i(TAG, "DccHostApduService created");
        TLog.i(TAG, "DccHostApduService SDK Version is" + Build.VERSION.SDK_INT);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TLog.i(TAG, "DccHostApduService onCreate()");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            TLog.i(TAG, "DccHostApduService using SDK Version " + Build.VERSION.SDK_INT);
            apduManager = DccApduManager.getInstance();

            if (apduManager != null) {
                apduManager.onStartCommand();
            } else {
                LiberoPrefManager pref = LiberoPrefManager.getInstance(getApplicationContext());
                apduManager = DccApduManager.getInstance(getApplicationContext(), pref.getReaderModelPreference());
            }
        } else {
            TLog.e(TAG, "DccHostApduService need SDK Version " + Build.VERSION.SDK_INT);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TLog.i(TAG, "onStartCommand");

        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        TLog.i(TAG, "processCommandApdu() | incoming commandApdu: " + Utils.hexStringFromByteArray(commandApdu));

        // apdu 명령어 처리
        apduManager = DccApduManager.getInstance();
        if(apduManager != null) {
            byte[] responseAPDU = apduManager.processApdu(commandApdu);
            return responseAPDU;
        }

        return null;
    }

    @Override
    public void onDeactivated(int reason) {
        apduManager = DccApduManager.getInstance();
        if(apduManager != null) {
            apduManager.onDeactivated();
            TLog.i(TAG, "onDeactivated() Fired! Reason: " + reason);
        }
    }
}