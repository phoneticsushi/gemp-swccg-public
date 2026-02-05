package com.gempukku.swccgo.game.layout;

import com.gempukku.swccgo.filters.Filters;

/**
 * Represents the location layout for Endor.
 */
public class EndorLayout extends AbstractSystemLayout {

    // Layout order for Endor:
    //  1) Sites (in forward or reverse order)
    //      A) Bunker
    //      B) Generator Chamber (Beezer Bowl 2025)
    //      C) Landing Platform
    //      D) Exterior sites
    //      E) Mt. Krana Pass (Beezer Bowl 2025)
    //      F) Gorax's Lair (Beezer Bowl 2025)
    //      G) Mt. Krana: Apex (Beezer Bowl 2025)
    //      H) Zarrak's Hideout (Beezer Bowl 2025)
    //      I) Ewok Village
    //      J) Chief Chirpa's Hut
    //  2) Clouds
    //  3) Planet
    //  4) Asteroids
    //  5) Big One
    //  6) Big One: Asteroid Cave

    /**
     * Needed to generate snapshot.
     */
    public EndorLayout() {
    }

    /**
     * Creates the location layout for Endor.
     * @param systemName the system name
     * @param parsec the parsec number for the system
     */
    public EndorLayout(String systemName, int parsec) {
        super(systemName, parsec);

        //  1) Sites (in forward or reverse order)
        _groupOrders.add(
                new LocationReversibleGroupOrder(
                        //  A) Bunker
                        new LocationGroup("Bunker", Filters.Bunker),
                        //  B) Generator Chamber
                        new LocationGroup("Generator Chamber", Filters.Generator_Chamber),                        
                        //  C) Landing Platform
                        new LocationGroup("Landing Platform", Filters.Landing_Platform),
                        //  D) Exterior sites (excluding Mt. Krana sites)
                        new LocationGroup("Exterior sites", Filters.and(Filters.exterior_site, Filters.not(Filters.or(Filters.Landing_Platform,
                                Filters.Ewok_Village, Filters.Endor_Mt_Krana_Pass, Filters.Goraxs_Lair, Filters.Apex)), Filters.partOfSystem(systemName))),
                        //  E) Mt. Krana Pass (must be on end of Mt. Krana group)
                        new LocationGroup("Mt. Krana Pass", Filters.Endor_Mt_Krana_Pass),
                        //  F) Gorax's Lair (middle of Mt. Krana group)
                        new LocationGroup("Gorax's Lair", Filters.Goraxs_Lair),
                        //  G) Mt. Krana: Apex (must be on end of Mt. Krana group)
                        new LocationGroup("Mt. Krana: Apex", Filters.Apex),
                        //  H) Zarrak's Hideout
                        new LocationGroup("Zarrak's Hideout", Filters.Zarraks_Hideout),
                        //  I) Ewok Village
                        new LocationGroup("Ewok Village", Filters.Ewok_Village),
                        //  J) Chief Chirpa's Hut
                        new LocationGroup("Chief Chirpa's Hut", Filters.Chief_Chirpas_Hut)
                        
                )
        );
        //  2) Clouds
        //  3) Planet
        //  4) Asteroids
        //  5) Big One
        //  6) Big One: Asteroid Cave
        _groupOrders.add(
                new LocationFixedGroupOrder(getPlanetSystemAndSectorsLocationGroups(systemName)));
    }
}
