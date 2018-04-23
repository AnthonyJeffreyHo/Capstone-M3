package edu.temple.ollycontroller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class BoardControls extends AppCompatActivity {

    //stuff for bluetooth
    private Set<BluetoothDevice> pairedDevices;

    Button btnOn, btnDis;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private boolean stopMe = false;
    private boolean startMe = false;
    private boolean tog = true;
    Spinner mySpinner;
    public static final String[] speeds = new String[] {"Beginner", "Intermediate", "Pro", "Expert", "ludicrous","Wumbo"};
    String level = "120";

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static String EXTRA_ADDRESS = "device_address";
    public static String SPEED_LEVELS = "Speed";


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_controls);



        // Random rng = new Random();

        String message = null;



        btnOn = (Button) findViewById(R.id.buttonOn);
        btnDis = (Button) findViewById(R.id.buttonDisconnect);


        //call the widgets

        //---------------------------------------Check if the device has bluetooth------------------------------------

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
        } else if (!myBluetooth.isEnabled()) {
            //Ask user to turn on bluetooth
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }


        //--------------------------------------Show dialog with paired devices list------------------------------------
        //alertDialog();

        //new ConnectBT().execute(); //Call the class to connect

        //----------------------------------------Start of Receiving From Arduino----------------------------------------
        try {
            byte[] bytes_from_arduino = new byte[64];
            btSocket.getInputStream().read(bytes_from_arduino);
            message = bytes_from_arduino.toString();

        } catch (Exception e) {
            //nah nothing will go wrong.......
        }

        //----------------------------------------End of Receiving From Arduino----------------------------------------


        //----------------------------------------Start of Commands to Send to Arduino----------------------------------------

        //------------------turn on board------------------
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                turnOnBoard();   //method to turn on
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();   //method to disconnect board
            }
        });

        //----------------------------------------End of Commands to Send to Arduino----------------------------------------


        //-----------------Spinner IMPLEMENTATION-----------------------------------

        mySpinner = (Spinner) findViewById(R.id.spinner);              //connect spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, speeds);
//set the spinners adapter to the previously created one.
        mySpinner.setAdapter(adapter);



        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {

                } else if (position == 1) {
                    level = "104";
                } else if (position == 2) {
                    level = "108";
                } else if (position == 3) {
                    level = "112";
                } else if (position == 4) {
                    level = "116";
                } else if (position == 5) {
                    level = "120";
                } else if (position == 6) {
                    Toast.makeText(BoardControls.this, "YOU'RE MAD!!!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // sometimes you need nothing here
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},111);
        }
    }
    //----------------------------------------Start of Private Stuff That Does The Low Level Stuff----------------------------------------


        final Random rng = new Random();

    private void Disconnect()//DISCONNECTING THE PHONE FROM THE BLUETOOTH MODULE
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }


    private void turnOnBoard()//ARMS THE ESC
    {

      /*  if (btSocket!=null)
        {
            try
            {//a for arm
                int message_id =  + (rng.nextInt(89)+10);
                //String message = "a" + message_id;
                String message = "on";
                btSocket.getOutputStream().write(message.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }*/
        Toggler();
        Intent i = new Intent(BoardControls.this, DriveMode.class);

        //Change the activity.
        i.putExtra(EXTRA_ADDRESS, address); //this will be received at DriveMode (class) Activity
        if (tog == true) {
            i.putExtra(SPEED_LEVELS, level);
        }
        else{
            i.putExtra(SPEED_LEVELS, "120");
        }
        startActivity(i);
    }


    private void Toggler(){

        ToggleButton toggle = (ToggleButton) findViewById(R.id.speedToggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tog = true;
                } else {
                    tog = false;
                }
            }
        });

    }


    //----------------------------------------------------Start of Temp. Rocker Controls For Testing----------------------------------------------------

    //----------------------------------------------------End of Temp. Rocker Controls For Testing----------------------------------------------------


    //----------------------------------------------------Alert Dialog Method---------------------------------------------------

    private void alertDialog(){

        pairedDevices = myBluetooth.getBondedDevices();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Devices");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(BoardControls.this, android.R.layout.select_dialog_singlechoice);


        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                arrayAdapter.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alert.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String info = arrayAdapter.getItem(i);
                address = info.substring(info.length() - 17);
                new ConnectBT().execute();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();

    }

    //----------------------------------------------------Menu info-------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
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


    //-----------------------------------------------End of Menu----------------------------------------------------------


    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(BoardControls.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    class protocol_thread implements Runnable{
        String app_message;
        int message_id;

        byte[] arduino_message_byte_array;
        String arduino_ok_message = "";
        boolean got_message;

        Thread wait_thread = new Thread();

        //constructor for the thread. gets the message and the id
        protocol_thread (String message, int m_id){
            app_message = message;
            message_id = m_id;
        }

        @Override
        public void run(){
            got_message = false;
            while(!got_message){
                got_message = did_app_get_message();
            }


        }
        public boolean did_app_get_message(){
            try{
                //this will write the message out to the arduino
                btSocket.getOutputStream().write(app_message.getBytes());

                //this will cause the runnable to wait for half a second
                wait_thread.wait(500);

                //this will get a message from the arduino
                btSocket.getInputStream().read(arduino_message_byte_array);

                //this converts the byte array into a string
                arduino_ok_message = arduino_message_byte_array.toString();

                //if else chain to check the returning message
                if(arduino_ok_message != "k"){
                    return false;
                }
                else if(arduino_ok_message == "k"){
                    return true;
                }
                else{
                    return false;
                }

            }
            catch(Exception e){

            }
            return false;
        }

    }


    //----------------------------------------End of Private Stuff That Does The Low Level Stuff----------------------------------------
}