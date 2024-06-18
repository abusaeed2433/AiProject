package com.unknownn.aiproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import static com.unknownn.aiproject.HomeScreen.Difficulty.*;

import com.unknownn.aiproject.classes.MyTextView;
import com.unknownn.aiproject.databinding.ActivityHomeScreenBinding;

public class HomeScreen extends AppCompatActivity {

    private Difficulty diffLevel = EASY;
    private ActivityHomeScreenBinding binding = null;
    private boolean debugMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setClickListener();
        animateTV(binding.tvEasy);
    }

    private void setClickListener(){
        binding.btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, MainActivity.class);
            intent.putExtra("difficulty_mode",diffLevel.name());
            intent.putExtra("debug_mode", debugMode);
            startActivity(intent);
        });

        binding.tvDebugMode.setOnClickListener(v -> {
            final String text = binding.tvDebugMode.getText().toString();
            debugMode = !("on".equalsIgnoreCase(text));

            binding.tvDebugMode.setText( debugMode ? "ON" : "OFF" );
        });

        binding.tvEasy.setOnClickListener(v -> {
            diffLevel = EASY;
            animateTV(binding.tvEasy);
        });

        binding.tvMedium.setOnClickListener(v -> {
            diffLevel = MEDIUM;
            animateTV(binding.tvMedium);
        });

        binding.tvHard.setOnClickListener(v -> {
            diffLevel = HARD;
            animateTV(binding.tvHard);
        });

    }

    private void animateTV(MyTextView textView){
        final MyTextView[] textViews = new MyTextView[]{binding.tvEasy, binding.tvMedium, binding.tvHard};

        for(MyTextView myTextView : textViews){
            myTextView.reset();
        }
        textView.animateBackground();
    }

    enum Difficulty{
        EASY(1), MEDIUM(2), HARD(3);

        final int id;
        Difficulty(int id) {
            this.id = id;
        }
    }
}
