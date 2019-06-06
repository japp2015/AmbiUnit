package com.example.joaoafonsopereira.ambiunit;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.Fragment;

import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.gatt.callback.BleNotifyCallback;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Asus on 31/03/2019.
 */

public class Sensor extends Fragment {

    private static final String TAG = "Sensor";

    DatabaseConnection db;
    String username;
    private Button check_environment;
    private Switch bswitch;
    private Button unit_btn;
    private boolean unit_ppm = true;
    private TextView mean_txt;
    private TextView battery_value, ax_value, d4_value, mics_value, temperature_value, humidity_value, mean_value, cohb_value;
    private Button startRecord;
    private Button stopRecord;
    private double measurement_mgm3;
    private double measurement_ppm;
    private final double CONVERSION_FACTOR = 28.01 / 22.41;
    private static DecimalFormat round = new DecimalFormat(".##");
    private boolean recording = false;
    private Handler handler;
    private int recording_delay;
    private BleManager manager;
    private BleDevice connDevice;
    private RadioButton mgm3, ppm;

    private final static String SERVICE_UUID = "68cd187c-94b9-4bdf-98f6-96f18f8e565f";
    private final static String CHARACTERISTIC_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f5";


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.sensor, container, false);

        // ---------------------- DATABASE RESET ----------------------------
        //getContext().deleteDatabase(DATABASE_NAME);
        // ------------------------------------------------------------------

        mean_txt = view.findViewById(R.id.mean_txt);

        battery_value = view.findViewById(R.id.battery_value);
        ax_value = view.findViewById(R.id.ax_value);
        d4_value = view.findViewById(R.id.d4_value);
        mics_value = view.findViewById(R.id.mics_value);
        temperature_value = view.findViewById(R.id.temperature_value);
        humidity_value = view.findViewById(R.id.humidity_value);
        mean_value = view.findViewById(R.id.mean_value);
        cohb_value = view.findViewById(R.id.cohb_pred);

        db = new DatabaseConnection(Sensor.this.getActivity());

        username = getActivity().getIntent().getStringExtra("username");

        manager = BleManager.getInstance(getContext());

        // ------------------------------------BLUETOOTH ----------------------------------------------------------------

        bswitch = view.findViewById(R.id.bluetooth_switch);

        if (getActivity().getIntent().getStringExtra("bluetooth_on") == null) {
            bswitch.setChecked(false);
        } else {
            bswitch.setChecked(true);
        }

        bswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {

                    Intent bt = new Intent(getActivity(), Bluetooth.class);
                    bt.putExtra("username", username);
                    startActivity(bt);

                } else {
                    if (recording == true) {
                        handler.removeMessages(0);
                        recording = false;
                    }
                    final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    adapter.disable();
                    manager.destroy();
                    Toast.makeText(getActivity(), "Bluetooth turned off", Toast.LENGTH_SHORT).show();
                    battery_value.setText("Battery Level:   ---");
                    ax_value.setText("---");
                    d4_value.setText("---");
                    mics_value.setText("---");
                    temperature_value.setText("---");
                    humidity_value.setText("---");
                    mean_value.setText("---");
                    cohb_value.setText("---");
                }
            }
        });


        // ---------------------------------------------------------------------------------------------------------------


        if (manager.getConnectedDevices().isEmpty()) {
            battery_value.setText("Battery Level:   ---");
            ax_value.setText("---");
            d4_value.setText("---");
            mics_value.setText("---");
            temperature_value.setText("---");
            humidity_value.setText("---");
            mean_value.setText("---");
            cohb_value.setText("---");
            Toast.makeText(getActivity(), "No connection", Toast.LENGTH_SHORT).show();
        } else {
            connDevice = manager.getConnectedDevices().get(0);
            manager.notify(connDevice, SERVICE_UUID, CHARACTERISTIC_UUID, new BleNotifyCallback() {
                @Override
                public void onFail(int failCode, String info, BleDevice device) {
                    Toast.makeText(getActivity(), "Failed to receive data - " + info, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCharacteristicChanged(byte[] data, BleDevice device) {
                    String dataString = new String(data);
                    String[] values = dataString.split(";");
                    String temperature = values[1].trim();
                    String humidity = values[2].trim();
                    String co_ax = values[3].trim();
                    String co_d4 = values[4].trim();
                    String mics = values[5].trim();
                    String battery = values[6].trim();

                    battery_value.setText("Battery Level:   " + battery);
                    ax_value.setText(co_ax);
                    d4_value.setText(co_d4);
                    mics_value.setText(mics);
                    temperature_value.setText(temperature);
                    humidity_value.setText(humidity);

                    double mean = (Double.parseDouble(co_ax) + Double.parseDouble(co_d4) + Double.parseDouble(mics))/3;
                    mean_value.setText(String.valueOf(round.format(mean)));
                    mean_txt.setText("Mean (ppm)");

                    double co_hb = 3.217383 * mean + 0.0067;
                    cohb_value.setText(String.valueOf(round.format(co_hb)));

                    if (unit_ppm == false) {
                        mgm3 = getActivity().findViewById(R.id.mgm3);
                        ppm = getActivity().findViewById(R.id.ppm);
                        mgm3.setChecked(false);
                        ppm.setChecked(true);
                    }
                }

                @Override
                public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
                    Toast.makeText(getActivity(), "Receiving data", Toast.LENGTH_SHORT).show();
                }
            });
        }


            unit_btn = view.findViewById(R.id.unit_btn);
            unit_btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (!mean_value.getText().toString().equals("---")) {

                        measurement_ppm = Double.parseDouble(mean_value.getText().toString().trim().replace(",", "."));

                        mgm3 = view.findViewById(R.id.mgm3);
                        ppm = view.findViewById(R.id.ppm);

                        if (mgm3.isChecked()) {

                            if (unit_ppm == true) {
                                measurement_mgm3 = measurement_ppm * CONVERSION_FACTOR;
                                String.format("%.2f", measurement_mgm3);
                                mean_value.setText(String.valueOf(round.format(measurement_mgm3)));
                                mean_txt.setText("Mean (mg/m3)");
                            }

                            unit_ppm = false;
                        } else {

                            if (unit_ppm == false) {
                                measurement_ppm = measurement_mgm3 / CONVERSION_FACTOR;
                                mean_value.setText(String.valueOf(round.format(measurement_ppm)));
                                mean_txt.setText("Mean (ppm)");
                            }

                            unit_ppm = true;
                        }
                    } else {
                        Toast.makeText(getActivity(), "Conversion not possible", Toast.LENGTH_SHORT).show();
                    }

                }

            });

            startRecord = view.findViewById(R.id.start_recording_btn);
            stopRecord = view.findViewById(R.id.stop_recording_btn);

            startRecord.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (ax_value.getText().equals("---") || d4_value.getText().equals("---") || mics_value.getText().equals("---") || temperature_value.getText().equals("---") || humidity_value.getText().equals("---")) {
                        Toast.makeText(getActivity(), "No value displayed!", Toast.LENGTH_SHORT).show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Enter recording delay in seconds(Min = 1 ; Max = 10)");

                        // Set up the input
                        final EditText input = new EditText(getActivity());
                        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        builder.setView(input);

                        // Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {


                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (input.getText().toString().isEmpty()) {
                                    recording_delay = 5;
                                } else {
                                    recording_delay = Integer.parseInt(input.getText().toString());
                                }
                                if (recording_delay > 10 || recording_delay < 1) {
                                    recording_delay = 5;
                                    Toast.makeText(getActivity(), "Invalid input! Delay set to 5 seconds", Toast.LENGTH_SHORT).show();
                                }
                                handler = new Handler();
                                final int delay = recording_delay * 1000; //milliseconds
                                Toast.makeText(getActivity(), "Recording...", Toast.LENGTH_SHORT).show();
                                recording = true;
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        SimpleDateFormat d = new SimpleDateFormat("MM-dd-yyyy");
                                        String date = d.format(new Date());
                                        SimpleDateFormat t = new SimpleDateFormat("hh:mm:ss");
                                        String time = t.format(new Date());

                                        db.addData(Double.parseDouble(ax_value.getText().toString().trim()), Double.parseDouble(d4_value.getText().toString().trim()), Double.parseDouble(mics_value.getText().toString().trim()), Double.parseDouble(temperature_value.getText().toString().trim()), Double.parseDouble(humidity_value.getText().toString().trim()), date, time, username);
                                        handler.postDelayed(this, delay);
                                    }
                                }, delay);

                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();

                    }
                }

            });

            stopRecord.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (ax_value.getText().equals("---") || d4_value.getText().equals("---") || mics_value.getText().equals("---") || temperature_value.getText().equals("---") || humidity_value.getText().equals("---")) {
                        Toast.makeText(getActivity(), "No values displayed!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (recording == true) {
                            handler.removeMessages(0);
                            Toast.makeText(getActivity(), "Stop recording", Toast.LENGTH_SHORT).show();
                            recording = false;
                        }
                        else {
                            Toast.makeText(getActivity(), "Not recording", Toast.LENGTH_SHORT).show();
                        }

                    }
                }

            });

        return view;
    }

}

