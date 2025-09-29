package com.example.pokedex_mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokedex_mobile.BattleActivity;
import com.example.pokedex_mobile.R;
import com.example.pokedex_mobile.data.PokemonRepository;
import com.example.pokedex_mobile.dataRemote.RetrofitProvider;
import com.example.pokedex_mobile.domain.PokemonUi;
import com.example.pokedex_mobile.ui.adapter.PickAdapter;

import java.util.List;
import java.util.concurrent.Executors;

public class PokemonPickActivity extends AppCompatActivity {

    private RecyclerView rv;
    private PickAdapter adapter;
    private final PokemonRepository repo =
            new PokemonRepository(RetrofitProvider.getApi());

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_pick);

        rv = findViewById(R.id.rvPick);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PickAdapter(p -> {
            Intent it = new Intent(this, BattleActivity.class);
            it.putExtra(BattleActivity.EXTRA_PLAYER, p);
            startActivity(it);
        });
        rv.setAdapter(adapter);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                final List<PokemonUi> list = repo.loadDetail();
                runOnUiThread(() -> adapter.submit(list));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
