package kr.co.ttcnc.ucsdk.dccctrllib.ble;

public interface GattClientActionListener {

    void log(String message);

    void logError(String message);

    void cryptoTokenProcessStart();

    void cryptoTokenProcessEnd();

    void disconnectGattServer();
}
