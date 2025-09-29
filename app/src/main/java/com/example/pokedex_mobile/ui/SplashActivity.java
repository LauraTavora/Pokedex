package com.example.pokedex_mobile.ui;

import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pokedex_mobile.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView ivTop, ivBottom;
    private View container;
    private boolean animated = false;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Deixa a janela “por cima”, sem status bar piscando
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ivTop = findViewById(R.id.ivTop);
        ivBottom = findViewById(R.id.ivBottom);
        container = findViewById(R.id.pokeballContainer);

        // Cada ClipDrawable precisa começar em 50% (5000) para mostrar só a sua metade
        Drawable dt = ivTop.getDrawable();
        Drawable db = ivBottom.getDrawable();
        if (dt instanceof ClipDrawable) ((ClipDrawable) dt).setLevel(5000);
        if (db instanceof ClipDrawable) ((ClipDrawable) db).setLevel(5000);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus || animated) return;
        animated = true;

        // Animação quando já temos medidas (full-screen de verdade)
        container.post(() -> {
            final int half = container.getHeight() / 2;
            ivTop.setTranslationY(0f);
            ivBottom.setTranslationY(0f);

            final long D = 1200L; // mais sutil
            final AccelerateDecelerateInterpolator itp = new AccelerateDecelerateInterpolator();

            ivTop.animate()
                    .translationY(-half - dp(24))
                    .setDuration(D)
                    .setInterpolator(itp)
                    .start();

            ivBottom.animate()
                    .translationY(half + dp(24))
                    .setDuration(D)
                    .setInterpolator(itp)
                    .withEndAction(() -> {
                        // Ao terminar a abertura, segue pra Main sem “flash”
                        startActivity(new Intent(this, MainActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                    })
                    .start();
        });
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
