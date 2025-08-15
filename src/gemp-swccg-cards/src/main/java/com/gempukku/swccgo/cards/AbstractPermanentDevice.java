package com.gempukku.swccgo.cards;

import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;

import java.util.List;

/**
 * Defines the base implementation of a permanent device.
 */
public abstract class AbstractPermanentDevice extends AbstractPermanent {

    /**
     * Creates the base implementation of a permanent device.
     * @param persona the persona
     */
    protected AbstractPermanentDevice(Persona persona) {
        super(persona.getHumanReadable(), persona.getCrossedOverPersona().getHumanReadable(), Uniqueness.UNIQUE);
        addPersona(persona);
    }

    /**
     * Determines if the built-in is a permanent device.
     * @return true or false
     */
    @Override
    public final boolean isDevice() {
        return true;
    }

    /**
     * Gets the top-level game text actions for the permanent device listed on self, if one exists.
     * @param playerId the player
     * @param game the game
     * @param self the card
     * @return the actions, or null
     */
    @Override
    public List<TopLevelGameTextAction> getPermanentDeviceTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self) {
        return null;
    }
}
