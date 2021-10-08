package com.p17191.ergasies.exercise2;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class MyTts { // Helper class implementing TextToSpeech functionality
    private TextToSpeech tts;

    // Interface used after initialization of machine. Machine can be used
    private TextToSpeech.OnInitListener initListener =
            new TextToSpeech.OnInitListener(){

                @Override
                public void onInit(int status) {
                    // Here we put what language we want
                    tts.setLanguage(Locale.ENGLISH); // I wanted english
                }
            };

    public MyTts(Context context){ // Constructor of class
        // We need who called it and to check if it has been initialised (initListener)
        tts = new TextToSpeech(context, initListener);
    }

    public void speak(String message){
        // Speak the message, with queue (wait for current message to finish then speak the next one in queue)
        tts.speak(message, TextToSpeech.QUEUE_ADD, null, null);
    }
}
