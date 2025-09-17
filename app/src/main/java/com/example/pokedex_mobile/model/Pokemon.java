package com.example.pokedex_mobile.model;


import java.io.Serializable;
import java.util.List;

public class Pokemon implements Serializable {
    public int id;
    public String name;
    public int height;
    public int weight;
    public Sprites sprites;
    public List<TypeWrapper> types;
    public List<StatWrapper> stats;

    public static class Sprites implements Serializable {
        public String front_default;
        // existem mais campos, mas esse já atende ao requisito
    }

    public static class TypeWrapper implements Serializable {
        public int slot;
        public Type type;
    }

    public static class Type implements Serializable {
        public String name;
        public String url;
    }

    public static class StatWrapper implements Serializable {
        public int base_stat;
        public int effort;
        public Stat stat;
    }

    public static class Stat implements Serializable {
        public String name;
        public String url;
    }
}

