package com.example.pokedex_mobile.dataRemote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitProvider {
    private static PokeApi api;

    public static PokeApi getApi() {
        if (api == null) {
            api = new Retrofit.Builder()
                    .baseUrl("https://pokeapi.co/api/v2/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(PokeApi.class);
        }
        return api;
    }
}
