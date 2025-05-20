package com.example.app;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_SET_DEFAULT_DIALER = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        registerPhoneAccount();         // 전화 계정 등록
        requestSetDefaultDialer();      // 기본 전화 앱 요청

        // 기본 시작화면: 연락처
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new ContactsFragment())
                .commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.tab3);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
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
    }

    private void registerPhoneAccount() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager == null) return;

            ComponentName componentName = new ComponentName(this, MyConnectionService.class);
            PhoneAccountHandle handle = new PhoneAccountHandle(componentName, "my_call_account");

            PhoneAccount phoneAccount = PhoneAccount.builder(handle, "My Phone App")
                    .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                    .build();

            telecomManager.registerPhoneAccount(phoneAccount);
        }
        TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null) {
            try {
                List<PhoneAccountHandle> accounts = telecomManager.getCallCapablePhoneAccounts();
                if (accounts != null && !accounts.isEmpty()) {
                    for (PhoneAccountHandle handle : accounts) {
                        if (handle != null) {
                            PhoneAccount account = telecomManager.getPhoneAccount(handle);
                            if (account != null) {
                                Log.d("PhoneAccount", "✅ ID: " + handle.getId() + ", Label: " + account.getLabel());
                            } else {
                                Log.w("PhoneAccount", "⚠️ account is null for handle: " + handle.getId());
                            }
                        } else {
                            Log.w("PhoneAccount", "⚠️ handle is null");
                        }
                    }
                } else {
                    Log.w("PhoneAccount", "⚠️ No call-capable phone accounts found.");
                }
            } catch (Exception e) {
                Log.e("PhoneAccount", "❌ 예외 발생: " + e.getMessage(), e);
            }
        } else {
            Log.e("PhoneAccount", "❌ TelecomManager is null");
        }

    }




    private void requestSetDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "기본 전화 앱으로 설정되었습니다", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "기본 전화 앱 설정이 거부되었습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }



    // 커스텀 통화 객체
    public class MyConnection extends Connection {
        @Override
        public void onAnswer() {
            setActive();
        }

        @Override
        public void onDisconnect() {
            setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
            destroy();
        }
    }

}
