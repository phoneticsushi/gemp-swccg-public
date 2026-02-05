package com.gempukku.swccgo.logic.modifiers;

import com.gempukku.swccgo.common.Filterable;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.logic.conditions.Condition;

/**
 * A modifier that prevents affected cards from going 'missing'.
 * Used for card game text like "Cannot be missing."
 */
public class MayNotGoMissingModifier extends AbstractModifier {

    /**
     * Creates a modifier that prevents the source card from going 'missing'.
     * @param source the card that is the source of the modifier and that cannot go missing
     */
    public MayNotGoMissingModifier(PhysicalCard source) {
        super(source, "May not go missing", source, ModifierType.MAY_NOT_GO_MISSING);
    }

    /**
     * Creates a modifier that prevents cards accepted by the filter from going 'missing'.
     * @param source the source of the modifier
     * @param affectFilter the filter for cards that cannot go missing
     */
    public MayNotGoMissingModifier(PhysicalCard source, Filterable affectFilter) {
        super(source, "May not go missing", affectFilter, ModifierType.MAY_NOT_GO_MISSING);
    }

    /**
     * Creates a modifier that prevents cards accepted by the filter from going 'missing'.
     * @param source the source of the modifier
     * @param condition the condition that must be fulfilled for the modifier to be in effect
     * @param affectFilter the filter for cards that cannot go missing
     */
    public MayNotGoMissingModifier(PhysicalCard source, Condition condition, Filterable affectFilter) {
        super(source, "May not go missing", affectFilter, condition, ModifierType.MAY_NOT_GO_MISSING);
    }
}
