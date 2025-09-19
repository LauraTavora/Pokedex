package com.example.pokedex_mobile.dataRemote;

import com.example.pokedex_mobile.dataRemote.model.PokemonDetailClass;
import com.example.pokedex_mobile.dataRemote.model.PokemonListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PokeApi {
    @GET("pokemon")
    Call<PokemonListResponse> list(@Query("limit") int limit);

    @GET("pokemon/{idOrName}")
    Call<PokemonDetailClass> detail(@Path("idOrName") String idOrName);
}

