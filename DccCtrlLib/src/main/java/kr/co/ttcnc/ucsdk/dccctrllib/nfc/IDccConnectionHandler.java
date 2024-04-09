/*
 * IDccConnectionHandler.java
 * This file is part of UsbController
 *
 * Copyright (C) 2012 - Manuel Di Cerbo
 *
 * UsbController is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * UsbController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UsbController. If not, see <http://www.gnu.org/licenses/>.
 */
package kr.co.ttcnc.ucsdk.dccctrllib.nfc;

/**
 * (c) Neuxs-Computing GmbH Switzerland
 * Apdu 연결 핸들러
 * @author Manuel Di Cerbo, 02.02.2012
 */
public interface IDccConnectionHandler {

    /**
     * DccHostApduService Instance Start
     */
    void onStartCommand();

    /**
     *
     * @param token
     * @param jsonString
     */
    void onSetTokenCompleted(String token, String jsonString);

    /**
     * Terminal ID 값을 확인하고 true를 리턴하면 , Token 을 전송한다.
     * @param sTerminalId
     * @return
     */
    default boolean onGetTerminalId(String sTerminalId)
    {
        return true;
    }

    /**
     * 리더기의 요청의 사용자 토큰을 암호화 하여 회신
     * @param result
     */
    void onGetTokenCompleted(boolean result);

    /**
     * 리더기의 요청으로 사용자의 정보 전송 ( 전화번호 )
     * @param commandApdu
     */
    void onGetData(byte[] commandApdu);

    /**
     * Application Select
     * @param commandApdu
     */
    void onSelectFile(byte[] commandApdu);

    /**
     * Token Alive Time is out , Token Time out
     */
    void onTokenTimeout();

    /**
     *
     * @param commandApdu
     * @param responseApdu
     */
    void onError(byte[] commandApdu, byte[] responseApdu);

    /**
     *
     */
    void onDeactivated();
}
