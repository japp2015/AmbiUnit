package com.example.joaoafonsopereira.ambiunit;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joaoafonsopereira.ambiunit.adapter.ScanDeviceAdapter;
import com.example.joaoafonsopereira.ambiunit.adapter.common.CommonRecyclerViewAdapter;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.gatt.bean.CharacteristicInfo;
import com.ficat.easyble.gatt.bean.ServiceInfo;
import com.ficat.easyble.gatt.callback.BleCallback;
import com.ficat.easyble.gatt.callback.BleConnectCallback;
import com.ficat.easyble.gatt.callback.BleMtuCallback;
import com.ficat.easyble.gatt.callback.BleNotifyCallback;
import com.ficat.easyble.gatt.callback.BleReadCallback;
import com.ficat.easyble.gatt.callback.BleRssiCallback;
import com.ficat.easyble.gatt.callback.BleWriteCallback;
import com.ficat.easyble.scan.BleScanCallback;
import com.ficat.easypermissions.EasyPermissions;
import com.ficat.easypermissions.Permission;
import com.ficat.easypermissions.RequestSubscriber;
/*
import com.ficat.sample.adapter.ScanDeviceAdapter;
import com.ficat.sample.adapter.common.CommonRecyclerViewAdapter;*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "EasyBle";

    private final static int CHARACTERISTIC_READABLE = 101;
    private final static int CHARACTERISTIC_WRITEABLE = 102;
    private final static int CHARACTERISTIC_NOTIFICATION = 103;

    private final static String SERVICE_UUID = "68cd187c-94b9-4bdf-98f6-96f18f8e565f";
    private final static String TEMPERATURE_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f4";
    private final static String HUMIDITY_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f5";
    private final static String CO_AX_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f6";
    private final static String CO_D4_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f7";
    private final static String MICS_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f8";
    private final static String BATTERY_UUID = "884d33ea-23bc-439c-add5-1aed2fc7b9f9";

    private String username;

    private RecyclerView rv;
    private BleManager manager;
    private List<BleDevice> deviceList = new ArrayList<>();
    private ScanDeviceAdapter adapter;
    private String temperature, humidity, co_ax, co_d4, mics, battery;


    private String value_test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        initView();
        initBleManager();
        showDevicesByRv();
        username = getIntent().getStringExtra("username");
    }

    private void initView() {
        Button btnScan = findViewById(R.id.btn_scan);
        Button btnDisconnect = findViewById(R.id.btn_disconnect);
        /*
        Button btnNotify = findViewById(R.id.btn_notify);
        Button btnWirte = findViewById(R.id.btn_write);
        Button btnReadRssi = findViewById(R.id.btn_read_rssi);
        Button btnMtu = findViewById(R.id.btn_mtu);
        */
        rv = findViewById(R.id.rv);

        btnScan.setOnClickListener(this);
        btnDisconnect.setOnClickListener(this);
        /*
        btnNotify.setOnClickListener(this);
        btnWirte.setOnClickListener(this);
        btnReadRssi.setOnClickListener(this);
        btnMtu.setOnClickListener(this);
        */
    }

    private void initBleManager() {
        //check if this android device supports ble
        if (!BleManager.supportBle(this)) {
            return;
        }
        //open bluetooth without a request dialog
        BleManager.toggleBluetooth(true);

        BleManager.Options options = new BleManager.Options();
        options.loggable = true;
        options.scanPeriod = 10000;
        options.connectTimeout = 10000;
        manager = BleManager.getInstance(getApplication());
        manager.option(options);
    }

    private void showDevicesByRv() {
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 3;
            }
        });
        SparseArray<int[]> res = new SparseArray<>();
        res.put(R.layout.item_rv_scan_devices, new int[]{R.id.tv_name, R.id.tv_address, R.id.tv_connection_state});
        adapter = new ScanDeviceAdapter(this, deviceList, res);

        adapter.setOnItemClickListener(new CommonRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                manager.stopScan();
                //Toast.makeText(MainActivity.this, "Connecting to " + deviceList.get(position).name + "...", Toast.LENGTH_SHORT).show();
                manager.connect(deviceList.get(position), new BleConnectCallback() {
                    @Override
                    public void onStart(boolean startConnectSuccess, String info, BleDevice device) {
                        Log.e(TAG, "start connecting = " + startConnectSuccess + "     info: " + info);
                    }

                    @Override
                    public void onTimeout(BleDevice device) {
                        Toast.makeText(Bluetooth.this, "connect timeout!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnected(BleDevice device) {
                        adapter.notifyDataSetChanged();

                        manager.getDeviceServices(device);

                        final BleDevice connDevice = manager.getConnectedDevices().get(0);

                        Toast.makeText(Bluetooth.this, "Connected", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Bluetooth.this, MainActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("bluetooth_on", "true");
                        startActivity(intent);
                        /*
                        //--------- TEMPERATURE --------------------------------------------------------------------
                        manager.notify(connDevice, SERVICE_UUID, TEMPERATURE_UUID, new BleNotifyCallback() {
                            @Override
                            public void onCharacteristicChanged(byte[] data_t, BleDevice device) {
                                //Toast.makeText(Bluetooth.this, "receive notification data" + new String(data_t), Toast.LENGTH_SHORT).show();
                                temperature = new String(data_t);

                                Intent intent = new Intent(Bluetooth.this, Connection.class);
                                intent.putExtra("username", username);
                                intent.putExtra("device_name", device.name);
                                intent.putExtra("s_uuid", SERVICE_UUID);
                                intent.putExtra("t_uuid", TEMPERATURE_UUID);
                                intent.putExtra("temperature", temperature);
                                intent.putExtra("h_uuid", HUMIDITY_UUID);
                                intent.putExtra("humidity", humidity);
                                intent.putExtra("h_uuid", CO_AX_UUID);
                                intent.putExtra("humidity", co_ax);
                                intent.putExtra("h_uuid", CO_D4_UUID);
                                intent.putExtra("humidity", co_d4);
                                intent.putExtra("h_uuid", MICS_UUID);
                                intent.putExtra("humidity", mics);
                                intent.putExtra("h_uuid", BATTERY_UUID);
                                intent.putExtra("humidity", battery);
                                startActivity(intent);
                            }

                            @Override
                            public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
                                Toast.makeText(Bluetooth.this, "Success! UUID - " + notifySuccessUuid, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFail(int failCode, String info, BleDevice device) {
                                Toast.makeText(Bluetooth.this, "Failed to notify - " + info, Toast.LENGTH_LONG).show();
                            }
                        }); */

                        //--------- HUMIDITY --------------------------------------------------------------------
                        /*manager.notify(connDevice, SERVICE_UUID, HUMIDITY_UUID, new BleNotifyCallback() {
                            @Override
                            public void onCharacteristicChanged(byte[] data_h, BleDevice device) {
                                //Toast.makeText(Bluetooth.this, "receive notification data" + new String(data_h), Toast.LENGTH_SHORT).show();
                                humidity = new String(data_h);
                                Intent s = new Intent(Bluetooth.this, MainActivity.class);
                                s.putExtra("username", username);
                                s.putExtra("humidity", humidity);
                                s.putExtra("bluetooth_on", "true");
                                startActivity(s);
                                manager.cancelNotify(connDevice, SERVICE_UUID, HUMIDITY_UUID);
                            }

                            @Override
                            public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
                            }

                            @Override
                            public void onFail(int failCode, String info, BleDevice device) {
                                Toast.makeText(Bluetooth.this, "Failed to notify - " + info, Toast.LENGTH_LONG).show();
                            }
                        });*//*
                        //--------- COAX --------------------------------------------------------------------
                            manager.notify(connDevice, SERVICE_UUID, CO_AX_UUID, new BleNotifyCallback() {
                            @Override
                            public void onCharacteristicChanged(byte[] data_a, BleDevice device) {
                                //Toast.makeText(Bluetooth.this, "receive notification data" + new String(data_a), Toast.LENGTH_SHORT).show();
                                co_ax = new String(data_a);
                                Intent intent = new Intent(Bluetooth.this, Connection.class);
                                intent.putExtra("username", username);
                                intent.putExtra("device_name", device.name);
                                intent.putExtra("s_uuid", SERVICE_UUID);
                                intent.putExtra("t_uuid", TEMPERATURE_UUID);
                                intent.putExtra("temperature", temperature);
                                intent.putExtra("h_uuid", HUMIDITY_UUID);
                                intent.putExtra("humidity", humidity);
                                intent.putExtra("h_uuid", CO_AX_UUID);
                                intent.putExtra("humidity", co_ax);
                                intent.putExtra("h_uuid", CO_D4_UUID);
                                intent.putExtra("humidity", co_d4);
                                intent.putExtra("h_uuid", MICS_UUID);
                                intent.putExtra("humidity", mics);
                                intent.putExtra("h_uuid", BATTERY_UUID);
                                intent.putExtra("humidity", battery);
                                startActivity(intent);
                            }

                            @Override
                            public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
                                Toast.makeText(Bluetooth.this, "Success! UUID - " + notifySuccessUuid, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFail(int failCode, String info, BleDevice device) {
                                Toast.makeText(Bluetooth.this, "Failed to notify - " + info, Toast.LENGTH_LONG).show();
                            }
                        });
                        //--------- COD4 --------------------------------------------------------------------
                        manager.notify(connDevice, SERVICE_UUID, CO_D4_UUID, new BleNotifyCallback() {
                            @Override
                            public void onCharacteristicChanged(byte[] data_d, BleDevice device) {
                                //Toast.makeText(Bluetooth.this, "receive notification data" + new String(data_d), Toast.LENGTH_SHORT).show();
                                co_d4 = new String(data_d);
                                Intent intent = new Intent(Bluetooth.this, Connection.class);
                                intent.putExtra("username", username);
                                intent.putExtra("device_name", device.name);
                                intent.putExtra("s_uuid", SERVICE_UUID);
                                intent.putExtra("t_uuid", TEMPERATURE_UUID);
                                intent.putExtra("temperature", temperature);
                                intent.putExtra("h_uuid", HUMIDITY_UUID);
                                intent.putExtra("humidity", humidity);
                                intent.putExtra("h_uuid", CO_AX_UUID);
                                intent.putExtra("humidity", co_ax);
                                intent.putExtra("h_uuid", CO_D4_UUID);
                                intent.putExtra("humidity", co_d4);
                                intent.putExtra("h_uuid", MICS_UUID);
                                intent.putExtra("humidity", mics);
                                intent.putExtra("h_uuid", BATTERY_UUID);
                                intent.putExtra("humidity", battery);
                                startActivity(intent);
                            }

                            @Override
                            public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
                                Toast.makeText(Bluetooth.this, "Success! UUID - " + notifySuccessUuid, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFail(int failCode, String info, BleDevice device) {
                                Toast.makeText(Bluetooth.this, "Failed to notify - " + info, Toast.LENGTH_LONG).show();
                            }
                        });
                        //--------- MICS --------------------------------------------------------------------
                        manager.notify(connDevice, SERVICE_UUID, MICS_UUID, new BleNotifyCallback() {
                            @Override
                            public void onCharacteristicChanged(byte[] data_m, BleDevice device) {
                                //Toast.makeText(Bluetooth.this, "receive notification data" + new String(data_m), Toast.LENGTH_SHORT).show();
                                mics = new String(data_m);
                                Intent intent = new Intent(Bluetooth.this, Connection.class);
                                intent.putExtra("username", username);
                                intent.putExtra("device_name", device.name);
                                intent.putExtra("s_uuid", SERVICE_UUID);
                                intent.putExtra("t_uuid", TEMPERATURE_UUID);
                                intent.putExtra("temperature", temperature);
                                intent.putExtra("h_uuid", HUMIDITY_UUID);
                                intent.putExtra("humidity", humidity);
                                intent.putExtra("h_uuid", CO_AX_UUID);
                                intent.putExtra("humidity", co_ax);
                                intent.putExtra("h_uuid", CO_D4_UUID);
                                intent.putExtra("humidity", co_d4);
                                intent.putExtra("h_uuid", MICS_UUID);
                                intent.putExtra("humidity", mics);
                                intent.putExtra("h_uuid", BATTERY_UUID);
                                intent.putExtra("humidity", battery);
                                startActivity(intent);
                            }

                            @Override
                            public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
                                Toast.makeText(Bluetooth.this, "Success! UUID - " + notifySuccessUuid, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFail(int failCode, String info, BleDevice device) {
                                Toast.makeText(Bluetooth.this, "Failed to notify - " + info, Toast.LENGTH_LONG).show();
                            }
                        });
                        //--------- BATTERY --------------------------------------------------------------------
                        manager.notify(connDevice, SERVICE_UUID, BATTERY_UUID, new BleNotifyCallback() {
                            @Override
                            public void onCharacteristicChanged(byte[] data_b, BleDevice device) {
                                //Toast.makeText(Bluetooth.this, "receive notification data" + new String(data_b), Toast.LENGTH_SHORT).show();
                                battery = new String(data_b);
                                Intent intent = new Intent(Bluetooth.this, Connection.class);
                                intent.putExtra("username", username);
                                intent.putExtra("device_name", device.name);
                                intent.putExtra("s_uuid", SERVICE_UUID);
                                intent.putExtra("t_uuid", TEMPERATURE_UUID);
                                intent.putExtra("temperature", temperature);
                                intent.putExtra("h_uuid", HUMIDITY_UUID);
                                intent.putExtra("humidity", humidity);
                                intent.putExtra("h_uuid", CO_AX_UUID);
                                intent.putExtra("humidity", co_ax);
                                intent.putExtra("h_uuid", CO_D4_UUID);
                                intent.putExtra("humidity", co_d4);
                                intent.putExtra("h_uuid", MICS_UUID);
                                intent.putExtra("humidity", mics);
                                intent.putExtra("h_uuid", BATTERY_UUID);
                                intent.putExtra("humidity", battery);
                                startActivity(intent);
                            }

                            @Override
                            public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
                                Toast.makeText(Bluetooth.this, "Success! UUID - " + notifySuccessUuid, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFail(int failCode, String info, BleDevice device) {
                                Toast.makeText(Bluetooth.this, "Failed to notify - " + info, Toast.LENGTH_LONG).show();
                            }
                        }); */
                    }

                    @Override
                    public void onDisconnected(BleDevice device) {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        rv.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (!BleManager.isBluetoothOn()) {
                    BleManager.toggleBluetooth(true);
                }
                final EasyPermissions easyPermissions = new EasyPermissions(this);
                easyPermissions.requestEach(Manifest.permission.ACCESS_COARSE_LOCATION)
                        .subscribe(new RequestSubscriber<Permission>() {
                            @Override
                            public void onPermissionsRequestResult(Permission permission) {
                                if (permission.granted) {
                                    if (manager.isScanning()) return;
                                    startScan();
                                } else {
                                    if (permission.shouldShowRequestPermissionRationale) {
                                        easyPermissions.requestEach(Manifest.permission.ACCESS_COARSE_LOCATION)
                                                .subscribe(this);
                                    } else {
                                        Toast.makeText(Bluetooth.this,
                                                "Please go to settings to grant location permission manually",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                break;
            case R.id.btn_disconnect:
                manager.disconnectAll();
                break;
            /*case R.id.btn_notify:
                testNotify();
                break;
            case R.id.btn_write:
                testWrite();
                break;
            case R.id.btn_read_rssi:
                testReadRssi();
                break;
            case R.id.btn_mtu:
                testSetMtu();
                break;*/
            default:
                break;
        }
    }

    private void startScan() {
        manager.startScan(new BleScanCallback() {
            @Override
            public void onLeScan(BleDevice device, int rssi, byte[] scanRecord) {
                for (BleDevice d : deviceList) {
                    if (device.address.equals(d.address)) {
                        return;
                    }
                }
                deviceList.add(device);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onStart(boolean startScanSuccess, String info) {
                Log.e(TAG, "start scan = " + startScanSuccess + "   info: " + info);
                if (startScanSuccess) {
                    deviceList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFinish() {
                Log.e(TAG, "scan finish");
            }
        });
    }
    /*
    private void testReadRssi() {
        if (manager.getConnectedDevices().size() <= 0) {
            Toast.makeText(MainActivity.this, "No connected devices", Toast.LENGTH_SHORT).show();
            return;
        }
        //we use the first connected device to test
        BleDevice device2 = manager.getConnectedDevices().get(0);
        manager.readRssi(device2, new BleRssiCallback() {
            @Override
            public void onRssi(int rssi, BleDevice bleDevice) {
                Toast.makeText(MainActivity.this, "Rssi: " + rssi, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(int failCode, String info, BleDevice device) {
                Toast.makeText(MainActivity.this, "read rssi fail: " + info, Toast.LENGTH_SHORT).show();
            }
        });
    }
    */
    /*
    private void testSetMtu() {
        if (manager.getConnectedDevices().size() <= 0) {
            Toast.makeText(MainActivity.this, "No connected devices", Toast.LENGTH_SHORT).show();
            return;
        }
        //we use the first connected device to test
        BleDevice device3 = manager.getConnectedDevices().get(0);
        manager.setMtu(device3, 128, new BleMtuCallback() {
            @Override
            public void onMtuChanged(int mtu, BleDevice device) {
                Toast.makeText(MainActivity.this, "Request MTU success: " + mtu, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(int failCode, String info, BleDevice device) {
                Toast.makeText(MainActivity.this, "set MTU fail: " + info, Toast.LENGTH_SHORT).show();
            }
        });
    }
    */
    /*
    private void testWrite() {
        if (manager.getConnectedDevices().size() <= 0) {
            Toast.makeText(MainActivity.this, "No connected devices", Toast.LENGTH_SHORT).show();
            return;
        }
        //we use the first connected device to test
        BleDevice device1 = manager.getConnectedDevices().get(0);
        //randomly finding a writeable characteristic to test
        Map<String, String> notificationInfo1 = getSpecificServiceInfo(device1, CHARACTERISTIC_WRITEABLE);
        for (Map.Entry<String, String> e : notificationInfo1.entrySet()) {
            manager.write(device1, e.getKey(), e.getValue(), "TestWriteData001".getBytes(), new BleWriteCallback() {
                @Override
                public void onWrite(byte[] data, BleDevice device) {
                    Toast.makeText(MainActivity.this, "write success!   data:  " + new String(data), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFail(int failCode, String info, BleDevice device) {
                    Toast.makeText(MainActivity.this, "write fail: " + info, Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
    }
    */
    /*
    private void testNotify() {
        if (manager.getConnectedDevices().size() <= 0) {
            Toast.makeText(MainActivity.this, "No connected devices", Toast.LENGTH_SHORT).show();
            return;
        }
        //we use the first connected device to test
        BleDevice device = manager.getConnectedDevices().get(0);
        //randomly finding a characteristic supporting notification to test
        Map<String, String> notificationInfo = getSpecificServiceInfo(device, CHARACTERISTIC_NOTIFICATION);
        for (final Map.Entry<String, String> e : notificationInfo.entrySet()) {
            manager.notify(device, e.getKey(), e.getValue(), new BleNotifyCallback() {
                @Override
                public void onCharacteristicChanged(byte[] data, BleDevice device) {
                    Toast.makeText(MainActivity.this, "receive notification data" + new String(data), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
                    Log.e(TAG, "notify succcess: " + notifySuccessUuid);
                }

                @Override
                public void onFail(int failCode, String info, BleDevice device) {
                    Toast.makeText(MainActivity.this, "set notify fail: " + info, Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
    } */

    /**
     * randomly finding a characteristic supporting specific property ,and using the characteristic
     * to test like notify() or write()
     *
     * @return the map-value is the uuid of characteristic used for test,and the map-key is the
     * uuid of service that contains this characteristic
     */
    private Map<String, String> getSpecificServiceInfo(BleDevice device, int characteristicProperty) {
        Map<String, String> map = new HashMap<>();
        Map<ServiceInfo, List<CharacteristicInfo>> serviceInfo = manager.getDeviceServices(device);
        for (Map.Entry<ServiceInfo, List<CharacteristicInfo>> entry : serviceInfo.entrySet()) {
            String serviceUuid = entry.getKey().uuid;
            for (CharacteristicInfo charInfo : entry.getValue()) {
                boolean specificReadable = characteristicProperty == CHARACTERISTIC_READABLE && charInfo.readable;
                boolean specificWriteable = characteristicProperty == CHARACTERISTIC_WRITEABLE && charInfo.writeable;
                boolean specificNotify = characteristicProperty == CHARACTERISTIC_NOTIFICATION && (charInfo.notify ||
                        charInfo.indicative);
                if (specificReadable || specificWriteable || specificNotify) {
                    map.put(serviceUuid, charInfo.uuid);
                }
            }
        }
        return map;
    }


}
