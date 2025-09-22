package com.example.pokedex_mobile.ui.detail;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.pokedex_mobile.R;
import com.example.pokedex_mobile.data.mapper.PokemonMapper;
import com.example.pokedex_mobile.dataRemote.RetrofitProvider;
import com.example.pokedex_mobile.domain.PokemonUi;
import com.example.pokedex_mobile.ui.TypeColors;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class PokemonDetailDialogFragment extends DialogFragment {

    private static final String ARG_UI = "arg_ui";
    private static final String ARG_COLOR = "arg_color";

    /** A Activity deve devolver o anterior/próximo da lista atual (sem reabrir dialog) */
    public interface OnNavigate {
        void onNavigatePrev(@NonNull PokemonUi current);
        void onNavigateNext(@NonNull PokemonUi current);
        @Nullable PokemonUi requestPrev(@NonNull PokemonUi current);
        @Nullable PokemonUi requestNext(@NonNull PokemonUi current);
    }

    private OnNavigate nav;

    private PokemonUi pokemon;
    private int typeColor;

    // Views
    private ImageView ivSprite, btPrev, btNext;
    private TextView tvTitle;
    private TabLayout tabs;

    // ChipGroup de tipos (você já tinha)
    private ChipGroup cgTypes;

    private LinearLayout abilitiesContainer, groupStats, groupAbilities, groupEvolutions, evolutionsRow;
    private View abilitiesLoading;

    private volatile boolean evolutionsLoaded = false;

    // Linhas de stats (você já tinha)
    private View rowHp, rowAtk, rowDef, rowSpAtk, rowSpDef, rowSpeed;

    public static PokemonDetailDialogFragment newInstance(@NonNull PokemonUi ui, int typeColor) {
        Bundle b = new Bundle();
        b.putSerializable(ARG_UI, ui);
        b.putInt(ARG_COLOR, typeColor);
        PokemonDetailDialogFragment f = new PokemonDetailDialogFragment();
        f.setArguments(b);
        return f;
    }

    @Override public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigate) nav = (OnNavigate) context;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle a = getArguments();
        if (a != null) {
            Serializable s = a.getSerializable(ARG_UI);
            if (s instanceof PokemonUi) pokemon = (PokemonUi) s;
            typeColor = a.getInt(ARG_COLOR, 0);
        }
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_Pokedex_Dialog_NoFrame);
        setCancelable(true);
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = d.getWindow().getAttributes();
            lp.dimAmount = 0.85f; // intensidade da sombra
            d.getWindow().setAttributes(lp);
            d.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        d.setCanceledOnTouchOutside(true);
        return d;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_pokemon_detail, container, false);

        // clique fora fecha; clique no card não propaga
        v.setOnClickListener(x -> dismissAllowingStateLoss());
        v.findViewById(R.id.cardDetail).setOnClickListener(x -> { /* consume */ });

        btPrev    = v.findViewById(R.id.btPrev);
        btNext    = v.findViewById(R.id.btNext);
        ivSprite  = v.findViewById(R.id.ivSprite);
        tvTitle   = v.findViewById(R.id.tvTitle);
        tabs      = v.findViewById(R.id.tabs);

        cgTypes   = v.findViewById(R.id.chipGroupTypes);

        groupStats         = v.findViewById(R.id.groupStats);
        groupAbilities     = v.findViewById(R.id.groupAbilities);
        abilitiesContainer = v.findViewById(R.id.abilitiesContainer);
        abilitiesLoading   = v.findViewById(R.id.abilitiesLoading);

        // Evolutions (NOVO)
        groupEvolutions = v.findViewById(R.id.groupEvolutions);
        evolutionsRow   = v.findViewById(R.id.evolutionsRow);

        rowHp    = v.findViewById(R.id.row_hp);
        rowAtk   = v.findViewById(R.id.row_atk);
        rowDef   = v.findViewById(R.id.row_def);
        rowSpAtk = v.findViewById(R.id.row_sp_atk);
        rowSpDef = v.findViewById(R.id.row_sp_def);
        rowSpeed = v.findViewById(R.id.row_speed);

        // Tabs – agora com 3 abas (Stats / Habilities / Evolutions)
        tabs.removeAllTabs();
        tabs.addTab(tabs.newTab().setText(R.string.tab_stats));
        tabs.addTab(tabs.newTab().setText(R.string.tab_habilities));
        tabs.addTab(tabs.newTab().setText(R.string.tab_evolutions));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                showSection(pos);
                if (pos == 1) { // Habilities
                    fillAbilitiesFromUi();
                } else if (pos == 2) { // Evolutions
                    loadEvolutionsIfNeeded();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) fillAbilitiesFromUi();
                if (tab.getPosition() == 2) loadEvolutionsIfNeeded();
            }
        });

        // Preenche o primeiro
        if (pokemon != null) bindAll(pokemon);

        // Navegação in-place (sem animação; não fecha o dialog)
        btPrev.setOnClickListener(view -> {
            if (nav == null || pokemon == null) return;
            PokemonUi prev = nav.requestPrev(pokemon);
            if (prev != null) bindAll(prev);
        });
        btNext.setOnClickListener(view -> {
            if (nav == null || pokemon == null) return;
            PokemonUi next = nav.requestNext(pokemon);
            if (next != null) bindAll(next);
        });

        return v;
    }

    private void showSection(int tabIndex) {
        // 0 = stats, 1 = habilities, 2 = evolutions
        groupStats.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        groupAbilities.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        groupEvolutions.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
    }

    private void bindAll(@NonNull PokemonUi p) {
        this.pokemon = p;
        this.evolutionsLoaded = false; // força recarregar ao navegar

        if (getContext() != null) {
            typeColor = TypeColors.bgFor(p.typesPt, requireContext());
        }

        Glide.with(this).load(p.imageUrl).into(ivSprite);
        tvTitle.setText(p.name);

        // Chips (primário + secundário mantendo a cor de cada)
        if (cgTypes != null) {
            cgTypes.removeAllViews();
            if (p.typesPt != null && !p.typesPt.isEmpty()) {
                cgTypes.addView(buildTypeChip(p.typesPt.get(0), typeColor));
                if (p.typesPt.size() > 1) {
                    int secColor = TypeColors.bgFor(
                            java.util.Collections.singletonList(p.typesPt.get(1)), requireContext());
                    cgTypes.addView(buildTypeChip(p.typesPt.get(1), secColor));
                }
            }
        }

        // Tab indicator na cor do tipo (vale para Evolutions também)
        tabs.setSelectedTabIndicatorColor(typeColor);
        tabs.setTabTextColors(
                ContextCompat.getColor(requireContext(), R.color.text_secondary),
                ContextCompat.getColor(requireContext(), R.color.text_primary)
        );

        // Setas (background circular já no drawable; aqui aplicamos tint)
        ColorStateList tint = ColorStateList.valueOf(typeColor);
        btPrev.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        btNext.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        btPrev.setBackgroundTintList(tint);
        btNext.setBackgroundTintList(tint);

        // Stats
        bindValueOnly(rowHp,  getString(R.string.stat_hp),      p.hp);
        bindValueOnly(rowAtk, getString(R.string.stat_attack),  p.attack);
        bindValueOnly(rowDef, getString(R.string.stat_defense), p.defense);
        bindWithBar(rowSpAtk, getString(R.string.stat_sp_atk), p.spAttack, typeColor);
        bindWithBar(rowSpDef, getString(R.string.stat_sp_def), p.spDefense, typeColor);
        bindWithBar(rowSpeed, getString(R.string.stat_speed),  p.speed,     typeColor);

        // limpa dinâmicos
        abilitiesContainer.removeAllViews();
        evolutionsRow.removeAllViews();
        if (abilitiesLoading != null) abilitiesLoading.setVisibility(View.GONE);

        // volta para Stats por padrão
        TabLayout.Tab t = tabs.getTabAt(0);
        if (t != null) t.select();
        showSection(0);
    }

    private void bindValueOnly(View row, String labelText, int value) {
        TextView tvLabel = row.findViewById(R.id.tvLabel);
        TextView tvValue = row.findViewById(R.id.tvValue);
        tvLabel.setText(labelText);
        tvValue.setText(String.valueOf(value));
    }

    private void bindWithBar(View row, String labelText, int value, int color) {
        TextView tvLabel   = row.findViewById(R.id.tvLabel);
        TextView tvValue   = row.findViewById(R.id.tvValue);
        ProgressBar pbStat = row.findViewById(R.id.pbStat);

        tvLabel.setText(labelText);
        tvValue.setText(String.valueOf(value));

        pbStat.setMax(180);
        pbStat.setProgress(value);
        pbStat.setProgressTintList(ColorStateList.valueOf(color));
        pbStat.setProgressBackgroundTintList(
                ColorStateList.valueOf(Color.argb(60, Color.red(color), Color.green(color), Color.blue(color)))
        );
    }

    private void fillAbilitiesFromUi() {
        abilitiesContainer.removeAllViews();
        if (abilitiesLoading != null) abilitiesLoading.setVisibility(View.GONE);

        List<String> abs = (pokemon != null) ? pokemon.abilities : null;
        if (abs == null || abs.isEmpty()) {
            abilitiesContainer.addView(buildAbilityText("—"));
            return;
        }
        for (String ab : abs) abilitiesContainer.addView(buildAbilityText(ab));
    }

    private TextView buildAbilityText(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(16f);
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        int p = dp(8);
        tv.setPadding(p*2, p, p*2, p);
        return tv;
    }

    private Chip buildTypeChip(@NonNull String text, int bgColor) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2);
        chip.setChipBackgroundColor(ColorStateList.valueOf(bgColor));
        chip.setTextColor(Color.WHITE);
        chip.setCheckable(false);
        chip.setClickable(false);
        chip.setChipMinHeight(dp(32));
        chip.setChipCornerRadius(dp(16));
        int hp = dp(10);
        chip.setPadding(hp, 0, hp, 0);
        return chip;
    }

    // ===== Evolutions (NOVO) =====

    private void loadEvolutionsIfNeeded() {
        if (evolutionsLoaded || pokemon == null) return;

        evolutionsRow.removeAllViews();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 1) species -> evolution_chain
                Response<SpeciesDTO> sp = RetrofitProvider.getApi()
                        .species(pokemon.name.toLowerCase())
                        .execute();
                if (!sp.isSuccessful() || sp.body() == null || sp.body().evolution_chain == null) return;

                int chainId = parseChainId(sp.body().evolution_chain.url);
                if (chainId <= 0) return;

                // 2) evolution chain
                Response<EvolutionChainDTO> ch = RetrofitProvider.getApi()
                        .evolutionChain(chainId)
                        .execute();
                if (!ch.isSuccessful() || ch.body() == null || ch.body().chain == null) return;

                // 3) percorre a chain em ordem linear
                List<String> names = new ArrayList<>();
                collectChainNames(ch.body().chain, names);

                // 4) pega detalhes de cada estágio para montar imagem/tipos
                List<PokemonUi> evoUis = new ArrayList<>();
                for (String nm : names) {
                    Response<com.example.pokedex_mobile.dataRemote.model.PokemonDetailClass> d =
                            RetrofitProvider.getApi().detail(nm).execute();
                    if (d.isSuccessful() && d.body() != null) {
                        evoUis.add(PokemonMapper.toUi(d.body()));
                    }
                }

                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    evolutionsRow.removeAllViews();
                    for (int i = 0; i < evoUis.size(); i++) {
                        PokemonUi ui = evoUis.get(i);
                        evolutionsRow.addView(buildEvolutionItem(ui));
                        if (i < evoUis.size() - 1) {
                            evolutionsRow.addView(buildArrow(typeColor));
                        }
                    }
                    evolutionsLoaded = true;
                });

            } catch (Exception ignored) { }
        });
    }

    private int parseChainId(String url) {
        try {
            // exemplo: https://pokeapi.co/api/v2/evolution-chain/67/
            String path = URI.create(url).getPath();
            String[] parts = path.split("/");
            for (int i = parts.length - 1; i >= 0; i--) {
                if (!parts[i].isEmpty()) return Integer.parseInt(parts[i]);
            }
        } catch (Exception ignored) { }
        return -1;
    }

    private void collectChainNames(EvolutionChainDTO.ChainLink link, List<String> out) {
        if (link == null || link.species == null || link.species.name == null) return;
        out.add(link.species.name);
        if (link.evolves_to != null) {
            for (EvolutionChainDTO.ChainLink child : link.evolves_to) {
                collectChainNames(child, out);
            }
        }
    }

    private View buildEvolutionItem(@NonNull PokemonUi ui) {
        LinearLayout col = new LinearLayout(requireContext());
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(4), 0, dp(4), 0);
        col.setGravity(Gravity.CENTER);

        ImageView img = new ImageView(requireContext());
        int size = dp(72); // era 84dp -> menor
        LinearLayout.LayoutParams lpI = new LinearLayout.LayoutParams(size, size);
        img.setLayoutParams(lpI);
        img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        Glide.with(this).load(ui.imageUrl).into(img);

        TextView name = new TextView(requireContext());
        name.setText(ui.name);
        name.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        name.setTextSize(12f); // era 14f -> menor
        name.setGravity(Gravity.CENTER);
        name.setPadding(0, dp(4), 0, dp(2));

        ChipGroup chips = new ChipGroup(requireContext());
        chips.setSingleLine(false);
        chips.setSingleSelection(false);
        chips.setChipSpacingHorizontal(dp(4));
        chips.setChipSpacingVertical(dp(1));

        if (ui.typesPt != null) {
            for (int i = 0; i < ui.typesPt.size(); i++) {
                String tt = ui.typesPt.get(i);
                int c = TypeColors.bgFor(java.util.Collections.singletonList(tt), requireContext());
                chips.addView(buildTypeChipCompact(tt, c)); // chip menor
            }
        }

        col.addView(img);
        col.addView(name);
        col.addView(chips);
        return col;
    }

    private ImageView buildArrow(int color) {
        ImageView iv = new ImageView(requireContext());
        iv.setImageResource(R.drawable.ic_arrow_right);
        iv.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        iv.setBackgroundResource(R.drawable.bg_circle_button);
        iv.setBackgroundTintList(ColorStateList.valueOf(color));
        int s = dp(24); // era 28dp -> menor
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(s, s);
        lp.setMargins(dp(6), dp(16), dp(6), 0); // menos margem
        iv.setLayoutParams(lp);
        iv.setPadding(dp(4), dp(4), dp(4), dp(4));
        return iv;
    }

    private Chip buildTypeChipCompact(@NonNull String text, int bgColor) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setTextAppearance(
                com.google.android.material.R.style.TextAppearance_MaterialComponents_Caption
        );
        chip.setTextSize(10f);
        chip.setChipBackgroundColor(ColorStateList.valueOf(bgColor));
        chip.setTextColor(Color.WHITE);
        chip.setCheckable(false);
        chip.setClickable(false);

        chip.setChipMinHeight(dp(20));
        chip.setChipCornerRadius(dp(10));
        int hp = dp(6);
        chip.setPadding(hp, 0, hp, 0);
        chip.setEnsureMinTouchTargetSize(false);
        return chip;
    }


    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return (int) (v * d);
    }

    // ===== DTOs mínimos para species/evolution-chain =====
    public static class SpeciesDTO {
        public NamedApiResource evolution_chain; // tem "url"
        public static class NamedApiResource { public String url; }
    }
    public static class EvolutionChainDTO {
        public ChainLink chain;
        public static class ChainLink {
            public SpeciesRef species;
            public List<ChainLink> evolves_to;
        }
        public static class SpeciesRef { public String name; public String url; }
    }
}
