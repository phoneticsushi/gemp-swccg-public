package com.gempukku.swccgo.logic.modifiers;

import com.gempukku.swccgo.common.Filterable;
import com.gempukku.swccgo.game.PhysicalCard;

/**
 * A modifier that makes Force drains at specified locations immune to Battle Order for a specified player.
 */
public class ForceDrainImmuneToBattleOrderModifier extends AbstractModifier {
    private String _playerDrainingForce;

    /**
     * Creates a modifier that makes Force drains at specified locations immune to Battle Order for the specified player.
     * @param source the source of the modifier
     * @param locationFilter the filter for locations where Force drains are immune
     * @param playerDrainingForce the player whose Force drains are immune to Battle Order
     */
    public ForceDrainImmuneToBattleOrderModifier(PhysicalCard source, Filterable locationFilter, String playerDrainingForce) {
        super(source, "Force drains immune to Battle Order", locationFilter, ModifierType.FORCE_DRAIN_IMMUNE_TO_BATTLE_ORDER);
        _playerDrainingForce = playerDrainingForce;
    }

    /**
     * Gets the player whose Force drains are immune to Battle Order.
     * @return the player
     */
    public String getPlayerDrainingForce() {
        return _playerDrainingForce;
    }
}
