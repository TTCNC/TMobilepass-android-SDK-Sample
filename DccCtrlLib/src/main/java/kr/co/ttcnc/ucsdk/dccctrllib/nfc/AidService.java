package kr.co.ttcnc.ucsdk.dccctrllib.nfc;

import android.content.ComponentName;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;

import java.util.List;

class AidService {
    public AidService() {
    }

    public static boolean registAids(Context context, List<String> aids) {
        ComponentName componentName = new ComponentName(context, DccHostApduService.class.getName());
        return CardEmulation.getInstance(NfcAdapter.getDefaultAdapter(context)).registerAidsForService(componentName, "other", aids);
    }

    public static boolean removeAids(Context context) {
        ComponentName componentName = new ComponentName(context, DccHostApduService.class.getName());
        return CardEmulation.getInstance(NfcAdapter.getDefaultAdapter(context)).removeAidsForService(componentName, "other");
    }
}
