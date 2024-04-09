package kr.co.ttcnc.ucsdk.dccctrllib.nfc;

import static kr.co.ttcnc.ucsdk.dccctrllib.nfc.AesGcmCryptor.ConcatArrays;

import android.content.Context;
import android.content.Intent;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import kr.co.ttcnc.ucsdk.dccctrllib.JNIClass;
import kr.co.ttcnc.ucsdk.dccctrllib.util.TLog;
import kr.co.ttcnc.ucsdk.dccctrllib.util.Utils;

/**
 * apdu 처리 클래스
 * Created by parkjeongho on 2023-12-19 오전 10:59
 */
public class DccApduManager implements INfcConstants {
    private static final String TAG = DccApduManager.class.getSimpleName();

    private static DccApduManager instance = null;
    private Context context;
    private JNIClass jni;
    private NfcReader nfcReader;
    private IDccConnectionHandler connectionHandler = null;
    private TimerThread timerthread = null;

    private ProcessingState currentState = ProcessingState.WAIT;

    private byte[] currentApplicationError = DCC_MOBILEPASS_NO_ERROR;
    private byte[] bHceMessage = null;
    private byte[] gTRN;

    private byte gOption = (byte) 0x0;
    private int encryptionMode = CRYPTO_MODE;
    private int currentStateOfToken = ACTIVE;
    private int alivetime = 0;
    private int forceBreak = 0;
    private String jsonString;
    private boolean isThereOption = false;

    /**
     * DccApduManager 인스턴스를 반환한다.
     * 인스턴스가 null 일 경우 새로 생성하지 않는다.
     * @return DccApduManager 인스턴스
     */
    public static synchronized DccApduManager getInstance() {
        return instance;
    }

    /**
     * DccApduManager 싱글턴 인스턴스를 반환한다.
     * @param context
     * @param readerModel
     * @return DccApduManager 인스턴스
     */
    public static synchronized DccApduManager getInstance(Context context, ReaderModel readerModel) {
        if (instance == null) {
            instance = new DccApduManager(context, readerModel);
        }

        return instance;
    }

    /**
     * DccApduManager 생성자
     * @param context
     * @param readerModel
     */
    public DccApduManager(Context context, ReaderModel readerModel) {
        this.context = context;

        try {
            jsonString = null;
            jni = new JNIClass();
        } catch (Exception e) {
            TLog.i(TAG, "Constructor Error => " + e);
        }

        switch(readerModel) {
            case TMR300     : nfcReader = new TMR300Reader(this.context); break;
            case NC900CE    : nfcReader = new NC900CEReader(this.context); break;
            case NC900CM    : nfcReader = new NC900CMReader(this.context); break;
            case NC900NS    : nfcReader = new NC900NSReader(this.context); break;
            case SMR300     : nfcReader = new SMR300Reader(this.context); break;
            case NC400LST   : nfcReader = new NC400LSTReader(this.context); break;
            case CCAL100TX  : nfcReader = new CCAL100TXReader(this.context); break;
        }

        LiberoPrefManager pref = LiberoPrefManager.getInstance(context);
        pref.setReaderModelPreference(readerModel);
    }

    /**
     * SDK의 정보를 반환한다.
     * @return SDK 정보 문자열
     */
    public String getLibVersion() {
        return "TiMoPass v1.07-20240302 : Copyright© 2023 , TTCNC All Right Reserved";
    }

