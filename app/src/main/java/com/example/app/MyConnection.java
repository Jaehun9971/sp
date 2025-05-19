package com.example.app;

import android.telecom.Connection;
import android.telecom.DisconnectCause;

public class MyConnection extends Connection {

    @Override
    public void onAnswer() {
        // 수신 통화 수락 시
        setActive(); // 통화 상태 전환
    }

    @Override
    public void onDisconnect() {
        // 사용자가 통화 종료 시
        setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        destroy(); // 연결 종료
    }
}
