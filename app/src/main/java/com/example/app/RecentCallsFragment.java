package com.example.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Locale;

public class RecentCallsFragment extends Fragment {

    private static final int PERMISSION_CALL_LOG = 100;
    private static final int PERMISSION_CALL_PHONE = 101;

    private ListView callListView;
    private ArrayAdapter<String> callAdapter;
    private ArrayList<String> callLogList = new ArrayList<>();
    private ArrayList<String> phoneNumberList = new ArrayList<>(); // 통화용 실제 번호 리스트

    public RecentCallsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_calls, container, false);

        callListView = view.findViewById(R.id.callListView);
        callAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, callLogList);
        callListView.setAdapter(callAdapter);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS
            }, PERMISSION_CALL_LOG);
        } else {
            loadCallLogs();
        }

        // 리스트 클릭 시 통화
        callListView.setOnItemClickListener((parent, view1, position, id) -> {
            String phoneNumber = phoneNumberList.get(position); // 실제 번호 사용
            makePhoneCall(phoneNumber);
        });

        return view;
    }

    private void loadCallLogs() {
        callLogList.clear();
        phoneNumberList.clear();

        Cursor cursor = requireContext().getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null, null, null,
                CallLog.Calls.DATE + " DESC"
        );

        if (cursor != null) {
            int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
            int count = 0;

            while (cursor.moveToNext() && count < 20) {
                String number = cursor.getString(numberIndex);
                int callType = cursor.getInt(typeIndex);

                // 번호 하이픈 삽입
                String formattedNumber = PhoneNumberUtils.formatNumber(number, Locale.getDefault().getCountry());

                // 연락처 이름 조회
                String displayName = getContactName(formattedNumber);
                if (displayName == null) {
                    displayName = formattedNumber;
                }

                String type = (callType == CallLog.Calls.INCOMING_TYPE) ? "수신" :
                        (callType == CallLog.Calls.OUTGOING_TYPE) ? "발신" :
                                (callType == CallLog.Calls.MISSED_TYPE) ? "부재중" : "기타";

                callLogList.add(type + ": " + displayName);
                phoneNumberList.add(number); // 통화용 실제 번호 저장
                count++;
            }
            cursor.close();
        }

        callAdapter.notifyDataSetChanged();
    }

    private String getContactName(String phoneNumber) {
        Cursor cursor = requireContext().getContentResolver().query(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
                        .appendPath(phoneNumber)
                        .build(),
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
                null, null, null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(0);
                cursor.close();
                return name;
            }
            cursor.close();
        }
        return null;
    }

    private void makePhoneCall(String number) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CALL_PHONE);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CALL_LOG) {
            boolean grantedAll = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    grantedAll = false;
                    break;
                }
            }
            if (grantedAll) {
                loadCallLogs();
            }
        } else if (requestCode == PERMISSION_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 이후 클릭 시 통화 가능
            }
        }
    }
}
