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
import static com.unknownn.aiproject.HomeScreen.Difficulty.*;

public class HomeScreen extends AppCompatActivity {

    ImageButton btn_play,btn_sound,btn_debug;
    Boolean isSoundOn = true;

    private Difficulty diffLevel = EASY;
    private final Difficulty[] difficulties = {EASY, MEDIUM, HARD};

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
        btn_debug = findViewById(R.id.btn_debug);


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
                diffLevel = difficulties[position];
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
                intent.putExtra("difficulty_mode",diffLevel.name());
                startActivity(intent);
            }
        });

        btn_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSoundOn){
                    btn_sound.setImageResource(R.drawable.sound_off_orange_removebg);
                    isSoundOn = false;
                    Toast.makeText(HomeScreen.this,"Sound:OFF",Toast.LENGTH_SHORT).show();
                }
                else{
                    btn_sound.setImageResource(R.drawable.sound_on_orange_removebg);
                    isSoundOn = true;
                    Toast.makeText(HomeScreen.this,"Sound:ON",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_debug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeScreen.this,"Debug button",Toast.LENGTH_SHORT).show();
            }
        });


    }

    enum Difficulty{
        EASY(1), MEDIUM(2), HARD(3);

        final int id;
        Difficulty(int id) {
            this.id = id;
        }
    }
}
