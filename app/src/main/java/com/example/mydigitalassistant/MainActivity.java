package com.example.mydigitalassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSpeak = findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start speech recognition
                promptSpeechInput();
            }
        });
    }

    private void promptSpeechInput() {
        // Create intent for speech recognition
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "am-ET"); // Amharic language
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "ለመደወል ቁጥሩን ወይም ስሙን ይናገሩ");

        try {
            // Start intent
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            // Speech recognition not supported
            Toast.makeText(getApplicationContext(),
                    "Speech recognition not supported on this device",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Handle speech recognition result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String spokenText = result.get(0);

                    // Check if spoken text is a phone number
                    if (Pattern.matches("\\d+", spokenText)) {
                        // Initiate phone call
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + spokenText));
                        startActivity(callIntent);
                    } else {
                        // Look up phone number by contact name
                        String phoneNumber = getPhoneNumberFromContactName(spokenText);
                        if (phoneNumber != null) {
                            // Initiate phone call
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + phoneNumber));
                            startActivity(callIntent);
                        } else {
                            // Could not find contact
                            Toast.makeText(getApplicationContext(),
                                    "Could not find contact for " + spokenText,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            }
        }
    }

    // Helper method to get phone number from contact name
    private String getPhoneNumberFromContactName(String contactName) {
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?",
                new String[]{contactName},
                null);
//
//        if (cursor != null && cursor.moveToFirst()) {
//            @SuppressLint("Range") String phoneNumber = cursor.getString(
//                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            if (columnIndex >= 0) {
                // column exists, retrieve its value
                String phoneNumber = cursor.getString(columnIndex);
            cursor.close();
            return phoneNumber;
        }

        return null;
    }
}
