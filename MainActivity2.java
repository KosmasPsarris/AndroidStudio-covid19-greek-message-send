package com.p17191.ergasies.exercise2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity implements LocationListener {

    MyTts myTts; // For TextToSpeech
    SQLiteDatabase db; // For our database to save messages and codes
    FirebaseUser firebaseUser;
    LocationManager locationManager; // For gps data
    FirebaseDatabase firebaseDatabase; // For our firebase database
    DatabaseReference databaseReference;
    Button sendButton, speechButton, LogOutButton, EditModeButton;
    EditText nameEditText, addressEditText;
    RadioGroup radioGroup; // For group of messages
    private static final int REQ_CODE_GPS = 123;
    private static final int REQ_CODE_SMS = 456;


    // When data is returned from speech recognition, this method is called. Used to handle the data
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MainActivity.REC_RESULT && resultCode == RESULT_OK){ // requestCode = our REC_RESULT | resultCode = if everything went okay ( user spoke and machine recognised the message)
            // Data returned from voice recognition is basically matches between what the user said and what the machine thinks the user said
            // Top of the list are the best matches and as we iterate through the ArrayList, the least probable matches are stored
            // If we have phrases we need to check each phrase with the one we want
            // Also we need to have one general letter case so we don't have match problems.
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // Check user's command
            if(matches.get(0).toLowerCase().equals("log out"))
                LogOutButton.performClick();
            else if(matches.get(0).toLowerCase().equals("edit mode"))
                EditModeButton.performClick();
            else if(matches.get(0).toLowerCase().equals("send"))
                sendButton.performClick();
            else {
                myTts.speak("That is not a valid command.");
                Toast.makeText(getApplicationContext(),"That is not a valid command.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        myTts = new MyTts(this); // We put it onCreate so we give some time to the initListener to get ready

        radioGroup = findViewById(R.id.radiogroup); // Radio group for messages

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); // Get service required for gps


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); // Get instance of firebase authentication
        firebaseUser = firebaseAuth.getCurrentUser(); // Get current logged in user

        firebaseDatabase = FirebaseDatabase.getInstance(); // Instance of our realtime database data
        databaseReference = firebaseDatabase.getReference(firebaseUser.getUid()); // Current user node


        // Create database or open it if it's already created
        db = openOrCreateDatabase("MessagesDB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Messages(Code INTEGER, Message TEXT)");

        // Get all messages
        Cursor cursor = db.rawQuery("SELECT * FROM Messages", null);
        if(cursor.getCount() > 0){ // If there is at least one message in messages table

            while(cursor.moveToNext()){ // While there is data in table create radio buttons

                RadioButton radioButton = new RadioButton(this); // Create new radio button
                radioButton.setId(View.generateViewId()); // Attach new non given id
                radioButton.setText(cursor.getInt(0)+". "+cursor.getString(1)); // Set text based on code and message
                radioGroup.addView(radioButton);
            }
            cursor.close();
        }



        LogOutButton = findViewById(R.id.button4); // Log out from user's account
        LogOutButton.setOnClickListener((view) -> {
            // Clear radiogroup selection and delete all radio buttons as we don't want to keep creating
            // the same buttons on top of each other. We create them based on the messages on our Database
            // every time we go to activity2, so going back and forth will create radio buttons on top of already created ones
            radioGroup.clearCheck();
            radioGroup.removeAllViews();

            startActivity(new Intent(MainActivity2.this, MainActivity.class)); // Go to activity1

            myTts.speak("Log out successful.");
            Toast.makeText(getApplicationContext(),"Log out successful.",Toast.LENGTH_SHORT).show();
        });


        sendButton = findViewById(R.id.button12); // Send selected message to 13033
        sendButton.setOnClickListener((view) -> {

            // Get selected radio button from radioGroup
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if(selectedId != -1) { // If selectedId is not null (-1), so there is a radio button selected

                // If user gave their name and their address correctly
                if(!nameEditText.getText().toString().equals("") && !addressEditText.getText().toString().equals("")) {


                    // In order to collect gps data we need permission of user so we can access their location
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,
                                REQ_CODE_GPS);
                        return; // Permission denied
                    }
                    // Permission granted for gps

                    if(ActivityCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)!=
                    PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},
                                REQ_CODE_SMS);
                    }else{ // SMS permission granted
                        // Start requesting location updates/data
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

                        // Find the radiobutton by returned id
                        RadioButton radioButton = (RadioButton) findViewById(selectedId);

                        // Create message to send based on correct data user gave us
                        String finalMessageToSend;
                        finalMessageToSend = "ΜΕΤΑΚΙΝΗΣΗ "+ radioButton.getText().toString().charAt(0) + " " +
                                nameEditText.getText().toString().toUpperCase() + " " +
                                addressEditText.getText().toString().toUpperCase();

                        // Send the final message to number 13033
                        SmsManager manager = SmsManager.getDefault();
                        manager.sendTextMessage(String.valueOf(13033), null, finalMessageToSend, null, null);

                        // Reset values
                        nameEditText.setText("");
                        addressEditText.setText("");
                        radioGroup.clearCheck();

                        myTts.speak("Message sent successfully.");
                        Toast.makeText(getApplicationContext(),"Message sent successfully.",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    myTts.speak("Please fill in both name and address.");
                    Toast.makeText(getApplicationContext(),"Please fill in both name and address.",Toast.LENGTH_SHORT).show();
                }
            }
            else{ // If selectedId is null (-1), so there is no radio button selected
                myTts.speak("Please select a message.");
                Toast.makeText(getApplicationContext(),"Please select a message.",Toast.LENGTH_SHORT).show();
            }
        });


        EditModeButton = findViewById(R.id.button5); // Move to edit of messages activity
        EditModeButton.setOnClickListener((view) -> {
                // Clear radiogroup selection and delete all radio buttons as we don't want to keep creating
                // the same buttons on top of each other. We create them based on the messages on our Database
                // every time we go to activity2, so going back and forth will create radio buttons on top of already created ones
                radioGroup.clearCheck();
                radioGroup.removeAllViews();

                startActivity(new Intent(MainActivity2.this, MainActivity3.class)); // Go to activity3
        });

        speechButton = findViewById(R.id.button11); // Start speech recognition
        // On click start listening for user voice. open -> recognise -> return data
        speechButton.setOnClickListener((view) -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // Start new intent
            // We prefer the extra model and the free form of speech
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What action would you like to perform?(log out, edit mode, send)"); // Ask user to give command to sign in or sing up
            startActivityForResult(intent, MainActivity.REC_RESULT); // We use ForResult because we need to wait fo a result to be returned
            // REC_RESULT is used so we know which return we are talking about
        });

        nameEditText = findViewById(R.id.editTextName); // Name and surname for sms
        addressEditText = findViewById(R.id.editTextAddress); // Address for sms
    }

    // Imported when we our class implemented interface LocationListener, gets called every time our location is changed
    @Override
    public void onLocationChanged(@NonNull Location location) {

        // After permission is granted and we started the location updates, we get the information we need with each update
        double x = location.getLatitude();
        double y = location.getLongitude();
        long timestamp = location.getTime();

        // Write location and timestamp when message is send to node of the current user
        databaseReference.push().setValue("Latitude: "+x+" Longitude: "+y+" Timestamp: "+timestamp);

        locationManager.removeUpdates(this); // Stop gps updates after we got 1 update, we don't need multiple
    }
}