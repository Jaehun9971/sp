package com.example.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView txtList;
    LinearLayout emergencyLayout;
    Button button112, button119;

    EditText nameInput, phoneInput;
    Button saveBtn, addButton;
    ListView contactListView;
    LinearLayout inputLayout;
    ArrayAdapter<String> adapter;
    ArrayList<String> contactDisplayList = new ArrayList<>();
    private static final int PERMISSION_CALL_LOG = 100;

    final int PERMISSION_REQUEST = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emergencyLayout = findViewById(R.id.emergencyLayout);
        button112 = findViewById(R.id.button112);
        button119 = findViewById(R.id.button119);

        txtList = findViewById(R.id.txtList);
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        saveBtn = findViewById(R.id.saveBtn);
        addButton = findViewById(R.id.button);
        contactListView = findViewById(R.id.contactListView);
        inputLayout = findViewById(R.id.inputLayout);

        phoneInput.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactDisplayList);
        contactListView.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                    PERMISSION_REQUEST);
        } else {
            loadContacts();
        }

        addButton.setOnClickListener(v -> inputLayout.setVisibility(View.VISIBLE));

        saveBtn.setOnClickListener(v -> {
            button112.setOnClickListener(view -> makeCall("112"));
            button119.setOnClickListener(view -> makeCall("119"));
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (!name.isEmpty() && !phone.isEmpty()) {
                addContact(name, phone);
                nameInput.setText("");
                phoneInput.setText("");
                inputLayout.setVisibility(View.GONE);
            }
        });

        nameInput.setOnEditorActionListener((v, actionId, event) -> {
            phoneInput.requestFocus();
            return true;
        });

        //하단탭 버튼

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.tab1) {
                // 긴급전화 레이아웃만 보이기
                emergencyLayout.setVisibility(View.VISIBLE);
                contactListView.setVisibility(View.GONE);
                inputLayout.setVisibility(View.GONE);
                addButton.setVisibility(View.GONE); // 연락처 추가 버튼 숨김
                txtList.setVisibility(View.GONE);

                return true;

            } else if (id == R.id.tab2) {
                // 통화기록 보기
                contactListView.setVisibility(View.VISIBLE);
                inputLayout.setVisibility(View.GONE);
                emergencyLayout.setVisibility(View.GONE);
                addButton.setVisibility(View.GONE); // 숨김
                txtList.setVisibility(View.GONE);

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CALL_LOG},
                            PERMISSION_CALL_LOG);
                } else {
                    loadCallLogs();  // 권한이 있으면 바로 통화기록 로드
                }
                return true;
            } else if (id == R.id.tab3) {
                // 연락처 목록 보기
                contactListView.setVisibility(View.VISIBLE);
                inputLayout.setVisibility(View.GONE);
                emergencyLayout.setVisibility(View.GONE);
                addButton.setVisibility(View.VISIBLE); // ✅ 보여줌
                txtList.setVisibility(View.VISIBLE);

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                            PERMISSION_REQUEST);
                } else {
                    loadContacts();
                    contactListView.setAdapter(adapter);
                }
                return true;
            } else if (id == R.id.tab4) {
                contactListView.setVisibility(View.GONE);
                inputLayout.setVisibility(View.GONE);
                emergencyLayout.setVisibility(View.GONE);
                addButton.setVisibility(View.GONE); // 숨김
                txtList.setVisibility(View.GONE);

                return true;
            }
            return false;
        });

        contactListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = contactDisplayList.get(position);
            String oldPhone = selectedItem.split("\n")[1];

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("연락처 관리")
                    .setMessage("연락처를 수정 또는 삭제할 수 있습니다.")
                    .setPositiveButton("수정", (dialog, which) -> showEditDialog(position, selectedItem))
                    .setNegativeButton("삭제", (dialog, which) -> deleteContactByPhone(oldPhone))
                    .setNeutralButton("취소", null)
                    .show();
        });
    }

    @Override
    public void onBackPressed() {
        if (inputLayout.getVisibility() == View.VISIBLE) {
            inputLayout.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void addContact(String name, String phone) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            loadContacts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteContactByPhone(String phoneNumber) {
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                new String[]{phoneNumber},
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            getContentResolver().delete(
                    ContactsContract.RawContacts.CONTENT_URI,
                    ContactsContract.RawContacts.CONTACT_ID + " = ?",
                    new String[]{contactId}
            );
            cursor.close();
            loadContacts();
        }
    }

    private void updateContactByPhone(String oldPhone, String newName, String newPhone) {
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                new String[]{oldPhone},
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            cursor.close();

            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            ContactsContract.Data.CONTACT_ID + "=? AND " +
                                    ContactsContract.Data.MIMETYPE + "=?",
                            new String[]{contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newName)
                    .build());

            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            ContactsContract.Data.CONTACT_ID + "=? AND " +
                                    ContactsContract.Data.MIMETYPE + "=?",
                            new String[]{contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newPhone)
                    .build());

            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                loadContacts();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showEditDialog(int position, String original) {
        String[] parts = original.split("\n");
        String oldName = parts[0];
        String oldPhone = parts.length > 1 ? parts[1] : "";

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_contact, null);
        EditText editName = dialogView.findViewById(R.id.editName);
        EditText editPhone = dialogView.findViewById(R.id.editPhone);

        editName.setText(oldName);
        editPhone.setText(oldPhone);

        new AlertDialog.Builder(this)
                .setTitle("연락처 수정")
                .setView(dialogView)
                .setPositiveButton("저장", (dialog, which) -> {
                    String newName = editName.getText().toString().trim();
                    String newPhone = editPhone.getText().toString().trim();
                    if (!newName.isEmpty() && !newPhone.isEmpty()) {
                        updateContactByPhone(oldPhone, newName, newPhone);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void loadContacts() {
        contactDisplayList.clear();

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contactDisplayList.add(name + "\n" + phone);
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALL_LOG) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadCallLogs();
            }
        } else if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            }
        }
    }
    private void loadCallLogs() {
        ArrayList<String> callLogList = new ArrayList<>();

        Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null, null, null,
                CallLog.Calls.DATE + " DESC"
        );

        if (cursor != null) {
            int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
            int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);

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

        ArrayAdapter<String> callAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, callLogList);
        contactListView.setAdapter(callAdapter);
    }
    private void makeCall(String number) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, 101);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        }
    }

}




