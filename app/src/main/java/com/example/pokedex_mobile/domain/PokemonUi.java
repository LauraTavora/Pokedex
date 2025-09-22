package com.example.pokedex_mobile.domain;

import java.io.Serializable;
import java.util.List;

public class PokemonUi implements Serializable {
    public final int id;
    public final String name;
    public final String imageUrl;
    public final List<String> typesPt;

    // stats “achatados”
    public final int hp;
    public final int attack;
    public final int defense;
    public final int spAttack;
    public final int spDefense;
    public final int speed;

    // novas: habilidades já prontas para exibir
    public final List<String> abilities;

    public PokemonUi(int id,
                     String name,
                     String imageUrl,
                     List<String> typesPt,
                     int hp, int attack, int defense, int spAttack, int spDefense, int speed,
                     List<String> abilities) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.typesPt = typesPt;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.spAttack = spAttack;
        this.spDefense = spDefense;
        this.speed = speed;
        this.abilities = abilities;
    }
}
