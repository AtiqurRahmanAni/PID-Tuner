package com.atiqur.pidtuner;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.Helper;
import androidx.core.content.ContextCompat;

import com.atiqur.pidtuner.databinding.ActivityMainBinding;
import com.atiqur.pidtuner.utils.HelperUtils;
import com.atiqur.pidtuner.utils.ToolbarHelper;
import com.google.android.material.slider.Slider;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;
    private static final double THRESHOLD = .0001;
    private volatile float kp = 0;
    private volatile float ki = 0;
    private volatile float kd = 0;
    private boolean menuCreated = false;
    private BluetoothAdapter mBluetoothAdapter = null;
    public Bluetooth mBluetooth = null;
    private String deviceAddress = null;
    private static final int REQUEST_ENABLE_BT = 222;
    private static final int ENABLED = 111;
    public static final int SETTINGS = 333;
    private boolean isConnected = false;
    private Menu menu;
    private Thread mPIDThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        sliderListener();
        ToolbarHelper.create(binding.toolbar, null, this, "PID Tuner");
        SharedPreferences sharedPreferences = getSharedPreferences(Settings.SHARED_PREFS, MODE_PRIVATE);
        float kp = sharedPreferences.getFloat(Settings.KEY_KP, 50f);
        float kd = sharedPreferences.getFloat(Settings.KEY_KD, 50f);
        float ki = sharedPreferences.getFloat(Settings.KEY_KI, 50f);
        setSliderValues(kp, kd, ki);
    }

    private void setSliderValues(float kp, float kd, float ki) {
        binding.kpMaxValue.setText(String.format("KP(max %.3s):", (int)kp));
        binding.kdMaxValue.setText(String.format("KD(max %.3s):", (int)kd));
        binding.kiMaxValue.setText(String.format("KI(max %.3s):", (int)ki));
        binding.sliderKP.setValueTo(kp);
        binding.sliderKD.setValueTo(kd);
        binding.sliderKi.setValueTo(ki);
        binding.sliderKP.setValue(0f);
        binding.sliderKD.setValue(0f);
        binding.sliderKi.setValue(0f);
    }

    public void onResume() {
        super.onResume();
        binding.kpTextView.setText(String.format("%.5s", binding.sliderKP.getValue()));
        binding.kdTextView.setText(String.format("%.5s", binding.sliderKD.getValue()));
        binding.kiTextView.setText(String.format("%.5s", binding.sliderKi.getValue()));
        checkBluetooth();
        if (mBluetooth == null) {
            mBluetooth = new Bluetooth(mHandler);
        }
        if (mBluetooth.getState() == 0) {
            mBluetooth.start();
        }
        mPIDThread = new Thread(sendValue);
        mPIDThread.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mPIDThread.interrupt();
    }

    public void onDestroy() {
        super.onDestroy();
        mPIDThread.interrupt();
        if (this.mBluetooth != null) {
            this.mBluetooth.stop();
        }
    }

    private final Runnable sendValue = new Runnable() {
        float prevkp = 0;
        float prevkd = 0;
        float prevki = 0;

        public void run() {
            while (true) {
                if (Math.abs(kp - prevkp) >= THRESHOLD && mBluetooth.getState() == 2) {
                    Log.d("Test", "KP: " + kp);
                    prevkp = kp;
                    mBluetooth.write(HelperUtils.toBytesFloat('P', kp, 5));
                }
                if (Math.abs(kd - prevkd) >= THRESHOLD && mBluetooth.getState() == 2) {
                    Log.d("Test", "KD: " + kd);
                    prevkd = kd;
                    mBluetooth.write(HelperUtils.toBytesFloat('D', kd, 5));
                }
                if (Math.abs(ki - prevki) >= THRESHOLD && mBluetooth.getState() == 2) {
                    Log.d("Test", "KI: " + ki);
                    prevki = ki;
                    mBluetooth.write(HelperUtils.toBytesFloat('I', ki, 5));
                }
                synchronized (this) {
                    try {
                        wait(25);
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        }
    };

    private void sliderListener() {

        binding.sliderKP.addOnChangeListener((slider, value, fromUser) -> {
            kp = slider.getValue();
            binding.kpTextView.setText(String.format("%.5s", kp));
        });
        binding.sliderKD.addOnChangeListener((slider, value, fromUser) -> {
            kd = slider.getValue();
            binding.kdTextView.setText(String.format("%.5s", kd));
        });
        binding.sliderKi.addOnChangeListener((slider, value, fromUser) -> {
            ki = slider.getValue();
            binding.kiTextView.setText(String.format("%.5s", ki));
        });
    }

    private void checkBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        this.menu = menu;
        menuCreated = true;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.connect_scan) {
            if (!mBluetoothAdapter.isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
            } else if (!isConnected) {
                startActivityForResult(new Intent(this, PairedActivity.class), ENABLED);
            } else {
                Toast.makeText(this, "You are already connected!", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == R.id.menu_disconnect) {
            mBluetooth.disconnect();
        } else if (item.getItemId() == R.id.menu_settings) {
            startActivityForResult(new Intent(this, Settings.class), SETTINGS);
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectDevice(Intent data) {
        if (mBluetooth != null) {
            mBluetooth.stop();
        }
        String address = data.getExtras().getString(PairedActivity.EXTRA_DEVICE_ADDRESS);
        deviceAddress = address;
        mBluetooth.connect(mBluetoothAdapter.getRemoteDevice(address));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ENABLED && resultCode == RESULT_OK) {
            if (mBluetooth.getState() != 0) {
                if (!deviceAddress.equals(data.getExtras().getString(PairedActivity.EXTRA_DEVICE_ADDRESS))) {
                    mBluetooth.stop();
                    mBluetooth = new Bluetooth(mHandler);
                    try {
                        wait(10);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error" + e, Toast.LENGTH_SHORT).show();
                    }
                    connectDevice(data);
                    return;
                }
                return;
            }
            mBluetooth.stop();
            mBluetooth = new Bluetooth(mHandler);
            connectDevice(data);
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Bluetooth enabled", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Bluetooth not enabled. Leaving", Toast.LENGTH_SHORT).show();
            finish();
        } else if (requestCode == SETTINGS && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            setSliderValues(bundle.getFloat(Settings.KEY_KP), bundle.getFloat(Settings.KEY_KD), bundle.getFloat(Settings.KEY_KI));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private final Handler mHandler = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (msg.arg1 == 0 && menuCreated) {
                    isConnected = false;
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_bluetooth));
                    menu.getItem(0).setShowAsAction(5);
                    menu.getItem(0).setTitle("Connect");
                } else if (msg.arg1 == 1 && menuCreated) {
                    isConnected = false;
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_bluetooth_connecting));
                    menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                } else if (msg.arg1 == 2 && menuCreated) {
                    isConnected = true;
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_bluetooth));
                    menu.getItem(0).setShowAsAction(5);
                    menu.getItem(0).setTitle("Connected");
                }
            } else if (msg.what == 2) {
                Toast.makeText(MainActivity.this, "Connected to " + msg.getData().getString("device_name"), Toast.LENGTH_SHORT).show();
            } else if (msg.what == 3) {
                Toast.makeText(MainActivity.this, msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
            }
        }
    };
}