    /**
     * DccApduManager Handler is needed for onResult
     *
     * @param connectionHandler
     */
    public void setDccApduManagerHandler(IDccConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    /**
     * 어플리케이션 레이블을 설정한다.
     * @param applicationLabel 설정하고자 하는 어플리케이션 레이블
     */
    public void setApplicationLabel(String applicationLabel) {
        nfcReader.setApplicationLabel(applicationLabel);
    }

    /**
     * 어플리케이션 아이디를 설정한다.
     * @param hex 어플리케이션 아이디(바이트 배열)으로 전환하고자 하는 16진수 문자열
     */
    public void setAID(String hex) {
        nfcReader.setAID(hex);
    }

    /**
     * 관리 모드로 설정한다.
     */
    public void setManagementMode() {
        nfcReader.setMode(APPLET_GUBUN_MANAGEMENT);
    }

    /**
     * 기본 모드로 설정한다.
     */
    public void setNormalMode() {
        nfcReader.setMode(APPLET_GUBUN_REAL);
    }

    /**
     * 설정된 모드를 반환한다.
     * @return
     */
    public String getMode() {
        return nfcReader.getMode();
    }

    /**
     * 사운드 모드를 반환한다.
     * @param iSoundMode 사운드 모드
     */
    public void setSound(int iSoundMode) {
        LiberoPrefManager pref = LiberoPrefManager.getInstance(context);
        pref.setSoundMode(iSoundMode);
    }

    /**
     * 평문으로 된 토큰을 설정한다
     * @param sTokenValue 토큰
     * @param alivetime 토큰 지속 타임
     */
    public void setToken(String sTokenValue, int alivetime) {
        byte[] message = sTokenValue.getBytes();
        currentApplicationError = DCC_MOBILEPASS_NO_ERROR;
        encryptionMode = 0;
        this.alivetime = alivetime;
        if (this.alivetime > 60000) {
            this.alivetime = 60000;
        }

        if (this.alivetime > 0 && this.alivetime < 20000) {
            this.alivetime = 20000;
        }

        LiberoPrefManager pref;
        if (this.alivetime > 0) {
            if (timerthread != null) {
                timerthread = null;
                setHceMessage((byte[])null);
            }

            pref = LiberoPrefManager.getInstance(context);
            pref.setTokenAliveOption(this.alivetime);
            activeCryptoToken();
            setHceMessage(message);
            timerthread = new TimerThread();
            timerthread.setDaemon(true);
            timerthread.start();
        } else {
            pref = LiberoPrefManager.getInstance(context);
            pref.setToken(message);
            pref.setTokenAliveOption(this.alivetime);
            activeCryptoToken();
            setHceMessage(message);
        }
    }

    /**
     * 토큰을 암호화 한 후 설정한다.
     * @param sTokenValue 토큰
     * @param alivetime 토큰 지속 타임
     * @param isEncrypted 암호화 여부
     * @throws Exception
     */
    public void setCryptoToken(String sTokenValue, int alivetime, boolean isEncrypted) throws Exception {
        currentApplicationError = DCC_MOBILEPASS_NO_ERROR;
        encryptionMode = CRYPTO_MODE;

        this.alivetime = alivetime;
        if (this.alivetime > MAX_ALIVE_TIME) {
            this.alivetime = MAX_ALIVE_TIME;
        }

        if (this.alivetime > 0 && this.alivetime < MIN_ALIVE_TIME) {
            this.alivetime = MIN_ALIVE_TIME;
        }

        byte[] message;
        if(isEncrypted) {
            message = Utils.hexStringToByteArray(sTokenValue);
        } else {
            message = nfcReader.getServerAes256Cbc(Utils.hexStringFromByteArray(sTokenValue.getBytes()).getBytes());
        }

        LiberoPrefManager pref = LiberoPrefManager.getInstance(context);

        if (this.alivetime > 0) {
            if (timerthread != null) {
                timerthread = null;
                setHceMessage(null);
            }

            pref.setTokenAliveOption(this.alivetime);
            activeCryptoToken();

            setHceMessage(message);

            timerthread = new TimerThread();
            timerthread.setDaemon(true);    // 메인스레드와 종료 동기화
            timerthread.start();            // 작업스레드 시작 -> run() 이 작업스레드로 실행됨
        } else {
            pref.setToken(message);
            pref.setTokenAliveOption(this.alivetime);
            setHceMessage(message);
            activeCryptoToken();
        }
    }

    /**
     * 토큰을 암호화 한 후 설정한다.
     * @param sTokenValue 토큰
     * @param option 옵션
     * @param alivetime 토큰 지속 타임
     * @param isEncrypted 암호화 여부
     * @throws Exception
     */
    public void setCryptoToken(String sTokenValue, byte option, int alivetime, boolean isEncrypted) throws Exception {
        currentApplicationError = DCC_MOBILEPASS_NO_ERROR;
        encryptionMode = CRYPTO_MODE;

        this.alivetime = alivetime;
        if (this.alivetime > MAX_ALIVE_TIME) {
            this.alivetime = MAX_ALIVE_TIME;
        }

        if (this.alivetime > 0 && this.alivetime < MIN_ALIVE_TIME) {
            this.alivetime = MIN_ALIVE_TIME;
        }

        gOption = option;
        isThereOption = true;

        byte[] message;
        if(isEncrypted) {
            message = Utils.hexStringToByteArray(sTokenValue);
        } else {
            message = nfcReader.getServerAes256Cbc(Utils.hexStringFromByteArray(sTokenValue.getBytes()).getBytes(), gOption);
        }

        LiberoPrefManager pref = LiberoPrefManager.getInstance(context);

        if (this.alivetime > 0) {
            if (timerthread != null) {
                timerthread = null;
                setHceMessage(null);
            }

            pref.setTokenAliveOption(alivetime);
            activeCryptoToken();

            setHceMessage(message);

            timerthread = new TimerThread();
            timerthread.setDaemon(true);    // 메인스레드와 종료 동기화
            timerthread.start();            // 작업스레드 시작 -> run() 이 작업스레드로 실행됨
        } else {
            pref.setToken(message);
            pref.setTokenAliveOption(this.alivetime);
            setHceMessage(message);
            activeCryptoToken();
        }
    }

    /**
     * 토큰을 활성상태로 설정한다.
     */
    public void activeCryptoToken() {
        TLog.i(TAG, "DCCSDK activeCryptoToken");
        setCryptoTokenLoad();
        currentStateOfToken = ACTIVE;

        LiberoPrefManager pref = LiberoPrefManager.getInstance(context);
        pref.setActivePauseState(currentStateOfToken);
    }

    /**
     * 토큰을 일시정지상태로 설정한다.
     */
    public void pauseCryptoToken() {
        TLog.i(TAG, "DCCSDK pauseCryptoToken");
        setHceMessage((byte[]) null);
        currentStateOfToken = PAUSE;

        LiberoPrefManager pref = LiberoPrefManager.getInstance(context);
        pref.setActivePauseState(currentStateOfToken);
    }

    /**
     * 설정된 토큰을 지운다.
     */
    public void setClearCryptoToken() {
        TLog.i(TAG, "DCCSDK setClearCryptoToken");

        LiberoPrefManager pref = LiberoPrefManager.getInstance(context);
        pref.setToken(null);
        currentStateOfToken = PAUSE;
        pref.setActivePauseState(currentStateOfToken);

        bHceMessage = null;
    }

    /**
     * HCE onStartCommand
     */
    public void onStartCommand() {
        if (connectionHandler != null) {
            connectionHandler.onStartCommand();
        }
    }


    /**
     * HCE onDeactivated
     */
    public void onDeactivated() {
        if (connectionHandler != null) {
            connectionHandler.onDeactivated();
        }

        if (alivetime > 0) {
            removeAidService();
        }
    }

    /**
     * APDU 처리 함수
     *
     * @param commandApdu 처리해야할 데이터
     * @return
     */
    public byte[] processApdu(byte[] commandApdu) {
        TLog.i(TAG, "processApdu : " + Utils.hexStringFromByteArray(commandApdu));
        byte[] responseApdu;
        boolean isOccurredError = false;

        // Class 확인
        byte bCLA = commandApdu[OFFSET_CLA];
        TLog.i(TAG, "CLA " + String.format("%02X", bCLA));

        switch (bCLA) {
            case CLA_NO_SECURE:    // 00
            case CLA_MOBILE_SECURE:    // 90
                byte bINS = commandApdu[OFFSET_INS];
                TLog.i(TAG, "INS " + String.format("%02X", bINS));

                switch (bINS) {
                    case INS_SELECT_FILE:  // A4
                        responseApdu = selectFile(commandApdu);
                        if (connectionHandler != null) {
                            connectionHandler.onSelectFile(commandApdu);
                        }
                        /*
                         * ALWAYS MODE로 설정되면
                         * ALWAY MODE로 설정된 토큰 값을
                         * NFC 에 자동으로 로딩하여
                         * REBOOT 할 때도 유지 되도록 함
                         */

                        LiberoPrefManager pref = LiberoPrefManager.getInstance(context);
                        currentStateOfToken = pref.getActivePauseState();

                        // Android NFC 에 키를 로딩할 때 , 영구적으로 키 로딩하는 옵션
                        if (pref.getTokenAliveOption() == 0 && currentStateOfToken == ACTIVE) {
                            setCryptoTokenLoad();
                        }
                        break;

                    case INS_GET_TOKEN_DATA:    // B0
                        if (currentState == ProcessingState.SELECT_APPLICATION) {
                            responseApdu = getToken(commandApdu);
                        } else {
                            TLog.i(TAG, "unExpected Process SW1SW2_COMMAND_NOT_ALLOWED - INS_GET_TOKEN_DATA");
                            responseApdu = SW1SW2_COMMAND_NOT_ALLOWED;
                            isOccurredError = true;
                        }
                        break;

                    case INS_PUT_TOKEN:         // D6 FOR SET TOKEN
                        if (currentState == ProcessingState.GET_DATA) {
                            responseApdu = putData(commandApdu);
                        } else {
                            TLog.i(TAG, "unExpected Process SW1SW2_COMMAND_NOT_ALLOWED - INS_PUT_TOKEN");
                            responseApdu = SW1SW2_COMMAND_NOT_ALLOWED;
                            isOccurredError = true;
                        }
                        break;

                    case INS_GET_DATA:         // CA  FOR SET TOKEN
                        if (currentState == ProcessingState.SELECT_APPLICATION) {
                            responseApdu = getData(commandApdu);
                            if (connectionHandler != null) {
                                connectionHandler.onGetData(commandApdu);
                            }
                        } else {
                            TLog.i(TAG, "unExpected Process SW1SW2_COMMAND_NOT_ALLOWED - INS_GET_DATA");
                            responseApdu = SW1SW2_COMMAND_NOT_ALLOWED;
                            isOccurredError = true;
                        }
                        break;

                    default:
                        TLog.i(TAG, "ins default");
                        responseApdu = SW1SW2_INS_NOT_SUPPORTED;
                        isOccurredError = true;
                        break;
                }
                break;

            default:
                TLog.i(TAG, "cla default");
                responseApdu = SW1SW2_CLA_NOT_SUPPORTED;
                isOccurredError = true;
                break;
        }

        if(isOccurredError) {
            if (connectionHandler != null) {
                connectionHandler.onError(commandApdu, responseApdu);
            }
            currentState = ProcessingState.WAIT;
        }

        TLog.i(TAG, "responseApdu : " + Utils.hexStringFromByteArray(responseApdu) + "  currentState : " + currentState);

        return responseApdu;
    }

    /**
     * APDU 코멘드(파일 선택) 처리 함수
     *
     * @param commandApdu 처리해야할 데이터
     * @return
     */
    private byte[] selectFile(byte[] commandApdu) {
        TLog.i(TAG, "selectFile");
        byte[] responseApdu;

        byte[] bP1P2 = new byte[]{commandApdu[OFFSET_P1], commandApdu[OFFSET_P2]};
        TLog.i(TAG, "P1P2 " + Utils.hexStringFromByteArray(bP1P2));

        byte bLc = commandApdu[OFFSET_LC];
        int lc = bLc & 0xff;
        TLog.i(TAG, "Lc " + lc);

        byte[] bDATA = new byte[lc];
        for (int i = 0; i < lc; i++) {
            bDATA[i] = commandApdu[OFFSET_CDATA + i];
        }
        TLog.i(TAG, "DATA " + Utils.hexStringFromByteArray(bDATA));

        if (Arrays.equals(bP1P2, P1P2_BY_NAME)) {   // P1-P2 == 0400
            TLog.i(TAG, "Select Application by name");

            if (nfcReader.isEqualPassAid(bDATA)) {  // 출입통제 어플리케이션
                String sFCI = nfcReader.getFCI();
                byte[] bFCI = sFCI.getBytes();
                responseApdu = new byte[bFCI.length + SW1SW2_SUCCESS.length];

                System.arraycopy(bFCI, 0, responseApdu, 0, bFCI.length);
                System.arraycopy(SW1SW2_SUCCESS, 0, responseApdu, bFCI.length, SW1SW2_SUCCESS.length);

                currentState = ProcessingState.SELECT_APPLICATION;
                TLog.i(TAG, "Select MOBILE_PASS_AID Application");

            } else if (nfcReader.isEqualPaymentAid(bDATA)) {  // 모바일 결제 어플리케이션
                String sFCI = nfcReader.getPaymentFCI();
                byte[] bFCI = sFCI.getBytes();
                responseApdu = new byte[bFCI.length + SW1SW2_SUCCESS.length];

                System.arraycopy(bFCI, 0, responseApdu, 0, bFCI.length);
                System.arraycopy(SW1SW2_SUCCESS, 0, responseApdu, bFCI.length, SW1SW2_SUCCESS.length);

                currentState = ProcessingState.SELECT_APPLICATION;
                TLog.i(TAG, "Select MOBILE_PAYMENT_AID Application");

            } else {
                responseApdu = SW1SW2_APPLET_SELECT_FAILED;
                currentState = ProcessingState.WAIT;
            }


            // 사용자 어플리케이션에서 임의 어플리케이션 오류를 지정하면
            // 해당 오류가 단말기에 전달 될 수 있도록 한다.
            if (currentApplicationError != DCC_MOBILEPASS_NO_ERROR) {
                responseApdu = currentApplicationError;
                currentState = ProcessingState.WAIT;
            }
        } else {
            TLog.i(TAG, "unknown Select");

            responseApdu = SW1SW2_INCORRECT_P1P2;
            currentState = ProcessingState.WAIT;
        }

        return responseApdu;
    }

    /**
     * 핸드폰의 정보 요청
     * - 핸드폰 전화번호 데이터를 반환한다.
     *
     * @param commandApdu 처리해야할 데이터
     * @return
     */
    private byte[] getData(byte[] commandApdu) {
        byte[] responseApdu;
        byte[] Datas;
        byte bLe = 0x00;
        String JsonString = null;

        byte[] bP1P2 = new byte[]{commandApdu[OFFSET_P1], commandApdu[OFFSET_P2]};
        TLog.i(TAG, "getData P1P2 " + Utils.hexStringFromByteArray(bP1P2));

        byte bLc = commandApdu[OFFSET_LC];
        int lc = bLc & 0xff;
        TLog.i(TAG, "lc => " + lc + ", commandApdu.length => " + commandApdu.length);

        if (lc > 0) {
            Datas = new byte[lc];
            try {
                System.arraycopy(commandApdu, OFFSET_LC + 1, Datas, 0, lc);
                TLog.i(TAG, "Datas " + Datas);
                bLe = commandApdu[OFFSET_LC + lc + 1];
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                TLog.i(TAG, "getData ArrayIndexOutOfBoundsException e " + e);
            }

            try {
                JsonString = new String(Datas, "ksc5601");
                TLog.i(TAG, "JsonString " + JsonString);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                TLog.i(TAG, "getData UnsupportedEncodingException e " + e);
            }
        } else {
            TLog.i(TAG, "Lc is minus");
            bLe = commandApdu[OFFSET_LC + 1];
        }

        // NOT FORMAT IS CORRECT
        if (JsonString == null) {
            jsonString = null;
            responseApdu = SW1SW2_WRONG_LENGTH;
            currentState = ProcessingState.WAIT;
        } else {
            jsonString = JsonString;

            int p1 = bP1P2[0] & 0xff;
            int le = bLe & 0xff;


            TLog.i(TAG, "p1 : " + p1);  //  11,12,14,15,16
            TLog.i(TAG, "le : " + le);  // 0x08 for 11, 0x0b for phone number , 0x01 for key version , 0x01 for life cycle , 0x01 for applet version

            LiberoPrefManager pref = LiberoPrefManager.getInstance(context);

            if (encryptionMode == CRYPTO_MODE) {
                gTRN = new byte[TDES_TRN_LENGTH];       // RANDOM NUMBER FROM READER
                new Random().nextBytes(gTRN);    // Fill RANDOM BYTEs
                TLog.i(TAG, "TRN=" + Utils.hexStringFromByteArray(gTRN));

                responseApdu = new byte[TDES_TRN_LENGTH + le + SW1SW2_SUCCESS.length];

                // TRN(16) + PHONENO(13) + 9000
                byte[] PHONE_ID = pref.getPhoneNo().getBytes();
                System.arraycopy(gTRN, 0, responseApdu, 0, TDES_TRN_LENGTH);
                System.arraycopy(PHONE_ID, 0, responseApdu, TDES_TRN_LENGTH, le);
                System.arraycopy(SW1SW2_SUCCESS, 0, responseApdu, TDES_TRN_LENGTH + le, SW1SW2_SUCCESS.length);
            } else {
                responseApdu = new byte[le + SW1SW2_SUCCESS.length];

                //  PHONENO(13) + 9000
                byte[] PHONE_ID = pref.getPhoneNo().getBytes();
                System.arraycopy(PHONE_ID, 0, responseApdu, 0, le);
                System.arraycopy(SW1SW2_SUCCESS, 0, responseApdu, le, SW1SW2_SUCCESS.length);
            }

            currentState = ProcessingState.GET_DATA;
        }

        return responseApdu;
    }


    /**
     * putData 처리 함수
     * - 사용자의 Token 정보를 설정한다.
     *
     * @param commandApdu 처리해야할 데이터
     * @return
     */
    private byte[] putData(byte[] commandApdu) {
        byte[] responseApdu = null;

        byte[] bP1P2 = new byte[]{commandApdu[OFFSET_P1], commandApdu[OFFSET_P2]};
        TLog.i(TAG, "P1P2 " + Utils.hexStringFromByteArray(bP1P2));

        byte bLc = commandApdu[OFFSET_LC];
        int lc = bLc & 0xff;
        TLog.i(TAG, "lc " + lc);

        byte[] bDATA = new byte[lc];
        for (int i = 0; i < lc; i++) {
            bDATA[i] = commandApdu[OFFSET_CDATA + i];
        }

        TLog.i(TAG, "DATA " + Utils.hexStringFromByteArray(bDATA));
        TLog.i(TAG, "stx " + Utils.hexStringFromByteArray(new byte[]{bDATA[0], bDATA[1]}));
        TLog.i(TAG, "opCode " + bDATA[2]);

        if (encryptionMode == CRYPTO_MODE) {
            byte[] HRN = new byte[8];
            byte[] E_TOKEN = new byte[24];
            System.arraycopy(bDATA, 0, HRN, 0, 8);
            System.arraycopy(bDATA, 8, E_TOKEN, 0, 24);

            TLog.i(TAG, "TRN=" + Utils.hexStringFromByteArray(gTRN));
            TLog.i(TAG, "HRN=" + Utils.hexStringFromByteArray(HRN));
            TLog.i(TAG, "E_TOKEN=" + Utils.hexStringFromByteArray(E_TOKEN));

            LiberoPrefManager pref;
            try {
                byte[] f53MK_from = jni.getf53MKValue();

                TLog.i(TAG, "TripleDES.f53MK_from=" + Utils.hexStringFromByteArray(f53MK_from));
                byte[] TK = TripleDES.encrypt(ConcatArrays(gTRN, HRN), f53MK_from, TripleDES.WorkType.CBC);
                TLog.i(TAG, "TK=" + Utils.hexStringFromByteArray(TK));

                // Encryption Data 24 byte
                byte[] TOKEN = TripleDES.decrypt(E_TOKEN, TK, TripleDES.WorkType.CBC);
                TLog.i(TAG, "TOKEN=" + Utils.hexStringFromByteArray(TOKEN));
                TLog.i(TAG, "TOKEN=" + TOKEN);

                byte[] saveTOKEN = new byte[16];
                System.arraycopy(TOKEN, 0, saveTOKEN, 0, 16);

                pref = LiberoPrefManager.getInstance(context);
                pref.setToken(saveTOKEN);

                currentState = ProcessingState.PUT_TOKEN;

                if (connectionHandler != null) {
                    connectionHandler.onSetTokenCompleted(pref.getStringToken(), jsonString);
                    forceBreak = 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Read From Reader by Plain Text Binary Format
            byte[] E_TOKEN = new byte[lc];
            System.arraycopy(bDATA, 0, E_TOKEN, 0, lc);

            TLog.i(TAG, "E_TOKEN=" + Utils.hexStringFromByteArray(E_TOKEN));

            LiberoPrefManager pref;
            try {
                pref = LiberoPrefManager.getInstance(context);
                pref.setToken(E_TOKEN);

                currentState = ProcessingState.PUT_TOKEN;

                if (connectionHandler != null) {
                    connectionHandler.onSetTokenCompleted(pref.getStringToken(), jsonString);
                    forceBreak = 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (true) {
            responseApdu = SW1SW2_SUCCESS;
        } else {
            responseApdu = SW1SW2_AUTHENTICATION_ERROR;
            currentState = ProcessingState.WAIT;
        }

        return responseApdu;
    }

    /**
     * getToken 처리 함수
     * - MobilePass 인증 처리  리더기의 요청으로 사용자 인증 정보를 암호화 하여 전송한다.
     *
     * @param commandApdu 처리해야할 데이터
     * @return
     */
    private byte[] getToken(byte[] commandApdu) {
        byte[] responseApdu = null;
        byte[] TOKEN;

        byte[] bTRN = new byte[TRN_LENGTH];    // RANDOM NUMBER FROM READER
        byte[] bHRN = new byte[HRN_LENGTH];    // HOME RANDOM NUMBER

        if (bHceMessage == null) {
            responseApdu = SW1SW2_UNREGISTERED_SNO_ERROR;
            currentState = ProcessingState.WAIT;

            return responseApdu;
        } else {
            TOKEN = bHceMessage;
            if (alivetime > 0) {
                TLog.i(TAG, "alivetime =" + alivetime + "So Clear Token");
                bHceMessage = null;
            }
        }
        TLog.i(TAG, "TOKEN=" + Utils.hexStringFromByteArray(TOKEN));

        byte[] bP1P2 = new byte[]{commandApdu[OFFSET_P1], commandApdu[OFFSET_P2]};
        TLog.i(TAG, "bP1P2 " + Utils.hexStringFromByteArray(bP1P2));

        byte bLc = commandApdu[OFFSET_LC];
        int lc = bLc & 0xff;
        TLog.i(TAG, "bLc " + lc);
        int tidLen = lc - 16;
        byte[] TERMINALID = new byte[tidLen];

        // Reader 로 부터 전달된 TRN ( Temporary Random Number ) 를 8 byte 를 할당 한다.
        if (lc > 0) {
            if (lc > (TRN_LENGTH)) {
                for (int i = 0; i < TRN_LENGTH; i++) {
                    bTRN[i] = commandApdu[OFFSET_CDATA + i];
                }
                TLog.i(TAG, "TRN from Reader" + Utils.hexStringFromByteArray(bTRN));
                for (int i = 0; i < tidLen; i++) {
                    TERMINALID[i] = commandApdu[OFFSET_CDATA + TRN_LENGTH + i];
                }
                TLog.i(TAG, "TERMINALID from Reader" + Utils.hexStringFromByteArray(TERMINALID));
            } else {
                for (int i = 0; i < TRN_LENGTH; i++) {
                    bTRN[i] = commandApdu[OFFSET_CDATA + i];
                }
                TLog.i(TAG, "TRN from Reader" + Utils.hexStringFromByteArray(bTRN));
                TERMINALID = Utils.stringToByte("NOTSUPPORTED");
            }
        }

        Boolean bresult = true;
        if (connectionHandler != null) {
            bresult = connectionHandler.onGetTerminalId(Utils.hexStringToString(Utils.hexStringFromByteArray(TERMINALID)));
        }

        if (bresult) {
            new Random().nextBytes(bHRN);    // Fill RANDOM BYTEs
            TLog.i(TAG, "HRN=" + Utils.hexStringFromByteArray(bHRN));
            /* 출입통제용 토클 암호화 처리
             * TK = AES192_CBC_ENC(TRN[16] || HRN[16], MK)
             * E_TOKEN = AES_GCM256_ENC(TOKEN[32] , TK)
             */

            try {
                if (encryptionMode == CRYPTO_MODE) {
                    // CBC IV 생성 ( 16 byte )
                    byte[] aCrypto_bCBCIV = jni.getf53TokenIVValue();
                    byte[] aCrypto_bGCMIV = null;

                    try {
                        aCrypto_bGCMIV = AesGcmCryptor.getAESIV();
                        TLog.i(TAG,"aCrypto_bGCMIV:" + Utils.hexStringFromByteArray(aCrypto_bGCMIV));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    byte[] f53MK_from = jni.getAes256MKValue();
                    TLog.i(TAG, "===AliceAesGcm.f53MK_from=" + Utils.hexStringFromByteArray(f53MK_from));

                    byte[] TK;
                    if(isThereOption) {
                        TK = AesGcmCryptor.encrypt(ConcatArrays(bTRN, bHRN), f53MK_from, aCrypto_bCBCIV, AesGcmCryptor.WorkType.CBC, gOption);
                    } else {
                        TK = AesGcmCryptor.encrypt(ConcatArrays(bTRN, bHRN), f53MK_from, aCrypto_bCBCIV, AesGcmCryptor.WorkType.CBC);
                    }
                    TLog.i(TAG, "TK=" + Utils.hexStringFromByteArray(TK));

                    byte[] E_TOKEN;
                    if(isThereOption) {
                        E_TOKEN = AesGcmCryptor.encrypt(TOKEN, TK, aCrypto_bGCMIV, AesGcmCryptor.WorkType.GCM, gOption);
                    } else {
                        E_TOKEN = AesGcmCryptor.encrypt(TOKEN, TK, aCrypto_bGCMIV, AesGcmCryptor.WorkType.GCM);
                    }
                    TLog.i(TAG, "E_TOKEN=" + Utils.hexStringFromByteArray(E_TOKEN));

                    byte[] bResponsePacket = ConcatArrays(bHRN, E_TOKEN);
                    TLog.i(TAG, "bResponsePacket=" + Utils.hexStringFromByteArray(bResponsePacket));

                    try {
                        responseApdu = new byte[HRN_LENGTH + E_TOKEN_LENGTH + SW1SW2_SUCCESS.length];

                        System.arraycopy(bResponsePacket, 0, responseApdu, 0, HRN_LENGTH + E_TOKEN_LENGTH);
                        System.arraycopy(SW1SW2_SUCCESS, 0, responseApdu, HRN_LENGTH + E_TOKEN_LENGTH, SW1SW2_SUCCESS.length);

                        bresult = true;
                        TLog.i(TAG, "1 bresult = true");
                        if (connectionHandler != null) {
                            TLog.i(TAG, "1 connectionHandler != null 0");
                            connectionHandler.onGetTokenCompleted(true);
                            TLog.i(TAG, "1 connectionHandler != null 1");
                            forceBreak = 1;
							
                            Intent intent = new Intent();
                            intent.setAction("tmobilepass.event");
                            intent.putExtra("TMobilepass", "Notice me senpai!");
                            context.sendBroadcast(intent);
                            TLog.i(TAG, "1 connectionHandler != null 2");
                        }

                        currentState = ProcessingState.GET_TOKEN;
                    } catch (Exception e2) {
                        TLog.i(TAG, "1.Exception e2=" + e2);
                        bresult = false;
                    }
                } else {
                    byte[] bResponsePacket = TOKEN;
                    TLog.i(TAG, "bResponsePacket=" + Utils.hexStringFromByteArray(bResponsePacket));

                    try {
                        responseApdu = new byte[TOKEN.length + SW1SW2_SUCCESS.length];
                        System.arraycopy(bResponsePacket, 0, responseApdu, 0, TOKEN.length);
                        System.arraycopy(SW1SW2_SUCCESS, 0, responseApdu, TOKEN.length, SW1SW2_SUCCESS.length);

                        bresult = true;
                        TLog.i(TAG, "2 bresult = true");
                        if (connectionHandler != null) {
                            TLog.i(TAG, "2 connectionHandler != null");
                            connectionHandler.onGetTokenCompleted(true);
                            forceBreak = 1;
                        }

                        currentState = ProcessingState.GET_TOKEN;
                    } catch (Exception e2) {
                        TLog.i(TAG, "2.Exception e2=" + e2);
                        bresult = false;
                    }
                }

            } catch (Exception e3) {
                TLog.i(TAG, "Exception e3=" + e3.toString());
                bresult = false;
            }
        }

        if (!bresult) {
            if (connectionHandler != null) {
                connectionHandler.onGetTokenCompleted(false);
                forceBreak = 1;
            }
            responseApdu = SW1SW2_AUTHENTICATION_ERROR;
            currentState = ProcessingState.WAIT;
        }

        return responseApdu;
    }

    /**
     * 암호화된 토큰을 통신 버퍼에 로드한다.
     */
    private void setCryptoTokenLoad() {
        TLog.i(TAG, "setCryptoTokenLoad()");

        LiberoPrefManager pref = LiberoPrefManager.getInstance(context);
        TLog.i(TAG, "setCryptoTokenLoad() pref.getTokenAliveOption()=" + pref.getTokenAliveOption());
        if (pref.getTokenAliveOption() == 0) {
            TLog.i(TAG, "setCryptoTokenLoad() pref.getTokenAliveOption()=" + pref.getTokenAliveOption());
            byte[] message = pref.getToken();

            setHceMessage(message);
        }
    }

    /**
     * 어플리케이션 아이디를 설정하고 통신 버퍼에 데이터를 로드한다.
     * @param message 버퍼에 로드할 데이터
     */
    private void setHceMessage(byte[] message) {
        TLog.i(TAG, "setHceMessage()");

        if (message != null) {
            nfcReader.addAidService();
        }
        bHceMessage = message;
    }

    /**
     * 토큰 유효시간 만료 콜백
     */
    private void setTimeout() {
        connectionHandler.onTokenTimeout();
    }

    /**
     * 어플리케이션 아이디를 제거한다.
     * @return
     */
    private boolean removeAidService() {
        AidService aidservice = new AidService();
        boolean result = aidservice.removeAids(context);
        TLog.i(TAG, "removeAidService() result=" + result);

        return result;
    }


    /**
     * 토큰 유효 시간 처리를 위한 쓰레드
     */
    class TimerThread extends Thread {
        @Override
        public void run() {
            int loop = 0;
            forceBreak = 0;
            while (true) {
                loop++;  // 작업스레드 값 증가.
                try {
                    Thread.sleep(100);
                    if (forceBreak != 0) {
                        setHceMessage(null);
                        break;
                    }

                    if (loop >= (alivetime / 100)) {
                        setTimeout();
                        setHceMessage(null);
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
