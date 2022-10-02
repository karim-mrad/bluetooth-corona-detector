package com.example.coronadetector1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //Creating the broadcast receiver for the discovery mode of the bluetooth device
    private BroadcastReceiver receiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Inside the receiver.");

            if (action.equalsIgnoreCase(BluetoothDevice.ACTION_FOUND)){
                Log.d(TAG, "Found bluetooth.");

                DeviceClass device = new DeviceClass((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));

                if(!macList.contains(device.macAddress)){
                    Toast.makeText(getApplicationContext(),"Discovered new device!", Toast.LENGTH_SHORT).show();
                    deviceList.add(device);
                    macList.add(device.macAddress);
                    saveData();
                    TextView tv =(TextView)findViewById(R.id.textView);
                    tv.setText("You have encountered " + Integer.toString(deviceList.size()) + " people.");
                    ref.child("User").child(user.getUid()).child("Encounters").child("N " + (deviceList.size() - 1)).setValue(device);
                    //else up.add(deviceList.size() -1);
                }
                else {
                    for(int i =0; i< deviceList.size();i++){
                        if(deviceList.get(i).macAddress.equals( device.macAddress)){
                            Log.d(TAG, "Changing calendars.");
                            deviceList.get(i).detectedDate = currentCalendar.getTime();
                            ref.child("User").child(user.getUid()).child("Encounters").child("N " + i).setValue(deviceList.get(i));
                            //else up.add(i);
                        }
                    }

                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Discovery ending.");
               /* if(uploadData()){
                    for(int i = 0; i<deviceList.size()-1;i++){
                        ref.child("User").child(user.getUid()).child("Encounters").child("N " + (up.get(i).intValue())).setValue(deviceList.get(up.get(i).intValue()));
                    }
                }*/
                if (repeat) finishedDiscovery();

            }
        }
    };

        Calendar currentCalendar = Calendar.getInstance();
        private boolean repeat = true;// Created to be able to turn off discovery
        public BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        private static final String TAG = "MainActivity";
        public ArrayList<DeviceClass> deviceList;
        public ArrayList<String> macList;
        public FirebaseAuth fAuth;
        public ProgressBar progressBar;
        public DatabaseReference ref;
        public FirebaseUser user;
        public ArrayList<Integer> up;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            repeat = true;
            fAuth = FirebaseAuth.getInstance();
            ref = FirebaseDatabase.getInstance().getReference().child("User");
            user  = fAuth.getCurrentUser();
            loadData();

            //Start activating bluetooth services

            //Check if bluetooth adapter exists
            if (btAdapter == null) {
                Toast.makeText(getApplicationContext(), "Bluetooth is not supported.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "No bluetooth device.");
            }

            //Turn on bluetooth adapter if it's turned off
            if (!btAdapter.isEnabled()) {
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i, 1);// Sent to onActivityResult to make sure of user input
            }
            //If it's already on, start discoverable
            else if (btAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Bluetooth is already on.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Bluetooth already on.");
                startDiscoverable();
                startDiscovery();
            }
            Button button = findViewById(R.id.button);// Initializing the buttons
            Button button2 = findViewById(R.id.button2);
            Button button3 = findViewById(R.id.button3);
            Button logOut = findViewById(R.id.logOut);


            button.setOnClickListener(this);// Sending the buttons  over to the listener
            button2.setOnClickListener(this);
            button3.setOnClickListener(this);
            logOut.setOnClickListener(this);



        }

    //First case: stopping discovery mode
    //Second case: start discovery back
    //Third case: delete saved data (used for debugging and checking)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                if(btAdapter.isDiscovering()){
                    Toast.makeText(getApplicationContext(),"Discovery stopping...", Toast.LENGTH_SHORT).show();
                    btAdapter.cancelDiscovery();
                    repeat = false;
                    Log.d(TAG, "Discovery is stopped.");
                    Log.d(TAG, btAdapter.getAddress());
                    }
                break;
            case R.id.button2:
                if(!btAdapter.isDiscovering()){
                    repeat = true;
                    startDiscovery();
                    Log.d(TAG, "Discovery is stopped.");
                }
                break;

            case R.id.button3:
                SharedPreferences sharedPreferences = getSharedPreferences( "shared preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.clear();
                editor.commit();

                SharedPreferences sharedPreferences2 = getSharedPreferences( "shared preferences2", MODE_PRIVATE);
                SharedPreferences.Editor editor2 = sharedPreferences2.edit();

                editor2.clear();
                editor2.commit();


                SharedPreferences sharedPreferences3 = getSharedPreferences( "shared preferences3", MODE_PRIVATE);
                SharedPreferences.Editor editor3 = sharedPreferences3.edit();

                editor3.clear();
                editor3.commit();

                loadData();
                break;

            case R.id.logOut:
                repeat = false;
                fAuth.signOut();
                startActivity(new Intent(getApplicationContext(),Login.class));


        }

        }

    //Method implemented to react to the user input, mainly used to keep asking for bluetooth to be turned on,
    //It the calls startDiscoverable()
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth is now enabled.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Bluetooth on.");
                startDiscoverable();
            }
            else {
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i, 1);// Keep asking until user turns on bluetooth
                }
            }

    //Changes the bluetooth mode to "Discoverable" after checking if it's on or off
    //It then calls startDiscovery()
    public void startDiscoverable(){

         if(btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){

            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(intent);
            Log.d(TAG, " Starting discovery.");
             startDiscovery();
         }
         else {
             Log.d(TAG,"Bluetooth already discoverable.");
            startDiscovery();
         }
}

    //If repeat flag is true, asks for the necessary permissions (required since Android V.6)
    //Turns on discovery mode, sets up intent filters for ACTION_FOUND (found a bluetooth device)
    // and ACTION_DISCOVERY_FINISHED (discovery mode timeout after 12s)
    //If discovery mode already on, re-send the intent filters to the broadcast receiver
    public void startDiscovery() {

            if (repeat) {
                int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                if (!btAdapter.isDiscovering()) {
                    btAdapter.startDiscovery();
                    Log.d(TAG, "Starting discovery.");
                    Toast.makeText(getApplicationContext(), "Bluetooth device started discovery.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Before intent filter.");
                    IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    Log.d(TAG, "Sending bluetooth to receiver.");
                    this.registerReceiver(receiver, filter1);

                    IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    this.registerReceiver(receiver, filter);
                } else {
                    Log.d(TAG, "Discovery already on.");
                    Log.d(TAG, "Before intent filter.");
                    IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    Log.d(TAG, "Sending bluetooth to receiver.");
                    this.registerReceiver(receiver, filter1);

                    IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    this.registerReceiver(receiver, filter);
                }
            }
    }

    // Method that works with re-turning on discovery if the repeat flag is true: Creating intent filters and sending to the receiver
    public void finishedDiscovery(){
        if(repeat){
            btAdapter.startDiscovery();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);

            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);

        }
    }

    //saveData() uses the Gson library to store our ArrayLists in the "Shared Preferences" section of the local storage
    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences( "shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(deviceList);
        editor.putString("task list", json);
        editor.apply();

        SharedPreferences sharedPreferences2 = getSharedPreferences( "shared preferences2", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sharedPreferences2.edit();
        Gson gson2 = new Gson();
        String json2 = gson2.toJson(macList);
        editor2.putString("task list2", json2);
        editor2.apply();

        SharedPreferences sharedPreferences3 = getSharedPreferences( "shared preferences3", MODE_PRIVATE);
        SharedPreferences.Editor editor3 = sharedPreferences3.edit();
        Gson gson3 = new Gson();
        String json3 = gson3.toJson(up);
        editor3.putString("task list3", json3);
        editor3.apply();



    }

    //loadData() makes use of the Gson library to retrieve the previously stored ArrayLists
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences( "shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task list", null);
        Type type = new TypeToken<ArrayList<DeviceClass>>(){}.getType();
        deviceList = gson.fromJson(json, type);

        if(deviceList == null){
            deviceList = new ArrayList<>();
        }

        SharedPreferences sharedPreferences2 = getSharedPreferences( "shared preferences2", MODE_PRIVATE);
        Gson gson2 = new Gson();
        String json2 = sharedPreferences2.getString("task list2", null);
        Type type2 = new TypeToken<ArrayList<String>>(){}.getType();
        macList = gson2.fromJson(json2, type2);

        if(macList == null){
            macList = new ArrayList<>();
        }

        SharedPreferences sharedPreferences3 = getSharedPreferences( "shared preferences3", MODE_PRIVATE);
        Gson gson3 = new Gson();
        String json3 = sharedPreferences3.getString("task list3", null);
        Type type3 = new TypeToken<ArrayList<Integer>>(){}.getType();
        macList = gson3.fromJson(json3, type3);

        if(up == null){
            up = new ArrayList<Integer>();
        }

        TextView tv =(TextView)findViewById(R.id.textView);
        tv.setText("You have encountered " + Integer.toString(deviceList.size()) + " people.");

    }

    //Checks for a connection, and then should upload data
    public boolean uploadData(){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;

        return connected;
        }

}
