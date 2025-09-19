package com.example.pokedex_mobile.dataRemote.model;

import com.google.gson.annotations.SerializedName;

public class StatSlot {
    @SerializedName("base_stat")
    public int baseStat;
    public NamedUrl stat;
}

