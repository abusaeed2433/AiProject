package com.unknownn.aiproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeScreen extends AppCompatActivity {

    ImageButton btn_play,btn_sound;
    Boolean isSoundOn = true;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_play = findViewById(R.id.btn_play);
        btn_sound = findViewById(R.id.btn_sound);


        Spinner difficultySpinner = findViewById(R.id.difficulty_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_levels,R.layout.custom_text);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        difficultySpinner.setAdapter(adapter);

        // Set an item selected listener
        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDifficulty = parentView.getItemAtPosition(position).toString();
                Toast.makeText(HomeScreen.this, "Selected: " + selectedDifficulty, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Toast.makeText(HomeScreen.this,"Please select a difficulty",Toast.LENGTH_SHORT).show();
            }
        });

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreen.this, MainActivity.class);
                startActivity(intent);

            }
        });

        btn_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSoundOn){
                    btn_sound.setImageResource(R.drawable.sound_off_orange_removebg);
                    isSoundOn = false;
                }
                else{
                    btn_sound.setImageResource(R.drawable.sound_on_orange_removebg);
                    isSoundOn = true;
                }
            }
        });


    }
}