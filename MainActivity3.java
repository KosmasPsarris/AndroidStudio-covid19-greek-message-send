package com.p17191.ergasies.exercise2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity3 extends AppCompatActivity {

    MyTts myTts; // For TextToSpeech
    SQLiteDatabase db; // For our database to save messages and codes
    Button backButton, speechButton, addButton, editButton, deleteButton;
    EditText messageEditText, messageCodeEditText;


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
            if(matches.get(0).toLowerCase().equals("back"))
                backButton.performClick();
            else if(matches.get(0).toLowerCase().equals("edit"))
                editButton.performClick();
            else if(matches.get(0).toLowerCase().equals("add"))
                addButton.performClick();
            else if(matches.get(0).toLowerCase().equals("delete"))
                deleteButton.performClick();
            else {
                myTts.speak("That is not a valid command.");
                Toast.makeText(getApplicationContext(),"That is not a valid command.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        myTts = new MyTts(this); // We put it onCreate so we give some time to the initListener to get ready

        // Create database or open it if it's already created
        db = openOrCreateDatabase("MessagesDB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Messages(Code INTEGER, Message TEXT)");



        backButton = findViewById(R.id.button6); // Move to send activity
        backButton.setOnClickListener((view) -> startActivity(new Intent(MainActivity3.this,
                MainActivity2.class))); // Go to activity2

        addButton = findViewById(R.id.button7); // Add new message and code
        addButton.setOnClickListener((view) -> {
            // Check if user has typed a code they wish to add and a new message
            if(!messageCodeEditText.getText().toString().equals("") && !messageEditText.getText().toString().equals("")) {

                boolean alreadyExists = false; // To help determine if code or message already exists
                // Get all messages
                Cursor cursor = db.rawQuery("SELECT * FROM Messages", null);
                if(cursor.getCount() > 0) { // If there is at least one message in messages table

                    while (cursor.moveToNext()) { // While there is data in table continue
                        // Convert new code to int and check all codes if it already exists
                        if(cursor.getInt(0) == Integer.parseInt(messageCodeEditText.getText().toString())){
                            alreadyExists = true;
                            myTts.speak("Code already exists.");
                            Toast.makeText(getApplicationContext(), "Code already exists.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        // Check all messages in the database to see if the new message already exists
                        if(cursor.getString(1).equals(messageEditText.getText().toString())){
                            alreadyExists = true;
                            myTts.speak("Message already exists.");
                            Toast.makeText(getApplicationContext(), "Message already exists.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    cursor.close();
                }
                // If a code or a message doesn't already exist
                if(!alreadyExists) {
                    int givenCode = Integer.parseInt(messageCodeEditText.getText().toString());
                    String givenMessage = messageEditText.getText().toString();
                    // Add new code and message
                    db.execSQL("INSERT INTO Messages(Code, Message) " + "VALUES('" + givenCode + "','" + givenMessage + "')");

                    // Reset values
                    messageCodeEditText.setText("");
                    messageEditText.setText("");

                    myTts.speak("Addition successful.");
                    Toast.makeText(getApplicationContext(), "Addition successful.", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                myTts.speak("Please fill in the message and the message code you wish to add.");
                Toast.makeText(getApplicationContext(),"Please fill in the message and the message code you wish to add.",Toast.LENGTH_SHORT).show();
            }
        });

        editButton = findViewById(R.id.button8); // Edit/Swap the selected message with the new message
        editButton.setOnClickListener((view) -> {
            // Check if user has typed a code they wish to edit and a new message
            if(!messageCodeEditText.getText().toString().equals("") && !messageEditText.getText().toString().equals("")) {

                boolean codeExists = false; // To help determine if code exists
                // Get all messages
                Cursor cursor = db.rawQuery("SELECT * FROM Messages", null);
                if(cursor.getCount() > 0) { // If there is at least one message in messages table

                    while (cursor.moveToNext()) { // While there is data in table continue
                        // Convert new code to int and check all codes if it exists
                        if(cursor.getInt(0) == Integer.parseInt(messageCodeEditText.getText().toString())){
                            codeExists = true;
                            break;
                        }
                    }
                    cursor.close();
                }
                // If the code exists
                if(codeExists) {

                    int givenCode = Integer.parseInt(messageCodeEditText.getText().toString());
                    String givenMessage = messageEditText.getText().toString();
                    // Update message based on Code
                    db.execSQL("UPDATE Messages SET Message = '" + givenMessage + "' WHERE Code = '" + givenCode + "'");

                    // Reset values
                    messageCodeEditText.setText("");
                    messageEditText.setText("");

                    myTts.speak("Edit successful.");
                    Toast.makeText(getApplicationContext(),"Edit successful.",Toast.LENGTH_SHORT).show();
                }
                else{
                    myTts.speak("The given code does not exist.");
                    Toast.makeText(getApplicationContext(),"The given code does not exist.",Toast.LENGTH_SHORT).show();
                }
            }
            else{
                myTts.speak("Please fill in the message and the message code you wish to edit.");
                Toast.makeText(getApplicationContext(),"Please fill in the message and the message code you wish to edit.",Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton = findViewById(R.id.button9); // Delete selected message and code
        deleteButton.setOnClickListener((view) -> {
            // Check if user has typed a code they wish to delete
            if(!messageCodeEditText.getText().toString().equals("")) {

                boolean codeExists = false; // To help determine if code exists
                // Get all messages
                Cursor cursor = db.rawQuery("SELECT * FROM Messages", null);
                if(cursor.getCount() > 0) { // If there is at least one message in messages table

                    while (cursor.moveToNext()) { // While there is data in table continue
                        // Convert new code to int and check all codes if it exists
                        if(cursor.getInt(0) == Integer.parseInt(messageCodeEditText.getText().toString())){
                            codeExists = true;
                            break;
                        }
                    }
                    cursor.close();
                }
                // If the code exists
                if(codeExists) {

                    int givenCode = Integer.parseInt(messageCodeEditText.getText().toString());
                    // Delete the message and code
                    db.execSQL("DELETE FROM Messages WHERE Code = '" + givenCode + "'");

                    // Reset values
                    messageCodeEditText.setText("");
                    messageEditText.setText("");

                    myTts.speak("Deletion successful.");
                    Toast.makeText(getApplicationContext(),"Deletion successful.",Toast.LENGTH_SHORT).show();
                }
                else{
                    myTts.speak("The given code does not exist.");
                    Toast.makeText(getApplicationContext(),"The given code does not exist.",Toast.LENGTH_SHORT).show();
                }
            }
            else{
                myTts.speak("Please fill in the message code you wish to delete.");
                Toast.makeText(getApplicationContext(),"Please fill in the message code you wish to delete.",Toast.LENGTH_SHORT).show();
            }
        });


        speechButton = findViewById(R.id.button10); // Start speech recognition
        // On click start listening for user voice. open -> recognise -> return data
        speechButton.setOnClickListener((view) -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // Start new intent
            // We prefer the extra model and the free form of speech
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What action would you like to perform?(add, edit, delete, back)"); // Ask user to give command to sign in or sing up
            startActivityForResult(intent, MainActivity.REC_RESULT); // We use ForResult because we need to wait fo a result to be returned
            // REC_RESULT is used so we know which return we are talking about
        });


        messageEditText = findViewById(R.id.editTextMessage); // Message for editing

        messageCodeEditText = findViewById(R.id.editTextMessageCode); // Message's code for editing
    }
}