package com.unknownn.aiproject;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

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
import com.unknownn.aiproject.databinding.GameOverLayoutBinding;
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

        //showGameOver(false);
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
                showGameOver((winner == CellState.MyColor.RED));
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

    private void showGameOver(boolean haveIWon){
        final Dialog dialog = new Dialog(this);
        final GameOverLayoutBinding bindingDialog = GameOverLayoutBinding.inflate(LayoutInflater.from(this));
        dialog.setContentView(bindingDialog.getRoot());

        final Window window = dialog.getWindow();
        if(window != null){
            window.setBackgroundDrawable(new ColorDrawable(0));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            window.setWindowAnimations(R.style.dialogAnimation);
        }

        bindingDialog.tvMyStatus.setText( (haveIWon) ? getString(R.string.you_won) : getString(R.string.you_lose) );

        if(haveIWon){
            bindingDialog.lottieAnimationView.setAnimation(R.raw.lottie_winner);
        }

        bindingDialog.ivHome.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        bindingDialog.ivRetry.setOnClickListener(v ->{
            dialog.dismiss();
            binding.gameBoard.restart();
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
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
