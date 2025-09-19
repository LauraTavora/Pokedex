package com.example.pokedex_mobile.data;

import com.example.pokedex_mobile.data.mapper.PokemonMapper;
import com.example.pokedex_mobile.dataRemote.PokeApi;
import com.example.pokedex_mobile.dataRemote.model.PokemonDetailClass;
import com.example.pokedex_mobile.dataRemote.model.PokemonListResponse;
import com.example.pokedex_mobile.domain.PokemonUi;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import retrofit2.Response;

public class PokemonRepository {
    private final PokeApi api;
    private final ExecutorService pool = Executors.newFixedThreadPool(8);

    public PokemonRepository(PokeApi api) { this.api = api; }

    public List<PokemonUi> loadFirst50() throws Exception {
        Response<PokemonListResponse> resp = api.list(50).execute();
        if (!resp.isSuccessful() || resp.body() == null) throw new IOException("Falha na lista");

        List<Callable<PokemonDetailClass>> tasks = new ArrayList<>();
        resp.body().results.forEach(ref ->
                tasks.add(() -> {
                    Response<PokemonDetailClass> r = api.detail(ref.name).execute();
                    if (!r.isSuccessful() || r.body() == null) throw new IOException("Falha detalhe: " + ref.name);
                    return r.body();
                })
        );

        List<Future<PokemonDetailClass>> futures = pool.invokeAll(tasks);
        List<PokemonUi> out = new ArrayList<>();
        for (Future<PokemonDetailClass> f : futures) out.add(PokemonMapper.toUi(f.get()));
        out.sort(Comparator.comparingInt(p -> p.id));
        return out;
    }
}
