package com.ekey.appversion10;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;
import javax.crypto.Cipher;

public class MainActivity extends AppCompatActivity {

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("dad8bf14-b6c3-45fa-b9a7-94c1fde2e7c6");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("dad8bf14-b6c3-45fa-b9a7-94c1fde2e7c6");
    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;


    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;
    private ConnectedThread mConnectedThread = null;
    Button unlock;
    Button connect;
    //Button turnon;
    Boolean doorState = false; //starts locked
    private PublicKey p = null;
    Resources r;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unlock = (Button) findViewById(R.id.button);
        connect = (Button) findViewById(R.id.button2);
        MainActivity a = new MainActivity();
        r = getResources();
        try {
            p = get();
        } catch (Exception e) {
            //Toast.makeText(a, "aww shucks", Toast.LENGTH_SHORT);
            Log.e(TAG, "FAILURE");
            e.printStackTrace();
        }
        //Toast.makeText(a,p.getAlgorithm(),Toast.LENGTH_LONG );
        //turnon = (Button) findViewById(R.id.button3);
        //Intent serverIntent = new Intent(this, DeviceListActivity.class);
        unlock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                if(!doorState){
                    Log.e(TAG, "Unlocking");
                    doorState = true;
                    try {
                        sendMessage("unlock");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.e(TAG, "Locking");
                    doorState = false;
                    try {
                        sendMessage("lock");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        connect.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE_INSECURE);
                //connectDevice(serverIntent, false);
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            MainActivity activity = new MainActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        setupChat();
        ensureDiscoverable();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
       // mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        //mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
       // mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService();

        // Initialize the buffer for outgoing messages
        //mOutStringBuffer = new StringBuffer("");
    }
    private void sendMessage(String message) throws Exception {
        // Check that we're actually connected before trying anything
        /*if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this , "your not connected", Toast.LENGTH_SHORT).show();
            return;
        }*/
        Cipher c = Cipher.getInstance("RSA");
        c.init(Cipher.ENCRYPT_MODE, p);
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = c.doFinal(message.getBytes());
            mConnectedThread.write(send);

            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    private void connectDevice(Intent data, boolean secure) {
        Log.e(TAG, "AYYYY in connected DEVICE");
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        Log.e(TAG, "Go remote device now onto btooth connect");
        connect(device, secure);
        // Attempt to connect to the device
        //mChatService.connect(device, secure);
        //mChatService.start();
    }

    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.e(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        /*if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }*/
        Log.e(TAG, "Moving on to Connected Threaddddddddsadfjds;lfkjdsaf;lsadkfjdsa " + device);
        // Start the thread to connect with the given device
        ConnectThread mConnectThread = new ConnectThread(device, secure);
        Log.e(TAG, "STARTINGGGGGGGGGGGG");
        mConnectThread.start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            secure = true;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            Log.e(TAG, "socket is " + mmSocket.toString());
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.e(TAG, "attempting to connect mmSocket");
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                Log.e(TAG, "connection failed");
                Log.e(TAG, e.toString());
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                //connectionFailed();
                return;
            }

//            // Reset the ConnectThread because we're done
//            synchronized (BluetoothChatService.this) {
//                mConnectThread = null;
//            }

            // Start the connected thread
            //connected(mmSocket, mmDevice, mSocketType);
            mConnectedThread = new ConnectedThread(mmSocket, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                Log.e(TAG, "hola im in secure");
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    Log.e(TAG, "connectDevice called");
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                Log.e(TAG, "hola im in enable bt");
                /*// When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "BT is not enabled by",
                            Toast.LENGTH_SHORT).show();
                    this.finish();
                }*/
                break;
        }
    }
    class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    // Start the service over to restart listening mode
                    //BluetoothChatService.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    public PublicKey get()throws Exception{
            //File f = new File("");
            //FileInputStream fis = new FileInputStream(f);
            InputStream is = r.openRawResource(R.raw.pkey);
            Log.e(TAG, "Inputted Stream");
            DataInputStream dis = new DataInputStream(is);
            Log.e(TAG, "EEEEEEEEEEEEE");
            byte[] keyBytes = new byte[2000];
            try{
                dis.readFully(keyBytes);
            }catch(EOFException e){
                Log.e(TAG, "caught not thrown");
            }
            dis.close();
            Log.e(TAG, "FFFFFFFFFFFFF");
            String temp = new String(keyBytes);
            temp = temp.replace("-----BEGIN PUBLIC KEY-----", "");
            temp = temp.replace("-----END PUBLIC KEY-----", "");
            temp = temp.trim();
            Log.e(TAG,temp);
            //X509EncodedKeySpec spec = new X509EncodedKeySpec(temp.getBytes());
            byte[] encoded = Base64.decode(temp, Base64.DEFAULT);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded);
            Log.e(TAG, "ITSALLIVEEEE");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            Log.e(TAG, "Here be the problem");
            PublicKey p = kf.generatePublic(spec);
            Log.e(TAG, "NO HERE BE THE PROBLEM");
            return p;
        }
}
