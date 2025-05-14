package com.example.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class EmergencyFragment extends Fragment {

    public EmergencyFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency, container, false);

        Button button112 = view.findViewById(R.id.button112);
        Button button119 = view.findViewById(R.id.button119);

        button112.setOnClickListener(v -> makeCall("112"));
        button119.setOnClickListener(v -> makeCall("119"));

        return view;
    }

    private void makeCall(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        if (requireActivity().checkSelfPermission(android.Manifest.permission.CALL_PHONE)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        } else {
            requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE}, 101);
        }
    }
}