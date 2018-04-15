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
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Created by krati on 4/4/18.
 */

public class DriveMode extends AppCompatActivity implements OnMapReadyCallback {

    private Set<BluetoothDevice> pairedDevices;

    int maxSpeed = 120;
    String intentSpeed = "120";
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
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";


    MapView map;
    GoogleMap google_maps;
    LatLng user_location;

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
        intentSpeed = newint.getStringExtra(BoardControls.SPEED_LEVELS);

        maxSpeed = Integer.parseInt(intentSpeed);

        //alertDialog();

        String message = null;

        //call the widgtes
        stopButton = (Button) findViewById(R.id.driveStop);
        btnLeft = (Button) findViewById(R.id.leftButton);
        btnRight = (Button) findViewById(R.id.rightButton);
        btnStart = (Button) findViewById(R.id.start_button);
        btnOff = (Button) findViewById(R.id.off_button);

        btnOn = (Button) findViewById(R.id.buttonOn);
        atMax = MediaPlayer.create(this, R.raw.pew);
        atMin = MediaPlayer.create(this, R.raw.strange);



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

      /*  btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnOnBoard();
            }
        }); */

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




        //-----------------------Start of GOOGLE MAPS And Speed Tracking-----------------------




        //------------------------------------Start of Speed Tracking------------------------------------
        final TextView speed_textview = (TextView) findViewById(R.id.speed_text);
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);



        LocationListener ll = new LocationListener() {
            double lat;
            double lng;





            @Override
            public void onLocationChanged(Location location) {
                speed_textview.setText("Current Speed: " + (getSpeed(location)*(2.23694) + " MPH"));

                lat = location.getLatitude();
                lng = location.getLongitude();

                user_location = new LatLng(lat,lng);

                //CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(user_location,16);
                google_maps.clear();
                google_maps.addMarker(new MarkerOptions().position(user_location).title("You"));
                google_maps.moveCamera(CameraUpdateFactory.newLatLng(user_location));




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

        //------------------------------------End of Speed Tracking------------------------------------

        @SuppressLint("MissingPermission")
        Location current_location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        user_location = new LatLng(current_location.getLatitude(),current_location.getLongitude());

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        // mapFragment.onCreate(savedInstanceState);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        google_maps = googleMap;
       // CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(user_location,16);
       // google_maps.animateCamera(cu);
        //MarkerOptions pin = new MarkerOptions().position(user_location);
       // Toast.makeText(this, "user latlng = " + user_location, Toast.LENGTH_SHORT).show();
       // googleMap.addMarker(pin);
        //Toast.makeText(this, "made it here", Toast.LENGTH_SHORT).show();

        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        //LatLng sydney = new LatLng(-33.852, 151.211);
        googleMap.addMarker(new MarkerOptions().position(user_location).title("You"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(user_location));
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(user_location,16);
        google_maps.animateCamera(cu);
    }

    //-----------------------End of GOOGLE MAPS And Speed Tracking-----------------------


    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
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


   /* private void turnOnBoard()//ARMS THE ESC
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
    } */

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
                Disconnect();
                 finish();

            }
        }
    }

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
                String message = "on";
                try {
                    btSocket.getOutputStream().write(message.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                case "off":
                    turnOffBoard();
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







}
