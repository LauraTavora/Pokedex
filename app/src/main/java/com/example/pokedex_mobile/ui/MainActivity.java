package com.example.pokedex_mobile.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokedex_mobile.R;
import com.example.pokedex_mobile.data.PokemonRepository;
import com.example.pokedex_mobile.dataRemote.RetrofitProvider;
import com.example.pokedex_mobile.domain.PokemonUi;
import com.example.pokedex_mobile.ui.adapter.PokemonAdapter;
import com.example.pokedex_mobile.ui.util.GridSpacingItemDecoration;
import com.example.pokedex_mobile.ui.util.SimpleTextWatcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView ivMenu;

    private RecyclerView rv;
    private TextInputEditText etSearch;
    private TextInputLayout tilSearch;

    private PokemonAdapter adapter;
    private final PokemonRepository repo =
            new PokemonRepository(RetrofitProvider.getApi());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ====== Views do conteúdo ======
        drawerLayout = findViewById(R.id.drawerLayout);
        ivMenu = findViewById(R.id.ivMenu);

        rv = findViewById(R.id.rv);
        etSearch = findViewById(R.id.etSearch);
        tilSearch = findViewById(R.id.tilSearch);

        // ====== RecyclerView ======
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        rv.setLayoutManager(glm);
        adapter = new PokemonAdapter(p -> openDetail(p.name.toLowerCase()));
        rv.setAdapter(adapter);

        int space = (int) (getResources().getDisplayMetrics().density * 8); // 8dp
        rv.addItemDecoration(new GridSpacingItemDecoration(space));

        // ====== Busca ======
        // Em tempo real (digitando)
        etSearch.addTextChangedListener(new SimpleTextWatcher(q -> adapter.filter(q)));

        // Ação do teclado (Enter/Buscar)
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });

        // Ícone de lupa no TextInputLayout
        tilSearch.setEndIconOnClickListener(v -> doSearch());

        // ====== Menu (Drawer) ======
        ivMenu.setOnClickListener(v -> drawerLayout.open());

        // Liga os botões de tipo do drawer
        wireDrawerTypeButtons();

        // ====== Carregar os 50 primeiros ======
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                final List<PokemonUi> list = repo.loadFirst50();
                runOnUiThread(() -> adapter.submit(list));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void doSearch() {
        String q = etSearch.getText() == null ? "" : etSearch.getText().toString();
        adapter.filter(q);
        hideKeyboard();
    }

    private void applyTypeFilter(String typePt) {
        // Mostra o termo no campo (opcional) e aplica o filtro
        if (etSearch != null) etSearch.setText(typePt);
        adapter.filter(typePt);
        hideKeyboard();
        if (drawerLayout != null) drawerLayout.close();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && etSearch != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            etSearch.clearFocus();
        }
    }

    private void openDetail(String idOrName) {
        Intent it = new Intent(this, PokemonDetailActivity.class);
        it.putExtra("idOrName", idOrName);
        startActivity(it);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isOpen()) {
            drawerLayout.close();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Conecta os botões do layout @layout/drawer_filter às ações de filtro.
     * Os IDs abaixo devem existir em drawer_filter.xml. Se algum ainda não
     * existir, o método ignora silenciosamente.
     */
    private void wireDrawerTypeButtons() {
        bindTypeButton(R.id.btnTypeGrass, "Planta");
        bindTypeButton(R.id.btnTypePoison, "Venenoso");
        bindTypeButton(R.id.btnTypeFire, "Fogo");
        bindTypeButton(R.id.btnTypeFlying, "Voador");
        bindTypeButton(R.id.btnTypeWater, "Água");
        bindTypeButton(R.id.btnTypeBug, "Inseto");
        bindTypeButton(R.id.btnTypeNormal, "Normal");
        bindTypeButton(R.id.btnTypePsychic, "Psíquico");
        bindTypeButton(R.id.btnTypeRock, "Rocha");
        bindTypeButton(R.id.btnTypeGround, "Terra");
        bindTypeButton(R.id.btnTypeElectric, "Elétrico");
        bindTypeButton(R.id.btnTypeGhost, "Fantasma");
        bindTypeButton(R.id.btnTypeFairy, "Fada");
    }

    private void bindTypeButton(int viewId, String typePt) {
        View v = findViewById(viewId);
        if (v != null) {
            v.setOnClickListener(x -> applyTypeFilter(typePt));
        }
    }
}
