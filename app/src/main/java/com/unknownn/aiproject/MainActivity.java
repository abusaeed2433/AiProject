package com.unknownn.aiproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.unknownn.aiproject.classes.AlphaBetaApplier;
import com.unknownn.aiproject.classes.CellState;
import com.unknownn.aiproject.classes.FuzzyApplier;
import com.unknownn.aiproject.classes.GameBoard;
import com.unknownn.aiproject.classes.GeneticApplier;
import com.unknownn.aiproject.classes.Helper;
import com.unknownn.aiproject.classes.SoundController;
import com.unknownn.aiproject.databinding.ActivityMainBinding;
import com.unknownn.aiproject.enums.PredictionAlgo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding = null;
    private SoundController soundController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            return view.onApplyWindowInsets(windowInsets);
        });

        final Intent intent = getIntent();

        final String strLevel = intent.getStringExtra("difficulty_mode");
        final boolean debugMode = intent.getBooleanExtra("debug_mode",true);
        HomeScreen.Difficulty difficulty = HomeScreen.Difficulty.valueOf(strLevel);

        setupBoard(difficulty,debugMode);
        setClickListener();
    }

    private void setClickListener(){
//        binding.tvAlgoType.setOnClickListener( v -> binding.gameBoard.swapPredictionAlgo(false));
    }

    private void setupBoard(HomeScreen.Difficulty difficulty, boolean debugMode){

        GeneticApplier.destroy();
        AlphaBetaApplier.destroy();
        FuzzyApplier.destroy();

        soundController = SoundController.getInstance(this);

        int N = 5;
        switch (difficulty){
            case EASY -> {
                N = 7;
                binding.gameBoard.fixPredictionAlgo(PredictionAlgo.GENETIC_ALGO); // use both GA
                binding.tvAlgoType.setText(getString(R.string.genetic_algo));
            }
            case MEDIUM -> binding.gameBoard.fixPredictionAlgo(null); // use both AB, GA
            case HARD -> binding.gameBoard.fixPredictionAlgo(PredictionAlgo.ALPHA_BETA_PRUNING); // use both AB
        }

        binding.gameBoard.drawBoard(N);
        binding.gameBoard.setDebugMode(debugMode);

        binding.gameBoard.setBoardListener(new GameBoard.BoardListener() {
            @Override
            public void onMessageToShow(String message) {
                Helper.showSafeToast(MainActivity.this,message);
            }

            @Override
            public void onAlgoChanged(boolean showToast) {
                if(showToast) {
                    Helper.showSafeToast(MainActivity.this,"Prediction algo is changed");
                }

                if(binding.gameBoard.getPredictionAlgo() == PredictionAlgo.ALPHA_BETA_PRUNING){
                    binding.tvAlgoType.setText(getString(R.string.alpha_beta));
                }
                else{
                    binding.tvAlgoType.setText(getString(R.string.genetic_algo));
                }
            }

            @Override
            public void showWhoseMove(boolean userMove) {
                binding.tvWhoseMove.setText(
                        userMove ? getString(R.string.your_move) : getString(R.string.bot_move)
                );
            }

            @Override
            public void onGameEnds(CellState.MyColor winner) {
                String strWinner = (winner == CellState.MyColor.RED) ? "You" : "Bot";

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Game Over")
                        .setMessage(strWinner+" won.")
                        .setPositiveButton("Restart", (dialogInterface, i) -> binding.gameBoard.restart())
                        .setNegativeButton("Exit", (dialogInterface, i) -> MainActivity.this.finishAffinity())
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onSoundPlayRequest(SoundController.SoundType soundType) {
                soundController.playSound(soundType);
            }

            @Override
            public void onProgressBarUpdate(boolean show) {
                if(show){
                    binding.myConfuseBar.startRotating();
                }
                else{
                    binding.myConfuseBar.stopRotating();
                }
            }

            @Override
            public void onProgressUpdate(String strProgress) {
                binding.myConfuseBar.setStrProgress(strProgress);
            }

        });

        final ExecutorService service = Executors.newFixedThreadPool(5);

        AlphaBetaApplier.getInstance().setPreSavedListener((strBoard, score) -> {
            service.submit(() -> {
                final SharedPreferences sp = getSharedPreferences("sp",MODE_PRIVATE);

                if(sp.contains(strBoard)) return;

                final int size = sp.getInt("size",0);
                final SharedPreferences.Editor editor = sp.edit();

                editor.putInt(strBoard, score);
                editor.putString((size+1)+"", strBoard);
                editor.putInt("size",size+1);

                editor.apply();
            });
        }, getPreSavedScore());
    }

    private Map<String, Integer> getPreSavedScore(){
        final Map<String,Integer> scoreMap = new HashMap<>();

        final SharedPreferences sp = getSharedPreferences("sp",MODE_PRIVATE);
        final int size = sp.getInt("size",0);

        for(int i=1; i<=size; i++){
            final String key = sp.getString((i+1)+"", null);
            if(key == null) continue;

            final int value = sp.getInt(key,0);
            if(value == 0) continue;

            scoreMap.put(key,value);
        }

        return scoreMap;
    }



}
