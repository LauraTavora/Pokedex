package com.example.pokedex_mobile.adapter;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pokedex_mobile.R;
import com.example.pokedex_mobile.model.Pokemon;
import com.example.pokedex_mobile.ui.PokemonDetailActivity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.ViewHolder> {
    private Context context;
    private List<Pokemon> list;
    private Map<String, Integer> typeColors;

    public PokemonAdapter(Context context, List<Pokemon> list, Map<String, Integer> typeColors) {
        this.context = context;
        this.list = list;
        this.typeColors = typeColors;
    }

    @Override
    public PokemonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_pokemon, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PokemonAdapter.ViewHolder holder, int position) {
        Pokemon p = list.get(position);
        holder.name.setText(capitalize(p.name));

        // tipos -> join
        if (p.types != null && p.types.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < p.types.size(); i++) {
                sb.append(capitalize(p.types.get(i).type.name));
                if (i < p.types.size() - 1) sb.append(" · ");
            }
            holder.types.setText(sb.toString());
        } else {
            holder.types.setText("");
        }

        // imagem
        if (p.sprites != null && p.sprites.front_default != null) {
            Glide.with(context).load(p.sprites.front_default).into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_pokeball);
        }

        // cor do card baseada no primeiro tipo
        Integer color = null;
        if (p.types != null && !p.types.isEmpty()) {
            String typeName = p.types.get(0).type.name.toLowerCase();
            color = typeColors.get(typeName);
        }
        if (color == null) color = 0xFFEFEFEF; // cor padrão

        // definir background com canto arredondado (CardView já tem)
        holder.card.setCardBackgroundColor(color.intValue());

        holder.itemView.setOnClickListener(v -> {
            // abrir detalhe: enviar índice e lista (Serializable)
            Intent it = new Intent(context, PokemonDetailActivity.class);
            it.putExtra(PokemonDetailActivity.EXTRA_POKEMON_INDEX, position);
            it.putExtra(PokemonDetailActivity.EXTRA_POKEMON_LIST, (Serializable) list);
            context.startActivity(it);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        ImageView image;
        TextView name, types;
        public ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardView);
            image = itemView.findViewById(R.id.pokemonImage);
            name = itemView.findViewById(R.id.pokemonName);
            types = itemView.findViewById(R.id.pokemonTypes);
        }
    }
}

