package dev.flath.wahlumfragen;

public sealed interface Party permits PartyUnion, SingleParty {

    String id();
    String name();

}
