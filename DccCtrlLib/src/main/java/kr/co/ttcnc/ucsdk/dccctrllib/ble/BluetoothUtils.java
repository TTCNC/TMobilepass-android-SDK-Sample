package kr.co.ttcnc.ucsdk.dccctrllib.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.util.SparseArray;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import kr.co.ttcnc.ucsdk.dccctrllib.util.TLog;
import kr.co.ttcnc.ucsdk.dccctrllib.util.Utils;


public class BluetoothUtils implements IBleConstants {
    private final static String TAG = BluetoothUtils.class.getSimpleName();

    @SuppressLint("MissingPermission")
    public static int getRssi(ScanResult result) {
        TLog.d(TAG," Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() +  " manufacture Data: " + result.getScanRecord().getManufacturerSpecificData() +"\n");
        String devicename = result.getDevice().getName();
        return  result.getRssi();
    }

    @SuppressLint("MissingPermission")
    public static String getDeviceName(ScanResult result) {
        return  result.getDevice().getName();
    }

    public static byte[] getTrnValue(ScanResult result) {
        /*
         * FIND TBleMobilePass Reader Device
         */
        SparseArray<byte[]> manufacturerData = result.getScanRecord().getManufacturerSpecificData();
        for(int i = 0; i < manufacturerData.size(); i++){
            int manufacturerId = manufacturerData.keyAt(i);
            TLog.d(TAG, " manufacture manufacturerId : " + manufacturerId +"\n");
            TLog.d(TAG, " manufacture manufacturerId : " + Utils.hexToString(manufacturerData.get(0)) +"\n");
        }
        return  manufacturerData.get(0);
    }

    public static boolean checkTMobilepassDevice(int rssi , int guideRssi , String devicename) {
        if( rssi > guideRssi && devicename != null &&  devicename.startsWith("T")) {
            return true;
        }
        return false;
    }

    // Characteristics

    public static List<BluetoothGattCharacteristic> findCharacteristics(BluetoothGatt bluetoothGatt) {
        List<BluetoothGattCharacteristic> matchingCharacteristics = new ArrayList<>();

        List<BluetoothGattService> serviceList = bluetoothGatt.getServices();
        BluetoothGattService service = BluetoothUtils.findService(serviceList);
        if (service == null) {
            return matchingCharacteristics;
        }

        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
        for (BluetoothGattCharacteristic characteristic : characteristicList) {
            if (isMatchingCharacteristic(characteristic)) {
                matchingCharacteristics.add(characteristic);
            }
        }

        return matchingCharacteristics;
    }

    @Nullable
    public static BluetoothGattCharacteristic findWriteCharacteristic(BluetoothGatt bluetoothGatt) {
        return findCharacteristic(bluetoothGatt, CHARACTERISTIC_WRITE);
    }

    @Nullable
    public static BluetoothGattCharacteristic findReadCharacteristic(BluetoothGatt bluetoothGatt) {
        return findCharacteristic(bluetoothGatt, CHARACTERISTIC_READ);
    }

    @Nullable
    private static BluetoothGattCharacteristic findCharacteristic(BluetoothGatt bluetoothGatt, String uuidString) {
        List<BluetoothGattService> serviceList = bluetoothGatt.getServices();
        BluetoothGattService service = BluetoothUtils.findService(serviceList);
        if (service == null) {
            return null;
        }

        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
        for (BluetoothGattCharacteristic characteristic : characteristicList) {
            if (characteristicMatches(characteristic, uuidString)) {
                return characteristic;
            }
        }

        return null;
    }

    public static boolean isWriteCharacteristic(BluetoothGattCharacteristic characteristic) {
        return characteristicMatches(characteristic, CHARACTERISTIC_WRITE);
    }

    public static boolean isReadCharacteristic(BluetoothGattCharacteristic characteristic) {
        return characteristicMatches(characteristic, CHARACTERISTIC_READ);
    }

    private static boolean characteristicMatches(BluetoothGattCharacteristic characteristic, String uuidString) {
        if (characteristic == null) {
            return false;
        }
        UUID uuid = characteristic.getUuid();
        return uuidMatches(uuid.toString(), uuidString);
    }

    private static boolean isMatchingCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) {
            return false;
        }
        UUID uuid = characteristic.getUuid();
        return matchesCharacteristicUuidString(uuid.toString());
    }

    private static boolean matchesCharacteristicUuidString(String characteristicIdString) {
        return uuidMatches(characteristicIdString, CHARACTERISTIC_WRITE, CHARACTERISTIC_READ);
    }

    public static boolean requiresResponse(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
                != BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
    }

    public static boolean requiresConfirmation(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE)
                == BluetoothGattCharacteristic.PROPERTY_INDICATE;
    }

    // Descriptor

    @Nullable
    public static BluetoothGattDescriptor findClientConfigurationDescriptor(List<BluetoothGattDescriptor> descriptorList) {
        for(BluetoothGattDescriptor descriptor : descriptorList) {
            if (isClientConfigurationDescriptor(descriptor)) {
                return descriptor;
            }
        }

        return null;
    }

    private static boolean isClientConfigurationDescriptor(BluetoothGattDescriptor descriptor) {
        if (descriptor == null) {
            return false;
        }
        UUID uuid = descriptor.getUuid();
        String uuidSubstring = uuid.toString().substring(4, 8);
        return uuidMatches(uuidSubstring, CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID);
    }

    // Service

    private static boolean matchesServiceUuidString(String serviceIdString) {
        return uuidMatches(serviceIdString, SERVICE_STRING);
    }

    @Nullable
    private static BluetoothGattService findService(List<BluetoothGattService> serviceList) {
        for (BluetoothGattService service : serviceList) {
            String serviceIdString = service.getUuid()
                    .toString();
            if (matchesServiceUuidString(serviceIdString)) {
                return service;
            }
        }
        return null;
    }

    // String matching

    // If manually filtering, substring to match:
    // 0000XXXX-0000-0000-0000-000000000000
    private static boolean uuidMatches(String uuidString, String... matches) {
        for (String match : matches) {
            if (uuidString.equalsIgnoreCase(match)) {
                return true;
            }
        }

        return false;
    }
}