package kr.co.ttcnc.ucsdk.dccctrllib.nfc;

/**
 * Created by parkjeongho on 2024-01-08 오후 8:43
 * APDU 처리 상태 표시 상수
 */
public enum ProcessingState {
    WAIT, // 대기 상태
    SELECT_APPLICATION, // 어플리케이션 선택 상태
    GET_DATA, // CC 파일 선택 상태
    PUT_TOKEN, // NDEF 레코드 선택 상태
    GET_TOKEN, // NDEF 레코드 업데이트 상태
}
