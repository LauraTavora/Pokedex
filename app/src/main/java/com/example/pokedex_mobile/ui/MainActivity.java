package com.example.pokedex_mobile.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokedex_mobile.R;
import com.example.pokedex_mobile.data.PokemonRepository;
import com.example.pokedex_mobile.dataRemote.RetrofitProvider;
import com.example.pokedex_mobile.domain.PokemonUi;
import com.example.pokedex_mobile.ui.TypeColors;
import com.example.pokedex_mobile.ui.adapter.PokemonAdapter;
import com.example.pokedex_mobile.ui.detail.PokemonDetailDialogFragment;
import com.example.pokedex_mobile.ui.util.GridSpacingItemDecoration;
import com.example.pokedex_mobile.ui.util.SimpleTextWatcher;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
        implements PokemonDetailDialogFragment.OnNavigate {

    private RecyclerView rv;
    private PokemonAdapter adapter;
    private final List<PokemonUi> current = new ArrayList<>();
    private int selectedIndex = -1;

    private ImageView ivMenu;
    private TextInputLayout tilSearch;
    private TextInputEditText etSearch;

    // Drawer
    private DrawerLayout drawerLayout;
    private LinearLayout navTypes; // <= PEGAR O NAVIGATIONVIEW

    // Dados
    private final PokemonRepository repo = new PokemonRepository(RetrofitProvider.getApi());
    private @Nullable String currentTypeFilter = null; // null = “Todos”
    private @NonNull String currentNameQuery = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // XML com Drawer + topbar + rv

        // ----- refs UI -----
        drawerLayout = findViewById(R.id.drawerLayout);
        navTypes     = findViewById(R.id.drawerRoot);
        ImageView ivPokeball = findViewById(R.id.ivPokeball);


        ivMenu    = findViewById(R.id.ivMenu);
        tilSearch = findViewById(R.id.tilSearch);
        etSearch  = findViewById(R.id.etSearch);
        rv        = findViewById(R.id.rv);

        // ----- RecyclerView -----
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        rv.setLayoutManager(glm);
        adapter = new PokemonAdapter(p -> openDetailPopup(p));
        rv.setAdapter(adapter);
        int space = (int) (getResources().getDisplayMetrics().density * 8); // 8dp
        rv.addItemDecoration(new GridSpacingItemDecoration(space));

        // ----- Busca por nome (não mexe em filtro por tipo) -----
        etSearch.addTextChangedListener(new SimpleTextWatcher(q -> {
            currentNameQuery = (q == null) ? "" : q;
            if (adapter != null) adapter.setNameQuery(currentNameQuery);
        }));

        // Ação do teclado (Buscar)
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });

        // Clique na lupa do TextInputLayout
        tilSearch.setEndIconOnClickListener(v -> doSearch());

        // ----- Menu lateral (abre/fecha) -----
        ivMenu.setOnClickListener(v -> {
            if (drawerLayout != null) {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                } else {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });

        // ----- Liga os botões de tipo do NavigationView -----
        wireTypeButtons(navTypes);

        // ----- Clique na Pokébola -----
        ivPokeball.setOnClickListener(v -> {
            // Só reseta se houver filtro de nome ou de tipo ativo
            boolean hasNameFilter = etSearch.getText() != null && !etSearch.getText().toString().trim().isEmpty();
            boolean hasTypeFilter = adapter != null && adapter.hasTypeFilter();

            if (hasNameFilter || hasTypeFilter) {
                // Animação de giro da Pokébola
                ivPokeball.animate()
                        .rotationBy(360f) // giro completo
                        .setDuration(500)
                        .withEndAction(() -> {
                            // Resetar filtros
                            etSearch.setText("");
                            if (adapter != null) {
                                adapter.setTypeFilter(null);
                                adapter.setNameQuery("");
                                adapter.notifyDataSetChanged();
                            }

                            // Animação suave nos cards da lista
                            LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(
                                    this, R.anim.layout_animation_fall_down
                            );
                            rv.setLayoutAnimation(controller);
                            rv.scheduleLayoutAnimation();
                        })
                        .start();
            }
        });


        // ----- Carregar os 50 primeiros -----
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                final List<PokemonUi> list = repo.loadDetail();
                runOnUiThread(() -> {
                    adapter.submit(list);
                    // estado inicial: sem filtro de tipo e busca atual (nome)
                    adapter.setTypeFilter(null);
                    adapter.setNameQuery(etSearch.getText() == null ? "" : etSearch.getText().toString());

                    current.clear();
                    current.addAll(list);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Procura os botões e liga os cliques
    private void wireTypeButtons(View parent) {
        // IDs devem existir no header do NavigationView (ou no conteúdo do próprio nav)
        bindTypeClick(parent, R.id.btnTypeGrass,    "Planta");
        bindTypeClick(parent, R.id.btnTypePoison,   "Veneno");
        bindTypeClick(parent, R.id.btnTypeFire,     "Fogo");
        bindTypeClick(parent, R.id.btnTypeWater,    "Água");
        bindTypeClick(parent, R.id.btnTypeFlying,   "Voador");
        bindTypeClick(parent, R.id.btnTypeBug,      "Inseto");
        bindTypeClick(parent, R.id.btnTypeNormal,   "Normal");
        bindTypeClick(parent, R.id.btnTypePsychic,  "Psíquico");
        bindTypeClick(parent, R.id.btnTypeRock,     "Pedra");
        bindTypeClick(parent, R.id.btnTypeGround,   "Terra");
        bindTypeClick(parent, R.id.btnTypeElectric, "Elétrico");
        bindTypeClick(parent, R.id.btnTypeGhost,    "Fantasma");
        bindTypeClick(parent, R.id.btnTypeFairy,    "Fada");
    }

    private void bindTypeClick(View parent, int viewId, @Nullable String typePtOrNull) {
        View v = parent.findViewById(viewId);
        if (v != null) {
            v.setOnClickListener(x -> {
                currentTypeFilter = typePtOrNull; // mantém o estado
                if (adapter != null) adapter.setTypeFilter(typePtOrNull); // null = “Todos”
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                hideKeyboard();
            });
        }
    }

    private void doSearch() {
        // Só esconde teclado; o filtro por nome já é feito no TextWatcher acima
        hideKeyboard();
    }

    private void resetFiltersToHome() {
        // limpa texto
        etSearch.setText("");
        currentNameQuery = "";
        if (adapter != null) adapter.setNameQuery("");

        // limpa tipo
        currentTypeFilter = null;
        if (adapter != null) adapter.setTypeFilter(null);

        // fecha drawer e sobe a lista
        if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
        rv.scrollToPosition(0);
        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && etSearch != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            etSearch.clearFocus();
        }
    }

    private void openDetailPopup(PokemonUi p) {
        int typeColor = TypeColors.bgFor(p.typesPt, this);
        PokemonDetailDialogFragment
                .newInstance(p, typeColor)
                .show(getSupportFragmentManager(), "pokemon_detail");
    }

    // ===== Navegação (setas no dialog) =====
    @Override
    public void onNavigatePrev(@NonNull PokemonUi current) {
        int idx = findIndex(current);
        if (idx > 0) {
            // deixo vazio, pois a navegação é controlada pelo próprio dialog chamando requestPrev/Next
        }
    }

    @Override
    public void onNavigateNext(@NonNull PokemonUi current) {
        int idx = findIndex(current);
        if (idx >= 0 && idx < adapterSize() - 1) {
            // idem acima
        }
    }

    @Nullable
    public PokemonUi requestPrev(@NonNull PokemonUi current) {
        int idx = findIndex(current);
        return (idx > 0) ? adapterItemAt(idx - 1) : null;
    }

    @Nullable
    public PokemonUi requestNext(@NonNull PokemonUi current) {
        int idx = findIndex(current);
        return (idx >= 0 && idx < adapterSize() - 1) ? adapterItemAt(idx + 1) : null;
    }

    // Helpers que consultam o adapter (sem duplicar estado)
    private int findIndex(PokemonUi p) { return adapter != null ? adapter.indexOfId(p.id) : -1; }
    private PokemonUi adapterItemAt(int i) { return adapter != null ? adapter.getItem(i) : null; }
    private int adapterSize() { return adapter != null ? adapter.getItemCount() : 0; }
}
