package com.gempukku.swccgo.logic.modifiers;

import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.modifiers.querying.ModifiersQuerying;

/**
 * A modifier that prohibits the affected cards from attacking characters owned by the specified player.
 */
public class MayNotAttackChactersOwnedByPlayerModifier extends AbstractModifier {
    private String _playerId;

    /**
     * Creates a modifier that prohibits source card from attacking chacters owned by the specified player.
     * @param source the source of the modifier
     * @param targetFilter the target filter
     */
    public MayNotAttackChactersOwnedByPlayerModifier(PhysicalCard source, String playerId) {
        // It doesn't matter if this effect is cumulative because stacking it is idempotent
        super(source, "May not attack characters owned by " + playerId, source, ModifierType.MAY_NOT_ATTACK_CHARACTERS_OWNED_BY_PLAYER);
        _playerId = playerId;
    }

    @Override
    public boolean isAffectedTarget(GameState gameState, ModifiersQuerying modifiersQuerying, PhysicalCard targetCard) {
        return targetCard.getOwner() == _playerId && Filters.character.accepts(gameState, modifiersQuerying, targetCard);
    }
}
