package com.example.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class KeypadFragment extends Fragment {

    private EditText phoneInputField;
    private static final int PERMISSION_WRITE_CONTACTS = 100;

    private final int[] buttonIds = {
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnStar, R.id.btnSharp
    };

    private final String[] buttonValues = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "#"
    };

    public KeypadFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_keypad, container, false);
        phoneInputField = view.findViewById(R.id.phoneInputField);

        // 숫자 입력
        for (int i = 0; i < buttonIds.length; i++) {
            Button btn = view.findViewById(buttonIds[i]);
            String value = buttonValues[i];

            btn.setOnClickListener(v -> {
                String current = phoneInputField.getText().toString().replaceAll("[^0-9*#]", "");
                String newText = current + value;
                phoneInputField.setText(PhoneNumberUtils.formatNumber(newText, "KR"));
            });
        }

        // 삭제
        Button deleteBtn = view.findViewById(R.id.btnDelete);
        deleteBtn.setOnClickListener(v -> {
            String current = phoneInputField.getText().toString().replaceAll("[^0-9*#]", "");
            if (!current.isEmpty()) {
                String newText = current.substring(0, current.length() - 1);
                phoneInputField.setText(PhoneNumberUtils.formatNumber(newText, "KR"));
            }
        });

        // 통화 (기능 없음)
        Button callBtn = view.findViewById(R.id.btnCall);
        callBtn.setOnClickListener(v -> {
            // 여기에 ACTION_CALL 등 구현 가능
        });

        // 연락처 추가
        Button addContactBtn = view.findViewById(R.id.btnAddContact);
        addContactBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        PERMISSION_WRITE_CONTACTS);
            } else {
                showAddContactDialog();
            }
        });

        return view;
    }

    // 이름 입력 다이얼로그 후 연락처 저장
    private void showAddContactDialog() {
        Context context = getContext();
        if (context == null) return;

        EditText nameInput = new EditText(context);
        nameInput.setHint("이름을 입력하세요");

        new AlertDialog.Builder(context)
                .setTitle("연락처 추가")
                .setView(nameInput)
                .setPositiveButton("저장", (dialog, which) -> {
                    String name = nameInput.getText().toString();
                    String number = phoneInputField.getText().toString().trim(); // 하이픈 유지
                    if (!name.isEmpty() && !number.isEmpty()) {
                        saveContact(context, name, number);

                }

    })
                .setNegativeButton("취소", null)
                .show();
    }

    // 실제 저장 작업
    private void saveContact(Context context, String name, String phone) {
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
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
