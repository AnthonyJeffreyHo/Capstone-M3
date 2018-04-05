package edu.temple.ollycontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class BoardControls extends AppCompatActivity {

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_controls);

        Random rng = new Random();

        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device


        String message = null;

        //call the widgtes
        btnOn = (Button)findViewById(R.id.onButton);
        btnOff = (Button)findViewById(R.id.offButton);
        btnDis = (Button)findViewById(R.id.button4);
        brightness = (SeekBar)findViewById(R.id.seekBar);
        lumn = (TextView)findViewById(R.id.lumn);
        btnStart = (Button)findViewById(R.id.startButton);
        btnStop = (Button)findViewById(R.id.stopButton);


        new ConnectBT().execute(); //Call the class to connect

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
            String message = "d" + message_id;

            protocol_thread pt = new protocol_thread(message,message_id);
            Thread disarm_thread = new Thread(pt);
            disarm_thread.start();

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
                String message = "a" + message_id;
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
                String message = "g" + message_id;
                btSocket.getOutputStream().write(message.getBytes());
                //message = "on";
                //btSocket.getOutputStream().write(message.getBytes());


                // Make an intent to start next activity.
                Intent i = new Intent(BoardControls.this, DriveMode.class);

                //Change the activity.
                i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
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
                String message = "s" + message_id;
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
                    String message = "v" + message_id;
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
                    String message = "p" + message_id;
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
