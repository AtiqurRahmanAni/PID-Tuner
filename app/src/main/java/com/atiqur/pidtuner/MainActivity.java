package com.atiqur.pidtuner;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
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
import androidx.core.content.ContextCompat;

import com.atiqur.pidtuner.databinding.ActivityMainBinding;
import com.atiqur.pidtuner.utils.HelperUtils;
import com.atiqur.pidtuner.utils.ToolbarHelper;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private boolean allow = false;
    private boolean menuCreated = false;
    private BluetoothAdapter mBluetoothAdapter = null;
    public Bluetooth mBluetooth = null;
    private String deviceAddress = null;
    private static final int REQUEST_ENABLE_BT = 222;
    private static final int ENABLED = 111;
    private boolean isConnected = false;
    private Menu menu;
    private Thread mPIDThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ToolbarHelper.create(binding.toolbar, null, this, "PID Tuner");
        sliderListener();
    }

    public void onResume() {
        super.onResume();
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
        private boolean once = false;

        public void run() {
            while (true) {
                if (allow) {
                    once = true;
                    int KPValue = (int) binding.sliderKP.getValue();
                    int KDValue = (int) binding.sliderKD.getValue();
                    mBluetooth.write(HelperUtils.toBytes('P', KPValue, 3));
                    mBluetooth.write(HelperUtils.toBytes('D', KDValue, 3));
                }
                synchronized (this) {
                    try {
                        wait(22);
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private void sliderListener() {
        binding.kpTextView.setText(String.format("KP: %s", (int)binding.sliderKP.getValue()));
        binding.kdTextView.setText(String.format("KD: %s", (int)binding.sliderKD.getValue()));
        binding.sliderKP.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                if (!allow && mBluetooth.getState() == 2) {
                    allow = true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                allow = false;
            }
            binding.kpTextView.setText(String.format("KP: %s", (int)binding.sliderKP.getValue()));
//            Log.d("allow", binding.sliderKP.getValue() + "KP");
//            Log.d("allow", binding.sliderKD.getValue() + "KD");
            if (event.getAction() == MotionEvent.ACTION_DOWN && mBluetooth.getState() != 2) {
                Toast.makeText(MainActivity.this, "You are not connected to a device", Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        binding.sliderKD.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                if (!allow && mBluetooth.getState() == 2) {
                    allow = true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                allow = false;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN && mBluetooth.getState() != 2) {
                Toast.makeText(MainActivity.this, "You are not connected to a device", Toast.LENGTH_SHORT).show();
            }
            binding.kdTextView.setText(String.format("KD: %s", (int)binding.sliderKD.getValue()));
            return false;
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
