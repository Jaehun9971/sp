package com.example.app;

import static androidx.core.content.ContextCompat.getSystemService;



import android.Manifest;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
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

    private static final int REQUEST_CODE_SET_DEFAULT_DIALER = 123;
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
        // 통화
        Button callBtn = view.findViewById(R.id.btnCall);
        callBtn.setOnClickListener(v -> {
            String number = phoneInputField.getText().toString().trim();
            if (!number.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + number));

                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.CALL_PHONE}, 200);
                } else {
                    startActivity(intent);
                }
            }
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

    private void requestSetDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) requireContext().getSystemService(RoleManager.class);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                    startActivityForResult(intent, 123); // 또는 상수로 대체
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) requireContext().getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, requireContext().getPackageName());
                startActivityForResult(intent, 123);
            }
        }
    }

}
