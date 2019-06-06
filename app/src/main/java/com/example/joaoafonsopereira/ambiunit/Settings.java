package com.example.joaoafonsopereira.ambiunit;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.gatt.callback.BleNotifyCallback;

/**
 * Created by Asus on 31/03/2019.
 */

public class Settings extends Fragment {

    private DatabaseConnection db;
    private Button logout_btn, battery;
    private EditText crt_pass;
    private EditText new_pass;
    private EditText new_pass_conf;
    private Button change_pass_btn;
    private String username;
    private Button delete_account_btn;
    private Button clear_data;
    private TextView user;
    private TextView usern, email, meas;
    private BleManager manager;

    private final static String SERVICE_UUID = "68cd187c-94b9-4bdf-98f6-96f18f8e565f";
    private final static String BATTERY_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f9";



    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);

        db = new DatabaseConnection(Settings.this.getActivity());

        username = getActivity().getIntent().getStringExtra("username");

        user = view.findViewById(R.id.user);
        user.setText(db.getName(username));

        usern = view.findViewById(R.id.username);
        usern.setText("Username:     " + username);

        email = view.findViewById(R.id.email);
        email.setText("Email:     " + db.getEmail(username));

        meas = view.findViewById(R.id.measurements);
        meas.setText("Measurements made:     " + String.valueOf(db.getMeasurementsCount(username)));

        logout_btn = (Button) view.findViewById(R.id.logout_btn);


        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent login = new Intent(getActivity(), Enter.class);
                startActivity(login);

            }
        });


        battery = view.findViewById(R.id.battery_btn);
        battery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                manager = BleManager.getInstance(getContext());
                if (manager.getConnectedDevices().isEmpty()) {
                    Toast.makeText(getActivity(), "No device connected!", Toast.LENGTH_SHORT).show();
                } else {
                    final BleDevice connDevice = manager.getConnectedDevices().get(0);
                    manager.notify(connDevice, SERVICE_UUID, BATTERY_UUID, new BleNotifyCallback() {
                        @Override
                        public void onFail(int failCode, String info, BleDevice device) {
                            Toast.makeText(getActivity(), "Failed to receive battery level", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCharacteristicChanged(byte[] data, BleDevice device) {
                            String bat = new String(data);
                            Toast.makeText(getActivity(), "Battery level: " + bat, Toast.LENGTH_SHORT).show();
                            manager.cancelNotify(connDevice,SERVICE_UUID,BATTERY_UUID);
                        }

                        @Override
                        public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
                        }
                    });
                }
            }
        });

        crt_pass = (EditText) view.findViewById(R.id.crt_pass);
        new_pass = (EditText) view.findViewById(R.id.new_pass);
        new_pass_conf = (EditText) view.findViewById(R.id.new_pass_conf);

        change_pass_btn = (Button) view.findViewById(R.id.change_pass_btn);

        change_pass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String password = db.getPassword(username);

                String old = crt_pass.getText().toString().trim();
                String new_ = new_pass.getText().toString().trim();
                String new_2 = new_pass_conf.getText().toString().trim();

                if (!old.isEmpty() && !new_.isEmpty() && !new_2.isEmpty()) {

                    if (old.equals(password)) {

                        if (new_.equals(new_2)) {

                            int v = db.setPassword(username, new_);
                            if (v > 0) {
                                Toast.makeText(Settings.this.getActivity(), "Password changed", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Settings.this.getActivity(), "Error", Toast.LENGTH_SHORT).show();
                            }


                        } else {
                            Toast.makeText(Settings.this.getActivity(), "New pass must match", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(Settings.this.getActivity(), "Password incorrect", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(Settings.this.getActivity(), "Fill all", Toast.LENGTH_SHORT).show();
                }

            }
        });

        delete_account_btn = (Button) view.findViewById(R.id.delete_account_btn);
        delete_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete Account")
                        .setMessage("Are you sure?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Delete Account")
                                        .setMessage("Are you really sure?")
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                db.deleteAccount(username);
                                                Intent enter = new Intent(getActivity(), Enter.class);
                                                startActivity(enter);
                                            }})
                                        .setNegativeButton(android.R.string.no, null).show();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();

            }
        });

        clear_data = view.findViewById(R.id.clear_data_btn);
        clear_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(getActivity())
                        .setTitle("Clear Data")
                        .setMessage("Are you sure?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                new AlertDialog.Builder(getActivity())
                                        .setTitle("You will loose all data")
                                        .setMessage("Are you sure?")
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                db.clearData(username);
                                                Intent enter = new Intent(getActivity(), Enter.class);
                                                startActivity(enter);
                                            }})
                                        .setNegativeButton(android.R.string.no, null).show();

                            }})
                        .setNegativeButton(android.R.string.no, null).show();

            }
        });


        return view;
    }

}
