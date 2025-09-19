package com.example.pokedex_mobile.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
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
    private final OnClick click;

    public PokemonAdapter(OnClick click) { this.click = click; }

    public void submit(List<PokemonUi> list) {
        items.clear(); items.addAll(list);
        full.clear();  full.addAll(list);
        notifyDataSetChanged();
    }

    public void filter(String q) {
        String s = q == null ? "" : q.trim().toLowerCase();
        items.clear();
        if (s.isEmpty()) {
            items.addAll(full);
        } else {
            for (PokemonUi p : full) {
                boolean match = p.name.toLowerCase().contains(s);
                if (!match) {
                    for (String t : p.typesPt) {
                        if (t.toLowerCase().contains(s)) { match = true; break; }
                    }
                }
                if (match) items.add(p);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pokemon, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(items.get(position), click);
    }

    @Override public int getItemCount() { return items.size(); }

    // ===== Helpers de cor =====

    private static int tintForPokeball(int base) {
        // candidatos: muito mais claro e muito mais escuro
        int light = mixWithColor(base, Color.WHITE, 0.72f); // 72% branco
        int dark  = mixWithColor(base, Color.BLACK, 0.55f); // 55% preto

        double lumBase  = ColorUtils.calculateLuminance(base);
        double lumLight = ColorUtils.calculateLuminance(light);
        double lumDark  = ColorUtils.calculateLuminance(dark);

        // escolhe o que contrasta mais com o fundo
        int chosen = Math.abs(lumLight - lumBase) >= Math.abs(lumDark - lumBase) ? light : dark;

        // dá uma "lavada": reduz saturação
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(chosen, hsl);
        hsl[1] = clamp(hsl[1] * 0.55f); // menos saturado
        int washed = ColorUtils.HSLToColor(hsl);

        // alpha agradável (ajuste fino aqui se quiser mais/menos presença)
        return ColorUtils.setAlphaComponent(washed, (int) (255 * 1f));
    }

    private static int mixWithColor(int color, int mix, float ratio) {
        // ratio: 0..1 (quanto de 'mix' entra)
        int a = 255;
        int r = Math.round(Color.red(color)   * (1f - ratio) + Color.red(mix)   * ratio);
        int g = Math.round(Color.green(color) * (1f - ratio) + Color.green(mix) * ratio);
        int b = Math.round(Color.blue(color)  * (1f - ratio) + Color.blue(mix)  * ratio);
        return Color.argb(a, r, g, b);
    }

    private static float clamp(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }


    private static int dp(Context ctx, int v) {
        return Math.round(v * ctx.getResources().getDisplayMetrics().density);
    }

    // ===== ViewHolder =====
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

            // Título
            tvName.setText(p.name);

            // Cor base do card por tipo
            int base = TypeColors.bgFor(p.typesPt, ctx);
            bg.setBackgroundColor(base);

            // Sprite
            Glide.with(ctx).load(p.imageUrl).into(ivSprite);

            // Pokébola do card: escolhe automaticamente um tom claro/escuro com bom contraste
            ivPokeballBg.setImageAlpha(255);
            int softTint = tintForPokeball(base); // <<< usa a função abaixo
            ivPokeballBg.setImageTintList(ColorStateList.valueOf(softTint));
            ivPokeballBg.setImageTintMode(PorterDuff.Mode.SRC_IN);


            // Chips de tipos (empilhados)
            chips.removeAllViews();
            for (String t : p.typesPt) {
                TextView chip = new TextView(ctx);
                chip.setText(t);
                chip.setBackgroundResource(R.drawable.bg_chip);
                chip.setTextColor(Color.WHITE);
                chip.setTextSize(12f);
                chip.setPadding(dp(ctx,14), dp(ctx,6), dp(ctx,14), dp(ctx,6));

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp.topMargin = dp(ctx,6);
                chips.addView(chip, lp);
            }

            itemView.setOnClickListener(v -> click.onClick(p));
        }
    }
}
