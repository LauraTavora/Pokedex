package com.example.pokedex_mobile.domain;

import java.util.List;
import java.util.Map;

public class PokemonUi {
    public final int id;
    public final String name;
    public final String imageUrl;
    public final List<String> typesPt;
    public final Map<String, Integer> stats;

    public PokemonUi(int id, String name, String imageUrl,
                     List<String> typesPt, Map<String, Integer> stats) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.typesPt = typesPt;
        this.stats = stats;
    }
}

