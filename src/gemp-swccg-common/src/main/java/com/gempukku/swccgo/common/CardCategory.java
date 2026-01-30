package com.gempukku.swccgo.common;

/**
 * Represents the card categories of SWCCG cards.
 */
public enum CardCategory implements Filterable {

    ADMIRALS_ORDER("Admiral's Order"),
    ARTIFACT("Artifact"),
    CHARACTER("Character"),
    CREATURE("Creature"),
    DEFENSIVE_SHIELD("Defensive Shield"),
    DEVICE("Device"),
    EFFECT("Effect"),
    EPIC_EVENT("Epic Event"),
    GAME_AID("Game Aid"),
    // Adding general's order for Beezer Bowl 2025 cardset
    GENERALS_ORDER("General's Order"),
    INTERRUPT("Interrupt"),
    JEDI_TEST("Jedi Test"),
    LOCATION("Location"),
    OBJECTIVE("Objective"),
    PODRACER("Podracer"),
    SORCERY_TEST("Sorcery Test"),
    STARSHIP("Starship"),
    VEHICLE("Vehicle"),
    WEAPON("Weapon");

    private String _humanReadable;

    CardCategory(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }
}
