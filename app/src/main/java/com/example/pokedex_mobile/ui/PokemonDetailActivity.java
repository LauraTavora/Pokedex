package com.example.pokedex_mobile.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pokedex_mobile.R;
import com.example.pokedex_mobile.model.Pokemon;

import java.util.ArrayList;

public class PokemonDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POKEMON_INDEX = "extra_pokemon_index";
    public static final String EXTRA_POKEMON_LIST = "extra_pokemon_list";

    private ImageView imageView;
    private TextView nameView, typesView, statsView;
    private ImageButton prevBtn, nextBtn;
    private ArrayList<Pokemon> pokemons;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_detail);

        imageView = findViewById(R.id.detailImage);
        nameView = findViewById(R.id.detailName);
        typesView = findViewById(R.id.detailTypes);
        statsView = findViewById(R.id.detailStats);
        prevBtn = findViewById(R.id.buttonPrev);
        nextBtn = findViewById(R.id.buttonNext);

        Intent it = getIntent();
        index = it.getIntExtra(EXTRA_POKEMON_INDEX, 0);
        pokemons = (ArrayList<Pokemon>) it.getSerializableExtra(EXTRA_POKEMON_LIST);

        if (pokemons == null) pokemons = new ArrayList<>();

        showPokemon(index);

        prevBtn.setOnClickListener(v -> {
            if (index > 0) {
                index--;
                showPokemon(index);
            }
        });

        nextBtn.setOnClickListener(v -> {
            if (index < pokemons.size() - 1) {
                index++;
                showPokemon(index);
            }
        });
    }

    private void showPokemon(int idx) {
        Pokemon p = pokemons.get(idx);
        nameView.setText(capitalize(p.name));
        // tipos
        StringBuilder typesSb = new StringBuilder();
        if (p.types != null) {
            for (int i = 0; i < p.types.size(); i++) {
                typesSb.append(capitalize(p.types.get(i).type.name));
                if (i < p.types.size() - 1) typesSb.append(" · ");
            }
        }
        typesView.setText(typesSb.toString());

        // stats: formatado
        StringBuilder statsSb = new StringBuilder();
        if (p.stats != null) {
            for (Pokemon.StatWrapper sw : p.stats) {
                statsSb.append(statShortName(sw.stat.name)).append(": ").append(sw.base_stat).append("\n");
            }
        }
        statsView.setText(statsSb.toString());

        if (p.sprites != null && p.sprites.front_default != null) {
            Glide.with(this)
                    .load(p.sprites.front_default)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_pokeball);
        }

        // habilitar/desabilitar botões conforme borda
        prevBtn.setEnabled(idx > 0);
        nextBtn.setEnabled(idx < pokemons.size() - 1);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String statShortName(String stat) {
        switch (stat) {
            case "hp": return "HP";
            case "attack": return "ATK";
            case "defense": return "DEF";
            case "special-attack": return "SPA";
            case "special-defense": return "SPD";
            case "speed": return "SPE";
            default: return stat.toUpperCase();
        }
    }
}
