package kr.co.ttcnc.ucsdk.dccctrllib.nfc;

/**
 * Created by parkjeongho on 2023-12-19 오전 11:04
 */
interface INfcConstants {
    /**
     * NFC Reader Device Name
     */
    /*
    enum ReaderModel {
        NC400LST,
        TMR300,
        CCAL100TX,
        SMR300,
        NC900CM, // NC900 공용
        NC900CE, // NC900 CLEAN ELEX
        NC900NS, // NC900 NEURO SYS
    }*/

    /**
     * APDU 처리 상태 표시 상수
     */
    /*enum ProcessingState {
        WAIT, // 대기 상태
        SELECT_APPLICATION, // 어플리케이션 선택 상태
        GET_DATA, // CC 파일 선택 상태
        PUT_TOKEN, // NDEF 레코드 선택 상태
        GET_TOKEN, // NDEF 레코드 업데이트 상태
    }*/

    String APPLET_GUBUN_REAL = "00";
    String APPLET_GUBUN_MANAGEMENT = "F0";

    int PLAIN_TEXT_MODE = 0;
    int CRYPTO_MODE = 1;
    int ACTIVE = 1;
    int PAUSE = 0;
    int MIN_ALIVE_TIME = 20 * 1000;
    int MAX_ALIVE_TIME = 60 * 1000;

    // offsets
    int OFFSET_CLA = 0;
    int OFFSET_INS = 1;
    int OFFSET_P1 = 2;
    int OFFSET_P2 = 3;
    int OFFSET_LC = 4;
    int OFFSET_CDATA = 5;

    int TERMINALID_LEN = 12;
    int HRN_LENGTH = 16;
    int TRN_LENGTH = 16;
    int IV_LENGTH = 16;
    int AUTHTAG_LENGTH = 16;
    int PLAIN_TOKEN_LENGTH = 32;
    int E_TOKEN_LENGTH = 61;

    int TDES_TRN_LENGTH = 8;
    int TDES_HRN_LENGTH = 8;

    int NFC_ACTIVE_MAX_ALIVE_TIME = 60 * 1000; // 20초 ~ 60 초
    int NFC_ACTIVE_UNLIMITED_ALIVE_TIME = 0; // 계속 유효;
    int NFC_ACTIVE_APPLICATION_ALIVE_TIME = -1;

    byte[] DCC_MOBILEPASS_NO_ERROR = {(byte) 0x90, (byte) 0x00};
    byte[] P1P2_BY_NAME = {(byte) 0x04, (byte) 0x00};
    byte[] P1P2_BY_FILE_IDENTIFIER = {(byte) 0x00, (byte) 0x0C};
    byte[] P1P2_BY_NAME_AGAIN = {(byte) 0x02, (byte) 0x04};
    byte[] P1P2_BY_FILE_IDENTIFIER_AGAIN = {(byte) 0x02, (byte) 0x0C};
    byte[] P1P2_AGAIN = {(byte) 0x02, (byte) 0x00};
    byte[] P1P2_NORMAL = {(byte) 0x00, (byte) 0x00};
    byte[] P1P2_FINISH_FIRMWARE_UPDATE = {(byte) 0xFF, (byte) 0xFF};

    byte[] SW1SW2_SUCCESS = {(byte) 0x90, (byte) 0x00};
    byte[] SW1SW2_CLA_NOT_SUPPORTED = {(byte) 0x6E, (byte) 0x00};
    byte[] SW1SW2_INS_NOT_SUPPORTED = {(byte) 0x6D, (byte) 0x00};
    byte[] SW1SW2_COMMAND_NOT_ALLOWED = {(byte) 0x69, (byte) 0x86};
    byte[] SW1SW2_APPLET_SELECT_FAILED = {(byte) 0x69, (byte) 0x99};
    byte[] SW1SW2_SECURITY_STATUS_NOT_SATISFIED = {(byte) 0x69, (byte) 0x82};
    byte[] SW1SW2_DATA_INVALID = {(byte) 0x69, (byte) 0x84};
    byte[] SW1SW2_CONDITIONS_NOT_SATISFIED = {(byte) 0x69, (byte) 0x85};
    byte[] SW1SW2_INCORRECT_P1P2 = {(byte) 0x6A, (byte) 0x80};
    byte[] SW1SW2_WRONG_P1P2 = {(byte) 0x6B, (byte) 0x00};
    byte[] SW1SW2_WRONG_LENGTH = {(byte) 0x67, (byte) 0x00};
    byte[] SW1SW2_WRONG_DATA = {(byte) 0x6A, (byte) 0x80};
    byte[] SW1SW2_FILE_NOT_FOUND = {(byte) 0x6A, (byte) 0x82};
    byte[] SW1SW2_UNKNOWN = {(byte) 0x6F, (byte) 0x00};

    // Application 오류
    byte[] SW1SW2_AUTHENTICATION_ERROR = {(byte) 0x91, (byte) 0xAE};
    byte[] SW1SW2_UNREGISTERED_SNO_ERROR = {(byte) 0xF0, (byte) 0x73};

    byte[] SW1SW2_MOBILE_WIFI_NETWORK_NOT_SERVICE = {(byte) 0xF0, (byte) 0x11};
    byte[] SW1SW2_UNAVAILABLE_SERVICE = {(byte) 0xF0, (byte) 0x12};
    byte[] SW1SW2_UNREGISTED_APP_CARD_SERVICE = {(byte) 0xF0, (byte) 0x71};
    byte[] SW1SW2_UNREGISTED_USER = {(byte) 0xF0, (byte) 0x73};
    byte[] SW1SW2_AUTHENTICATION_SERVER_ERROR = {(byte) 0xF0, (byte) 0x90};
    byte[] SW1SW2_USER_DEFINED_ERROR01 = {(byte) 0xF0, (byte) 0x61};
    byte[] SW1SW2_USER_DEFINED_ERROR02 = {(byte) 0xF0, (byte) 0x62};
    byte[] SW1SW2_USER_DEFINED_ERROR03 = {(byte) 0xF0, (byte) 0x63};
    byte[] SW1SW2_USER_DEFINED_ERROR04 = {(byte) 0xF0, (byte) 0x64};
    byte[] SW1SW2_USER_DEFINED_ERROR05 = {(byte) 0xF0, (byte) 0x65};
    byte[] SW1SW2_USER_DEFINED_ERROR06 = {(byte) 0xF0, (byte) 0x66};

    // APDU Values
    byte CLA_NO_SECURE = (byte) 0x00;
    byte CLA_MOBILE_SECURE = (byte) 0x90;
    byte INS_SELECT_FILE = (byte) 0xA4;
    byte INS_READ_BINARY = (byte) 0xB0;
    byte INS_UPDATE_BINARY = (byte) 0xD6;
    byte INS_GET_DATA = (byte) 0xCA;
    byte INS_GET_TOKEN_DATA = (byte) 0xB0;
    byte INS_GET_RESPONSE = (byte) 0xC0;
    byte INS_PUT_TOKEN = (byte) 0xD6;
}
