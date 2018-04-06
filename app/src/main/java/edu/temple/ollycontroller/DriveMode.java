package edu.temple.ollycontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Created by krati on 4/4/18.
 */

public class DriveMode extends AppCompatActivity{

    private Set<BluetoothDevice> pairedDevices;

    final int maxSpeed = 120;
    final int minSpeed = 100;
    MediaPlayer atMax;
    MediaPlayer atMin;

    int speed = 100;
    Button stopButton;
    Button btnLeft, btnRight, btnStop;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_mode);



        Intent newint = getIntent();
        address = newint.getStringExtra(BoardControls.EXTRA_ADDRESS); //receive the address of the bluetooth device

        alertDialog();

        String message = null;

        //call the widgtes
        btnStop = (Button) findViewById(R.id.stopButton);
        stopButton = (Button) findViewById(R.id.driveStop);
        btnLeft = (Button) findViewById(R.id.leftButton);
        btnRight = (Button) findViewById(R.id.rightButton);

        atMax = MediaPlayer.create(DriveMode.this, R.raw.pew);
        atMin = MediaPlayer.create(DriveMode.this, R.raw.strange);


        //new ConnectBT().execute(); //Call the class to connect

        try {
            byte[] bytes_from_arduino = new byte[64];
            btSocket.getInputStream().read(bytes_from_arduino);
            message = bytes_from_arduino.toString();

        } catch (Exception e) {
            //nah nothing will go wrong.......
        }

        if (message == "stop") {

        } else if(message == "on"){

        } else if (message == "off"){

        } else if (message == "accel") {

        } else if (message == "decel") {

        } else if (message == "left"){

        } else if (message == "right"){

        } else {
            Toast.makeText(this, "The message variable = " + message, Toast.LENGTH_LONG).show();
        }

        //------------------------------------------------------------------------------------------------------------------------------
        //commands to be sent to bluetooth

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopBoard();
                finish();
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftTurn();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rightTurn();
            }
        });

    }


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

    private void leftTurn(){
        if (btSocket!=null)
        {
            try
            {
                int message_id =  + (rng.nextInt(89)+10);
                //String message = "l" + message_id;
                String message = "left";
                btSocket.getOutputStream().write(message.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void rightTurn(){
        if (btSocket!=null)
        {
            try
            {
                int message_id =  + (rng.nextInt(89)+10);
                //String message = "r" + message_id;
                String message = "right";
                btSocket.getOutputStream().write(message.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void stopBoard(){
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

    Random rng = new Random();

    private void accelerateBoard(){
        if (btSocket!=null)
        {
            try
            {
                //speed range 100-118
                if (speed < maxSpeed){
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

    private void decelerateBoard(){
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



    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


    private void alertDialog(){

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = myBluetooth.getBondedDevices();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Devices");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(DriveMode.this, android.R.layout.select_dialog_singlechoice);


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




    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(DriveMode.this, "Connecting...", "Please wait!!!");  //show a progress dialog
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







}
