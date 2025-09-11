package com.gempukku.swccgo.cards.conditions;

import com.gempukku.swccgo.common.CharacterTestStatus;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.conditions.Condition;
import com.gempukku.swccgo.logic.modifiers.querying.ModifiersQuerying;

/**
 * A condition that is fulfilled when the specified Character Test is completed.
 */
public class CharacterTestCompletedCondition implements Condition {
    private int _permCardId;

    /**
     * Creates a condition that is fulfilled when the specified Jedi Test is completed.
     * @param card the card
     */
    public CharacterTestCompletedCondition(PhysicalCard card) {
        _permCardId = card.getPermanentCardId();
    }

    @Override
    public boolean isFulfilled(GameState gameState, ModifiersQuerying modifiersQuerying) {
        PhysicalCard card = gameState.findCardByPermanentId(_permCardId);

        return card.getCharacterTestStatus() == CharacterTestStatus.COMPLETED;
    }
}
