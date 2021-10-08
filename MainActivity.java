package com.p17191.ergasies.exercise2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    MyTts myTts; // For TextToSpeech
    private FirebaseAuth firebaseAuth;
    Button signInButton, signUpButton, speechButton;
    EditText emailEditText, passwordEditText;
    public static final int REC_RESULT = 123; // For result of voice recognition

    // When data is returned from speech recognition, this method is called. Used to handle the data
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REC_RESULT && resultCode == RESULT_OK){ // requestCode = our REC_RESULT | resultCode = if everything went okay ( user spoke and machine recognised the message)
            // Data returned from voice recognition is basically matches between what the user said and what the machine thinks the user said
            // Top of the list are the best matches and as we iterate through the ArrayList, the least probable matches are stored
            // If we have phrases we need to check each phrase with the one we want
            // Also we need to have one general letter case so we don't have match problems.
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // Check user's command
            if(matches.get(0).toLowerCase().equals("sign in"))
                signInButton.performClick();
            else if(matches.get(0).toLowerCase().equals("sign up"))
                signUpButton.performClick();
            else {
                myTts.speak("That is not a valid command.");
                Toast.makeText(getApplicationContext(),"That is not a valid command.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myTts = new MyTts(this); // We put it onCreate so we give some time to the initListener to get ready

        firebaseAuth = FirebaseAuth.getInstance(); // Get instance of firebase authentication


        signInButton = findViewById(R.id.button); // Sign in to user's account
        signInButton.setOnClickListener((view) -> {
            // Check if user has given an email and a password
            if(!emailEditText.getText().toString().equals("") && !passwordEditText.getText().toString().equals("")) {

                // Try to sign in the user
                firebaseAuth.signInWithEmailAndPassword(emailEditText.getText().toString(),
                        passwordEditText.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign is was successful
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(MainActivity.this, MainActivity2.class)); // Go to activity2

                                    // Reset values
                                    emailEditText.setText("");
                                    passwordEditText.setText("");

                                    myTts.speak("Sign in successful.");
                                    Toast.makeText(getApplicationContext(), "Sign in successful.", Toast.LENGTH_SHORT).show();
                                }
                                else{ // Else if something went wrong display the reason
                                    myTts.speak(task.getException().getMessage());
                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
            else{
                myTts.speak("Please fill in both email and password.");
                Toast.makeText(getApplicationContext(),"Please fill in both email and password.",Toast.LENGTH_SHORT).show();
            }
        });


        signUpButton = findViewById(R.id.button2); // Sign up new account
        signUpButton.setOnClickListener((view) -> {
            // Check if user has given an email and a password
            if(!emailEditText.getText().toString().equals("") && !passwordEditText.getText().toString().equals("")) {
                // Try to sign up the user
                firebaseAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(),
                        passwordEditText.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign up was successful. Once user is signed up, they are signed in as well
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(MainActivity.this, MainActivity2.class)); // Go to activity2

                                    // Reset values
                                    emailEditText.setText("");
                                    passwordEditText.setText("");

                                    myTts.speak("Sign up successful.");
                                    Toast.makeText(getApplicationContext(), "Sign up successful.", Toast.LENGTH_SHORT).show();
                                }
                                else{ // Else if something went wrong display the reason
                                    myTts.speak(task.getException().getMessage());
                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
            else{
                myTts.speak("Please fill in both email and password.");
                Toast.makeText(getApplicationContext(),"Please fill in both email and password.",Toast.LENGTH_SHORT).show();
            }
        });


        speechButton = findViewById(R.id.button3); // Start speech recognition
        // On click start listening for user voice. open -> recognise -> return data
        speechButton.setOnClickListener((view) -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // Start new intent
            // We prefer the extra model and the free form of speech
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What action would you like to perform?(sign in, sign up)"); // Ask user to give command to sign in or sing up
            startActivityForResult(intent, REC_RESULT); // We use ForResult because we need to wait fo a result to be returned
            // REC_RESULT is used so we know which return we are talking about
        });

        emailEditText = findViewById(R.id.editTextEmail); // User's email
        passwordEditText = findViewById(R.id.editTextPassword); // User's password
    }
}