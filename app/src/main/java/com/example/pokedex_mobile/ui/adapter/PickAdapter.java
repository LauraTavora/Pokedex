package com.example.pokedex_mobile.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pokedex_mobile.R;
import com.example.pokedex_mobile.domain.PokemonUi;
import com.example.pokedex_mobile.ui.TypeColors;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class PickAdapter extends RecyclerView.Adapter<PickAdapter.VH> {

    public interface OnPick {
        void onPicked(@NonNull PokemonUi p);
    }

    private final List<PokemonUi> data = new ArrayList<>();
    private final OnPick onPick;

    public PickAdapter(@NonNull OnPick onPick) {
        this.onPick = onPick;
    }

    public void submit(List<PokemonUi> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pick_pokemon, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PokemonUi p = data.get(position);

        // cor do card pelo tipo primário
        int color = TypeColors.bgFor(p.typesPt, h.itemView.getContext());
        h.card.setCardBackgroundColor(color);

        // nome + sprite
        h.tvName.setText(p.name);
        Glide.with(h.itemView).load(p.imageUrl).into(h.ivSprite);

        // tipos
        h.tvType1.setVisibility(View.GONE);
        h.tvType2.setVisibility(View.GONE);
        if (p.typesPt != null && !p.typesPt.isEmpty()) {
            h.tvType1.setText(p.typesPt.get(0));
            h.tvType1.setVisibility(View.VISIBLE);
            if (p.typesPt.size() > 1) {
                h.tvType2.setText(p.typesPt.get(1));
                h.tvType2.setVisibility(View.VISIBLE);
            }
        }

        // stats grandes (hp/atk/def)
        h.tvHpValue.setText(String.valueOf(p.hp));
        h.tvAtkValue.setText(String.valueOf(p.attack));
        h.tvDefValue.setText(String.valueOf(p.defense));

        h.itemView.setOnClickListener(v -> onPick.onPicked(p));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView ivSprite;
        TextView tvName, tvType1, tvType2;
        TextView tvHpValue, tvAtkValue, tvDefValue;

        VH(@NonNull View v) {
            super(v);
            card       = v.findViewById(R.id.card);
            ivSprite   = v.findViewById(R.id.ivSprite);
            tvName     = v.findViewById(R.id.tvName);
            tvType1    = v.findViewById(R.id.tvType1);
            tvType2    = v.findViewById(R.id.tvType2);
            tvHpValue  = v.findViewById(R.id.tvHpValue);
            tvAtkValue = v.findViewById(R.id.tvAtkValue);
            tvDefValue = v.findViewById(R.id.tvDefValue);
        }
    }
}
