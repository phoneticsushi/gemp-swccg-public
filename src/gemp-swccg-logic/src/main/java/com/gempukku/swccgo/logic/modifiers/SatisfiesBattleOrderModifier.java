package com.gempukku.swccgo.logic.modifiers;

import com.gempukku.swccgo.common.Filterable;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.logic.conditions.Condition;

/**
 * A modifier that causes a location to satisfy the Battle Order requirements when occupied.
 * (Occupying this location counts as occupying both a battleground site and battleground system.)
 */
public class SatisfiesBattleOrderModifier extends AbstractModifier {

    /**
     * Creates a modifier that causes a location to satisfy Battle Order requirements when occupied.
     * @param source the source of the modifier (typically the location itself)
     */
    public SatisfiesBattleOrderModifier(PhysicalCard source) {
        this(source, source, null);
    }

    /**
     * Creates a modifier that causes a location to satisfy Battle Order requirements when occupied.
     * @param source the source of the modifier
     * @param locationFilter the filter for locations affected
     */
    public SatisfiesBattleOrderModifier(PhysicalCard source, Filterable locationFilter) {
        this(source, locationFilter, null);
    }

    /**
     * Creates a modifier that causes a location to satisfy Battle Order requirements when occupied.
     * @param source the source of the modifier
     * @param locationFilter the filter for locations affected
     * @param condition the condition under which this modifier is in effect
     */
    public SatisfiesBattleOrderModifier(PhysicalCard source, Filterable locationFilter, Condition condition) {
        super(source, "Satisfies Battle Order when occupied", Filters.and(Filters.location, locationFilter), condition, ModifierType.SATISFIES_BATTLE_ORDER);
    }
}
