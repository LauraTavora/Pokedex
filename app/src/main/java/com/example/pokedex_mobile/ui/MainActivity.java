package com.example.pokedex_mobile.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokedex_mobile.R;
import com.example.pokedex_mobile.adapter.PokemonAdapter;
import com.example.pokedex_mobile.api.ApiClient;
import com.example.pokedex_mobile.api.PokeApiService;
import com.example.pokedex_mobile.model.Pokemon;
import com.example.pokedex_mobile.model.PokemonListResponse;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private PokemonAdapter adapter;
    private List<Pokemon> pokemons = new ArrayList<>();
    private List<Pokemon> filteredList = new ArrayList<>();
    private ProgressBar progressBar;
    private PokeApiService apiService;
    private SearchView searchView;

    // cores por tipo
    private final Map<String, Integer> TYPE_COLORS = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // mapa de cores
        TYPE_COLORS.put("grass", ContextCompat.getColor(this, R.color.type_grass));
        TYPE_COLORS.put("poison", ContextCompat.getColor(this, R.color.type_poison));
        TYPE_COLORS.put("fire", ContextCompat.getColor(this, R.color.type_fire));
        TYPE_COLORS.put("water", ContextCompat.getColor(this, R.color.type_water));
        TYPE_COLORS.put("flying", ContextCompat.getColor(this, R.color.type_flying));
        TYPE_COLORS.put("bug", ContextCompat.getColor(this, R.color.type_bug));
        TYPE_COLORS.put("normal", ContextCompat.getColor(this, R.color.type_normal));
        TYPE_COLORS.put("psychic", ContextCompat.getColor(this, R.color.type_psychic));
        TYPE_COLORS.put("rock", ContextCompat.getColor(this, R.color.type_rock));
        TYPE_COLORS.put("ground", ContextCompat.getColor(this, R.color.type_ground));
        TYPE_COLORS.put("electric", ContextCompat.getColor(this, R.color.type_electric));
        TYPE_COLORS.put("ghost", ContextCompat.getColor(this, R.color.type_ghost));


        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Inicializa o botão do menu customizado
        ImageView menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchView);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new PokemonAdapter(this, filteredList, TYPE_COLORS);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(PokeApiService.class);

        loadPokemons(50);
        setupSearch();

        // substitui o onBackPressed antigo pelo OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterByName(newText);
                return true;
            }
        });
    }

    private void filterByName(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(pokemons);
        } else {
            String q = query.toLowerCase();
            for (Pokemon p : pokemons) {
                if (p.name.toLowerCase().contains(q)) filteredList.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadPokemons(int limit) {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getPokemonList(limit).enqueue(new Callback<PokemonListResponse>() {
            @Override
            public void onResponse(Call<PokemonListResponse> call, Response<PokemonListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Erro ao obter lista", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<PokemonListResponse.PokemonResult> results = response.body().results;
                AtomicInteger counter = new AtomicInteger(0);
                int total = results.size();

                for (PokemonListResponse.PokemonResult r : results) {
                    apiService.getPokemonDetails(r.name).enqueue(new Callback<Pokemon>() {
                        @Override
                        public void onResponse(Call<Pokemon> call, Response<Pokemon> resp) {
                            if (resp.isSuccessful() && resp.body() != null) {
                                pokemons.add(resp.body());
                                filteredList.add(resp.body());
                                adapter.notifyItemInserted(filteredList.size() - 1);
                            }
                            if (counter.incrementAndGet() == total) progressBar.setVisibility(View.GONE);
                        }
                        @Override
                        public void onFailure(Call<Pokemon> call, Throwable t) {
                            if (counter.incrementAndGet() == total) progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<PokemonListResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Falha: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);

        String typeToFilter = item.getTitle().toString().toLowerCase();

        filteredList.clear();
        for (Pokemon p : pokemons) {
            if (p.types != null) {
                for (Pokemon.TypeWrapper tw : p.types) {
                    if (tw.type != null && tw.type.name != null &&
                            tw.type.name.toLowerCase().equals(typeToFilter)) {
                        filteredList.add(p);
                        break;
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
        return true;
    }
}
