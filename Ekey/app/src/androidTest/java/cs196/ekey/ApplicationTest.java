package cs196.ekey;

import android.app.Application;
import android.test.ApplicationTestCase;

import android.widget.Button;
import android.widget.ListView;

import java.util.Set;
import java.util.ArrayList;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnClickListener;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
    }
    Button btnPaired;
    ListView devicelist;

    private BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
    private Set pairedDevices;

    if(myBluetooth == null)
    {
        //Show a mensag. that thedevice has no bluetooth adapter
        Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
        //finish apk
        finish();
    }
    else
    {
        if (myBluetooth.isEnabled())
        { }
        else
        {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }
    }

}