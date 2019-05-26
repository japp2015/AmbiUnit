package com.example.joaoafonsopereira.ambiunit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.gatt.callback.BleNotifyCallback;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Environment extends AppCompatActivity {

    private String username;
    private TextView date_txt, time_txt, temperature_txt, humidity_txt;
    private BleManager manager;
    private BleDevice connDevice;
    private String temperature, humidity;
    private final static String SERVICE_UUID = "68cd187c-94b9-4bdf-98f6-96f18f8e565f";
    private final static String TEMPERATURE_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f4";
    private final static String HUMIDITY_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment);

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        username = getIntent().getStringExtra("username");

        SimpleDateFormat d = new SimpleDateFormat("MM-dd-yyyy");
        String date = d.format(new Date());

        date_txt = findViewById(R.id.date);
        date_txt.setText(date);

        SimpleDateFormat t = new SimpleDateFormat("hh:mm:ss");
        String time = t.format(new Date());

        time_txt = findViewById(R.id.time);
        time_txt.setText(time);

        temperature_txt = findViewById(R.id.temperature);

        manager = BleManager.getInstance(getApplicationContext());
        connDevice = manager.getConnectedDevices().get(0);
        manager.notify(connDevice, SERVICE_UUID, TEMPERATURE_UUID, new BleNotifyCallback() {
            @Override
            public void onFail(int failCode, String info, BleDevice device) {
                Toast.makeText(Environment.this, "Failed to receive temperature", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCharacteristicChanged(byte[] data, BleDevice device) {
                temperature = new String(data);
                temperature_txt.setText(temperature);
            }

            @Override
            public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
                Toast.makeText(Environment.this, "Temperature received", Toast.LENGTH_SHORT).show();
            }
        });

        humidity_txt = findViewById(R.id.humidity);
        humidity_txt.setText(getIntent().getStringExtra("humidity"));
    }

    @Override
    public boolean onSupportNavigateUp(){
        Intent main = new Intent(Environment.this, MainActivity.class);
        main.putExtra("username", username);
        main.putExtra("bluetooth_on", "true");
        main.putExtra("humidity", humidity);
        startActivity(main);
        return true;
    }
}
