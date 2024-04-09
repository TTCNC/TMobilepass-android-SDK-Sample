# TMobilepass Android SDK


## 1. 개발 및 지원 환경
 Android Studio version : Giraffe(2022.3.1 Patch 3)

 Android : 5.0 ( minSDK : 23 / targetSDK : 33 )

 라이브러리 파일명 : tmobilepasslib-v1.08-20240409.aar

 리더기 : TMR300 ( 암호화 , 평문 지원 ) , CCAL100TX ( 암호화 , 평문 지원 ) ,  NC400/TMPP ( 평문지원 ) , NC900

-------------------------------------------------------------------------------------------------------------------

## 2. 기능 개요

 ### NFC
 - [ ]  안드로이드 NFC 기능을 지원하고 있는  단말기
 - [ ]  HCE 기능을 제공하고 있기 때문에 NFC CARD MODE 로 동작하는 것을 추천함.
 - [ ]  NFC 인식 범위 : 스마트폰의 안테나를 TMobilepass  리더기 안테나 상단에 터치 하면 동작하며 인식거리는 20 미리 이상 , 평균 35 미리 성능
 - [ ]  AES256 GCM 방식의 암복호화 방식을 사용하고 있으며 3-Pass Authentication Process  로직을 적용하여 보안성이 높음

 ### BLE
 - [ ]  안드로이드 BLE 기능을 지원하고 있는 지원환경 범위 이상의 단말기
 - [ ]  안드로이드 BLE 기능을 제공하고 있으며 , 단말기의 성능에 따라서 30미리 ~ 100미리 범위에서 인식함.
 - [ ]  TDES방식의 암복호화 방식을 사용하고 있으며 3-Pass Authentication Process  로직을 적용하여 보안성이 높음
 
-------------------------------------------------------------------------------------------------------------------
 
## 4. Features offered

 ### 1) 크리덴셜 암호화 데이타 전송 
 - [ ]  AES256/GCM , CBC 암복호화 알고리즘 지원
 - [ ]  16 Byte 크리덴셜 데이타 지원
 - [ ]  3-Pass Authentication 알고리즘 지원
 - setCryptoTocken
 - activeCryptoTocken
 - pauseCryptoTocken
 - setClearCryptoToken

 ### 2) 크리덴셜 평문 데이타 전송
 - [ ]  256 Byte 데이타 전송
 - setTocken
 - activeTocken
 - pauseTocken 
 - setClearToken

 ### 3) 안드로이드 NFC 에 크리덴셜 키를 등록 하여 사용하는 Always-On 기능

 ### 4) (v1.08)크리덴셜 + option 설정을 통한 서비스 구분 기능
 - [ ]  크리덴셜 키를 등록하면 , 어플이 종료 되거나 스마트폰이 리부팅 되어도 등록된 크리덴셜 키를 계속 서비스 하는 기능 제공
 - setCryptoTocken(sTokenValue,option,alivetime,isEncrypted); // alivetime = 0; ( UNLIMITED_ALIVE_TIME )
 - setCryptoTocken(sTokenValue,alivetime,isEncrypted); // alivetime = 0; ( UNLIMITED_ALIVE_TIME )

 ### 5) 어플리케이션 NFC 에 크리덴셜 키를 등록 하여 사용하는 어플리케이션이 살아 있는 동안 기능
 - [ ]  크리덴셜 키를 등록하면 , 어플이 종료 되기 전까지 크리덴셜 키를 계속 서비스 제공

 ### 6) 안드로이드 NFC 에 AID 등록하고 제거 기능을 제공하는 Security-On 기능
 - [ ]  20 ~ 60 초 사이의 시간에 NFC AID 와 크리덴셜 키 값을 활성화 하여 서비스 제공
 - [ ]  서비스 제공 시간 이외에는 해당 AID 서비스가 스마트폰에서는 노출되지 않음
 - setCryptoTocken(sTokenValue,alivetime,isEncrypted); // alivetime = MAX_ALIVE_TIME; (  20 <= iTimeout <= 60 )

 ### 7) 서버와의 통신 체널에도 암호화 데이타 서비스
 - [ ]  getServerAes256Cbc() 해당 암호화 로직을 서버에서 구현하여 적용
 - [ ]  서버 암복호화 모듈에 getServerAes256Cbc() 함수에 해당하는 Java Native API ( JNI ) 로 구현된 로직 사용
 
 ### 8) NFC 크리덴셜 키 중지 서비스
 -  [ ] 특정 단말기의 상태에 따라서  크리덴셜 서비스 중지 기능 제공
 -  [ ] onGetTerminalId(String sTernimalId) sTerminalId 값을 판단할 수 있으며 조건에 따라 리턴값으로 서비스를 종료 할 수 있음.
    = sTerminalId 값은 "," 로 단말기의 TID , 현재 리더기의 시간 "YYYY-MM-DD HH:MI:SS" , 정상/오류 메시지 로 구성된다.
 -  [ ] onGetTerminalId 리턴 값이 true 이면 서비스 계속 , false 이면 서비스 정지
 
 ### 9) 리더기 지정 기능
 -  [ ] 사용하려고 하는 리더기의 모델명칭을 지정한다.
 -  [ ] apduManager = DccApduManager.getInstance(FullscreenMainActivity.this,  ReaderModel );
 -  [ ] ReaderModel.NC400LST 또는 ReaderModel.CCAL100TX  또는 ReaderModel.TMR300
 -  [ ] 단말기 지정이 잘못 된 경우 기능 동작이 정상적으로 되지 않음
 
 ### 10) Management Mode
 -  [ ] setNormalMode()  // 운영모드
 -  [ ] setManagementMode() // 설정모드
 -  [ ] getMode()  // 현재 모드
 
