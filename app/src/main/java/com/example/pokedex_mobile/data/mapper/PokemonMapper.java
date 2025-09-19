package com.example.pokedex_mobile.data.mapper;

import com.example.pokedex_mobile.dataRemote.model.PokemonDetailClass;
import com.example.pokedex_mobile.domain.PokemonUi;

import java.util.*;
import java.util.stream.Collectors;

public class PokemonMapper {

    private static final Map<String,String> TYPE_PT;
    private static final Map<String,String> STAT_PT;

    static {
        Map<String,String> t = new HashMap<>();
        t.put("grass","Planta"); t.put("poison","Veneno"); t.put("fire","Fogo");
        t.put("water","Água");   t.put("flying","Voador"); t.put("electric","Elétrico");
        t.put("bug","Inseto");   t.put("normal","Normal"); t.put("ground","Terra");
        t.put("fairy","Fada");   t.put("fighting","Lutador"); t.put("psychic","Psíquico");
        t.put("rock","Pedra");   t.put("ghost","Fantasma"); t.put("ice","Gelo");
        t.put("dragon","Dragão"); t.put("dark","Sombrio");  t.put("steel","Aço");
        TYPE_PT = Collections.unmodifiableMap(t);

        Map<String,String> s = new HashMap<>();
        s.put("hp","Vida"); s.put("attack","Ataque"); s.put("defense","Defesa");
        s.put("special-attack","Atq. Esp."); s.put("special-defense","Def. Esp.");
        s.put("speed","Velocidade");
        STAT_PT = Collections.unmodifiableMap(s);
    }

    public static PokemonUi toUi(PokemonDetailClass d) {
        String name = d.name.substring(0,1).toUpperCase() + d.name.substring(1);
        String img = d.sprites != null && d.sprites.other != null &&
                d.sprites.other.officialArtwork != null ? d.sprites.other.officialArtwork.front_default : null;

        List<String> types = d.types.stream()
                .sorted(Comparator.comparingInt(ts -> ts.slot))
                .map(ts -> TYPE_PT.getOrDefault(ts.type.name, ts.type.name))
                .collect(Collectors.toList());

        Map<String,Integer> stats = new LinkedHashMap<>();
        d.stats.forEach(s -> {
            String k = STAT_PT.getOrDefault(s.stat.name, s.stat.name);
            int v = Math.max(0, Math.min(180, s.baseStat));
            stats.put(k, v);
        });

        return new PokemonUi(d.id, name, img, types, stats);
    }
}

