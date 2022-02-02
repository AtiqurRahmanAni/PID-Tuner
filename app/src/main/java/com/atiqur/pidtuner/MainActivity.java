package com.atiqur.pidtuner;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.atiqur.pidtuner.databinding.ActivityMainBinding;
import com.atiqur.pidtuner.utils.ToolbarHelper;

import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;
    private boolean menuCreated = false;
    private BluetoothAdapter mBluetoothAdapter = null;
    public Bluetooth mBluetooth = null;
    private String deviceAddress = null;
    private static final int REQUEST_ENABLE_BT = 222;
    private static final int ENABLED = 111;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private Menu menu;
    private Thread mPIDThread;

    boolean[] tables = new boolean[4];
    HashMap<Integer, byte[]> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        ToolbarHelper.create(binding.toolbar, null, this, "Robo Waiter");

        for (int i = 0; i < tables.length; i++) {
            map.put(i, new byte[]{(byte) ((char) (i + 49))});
        }
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

    void setTable(int index) {
        for (int i = 0; i < tables.length; i++) {
            tables[i] = i == index;
        }

        Log.d("HELLO", Arrays.toString(tables));

    }

    boolean isWriting = true;

    private final Runnable sendValue = new Runnable() {

        public void run() {
            while (true) {
                for (int i = 0; i < tables.length; i++) {
                    if (!isWriting && tables[i] && mBluetooth.getState() == 2) {
                        mBluetooth.write(map.get(i));
                        isWriting = true;
                    }
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
            } else if (!isConnected && !isConnecting) {
                startActivityForResult(new Intent(this, PairedActivity.class), ENABLED);
            } else if (mBluetooth.getState() == 2) {
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
                } else if (msg.arg1 == 1 && menuCreated) {
                    isConnected = false;
                    isConnecting = true;
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_bluetooth_connecting));
                    menu.getItem(0).setShowAsAction(5);
                } else if (msg.arg1 == 2 && menuCreated) {
                    isConnected = true;
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_bluetooth));
                    menu.getItem(0).setShowAsAction(5);
                }
            } else if (msg.what == 2) {
                Toast.makeText(MainActivity.this, "Connected to " + msg.getData().getString("device_name"), Toast.LENGTH_SHORT).show();
            } else if (msg.what == 3) {
                isConnecting = false;
                Toast.makeText(MainActivity.this, msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
            } else if (msg.what == 4) {
                Log.d("bluetooth_message", msg.getData().getString("bluetooth_message"));
                Log.d("message_length", String.valueOf(msg.getData().getInt("message_length")));
            }

        }
    };

    public void tableButtonClick(View view) {
        if (mBluetooth.getState() == 2) {
            isWriting = false;
            Button button = (Button) view;
            String buttonText = button.getText().toString();
            char lastIndex = buttonText.charAt(buttonText.length() - 1);
            setTable(Integer.parseInt(lastIndex + "") - 1);
        } else {
            Toast.makeText(MainActivity.this, "You are not connected", Toast.LENGTH_SHORT).show();
        }

    }
}