-------------------------------------------------------------------------------------------------------------------
 
## 5. Functions

### setCryptoToken(String sTokenValue, byte option, int alivetime, boolean isEncrypted)

	 __brief__
     토큰값을 적재한다. 안드로이드는 alivetime 옵션에 따라 토큰값의 유효 시간을 세 가지 모드 중 선택하여 사용할 수 있다. 
		예시)
			alivetime = 0; //NFC_ACTIVE_UNLIMITED_ALIVE_TIME
			alivetime = -1; //NFC_ACTIVE_APPLICATION_ALIVE_TIME
			alivetime = 60; //NFC_ACTIVE_MAX_ALIVE_TIME
	 __param__
			String sTokenValue : 토큰 값
			byte option : 옵션 데이터
			int alivetime : 토큰값의 유효 시간
			boolean isEncrypted : 토큰값의 암호화 여부 (true: 이미 암호화 된 토큰값 , false: 평문으로 된 토큰값)
	 __retval__
			none
			
### activeCryptoToken()

	 **brief**
		none
		
	 **param**
		none
 
	 **retval**
		none

-------------------------------------------------------------------------------------------------------------------

## 6. Example

### NFC SDK 사전 작업

 - 라이브러리 .aar 파일을 프로젝트 파일의 라이브러리 경로에 넣고, build.gradle의 dependencies에 추가합니다.
		dependencies {
			 implementation fileTree(include: ['*.aar'], dir: 'libs')
			 // TMobilePass Library Load
			 implementation files('libs/tmobilepasslib-v1.08-20240409.aar')
		}

 - AndroidManifest.xml 에 아래와 같은 permission을 추가합니다.
	 // FOR USE NFC
     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

     // FOR SAMPLE APPLICATION VIBRATION
     <uses-permission android:name="android.permission.VIBRATE"/>
	 	 

 - 프로젝트에 아래와 같이 import를 추가합니다.
	 import android.nfc.NfcAdapter;
	 
	 import kr.co.ttcnc.ucsdk.dccctrllib.nfc.DccApduManager;
	 import kr.co.ttcnc.ucsdk.dccctrllib.nfc.IDccConnectionHandler;
	 import kr.co.ttcnc.ucsdk.dccctrllib.nfc.ReaderModel;
	 
 - Activity 함수에 NFC APDU 명령어 처리기를 추가합니다.
	 // NFC APDU 명령어 처리기
	 DccApduManager apduManager;

 - 
	 
	
		내용




