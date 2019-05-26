package com.example.joaoafonsopereira.ambiunit;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

public class Connection extends AppCompatActivity {

    private TextView deviceName;
    private TextView sUUID, tUUID, hUUID, coaxUUID, cod4UUID, micsUUID, batUUID;
    private TextView tValue, hValue, coaxValue, cod4Value, micsValue, batValue;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        username = getIntent().getStringExtra("username");

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button


        deviceName = findViewById(R.id.device_name);
        sUUID = findViewById(R.id.service_uuid);
        tUUID = findViewById(R.id.t_uuid);
        hUUID = findViewById(R.id.h_uuid);
        coaxUUID = findViewById(R.id.co_ax_uuid);
        cod4UUID = findViewById(R.id.co_d4_uuid);
        micsUUID = findViewById(R.id.mics_uuid);
        batUUID = findViewById(R.id.bat_uuid);
        tValue = findViewById(R.id.t_value);
        hValue = findViewById(R.id.h_value);
        coaxValue = findViewById(R.id.co_ax_value);
        cod4Value = findViewById(R.id.co_d4_value);
        micsValue = findViewById(R.id.mics_value);
        batValue = findViewById(R.id.bat_value);


        Intent intent = getIntent();
        deviceName.setText(intent.getStringExtra("device_name"));
        sUUID.setText(intent.getStringExtra("s_uuid"));
        //tUUID.setText(intent.getStringExtra("t_uuid"));
        //hUUID.setText(intent.getStringExtra("h_uuid"));
        tValue.setText(intent.getStringExtra("temperature"));
        hValue.setText(intent.getStringExtra("humidity"));
    }

    @Override
    public boolean onSupportNavigateUp(){
        Intent main = new Intent(Connection.this, MainActivity.class);
        main.putExtra("username", username);
        startActivity(main);
        return true;
    }
}
