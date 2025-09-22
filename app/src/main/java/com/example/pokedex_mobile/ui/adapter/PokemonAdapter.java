package com.example.pokedex_mobile.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.graphics.PorterDuff;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pokedex_mobile.R;
import com.example.pokedex_mobile.domain.PokemonUi;
import com.example.pokedex_mobile.ui.TypeColors;

import java.util.ArrayList;
import java.util.List;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.VH> {

    public interface OnClick { void onClick(PokemonUi p); }

    private final List<PokemonUi> items = new ArrayList<>();
    private final List<PokemonUi> full  = new ArrayList<>();

    private String nameQuery = "";   // busca por nome (barra)
    private String typeFilter = "";  // filtro por tipo (drawer)

    private final OnClick click;

    public PokemonAdapter(OnClick click) { this.click = click; }

    public void submit(List<PokemonUi> list) {
        full.clear();
        if (list != null) full.addAll(list);
        applyFilters();
    }

    // ===== NOVO: filtros independentes =====
    public void setNameQuery(String q) {
        nameQuery = q == null ? "" : q.trim().toLowerCase();
        applyFilters();
    }

    public void setTypeFilter(String typePt) {
        typeFilter = typePt == null ? "" : typePt.trim().toLowerCase();
        applyFilters();
    }

    public boolean hasTypeFilter() {
        return typeFilter != null && !typeFilter.trim().isEmpty();
    }

    private void applyFilters() {
        items.clear();
        for (PokemonUi p : full) {
            boolean ok = true;

            // Nome (apenas nome)
            if (!nameQuery.isEmpty()) {
                ok = p.name != null && p.name.toLowerCase().contains(nameQuery);
            }

            // Tipo (apenas tipo) — combina com o filtro anterior (AND)
            if (ok && !typeFilter.isEmpty()) {
                boolean hasType = false;
                if (p.typesPt != null) {
                    for (String t : p.typesPt) {
                        if (t != null && t.trim().equalsIgnoreCase(typeFilter)) {
                            hasType = true; break;
                        }
                    }
                }
                ok = hasType;
            }

            if (ok) items.add(p);
        }
        notifyDataSetChanged();
    }

    public PokemonUi getItem(int position) {
        return (position >= 0 && position < items.size()) ? items.get(position) : null;
    }

    public int indexOfId(int id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == id) return i;
        }
        return -1;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pokemon, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) { h.bind(items.get(position), click); }
    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        LinearLayout chips;
        ImageView ivSprite, ivPokeballBg;
        View bg;

        VH(@NonNull View v) {
            super(v);
            tvName       = v.findViewById(R.id.tvName);
            chips        = v.findViewById(R.id.chips);
            ivSprite     = v.findViewById(R.id.ivSprite);
            ivPokeballBg = v.findViewById(R.id.bgPokeballCard);
            bg           = v.findViewById(R.id.bgColor);
        }

        void bind(PokemonUi p, OnClick click) {
            Context ctx = itemView.getContext();
            tvName.setText(p.name);

            // Fundo por tipo
            int base = TypeColors.bgFor(p.typesPt, ctx);
            bg.setBackgroundColor(base);

            // Sprite
            Glide.with(ctx).load(p.imageUrl).into(ivSprite);

            // Pokébola com leve tinta do tipo (destacada mas suave)
            int pokeTint = (base & 0x00FFFFFF) | (0xCC << 24); // ~80% opacidade
            ivPokeballBg.setColorFilter(pokeTint, PorterDuff.Mode.SRC_ATOP);

            // Chips empilhados
            chips.removeAllViews();
            int dp = (int) (ctx.getResources().getDisplayMetrics().density);
            if (p.typesPt != null) {
                for (String t : p.typesPt) {
                    TextView chip = new TextView(ctx);
                    chip.setText(t);
                    chip.setBackgroundResource(R.drawable.bg_chip);
                    chip.setTextColor(Color.WHITE);
                    chip.setTextSize(12f);
                    chip.setPadding(14*dp, 6*dp, 14*dp, 6*dp);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.topMargin = 6*dp;
                    chips.addView(chip, lp);
                }
            }

            itemView.setOnClickListener(v -> click.onClick(p));
        }
    }
}