-------------------------------------------------------------------------------------------------------------------



     [NFC SDK 사전 작업]


     [NFC SDK 제공 함수]
     
     /*
     * Start Service DccHostApudService and
     * NFC Handler Instance
     */
     apduManager = DccApduManager.getInstance(FullscreenMainActivity.this , ReaderModel.TMR300);

	 // SET LABEL
     apduManager.setApplicationLabel("TMOBILEPASS_CARD");
	 
     // NFC Event Hanlder
     apduManager.setDccApduManagerHandler(mConnectionHandler);

     // Encryped Token
     TokenEncryped = apduManager.getServerAes256Cbc(hexStringFromByteArray(sTokenValue.getBytes()).getBytes());

     // Token Set and Start CardService
     // iTimeout = UNLIMITED_ALIVE_TIME 로 설정되면
     // NFC 서비스에 토큰이 계속해서 로딩되어 프로그램이 종료 되었거나
     // 핸드폰이 재시작 되었어도 
     // 계속해서 토큰 서비스를 제공함.
     // iTimeout 값을 20 이상 , 60 이하의 값으로 지정하면
     // 지정된 시간동안에만 토큰 서비스를 제공함. ( 보안을 높이기 위함 )
     // Option 값은 리더기에 해당 옵션 값을 전달한다. ( Application Active 중일 때에만 유지 되는 값으로 프로그램이 종료 되면 유효하지 않다. 
     apduManager.setCryptoTocken(TokenEncryped , Option , iTimeout);

     [NFC 옵션 기능]
     1)  등록한 토큰을 활성화 한다.  토큰을 등록하면 자동으로 활성화 되므로 활성화 API 를 별도 호출하지 않음
     apduManager.activeCryptoTocken();
     
     2)  등록한 토큰을 비활성화 한다. 즉 토큰이 제출되지 않는다.
     apduManager.pauseCryptoTocken();
     
     3)  등록된 토큰을 비활성화 하고 삭제 한다.. 토큰이 제출되지 않는다.
     apduManager.setClearCryptoToken();

     4)  IDccConnectionHandler Interface위 구현
     
        %%%  onGetTerminalId() 함수의 리턴값은 true 로 해야 NFC 통신으로 데이터가 전송됨.
	리턴값을 false 로 하면 NFC 통신을 중단하고 FAILURE로 종료됨
        @Override
        public boolean onGetTerminalId(String sTernimalId) {
            Log.i("DCCSDK" ,"DCCSDK onGetTockenCompleted sTerminalId=" + sTernimalId);
            return true;
        }
	        @Override
        public void onGetTockenCompleted(boolean result) {
            if( result )
                Log.i("DCCSDK" ,"DCCSDK onGetTockenCompleted SUCCESS");
            else
                Log.i("DCCSDK" ,"DCCSDK onGetTockenCompleted FAILURE");

            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // 1초 진동
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
            mbuttonNfcScan.setEnabled(true);
        }

      
  2) BLE 지원
     - 안드로이드 BLE 기능을 지원하고 있는 지원환경 범위 이상의 단말기
     - 안드로이드 BLE 기능을 제공하고 있으며 , 단말기의 성능에 따라서 30미리 ~ 100미리 범위에서 인식함.
     - TDES방식의 암복호화 방식을 사용하고 있으며 3-Pass Authentication Process  로직을 적용하여 보안성이 높음

       
 [BLE SDK 사전 작업]
     // TMobilePass Library Load
     implementation files('libs/tmobilepasslib-v1.08-20210610.aar')

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    
    // FOR SAMPLE APPLICATION VIBRATION
    <uses-permission android:name="android.permission.VIBRATE"/>

    
 [BLE SDK 제공 함수]

    /*
     * TMobilePass BLE Crypto Service Implementation START
     */
    /** BLE BluetoothAdapter */
    private BluetoothAdapter mBluetoothAdapter;
    /** BLE Scanner */
    BluetoothLeScanner btScanner;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 5000;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    /*
     * TMobilePass BLE Crypto Service Implementation END
     */

    1) BLE BluetoothAdapter 생성

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

     2) Filter 적용
        /*
         * Fileter For TBleMobilePass Reader BLE Device
         */
	filters= new ArrayList<>();
	ScanFilter scan_filter= new ScanFilter.Builder()
		.setServiceUuid( new ParcelUuid( UUID_TDCS_SERVICE ) )
		.build();
	filters.add( scan_filter );

     3) BLE Scan ( Button ) 함수 호출   bleCryptoStart();()
        SCAN_PERIOD 이 후 stopScanning() 호출

     4) LeScanCallback 구현
	TMobilepass Reader 기기의 BLE Device 가 검색하여 확인 리더기인 경우 BLE 연결과 메시지 전송을 진행합니다.
	BluetoothUtils.checkTMobilepassDevice(rssi , -45 , devicename )  함수의 
		"guideRssi" 값은 출입통제인 경우 -45 값을 추천하며 , 패스 쓰루 서비스 인경우 -70 을 추천합니다.
	if( BluetoothUtils.checkTMobilepassDevice(rssi , -45 , devicename ) )
            {
                // BLE SCAN STOP
                stopScanning();

		// TRN VALUE 할당
                bTrnValue = BluetoothUtils.getTrnValue(result);    // bTRNVALUE
                Log.d(TAG, "sTokenValue" + sTokenValue);


		// BLE 연결
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connectDevice(result.getDevice());
                    }

                }, 1);
            }
	

     5) GattClientActionListener 를 implement 하여야 한다.
         cryptoTokenProcessStart() 와 cryptoTokenProcessEnd() 함수의 내부 구현 로직은 변경하면 안됩니다.
			
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
		cryptoProcessInstance.putToken( sTokenValue.getBytes()  , bTrnValue);

		CryptoProcess.bleWriteMessages( mGatt );
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
		/*
		 * BLE Connected , Create CryptoToken and Transfer End
		 */
		if( !CryptoProcess.bleWriteMessages( mGatt ))
		{
		    disconnectGattServer();
		}
	    }



  
     - 
