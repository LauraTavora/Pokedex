package com.example.pokedex_mobile.ui;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pokedex_mobile.R;
import com.example.pokedex_mobile.data.mapper.PokemonMapper;
import com.example.pokedex_mobile.dataRemote.RetrofitProvider;
import com.example.pokedex_mobile.dataRemote.model.PokemonDetailClass;
import com.example.pokedex_mobile.domain.PokemonUi;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class PokemonDetailActivity extends AppCompatActivity {

    private View overlay;
    private View card;
    private ImageView ivBig;
    private TextView tvTitle;
    private TextView tvMainType;
    private LinearLayout stats;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // tema com fundo translúcido
        setTheme(R.style.Theme_PokedexMobile_Transparent);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_detail);

        // Views
        overlay   = findViewById(R.id.overlay);
        card      = findViewById(R.id.card);
        ivBig     = findViewById(R.id.ivBig);
        tvTitle   = findViewById(R.id.tvTitle);
        tvMainType= findViewById(R.id.tvMainType);
        stats     = findViewById(R.id.stats);

        String idOrName = getIntent().getStringExtra("idOrName");
        if (idOrName == null) { finish(); return; }

        // Busca detalhe em background
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Response<PokemonDetailClass> resp =
                        RetrofitProvider.getApi().detail(idOrName).execute();
                if (!resp.isSuccessful() || resp.body() == null) {
                    throw new IOException("Erro ao carregar detalhes");
                }

                final PokemonUi p = PokemonMapper.toUi(resp.body());
                final int color = TypeColors.bgFor(p.typesPt, this);

                runOnUiThread(() -> {
                    // Preenche UI
                    Glide.with(this).load(p.imageUrl).into(ivBig);
                    tvTitle.setText(p.name);
                    tvMainType.setText(p.typesPt.isEmpty() ? "—" : p.typesPt.get(0));

                    // Cor de fundo do card (pode usar shape bg_card_round.xml)
                    if (card.getBackground() != null) {
                        card.getBackground().setTint(color);
                    }

                    // Monta as linhas de stats
                    stats.removeAllViews();
                    for (Map.Entry<String, Integer> e : p.stats.entrySet()) {
                        stats.addView(buildStatRow(e.getKey(), e.getValue(), color));
                    }

                    // Clique fora fecha; clique no card não propaga
                    overlay.setOnClickListener(v -> finish());
                    card.setOnClickListener(v -> { /* consume */ });
                    // (Opcional) impedir clique atravessar o card:
                    ViewGroup parent = (ViewGroup) card.getParent();
                    if (parent != null) parent.setOnClickListener(v -> {});
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(this::finish);
            }
        });
    }

    private LinearLayout buildStatRow(String label, int value, int color) {
        final int pad = (int) (getResources().getDisplayMetrics().density * 10);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setPadding(0, pad, 0, pad);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(getColor(R.color.text_primary));
        tv.setTextSize(14f);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        ProgressBar pb = new ProgressBar(this, null,
                android.R.attr.progressBarStyleHorizontal);
        pb.setMax(180);
        pb.setProgress(Math.max(0, Math.min(180, value)));
        pb.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        pb.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.4f));

        TextView tvVal = new TextView(this);
        tvVal.setText(String.valueOf(value));
        tvVal.setPadding(pad, 0, 0, 0);

        root.addView(tv);
        root.addView(pb);
        root.addView(tvVal);
        return root;
    }
}
