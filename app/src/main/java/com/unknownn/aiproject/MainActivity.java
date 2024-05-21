package com.unknownn.aiproject;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.unknownn.aiproject.classes.CellState;
import com.unknownn.aiproject.classes.GameBoard;
import com.unknownn.aiproject.classes.Helper;
import com.unknownn.aiproject.databinding.ActivityMainBinding;
import com.unknownn.aiproject.enums.PredictionAlgo;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding = null;

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

        setupBoard();
        setClickListener();
    }

    private void setClickListener(){
        binding.tvAlgoType.setOnClickListener( v -> binding.gameBoard.swapPredictionAlgo());
    }

    private void setupBoard(){
        binding.gameBoard.setBoardListener(new GameBoard.BoardListener() {
            @Override
            public void onMessageToShow(String message) {
                Helper.showSafeToast(MainActivity.this,message);
            }

            @Override
            public void onAlgoChanged() {
                Helper.showSafeToast(MainActivity.this,"Prediction algo is changed");

                if(binding.gameBoard.getPredictionAlgo() == PredictionAlgo.ALPHA_BETA_PRUNING){
                    binding.tvAlgoType.setText(getString(R.string.alpha_beta));
                }
                else{
                    binding.tvAlgoType.setText(getString(R.string.genetic_algo));
                }
            }

            @Override
            public void onGameEnds(CellState.MyColor winner) {
                String strWinner = (winner == CellState.MyColor.RED) ? "You" : "Bot";

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Game Over")
                        .setMessage(strWinner+" won.")
                        .setPositiveButton("Restart", (dialogInterface, i) -> binding.gameBoard.restart())
                        .setNegativeButton("Exit", (dialogInterface, i) -> MainActivity.this.finishAffinity())
                        .show();
            }
        });
    }

}
