package com.example.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private List<Contact> contactList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, phoneText;

        public ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(android.R.id.text1);
            phoneText = itemView.findViewById(android.R.id.text2);
        }
    }

    public ContactAdapter(List<Contact> contacts) {
        this.contactList = contacts;
    }

    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactAdapter.ViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.nameText.setText(contact.getName());
        holder.phoneText.setText(contact.getPhone());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void updateData(List<Contact> newList) {
        contactList.clear();
        contactList.addAll(newList);
        notifyDataSetChanged();
    }
}
