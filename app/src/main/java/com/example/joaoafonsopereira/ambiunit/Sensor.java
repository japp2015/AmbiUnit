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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
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

import static com.example.joaoafonsopereira.ambiunit.DatabaseConnection.DATABASE_NAME;

/**
 * Created by Asus on 31/03/2019.
 */

public class Sensor extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "Sensor";

    DatabaseConnection db;
    String username;
    private String sensor;
    private Button check_environment;
    private Switch bswitch;
    private TextView unit_txt;
    private Button unit_btn;
    private boolean unit_ppm = true;
    private TextView value;
    private TextView cohb_value;
    private Button startRecord;
    private Button stopRecord;
    private double measurement_mgm3;
    private double measurement_ppm;
    private final double CONVERSION_FACTOR = 28.01/22.41;
    private static DecimalFormat round = new DecimalFormat(".##");
    private boolean stop = false;
    private Handler handler;
    private int recording_delay;
    private String temperature, humidity;
    private BleManager manager;
    private BleDevice connDevice;
    private String sensor_value;
    private RadioButton mgm3, ppm;

    private final static String SERVICE_UUID = "68cd187c-94b9-4bdf-98f6-96f18f8e565f";
    private final static String TEMPERATURE_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f4";
    private final static String HUMIDITY_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f5";
    private final static String CO_AX_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f6";
    private final static String CO_D4_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f7";
    private final static String MICS_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f8";
    private final static String BATTERY_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f9";

    private String CURRENT_UUID;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.sensor, container, false);

        // ---------------------- DATABASE RESET ----------------------------
        //getContext().deleteDatabase(DATABASE_NAME);
        // ------------------------------------------------------------------

        db = new DatabaseConnection(Sensor.this.getActivity());

        username = getActivity().getIntent().getStringExtra("username");

        manager = BleManager.getInstance(getContext());


        // ------------------------------------BLUETOOTH ----------------------------------------------------------------

        bswitch = (Switch) view.findViewById(R.id.bluetooth_switch);

        if (getActivity().getIntent().getStringExtra("bluetooth_on") == null) {
            bswitch.setChecked(false);
        }
        else {
            bswitch.setChecked(true);
        }

        bswitch.setOnCheckedChangeListener (new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {

                    Intent bt = new Intent(getActivity(), Bluetooth.class);
                    bt.putExtra("username", username);
                    startActivity(bt);

                }
                else {
                    final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    adapter.disable();
                    manager.destroy();
                    Toast.makeText(getActivity(),"Bluetooth turned off", Toast.LENGTH_SHORT).show();
                }
            }
        });



        // ---------------------------------------------------------------------------------------------------------------

        Spinner spinner = (Spinner) view.findViewById(R.id.sensor_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(Sensor.this.getActivity(), R.array.sensors, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(0);

        value = view.findViewById(R.id.value);
        cohb_value = view.findViewById(R.id.cohb_pred);

        unit_btn = view.findViewById(R.id.unit_btn);
        unit_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!value.getText().toString().equals("---")) {

                    measurement_ppm = Double.parseDouble(value.getText().toString().trim().replace(",", "."));
                    unit_txt = view.findViewById(R.id.unit_txt);

                    mgm3 = view.findViewById(R.id.mgm3);
                    ppm = view.findViewById(R.id.ppm);

                    if (mgm3.isChecked()) {
                        unit_txt.setText("mg/m3");

                        if (unit_ppm == true) {
                            measurement_mgm3 = measurement_ppm * CONVERSION_FACTOR;
                            String.format("%.2f", measurement_mgm3);
                            value.setText(String.valueOf(round.format(measurement_mgm3)));
                        }

                        unit_ppm = false;
                    } else {
                        unit_txt.setText("ppm");

                        if (unit_ppm == false) {
                            measurement_ppm = measurement_mgm3 / CONVERSION_FACTOR;
                            value.setText(String.valueOf(round.format(measurement_ppm)));
                        }

                        unit_ppm = true;
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Conversion not possible", Toast.LENGTH_SHORT).show();
                }

            }

        });

        startRecord = view.findViewById(R.id.start_recording_btn);
        stopRecord = view.findViewById(R.id.stop_recording_btn);

        startRecord.setOnClickListener(new View.OnClickListener() {

           @Override
           public void onClick(View v) {

               if (value.getText().equals("---")) {
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
                           }
                           else {
                               recording_delay = Integer.parseInt(input.getText().toString());
                           }
                           if (recording_delay > 10 || recording_delay < 1) {
                               recording_delay = 5;
                               Toast.makeText(getActivity(), "Invalid input! Delay set to 5 seconds", Toast.LENGTH_SHORT).show();
                           }
                           handler = new Handler();
                           final int delay = recording_delay * 1000; //milliseconds
                           Toast.makeText(getActivity(), "Recording...", Toast.LENGTH_SHORT).show();
                           handler.postDelayed(new Runnable() {
                               public void run() {
                                   SimpleDateFormat d = new SimpleDateFormat("MM-dd-yyyy");
                                   String date = d.format(new Date());
                                   SimpleDateFormat t = new SimpleDateFormat("hh:mm:ss");
                                   String time = t.format(new Date());

                                   db.addData(Double.parseDouble(value.getText().toString().trim()), date, time, sensor, username);
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
                handler.removeMessages(0);
                Toast.makeText(getActivity(), "Stop recording", Toast.LENGTH_SHORT).show();
            }

        });

        check_environment = view.findViewById(R.id.environment);
        check_environment.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (manager.getConnectedDevices().isEmpty()) {
                    value.setText("---");
                } else {
                    Intent env = new Intent(getActivity(), Environment.class);
                    env.putExtra("username", username);
                    env.putExtra("humidity", getActivity().getIntent().getStringExtra("humidity"));
                    startActivity(env);
                }
            }
            });

        return view;

    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        sensor = text;
        switch (text) {
            case "CO-AX":
                CURRENT_UUID = CO_AX_UUID;
            case "CO-D4":
                CURRENT_UUID = CO_D4_UUID;
            case "MICS-4514":
                CURRENT_UUID = MICS_UUID;
        }
        if (manager.getConnectedDevices().isEmpty()) {
            value.setText("---");
        } else {
            connDevice = manager.getConnectedDevices().get(0);
            //manager.cancelNotify(connDevice, SERVICE_UUID, CURRENT_UUID);
            manager.notify(connDevice, SERVICE_UUID, CURRENT_UUID, new BleNotifyCallback() {
                @Override
                public void onFail(int failCode, String info, BleDevice device) {
                    Toast.makeText(getActivity(), "Failed to receive data from " + sensor, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCharacteristicChanged(byte[] data, BleDevice device) {
                    sensor_value = new String(data);
                    //Toast.makeText(getActivity(), sensor_value, Toast.LENGTH_SHORT).show();
                    value.setText(sensor_value);
                    double co_hb = 3.217383 * Double.parseDouble(sensor_value) + 0.0067;
                    cohb_value.setText(String.valueOf(round.format(co_hb)));

                    if (unit_ppm == false) {
                        unit_txt = getActivity().findViewById(R.id.unit_txt);
                        mgm3 = getActivity().findViewById(R.id.mgm3);
                        ppm = getActivity().findViewById(R.id.ppm);
                        unit_txt.setText("ppm");
                        mgm3.setChecked(false);
                        ppm.setChecked(true);
                    }
                }

                @Override
                public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
                    Toast.makeText(getActivity(), "Receiving data from " + sensor, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

}