package com.example.app;

import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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
    }
}
