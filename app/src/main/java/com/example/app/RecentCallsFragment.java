package com.example.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class RecentCallsFragment extends Fragment {

    private static final int PERMISSION_CALL_LOG = 100;
    private ListView callListView;
    private ArrayAdapter<String> callAdapter;
    private ArrayList<String> callLogList = new ArrayList<>();

    public RecentCallsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_calls, container, false);

        callListView = view.findViewById(R.id.callListView);
        callAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, callLogList);
        callListView.setAdapter(callAdapter);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSION_CALL_LOG);
        } else {
            loadCallLogs();
        }

        return view;
    }

    private void loadCallLogs() {
        callLogList.clear();

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
                String type = (callType == CallLog.Calls.INCOMING_TYPE) ? "수신" :
                        (callType == CallLog.Calls.OUTGOING_TYPE) ? "발신" :
                                (callType == CallLog.Calls.MISSED_TYPE) ? "부재중" : "기타";

                callLogList.add(type + ": " + number);
                count++;
            }
            cursor.close();
        }

        callAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CALL_LOG && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadCallLogs();
        }
    }
}
