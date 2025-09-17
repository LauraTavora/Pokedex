package com.example.pokedex_mobile.model;


import java.util.List;

public class PokemonListResponse {
    public List<PokemonResult> results;

    public static class PokemonResult {
        public String name;
        public String url;
    }
}

