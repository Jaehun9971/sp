package com.example.app;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import androidx.activity.OnBackPressedCallback;


public class ContactsFragment extends Fragment {

    private static final int PERMISSION_CONTACTS = 101;
    private ListView contactListView;
    private EditText nameInput, phoneInput;
    private Button saveBtn, addButton;
    private LinearLayout inputLayout;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> contactDisplayList = new ArrayList<>();

    public ContactsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        nameInput = view.findViewById(R.id.nameInput);
        phoneInput = view.findViewById(R.id.phoneInput);
        phoneInput.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        saveBtn = view.findViewById(R.id.saveBtn);
        addButton = view.findViewById(R.id.button);
        inputLayout = view.findViewById(R.id.inputLayout);
        contactListView = view.findViewById(R.id.contactListView);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, contactDisplayList);
        contactListView.setAdapter(adapter);

        addButton.setOnClickListener(v -> {
            if (inputLayout.getVisibility() == View.VISIBLE) {
                inputLayout.setVisibility(View.GONE);
                nameInput.setText("");
                phoneInput.setText("");
                addButton.setText("연락처 추가");
            } else {
                inputLayout.setVisibility(View.VISIBLE);
                addButton.setText("닫기");
            }
        });


        saveBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            if (!name.isEmpty() && !phone.isEmpty()) {
                addContact(name, phone);
                nameInput.setText("");
                phoneInput.setText("");
                inputLayout.setVisibility(View.GONE);
                addButton.setText("연락처 추가"); // 버튼 텍스트 초기화
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (inputLayout.getVisibility() == View.VISIBLE) {
                    // 입력창이 열려 있으면 닫기
                    inputLayout.setVisibility(View.GONE);
                    nameInput.setText("");
                    phoneInput.setText("");
                    addButton.setText("연락처 추가");
                } else {
                    // 입력창이 닫혀 있으면 원래 뒤로가기 동작 (앱 종료 or 이전 화면)
                    setEnabled(false); // 콜백 비활성화
                    requireActivity().onBackPressed(); // 시스템 기본 동작 위임
                }
            }
        });

        contactListView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedItem = contactDisplayList.get(position);
            String[] parts = selectedItem.split("\n");
            String oldName = parts[0];
            String oldPhone = parts.length > 1 ? parts[1] : "";

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("연락처 관리")
                    .setMessage("연락처를 수정 또는 삭제할 수 있습니다.")
                    .setPositiveButton("수정", (dialog, which) -> showEditDialog(oldName, oldPhone))
                    .setNegativeButton("삭제", (dialog, which) -> deleteContactByPhone(oldPhone))
                    .setNeutralButton("취소", null)
                    .show();
        });

        loadContacts();
        return view;
    }

    private void loadContacts() {
        contactDisplayList.clear();
        Cursor cursor = requireActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contactDisplayList.add(name + "\n" + phone);
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    private void addContact(String name, String phone) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        try {
            requireActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            loadContacts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteContactByPhone(String phoneNumber) {
        Cursor cursor = requireActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                new String[]{phoneNumber},
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            requireActivity().getContentResolver().delete(
                    ContactsContract.RawContacts.CONTENT_URI,
                    ContactsContract.RawContacts.CONTACT_ID + " = ?",
                    new String[]{contactId}
            );
            cursor.close();
            loadContacts();
        }
    }

    private void showEditDialog(String oldName, String oldPhone) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_contact, null);
        EditText editName = dialogView.findViewById(R.id.editName);
        EditText editPhone = dialogView.findViewById(R.id.editPhone);

        editName.setText(oldName);
        editPhone.setText(oldPhone);

        new AlertDialog.Builder(requireContext())
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

    private void updateContactByPhone(String oldPhone, String newName, String newPhone) {
        Cursor cursor = requireActivity().getContentResolver().query(
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
                requireActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                loadContacts();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
