package com.example.pokedex_mobile.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pokedex_mobile.R;
import com.example.pokedex_mobile.data.mapper.PokemonMapper;
import com.example.pokedex_mobile.dataRemote.RetrofitProvider;
import com.example.pokedex_mobile.dataRemote.model.PokemonDetailClass;
import com.example.pokedex_mobile.domain.PokemonUi;

import java.io.IOException;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class PokemonDetailActivity extends AppCompatActivity {

    private View overlay;
    private View card;
    private ImageView ivBig;
    private TextView tvTitle;
    private TextView tvMainType;

    // Rows dos stats (cada row contém: tvLabel, tvValue e, nas rows com barra, pbStat)
    private View rowHp, rowAtk, rowDef, rowSpAtk, rowSpDef, rowSpeed;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // tema com fundo translúcido (blur/dim)
        setTheme(R.style.Theme_PokedexMobile_Transparent);
        super.onCreate(savedInstanceState);
        // tira o título do Activity “popup”
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pokemon_detail);

        // Views básicas
        overlay    = findViewById(R.id.overlay);
        card       = findViewById(R.id.card);
        ivBig      = findViewById(R.id.ivBig);
        tvTitle    = findViewById(R.id.tvTitle);
        tvMainType = findViewById(R.id.tvMainType);

        // Rows de stats
        rowHp    = findViewById(R.id.row_hp);
        rowAtk   = findViewById(R.id.row_atk);
        rowDef   = findViewById(R.id.row_def);
        rowSpAtk = findViewById(R.id.row_sp_atk);
        rowSpDef = findViewById(R.id.row_sp_def);
        rowSpeed = findViewById(R.id.row_speed);

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

                    // (Opcional) deixa o card com fundo branco e/ou borda colorida
                    if (card != null && card.getBackground() != null) {
                        // se seu shape for branco, pode só tingir a borda; aqui deixo um leve tint
                        card.getBackground().setTint(Color.WHITE);
                    }

                    // === STATS ===
                    // 3 primeiros: apenas valor
                    bindValueOnly(rowHp,  getString(R.string.stat_hp),      p.hp);
                    bindValueOnly(rowAtk, getString(R.string.stat_attack),  p.attack);
                    bindValueOnly(rowDef, getString(R.string.stat_defense), p.defense);

                    // 3 últimos: com barra colorida
                    bindWithBar(rowSpAtk, getString(R.string.stat_sp_atk), p.spAttack, color);
                    bindWithBar(rowSpDef, getString(R.string.stat_sp_def), p.spDefense, color);
                    bindWithBar(rowSpeed, getString(R.string.stat_speed),  p.speed,    color);

                    // Clique fora fecha; clique no card não propaga
                    overlay.setOnClickListener(v -> finish());
                    card.setOnClickListener(v -> { /* consume */ });
                    ViewGroup parent = (ViewGroup) card.getParent();
                    if (parent != null) parent.setOnClickListener(v -> {});
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(this::finish);
            }
        });
    }

    // ===== Helpers =====
    private void bindValueOnly(View row, String labelText, int value) {
        if (row == null) return;
        TextView tvLabel = row.findViewById(R.id.tvLabel);
        TextView tvValue = row.findViewById(R.id.tvValue);
        if (tvLabel != null) tvLabel.setText(labelText);
        if (tvValue != null) tvValue.setText(String.valueOf(value));
        // Se houver ProgressBar nessa row por engano, esconda:
        ProgressBar pb = row.findViewById(R.id.pbStat);
        if (pb != null) pb.setVisibility(View.GONE);
    }

    private void bindWithBar(View row, String labelText, int value, int color) {
        if (row == null) return;
        TextView tvLabel   = row.findViewById(R.id.tvLabel);
        TextView tvValue   = row.findViewById(R.id.tvValue);
        ProgressBar pbStat = row.findViewById(R.id.pbStat);

        if (tvLabel != null) tvLabel.setText(labelText);
        if (tvValue != null) tvValue.setText(String.valueOf(value));

        if (pbStat != null) {
            pbStat.setVisibility(View.VISIBLE);
            pbStat.setMax(180);
            pbStat.setProgress(Math.max(0, Math.min(180, value)));
            // cor da barra e do fundo da barra
            pbStat.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
            pbStat.setProgressBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.argb(60, Color.red(color), Color.green(color), Color.blue(color))
                    )
            );
        }
    }
}
