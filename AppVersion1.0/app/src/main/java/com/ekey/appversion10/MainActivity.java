package com.ekey.appversion10;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    ConnectedThread connectedThread;
    Button button;
    Button button2;
    BluetoothAdapter bluetoothAdapter;
    BluetoothChatService chatService = null;
    BluetoothSocket bluetoothSocket = null;
    boolean lock = true;
    private StringBuffer stringBuffer;
    TextView textView;
    ArrayList<BluetoothDevice> foundDevices = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        foundDevices = new ArrayList<BluetoothDevice>();
        textView = (TextView) findViewById(R.id.textView);
        textView.setText("Not Connected to Lock");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
            Toast.makeText(getApplicationContext(), "There is no Bluetooth adapter on this device", Toast.LENGTH_LONG);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (lock) {
                    button.setText("Lock door");
                    lock = false;
                    connectedThread.write("unlock".getBytes());
                } else {
                    button.setText("Unlock door");
                    lock = true;
                    connectedThread.write("close".getBytes());
                }
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                //findPi();
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                for(BluetoothDevice b : pairedDevices){
                    Log.d("Mainactivity", b.getName() + " " + b.getAddress());
                    try {
                        bluetoothSocket = b.createInsecureRfcommSocketToServiceRecord(UUID.fromString("dad8bf14-b6c3-45fa-b9a7-94c1fde2e7c6"));
                        bluetoothSocket.connect();
                    }catch(IOException e){
                        Log.d("MainActivity", "it failed goddammnnn");
                    }
                }
                if(bluetoothSocket != null) {
                    try {
                        OutputStream outputStream = bluetoothSocket.getOutputStream();
                        outputStream.write("Hello World".getBytes());
                    }catch (IOException e){}
                    Log.d("MainActivity","ITSS ALIVEEEEEEE");
                    connectedThread = new ConnectedThread(bluetoothSocket);
                }
                //connectDevice(new Intent(), true);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void findPi(){
        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d("MainActivity", device.getName() + "\n" + device.getAddress());
                    foundDevices.add(device);
                    Log.d("MainActivity", "" + device.ACTION_UUID);
                    if(device.ACTION_UUID.equals( "dad8bf14-b6c3-45fa-b9a7-94c1fde2e7c6" )) {
                        Log.d("MainActivity", "we in");
                        try {
                            device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(device.ACTION_UUID));
                        }catch(IOException e){}
                        bluetoothAdapter.cancelDiscovery();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter);
        textView.setText("Looking For Pi");
        bluetoothAdapter.startDiscovery();
        /*big: for( BluetoothDevice btd : foundDevices) {
            if (btd.ACTION_UUID == "dad8bf14-b6c3-45fa-b9a7-94c1fde2e7c6") {
                textView.setText("Connecting to Pi....");
                try {
                    bluetoothSocket = btd.createRfcommSocketToServiceRecord(UUID.fromString(btd.ACTION_UUID));
                } catch (IOException e) {
                }
                textView.setText("Connected with Pi");
                break big;
            }
        }*/

    }
    private class ConnectedThread extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        public ConnectedThread(BluetoothSocket socket){
            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch(IOException e){}
            inputStream = tmpIn;
            outputStream = tmpOut;
        }
        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            }catch(IOException e){
            }
        }
        public void cancel(){
            try{
                bluetoothSocket.close();
            }catch(IOException e){
            }
        }
    }
}
