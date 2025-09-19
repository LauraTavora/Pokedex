package com.example.pokedex_mobile.ui;


import android.content.Context;
import androidx.core.content.ContextCompat;
import com.example.pokedex_mobile.R;
import java.util.List;

public class TypeColors {

    // Retorna a cor base usando o primeiro tipo do Pokémon (em PT-BR)
    public static int bgFor(List<String> typesPt, Context ctx) {
        if (typesPt == null || typesPt.isEmpty())
            return ContextCompat.getColor(ctx, R.color.type_water);

        String t = typesPt.get(0);

        if ("Planta".equals(t))   return ContextCompat.getColor(ctx, R.color.type_grass);     // #4DD6A0
        if ("Veneno".equals(t))   return ContextCompat.getColor(ctx, R.color.type_poison);    // #9C5BC9
        if ("Fogo".equals(t))     return ContextCompat.getColor(ctx, R.color.type_fire);      // #F77C7C
        if ("Água".equals(t))     return ContextCompat.getColor(ctx, R.color.type_water);     // #76BDFE
        if ("Voador".equals(t))   return ContextCompat.getColor(ctx, R.color.type_flying);    // #9CA0FF
        if ("Inseto".equals(t))   return ContextCompat.getColor(ctx, R.color.type_bug);       // #C6D17B
        if ("Normal".equals(t))   return ContextCompat.getColor(ctx, R.color.type_normal);    // #CFCFCF
        if ("Psíquico".equals(t)) return ContextCompat.getColor(ctx, R.color.type_psychic);   // #FF80AB
        if ("Pedra".equals(t))    return ContextCompat.getColor(ctx, R.color.type_rock);      // #B8A038
        if ("Terra".equals(t))    return ContextCompat.getColor(ctx, R.color.type_ground);    // #927D44
        if ("Elétrico".equals(t)) return ContextCompat.getColor(ctx, R.color.type_electric);  // #FFD966
        if ("Fantasma".equals(t)) return ContextCompat.getColor(ctx, R.color.type_ghost);     // #705898
        if ("Fada".equals(t))     return ContextCompat.getColor(ctx, R.color.type_fairy);     // #FFBED2

        // Fallback “seguro”
        return ContextCompat.getColor(ctx, R.color.type_water);
    }
}


