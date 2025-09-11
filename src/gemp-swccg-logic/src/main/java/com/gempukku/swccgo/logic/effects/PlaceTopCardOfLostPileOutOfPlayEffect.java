package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.common.Zone;
import com.gempukku.swccgo.logic.timing.Action;


/**
 * An effect to place the top card of Lost Pile out of play.
 */
public class PlaceTopCardOfLostPileOutOfPlayEffect extends PlaceTopCardFromCardPileOnTopOfCardPileEffect {

    /**
     * Creates an effect to place the top card of Lost Pile on top of Reserve Deck.
     * @param action the action performing this effect
     */
    public PlaceTopCardOfLostPileOutOfPlayEffect(Action action, String cardPileOwner) {
        super(action, cardPileOwner, Zone.LOST_PILE, Zone.OUT_OF_PLAY);
    }
}
