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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class BoardControls extends AppCompatActivity {

    //stuff for bluetooth
    private Set<BluetoothDevice> pairedDevices;

    Button btnOn, btnOff, btnDis, btnStart, btnStop;
    SeekBar brightness;
    TextView lumn;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static String EXTRA_ADDRESS = "device_address";

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_controls);


        TextView speed_textview = (TextView) findViewById(R.id.speed_textview);
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        LocationListener ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                speed_textview.setText(getSpeed(location) + "");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, ll);


        Random rng = new Random();

        String message = null;

        //call the widgets
        btnOn = (Button)findViewById(R.id.onButton);
        btnOff = (Button)findViewById(R.id.offButton);
        btnDis = (Button)findViewById(R.id.button4);
        brightness = (SeekBar)findViewById(R.id.seekBar);
        lumn = (TextView)findViewById(R.id.lumn);
        btnStart = (Button)findViewById(R.id.startButton);
        btnStop = (Button)findViewById(R.id.stopButton);

        //---------------------------------------Check if the device has bluetooth------------------------------------

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
        }
        else if(!myBluetooth.isEnabled())
        {
            //Ask user to turn on bluetooth
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }


        //--------------------------------------Show dialog with paired devices list------------------------------------
        alertDialog();

        //new ConnectBT().execute(); //Call the class to connect

        //----------------------------------------Start of Receiving From Arduino----------------------------------------
        try{
            byte [] bytes_from_arduino = new byte[64];
            btSocket.getInputStream().read(bytes_from_arduino);
            message = bytes_from_arduino.toString();

        }
        catch (Exception e){
            //nah nothing will go wrong.......
        }

        //-----------------Start of Handling Info From the Arduino-----------------
        if(message == "on"){

        }
        else if(message == "off"){

        }
        else if(message == "start"){

        }
        else if(message == "stop"){

        }
        else{//to see what the message from arduino is if it doesn't match anything in the if-else chain
            Toast.makeText(this, "The message variable = " + message, Toast.LENGTH_LONG).show();
        }
        //-----------------End of Handling Info From the Arduino-----------------


        //----------------------------------------End of Receiving From Arduino----------------------------------------


        //----------------------------------------Start of Commands to Send to Arduino----------------------------------------

        //------------------turn on board------------------
        btnOn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                turnOnBoard();      //method to turn on
            }
        });

        //------------------turn off board------------------
        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                turnOffBoard();   //method to turn off
            }
        });

        //------------------start movement------------------
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startBoard();   //method to start board
            }
        });

        //------------------stop movement------------------
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                stopBoard();   //method to stop board
            }
        });

        //------------------Disconnect------------------
        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });
        //----------------------------------------End of Commands to Send to Arduino----------------------------------------


        //brightness slider bar thing that might be taken out or reused for something else.....
        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser==true)
                {
                    lumn.setText(String.valueOf(progress));
                    try
                    {
                        btSocket.getOutputStream().write(String.valueOf(progress).getBytes());
                    }
                    catch (IOException e)
                    {

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    //----------------------------------------Start of Private Stuff That Does The Low Level Stuff----------------------------------------
    Location pre_loc = null;
    private float getSpeed(Location curr_loc){
        if(curr_loc.hasSpeed()){return curr_loc.getSpeed();}
        if(pre_loc != null){
            float distance_traveled = pre_loc.distanceTo(curr_loc);
            long time_since_last_location = curr_loc.getTime() - pre_loc.getTime();
            return (distance_traveled/time_since_last_location);
        }
        else {
            pre_loc = curr_loc;
            return 0;
        }

    }


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

    private void turnOffBoard()//DISARMS THE ESC
    {
        if (btSocket!=null)
        {
            int message_id =  + (rng.nextInt(89)+10);
            //String message = "d" + message_id;
            String message = "off";
            try {
                btSocket.getOutputStream().write(message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            //protocol_thread pt = new protocol_thread(message,message_id);
            //Thread disarm_thread = new Thread(pt);
            //disarm_thread.start();

            //String message = "d" + (rng.nextInt(89)+10);
            //btSocket.getOutputStream().write(message.getBytes());

            // finish();


        }
    }

    private void turnOnBoard()//ARMS THE ESC
    {

        if (btSocket!=null)
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
        }
    }

    private void startBoard()//STARTS BOARD MOVEMENT AND LAUNCHES DRIVE MODE ACTIVITY
    {
        if (btSocket!=null)
        {
            try
            {
                int message_id =  + (rng.nextInt(89)+10);
                //String message = "g" + message_id;
                String message = "on";
                btSocket.getOutputStream().write(message.getBytes());
                message = "start";
                btSocket.getOutputStream().write(message.getBytes());
                Disconnect();


                // Make an intent to start next activity.
                Intent i = new Intent(BoardControls.this, DriveMode.class);

                //Change the activity.
                i.putExtra(EXTRA_ADDRESS, address); //this will be received at DriveMode (class) Activity
                startActivity(i);

            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void stopBoard()//STOPS BOARD MOVEMENT
    {
        if (btSocket!=null)
        {
            try
            {
                int message_id =  + (rng.nextInt(89)+10);
                //String message = "s" + message_id;
                String message = "stop";
                btSocket.getOutputStream().write(message.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    //----------------------------------------------------Start of Temp. Rocker Controls For Testing----------------------------------------------------
    final int maxSpeed = 120;
    final int minSpeed = 100;
    int speed = 100;
    MediaPlayer atMax;
    MediaPlayer atMin;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            accelerateBoard();

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            decelerateBoard();

            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    private void accelerateBoard(){
        if (btSocket!=null)
        {
            try
            {
                //speed range 100-118
                if (speed <= maxSpeed){//UPPER ROCKER BUTTON
                    int message_id =  + (rng.nextInt(89)+10);
                    speed += 2;
                    //String message = "v" + message_id;
                    String message = "accel";
                    btSocket.getOutputStream().write(message.getBytes());
                    //message = "on";
                    //btSocket.getOutputStream().write(message.getBytes());

                } else{
                    //speed should equal 120
                    speed = maxSpeed;
                    Toast.makeText(this, "Max speed", Toast.LENGTH_SHORT).show();
                    atMax.start();


                }
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void decelerateBoard(){//LOWER ROCKER BUTTON
        if (btSocket!=null)
        {
            try {
                //speed range 102-120
                if (speed > minSpeed) {
                    int message_id =  + (rng.nextInt(89)+10);
                    speed -= 2;
                    //String message = "p" + message_id;
                    String message = "decel";
                    btSocket.getOutputStream().write(message.getBytes());
                    //message = "on";
                    //btSocket.getOutputStream().write(message.getBytes());

                }
                else{
                    //speed should == 85
                    speed = minSpeed;

                    Toast.makeText(this, "Lowest speed", Toast.LENGTH_SHORT).show();
                    atMin.start();
                }
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

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