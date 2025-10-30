package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.common.Zone;
import com.gempukku.swccgo.logic.timing.Action;


/**
 * An effect to place the bottom card of Lost Pile on top of Force Pile.
 */
public class LoseCardFromBottomOfReserveDeckEffect extends PlaceBottomCardFromCardPileOnTopOfCardPileEffect {

    /**
     * Creates an effect to place the bottom card of Lost Pile on top of Force Pile.
     * @param action the action performing this effect
     * @param cardPileOwner the card pile owner
     * @param hidden true or false
     */
    public LoseCardFromBottomOfReserveDeckEffect(Action action, String cardPileOwner, boolean hidden) {
        super(action, cardPileOwner, Zone.RESERVE_DECK, Zone.LOST_PILE, hidden);
    }
}
