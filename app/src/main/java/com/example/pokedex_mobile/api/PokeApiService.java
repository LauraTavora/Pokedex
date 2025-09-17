package com.example.pokedex_mobile.api;


import com.example.pokedex_mobile.model.Pokemon;
import com.example.pokedex_mobile.model.PokemonListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PokeApiService {
    @GET("pokemon")
    Call<PokemonListResponse> getPokemonList(@Query("limit") int limit);

    @GET("pokemon/{name}")
    Call<Pokemon> getPokemonDetails(@Path("name") String name);
}

