package com.example.app;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.DisconnectCause;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telecom.Connection;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.graphics.drawable.Icon;
import android.content.ComponentName;
import android.Manifest;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private void registerPhoneAccount() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);

            ComponentName componentName = new ComponentName(this, MyConnectionService.class);

            PhoneAccountHandle handle = new PhoneAccountHandle(componentName, "my_call_account");

            PhoneAccount phoneAccount = PhoneAccount.builder(handle, "My Phone App")
                    .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                    .build();

            telecomManager.registerPhoneAccount(phoneAccount);
        }
    }


    private static final int REQUEST_CODE_SET_DEFAULT_DIALER = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        registerPhoneAccount(); // ✅ 등록 요청 추가
        requestSetDefaultDialer(); // 기본 전화 앱 요청


        //기본 시작화면을 연락처로 설정
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new ContactsFragment())
                .commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.tab3);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.tab1) {
                selectedFragment = new EmergencyFragment();
            } else if (id == R.id.tab2) {
                selectedFragment = new RecentCallsFragment();
            } else if (id == R.id.tab3) {
                selectedFragment = new ContactsFragment();
            } else if (id == R.id.tab4) {
                selectedFragment = new KeypadFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });

        // 기본 전화 앱 설정 요청
        requestSetDefaultDialer();
    }

    private void requestSetDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER);
            }
        }
    }

    // 요청 결과 확인
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            if (resultCode == RESULT_OK) {
                // 기본 전화 앱 설정 성공
            } else {
                // 설정 거부됨
            }
        }
    }

    public class MyConnection extends Connection {

        @Override
        public void onAnswer() {
            // 수신 통화 수락
            setActive(); // 통화 상태 활성화
        }

        @Override
        public void onDisconnect() {
            // 통화 종료
            setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));

            destroy();
        }
    }
}