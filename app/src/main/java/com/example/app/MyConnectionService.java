package com.example.app;

import android.net.Uri;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

public class MyConnectionService extends ConnectionService {

    // 발신 통화 처리
    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount,
                                                 ConnectionRequest request) {

        // 전화번호 URI 받아오기
        Uri address = request.getAddress(); // 예: tel:01012345678

        // 커스텀 Connection 객체 생성
        MyConnection connection = new MyConnection();

        // 전화번호 및 상태 설정
        connection.setAddress(address, TelecomManager.PRESENTATION_ALLOWED);
        connection.setDialing(); // 발신 중
        connection.setInitialized();

        return connection;
    }

    // 수신 통화 처리
    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount,
                                                 ConnectionRequest request) {

        MyConnection connection = new MyConnection();
        connection.setRinging(); // 수신 중 (벨 울림 상태)
        return connection;
    }
}
