package kr.co.ttcnc.ucsdk.timopass;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import kr.co.ttcnc.ucsdk.dccctrllib.ble.CryptoProcess;
import kr.co.ttcnc.ucsdk.dccctrllib.nfc.DccApduManager;
import kr.co.ttcnc.ucsdk.dccctrllib.ble.GattClientActionListener;
import kr.co.ttcnc.ucsdk.dccctrllib.ble.GattClientCallback;
import kr.co.ttcnc.ucsdk.dccctrllib.nfc.IDccConnectionHandler;
import kr.co.ttcnc.ucsdk.dccctrllib.ble.BluetoothUtils;
import kr.co.ttcnc.ucsdk.dccctrllib.nfc.ReaderModel;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity implements GattClientActionListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Context mContext = null;
    private Button mbuttonNfcScan = null;
    private Button mbuttonBleScan = null;
    private ToggleButton validateSWitch;
    private Button btnClear;


    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 10;

    /*
     * TMobilePass NFC Crypto Service Implementation START
     * NFC APDU 명령어 처리기
     */
    DccApduManager apduManager;

    // DccApduManager.NFC_ACTIVE_APPLICATION_ALIVE_TIME // 어플리케이션 동작 중 유효;
    // DccApduManager.NFC_ACTIVE_MAX_ALIVE_TIME // 20초 ~ 60 초
    // DccApduManager.NFC_ACTIVE_UNLIMITED_ALIVE_TIME  // // 계속 유효;

    //private int iTimeout = DccApduManager.NFC_ACTIVE_APPLICATION_ALIVE_TIME;   // 타임 아웃을 지정하여 setCryptoTocken 을 하시면 , 키를 사용한 이후에는 다시 setCryptoTocken 를 사용하여 키를 적재해야 합니다.
    private int iTimeout = DccApduManager.NFC_ACTIVE_UNLIMITED_ALIVE_TIME;
    /*
     * TMobilePass NFC Crypto Service Implementation END
     */


    /*
     * TMobilePass BLE Crypto Service Implementation START
     */
    /**
     * BLE BluetoothAdapter
     */
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * BLE Scanner
     */
    BluetoothLeScanner btScanner;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 5000;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    /*
     * TMobilePass BLE Crypto Service Implementation END
     */

    /*
     * NFC * BLE Token Value
     */
    private String sTokenValue = "C123456789543210";
    private byte[] bTrnValue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main_fullscreen);

        mContext = this;

        mbuttonNfcScan = findViewById(R.id.buttonNfcScan);
        mbuttonNfcScan.setOnClickListener(mClickListener);
        mbuttonBleScan = findViewById(R.id.buttonBleScan);
        mbuttonBleScan.setOnClickListener(mClickListener);

        validateSWitch = findViewById(R.id.switchKeyValidate);
        validateSWitch.setChecked(true);
        Log.i("DCCSDK", "isChecked : " + validateSWitch.isChecked());
        btnClear = findViewById(R.id.btnClear);

        /* TMobilePass NFC Service Check */
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Log.i("DCCSDK", "NfcAdapter is not Support So, we use BLE Service");
        }

        /* TMobilePass BLE Crypto Service Implementation */
        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        btScanner = mBluetoothAdapter.getBluetoothLeScanner();

        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();


        /* Filter For TBleMobilePass Reader BLE Device */
        filters = new ArrayList<>();
        ScanFilter scan_filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(BluetoothUtils.SERVICE_UUID))
                .build();
        filters.add(scan_filter);

        /* TMobilePass BLE Crypto Service Implementation END  */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Marshmallow+ Permission APIs
            MarshMallow();
        }

    }

    Button.OnClickListener mClickListener = new View.OnClickListener() {

        @SuppressLint("NonConstantResourceId")
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonNfcScan:
                    mbuttonNfcScan.setEnabled(false);
                    Log.d("MainActivity", "===> buttonNfcScan!");

                    /* TMobilePass NFC Crypto Service Implementation START */

                    /* Start Service DccHostApudService and NFC Handler Instance */
                    /* ReaderModel = NFC Device Board Model */
                    apduManager = DccApduManager.getInstance(MainActivity.this, ReaderModel.TMR300);
                    /* AID = Set Your Service AID */
                    apduManager.setAID("F4100000000000");
                    if (apduManager == null) {
                        Log.i("DCCSDK", "apduManager is not created check error log");
                        return;
                    }
                    /* Set Application Label */
                    //apduManager.setApplicationLabel("TIMOPASS_APPCARD");
                    apduManager.setApplicationLabel("TMOBILEPASS_CARD");

                    /* NFC Event Handler */
                    apduManager.setDccApduManagerHandler(mConnectionHandler);

                    /* HexString Type Tocken */
                    Log.i("DCCSDK", "sTokenValue : " + sTokenValue);

                    /* ByteArray to HexaString Type Tocken */
                    //sTokenValue = hexStringFromByteArray(sTokenValue.getBytes());
                    Log.i("DCCSDK", "sTokenValue : " + hexStringFromByteArray(sTokenValue.getBytes()));

                    /* SetCryptoToken Example */
                    try {
                        apduManager.setCryptoToken(sTokenValue, (byte) 0x80, iTimeout, false);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    /* SetToken Example */
                    //try {
                    //    apduManager.setToken(sTokenValue,iTimeout);
                    //} catch (Exception e) {
                    //    throw new RuntimeException(e);
                    //}

                    /* Timopass Service Example */
                    //try {
                    //    apduManager.setCryptoToken(sTokenValue, (byte) 0x80, iTimeout, true);
                    //} catch (Exception e) {
                    //    throw new RuntimeException(e);
                    //}

                    /* Save and Start CardService */
                    validateSWitch.setOnClickListener(view -> {
                        String status = "ToggleButton : " + validateSWitch.getText() + "\n";
                        if (validateSWitch.getText().equals("Pause"))  // Current is Pause
                        {
                            Log.i("DCCSDK", "activeCryptoTocken");
                            apduManager.activeCryptoToken();
                        } else if (validateSWitch.getText().equals("Active"))  // Current is Active
                        {
                            Log.i("DCCSDK", "pauseCryptoTocken");
                            apduManager.pauseCryptoToken();
                        }
                    });
                    btnClear.setOnClickListener(view -> {
                        // 핸드폰에서 토큰 정보를 강제 Clear 한다.
                        Log.i("DCCSDK", "setClearCryptoToken");
                        apduManager.setClearCryptoToken();
                    });
                    /* TMobilePass NFC Crypto Service Implementation END */
                    break;

                case R.id.buttonBleScan:
                    mbuttonBleScan.setEnabled(false);
                    /* TMobilePass BLE Crypto Service Implementation START */
                    Log.d("MainActivity", "===> buttonBleScan!");
                    bleCryptoStart();
                    /* TMobilePass BLE Crypto Service Implementation END */
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        Log.i("DCCSDK", "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i("DCCSDK", "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /* TMobilePass NFC Crypto Service Implementation START */
    private final IDccConnectionHandler mConnectionHandler = new IDccConnectionHandler() {


        @Override
        public void onStartCommand() {
            Log.i("DCCSDK", "DCCSDK onStartCommand");
        }

        @Override
        public void onSetTokenCompleted(String token, String jsonString) {
            Log.i("DCCSDK", "DCCSDK onSetTokenCompleted token: " + token);
        }


        @Override
        public void onGetTokenCompleted(boolean result) {
            Log.i("DCCSDK", "DCCSDK onGetTokenCompleted result=" + result);
            if (result)
                Log.i("DCCSDK", "DCCSDK onGetTokenCompleted SUCCESS");
            else
                Log.i("DCCSDK", "DCCSDK onGetTokenCompleted FAILURE");

            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            // 1초 진동
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
            mbuttonNfcScan.setEnabled(true);

        }

        @Override
        public void onGetData(byte[] bytes) {
            Log.i("DCCSDK", "DCCSDK onGetData");
        }

        @Override
        public void onSelectFile(byte[] bytes) {
            Log.i("DCCSDK", "DCCSDK onSelectFile");
        }

        @Override
        public void onTokenTimeout() {
            Log.i("DCCSDK", "DCCSDK onTokenTimeout");
            try {
                mbuttonNfcScan.setEnabled(true);
            } catch (Exception e) {
                Log.i("DCCSDK", "Exception");
            }

        }

        @Override
        public void onError(byte[] bytes, byte[] bytes1) {
            Log.i("DCCSDK", "DCCSDK onError");
        }

        @Override
        public void onDeactivated() {
            Log.i("DCCSDK", "DCCSDK onDeactivated");
        }

        @Override
        public boolean onGetTerminalId(String sTerminalId) {
            Log.i("DCCSDK", "DCCSDK onGetTerminalId sTerminalId=" + sTerminalId);

            // YOU MUST RETURN true , If YOU return false , Reader Can't activate
            return true;
        }
    };
    /* TMobilePass NFC Crypto Service Implementation END */

    /* TMobilePass BLE Crypto Service Implementation Start */
    @SuppressLint("MissingPermission")
    public void bleCryptoStart() {
        Log.d(TAG, "start bleCryptoStart");

        btScanner.startScan(filters, settings, leScanCallback);

        mHandler.postDelayed(() -> {
            mbuttonBleScan.setEnabled(true);
            stopScanning();
        }, SCAN_PERIOD);
    }

    @SuppressLint("MissingPermission")
    public void stopScanning() {
        Log.d(TAG, "stopping scanning");
        mbuttonNfcScan.setEnabled(true);

        btScanner.stopScan(leScanCallback);
    }

    /*
     * FIND TBleMobilePass Reader Device
     */
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            String devicename = BluetoothUtils.getDeviceName(result);
            int rssi = BluetoothUtils.getRssi(result);
            /*
             * TMobilePass Validation Process
             * You can customize rssi detection value
             * Crypto Authentication rssi value = -45C123456789543210
             * C123456789543210
             * C123456789543210
             *
             * Service Authentication rssi value = -70
             */
            if (BluetoothUtils.checkTMobilepassDevice(rssi, -62, devicename)) {
                // BLE SCAN STOP
                stopScanning();

                bTrnValue = BluetoothUtils.getTrnValue(result);    // bTRNVALUE
                Log.d(TAG, "sTokenValue : " + sTokenValue);


                mHandler.postDelayed(() -> {
                    connectDevice(result.getDevice());

                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    // 1초 진동
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(500);
                    }
                }, 1);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            // bTRNVALUE
            Log.d(TAG, "onScanFailed errorCode=" + errorCode);
        }
    };

    // TMobilePass BLE Device Connection

    @SuppressLint("MissingPermission")
    private void connectDevice(BluetoothDevice device) {
        log("Connecting to " + device.getAddress());
        GattClientCallback gattClientCallback = new GattClientCallback(this);
        mGatt = device.connectGatt(this, false, gattClientCallback);
    }

    @Override
    public void log(String s) {
        Log.d(TAG, "log=" + s);
    }

    @Override
    public void logError(String s) {
        Log.d(TAG, "logError=" + s);
    }

    @Override
    public void cryptoTokenProcessStart() {
        Log.d(TAG, "cryptoTokenProcessStart");

        /*
         * BLE Connected , Create CryptoToken and Transfer Start
         */
        CryptoProcess cryptoProcessInstance = CryptoProcess.getInstance();

        /* You must assigned "TokenEncryped" */
        byte[] TokenEncryped = null;
        byte[] token = null;
        boolean EncryptOption = false;
        if (EncryptOption == true) // is Encrypted
        {
            try {
                token = cryptoProcessInstance.getClientAes256Cbc(TokenEncryped);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            token = sTokenValue.getBytes();
        cryptoProcessInstance.putCryptoToken(token, bTrnValue);
        mHandler.postDelayed(() -> CryptoProcess.bleWriteMessages(mGatt), 10);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // 1초 진동
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(500);
        }
    }

    @Override
    public void cryptoTokenProcessEnd() {
        // BLE Connected , Create CryptoToken and Transfer End
        if (!CryptoProcess.bleWriteMessages(mGatt)) {
            disconnectGattServer();
        }
    }

    /*
     * BLE  Disconnect
     */
    @SuppressLint("MissingPermission")
    @Override
    public void disconnectGattServer() {
        log("Closing Gatt connection");
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
    }

    /* TMobilePass BLE Crypto Service Implementation End */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void MarshMallow() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();

        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("ACCESS_FINE_LOCATION");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("ACCESS_COARSE_LOCATION");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!addPermission(permissionsList, Manifest.permission.BLUETOOTH_SCAN))
                permissionsNeeded.add("BLUETOOTH_SCAN");
            if (!addPermission(permissionsList, Manifest.permission.BLUETOOTH_CONNECT))
                permissionsNeeded.add("BLUETOOTH_CONNECT");
        }

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {

                // Need Rationale
                String message = "App need access to " + permissionsNeeded.get(0);

                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    /**
     * hex String 반환 함수
     * - 주어진 바이트 배열을 hex String으로 변환하여 반환한다.
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
}
