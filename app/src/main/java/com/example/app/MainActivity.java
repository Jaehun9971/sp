package com.example.app;

<<<<<<< HEAD
import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText nameInput, phoneInput;
    Button saveBtn, addButton;
    ListView contactListView;
    ArrayAdapter<String> adapter;
    ArrayList<String> contactDisplayList = new ArrayList<>();

    final int PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML에서 정의한 ID에 맞게 초기화
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        saveBtn = findViewById(R.id.saveBtn);
        addButton = findViewById(R.id.button);
        contactListView = findViewById(R.id.contactListView);

        // 전화번호 자동 하이픈
        phoneInput.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        // 연락처 리스트 표시용 어댑터
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactDisplayList);
        contactListView.setAdapter(adapter);

        // 퍼미션 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                    PERMISSION_REQUEST);
        } else {
            loadContacts();
        }

        // 연락처 추가 버튼 클릭 시 입력 폼 보여주기
        addButton.setOnClickListener(v -> {
            findViewById(R.id.nameInput).setVisibility(View.VISIBLE);
            findViewById(R.id.phoneInput).setVisibility(View.VISIBLE);
            saveBtn.setVisibility(View.VISIBLE);
        });

        // 저장 버튼 클릭 시 연락처 저장
        saveBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (!name.isEmpty() && !phone.isEmpty()) {
                addContact(name, phone);
                nameInput.setText("");
                phoneInput.setText("");
                nameInput.setVisibility(View.GONE);
                phoneInput.setVisibility(View.GONE);
                saveBtn.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "이름과 전화번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 이름 입력 후 엔터 → 전화번호 입력으로 이동
        nameInput.setOnEditorActionListener((v, actionId, event) -> {
            phoneInput.requestFocus();
            return true;
        });
    }

    // 연락처 추가 메소드
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
            Toast.makeText(this, "연락처가 추가되었습니다.", Toast.LENGTH_SHORT).show();
            loadContacts(); // 추가 후 목록 새로고침
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "연락처 추가 중 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }

    // 연락처 불러오기
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
                contactDisplayList.add(name + " - " + phone);
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    // 권한 처리 결과
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(this, "연락처 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
=======
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
>>>>>>> bf6fddbe55fc9dd0c9f324af91f1332fa7412d47
