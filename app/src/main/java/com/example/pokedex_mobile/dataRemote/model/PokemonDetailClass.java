package com.example.pokedex_mobile.dataRemote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PokemonDetailClass {
    public int id;
    public String name;
    public Sprites sprites;
    public List<TypeSlot> types;
    public List<StatSlot> stats;
    public List<AbilitySlot> abilities;

    // ====== modelos aninhados ======

    public static class Sprites {
        @SerializedName("front_default")
        public String front_default;

        public Other other;

        public static class Other {
            @SerializedName("official-artwork")
            public OfficialArtwork officialArtwork;

            public static class OfficialArtwork {
                @SerializedName("front_default")
                public String front_default;
            }
        }
    }

    public static class TypeSlot {
        public int slot;
        public NamedApiResource type;
    }

    public static class StatSlot {
        @SerializedName("base_stat")
        public Integer baseStat;
        public NamedApiResource stat;
    }

    public static class AbilitySlot {
        public NamedApiResource ability;
        @SerializedName("is_hidden")
        public boolean is_hidden;
    }

    public static class NamedApiResource {
        public String name;
        public String url;
    }
}
