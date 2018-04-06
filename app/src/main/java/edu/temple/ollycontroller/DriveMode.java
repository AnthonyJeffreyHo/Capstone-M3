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
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
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
    boolean turnedOff = false;
    MediaPlayer atMax;
    MediaPlayer atMin;
    ArrayList<String> results;

    int speed = 100;
    Button stopButton;
    Button btnLeft, btnRight, btnStop, btnStart, btnOn, btnOff;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onDestroy(){
        super.onDestroy();
        turnOffBoard();
    }

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
        btnStart = (Button) findViewById(R.id.start_button);
        btnOn = (Button) findViewById(R.id.on_button);
        btnOff = (Button) findViewById(R.id.off_button);

        atMax = MediaPlayer.create(this, R.raw.pew);
        atMin = MediaPlayer.create(this, R.raw.strange);


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
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBoard();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopBoard();
                //finish();
                //program a thing to pop the activity form the stack
            }
        });

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnOnBoard();
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnOffBoard();
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




        btnLeft.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                //specify free form input
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please start speaking");
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

                startActivityForResult(intent, 2);return true;
            }

        });

        btnRight.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                //specify free form input
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please start speaking");
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

                startActivityForResult(intent, 2);return true;
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





    //--------------------------Low Level Stuff--------------------------

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
        speed = 100;
        if (btSocket!=null)
        {
            try
            {//a for arm
                turnedOff = false;
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
        speed = 100;
        if(!turnedOff) {
            speed = 100;
            if (btSocket != null) {
                turnedOff=true;
                int message_id = +(rng.nextInt(89) + 10);
                //String message = "d" + message_id;

                try {
                    String message = "off";
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
        speed = 100;
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

    private void startBoard()//STARTS BOARD MOVEMENT AND LAUNCHES DRIVE MODE ACTIVITY
    {
        String message;
        speed = 100;
        if (btSocket!=null)
        {
            try
            {
                int message_id =  + (rng.nextInt(89)+10);
                //String message = "g" + message_id;
                //String message = "off";
                //btSocket.getOutputStream().write(message.getBytes());
                 //message = "on";
                //btSocket.getOutputStream().write(message.getBytes());
                message = "start";
                btSocket.getOutputStream().write(message.getBytes());
                //Disconnect();


                // Make an intent to start next activity.
                //Intent i = new Intent(BoardControls.this, DriveMode.class);

                //Change the activity.
                //i.putExtra(EXTRA_ADDRESS, address); //this will be received at DriveMode (class) Activity
                //startActivity(i);

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
                    //int message_id =  + (rng.nextInt(89)+10);
                    speed += 2;
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

    private void decelerateBoard(){
        if (btSocket!=null)
        {
            try {
                //speed range 102-120
                if (speed > minSpeed) {
                    int message_id =  + (rng.nextInt(89)+10);
                    speed -= 2;
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
                startBoard();
                turnOnBoard();
            }
            progress.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 2){
            results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String text = results.get(0);

            switch (text){
                case "stop":
                    stopBoard();
                    break;
                case "speed up":
                    accelerateBoard();
                    break;
                case "slow down":
                    decelerateBoard();
                    break;
                case "right":
                    rightTurn();
                    break;
                case "left":
                    leftTurn();
                    break;
                case "on":
                    turnOnBoard();
                    break;
                case "off":
                    turnOnBoard();
                    break;
                case "start":
                    startBoard();
                    break;
                case "giraffe":
                    Toast.makeText(this, "Why?", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, "Command not recognized", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }




}
