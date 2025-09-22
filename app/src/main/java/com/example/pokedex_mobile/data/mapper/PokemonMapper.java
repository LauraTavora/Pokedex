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

    private static String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    public static PokemonUi toUi(PokemonDetailClass d) {
        String name = cap(d.name);
        String img = (d.sprites != null && d.sprites.other != null &&
                d.sprites.other.officialArtwork != null)
                ? d.sprites.other.officialArtwork.front_default : null;

        List<String> types = d.types.stream()
                .sorted(Comparator.comparingInt(ts -> ts.slot))
                .map(ts -> TYPE_PT.getOrDefault(ts.type.name, ts.type.name))
                .collect(Collectors.toList());

        // stats “achatados”
        int hp = 0, atk = 0, def = 0, spAtk = 0, spDef = 0, speed = 0;
        for (PokemonDetailClass.StatSlot s : d.stats) {
            String k = s.stat.name;
            int v = Math.max(0, Math.min(180, s.baseStat));
            switch (k) {
                case "hp": hp = v; break;
                case "attack": atk = v; break;
                case "defense": def = v; break;
                case "special-attack": spAtk = v; break;
                case "special-defense": spDef = v; break;
                case "speed": speed = v; break;
            }
        }

        // abilities (já em PT “bonitinho” = só capitaliza e troca ‘-’ por espaço)
        List<String> abilities = new ArrayList<>();
        if (d.abilities != null) {
            for (PokemonDetailClass.AbilitySlot a : d.abilities) {
                if (a != null && a.ability != null && a.ability.name != null) {
                    String nm = a.ability.name.trim();
                    if (!nm.isEmpty()) abilities.add(cap(nm.replace('-', ' ')));
                }
            }
        }

        return new PokemonUi(
                d.id, name, img, types,
                hp, atk, def, spAtk, spDef, speed,
                abilities
        );
    }
}
