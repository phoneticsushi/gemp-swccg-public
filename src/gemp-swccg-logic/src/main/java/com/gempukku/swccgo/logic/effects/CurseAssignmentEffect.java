package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.timing.AbstractSuccessfulEffect;
import com.gempukku.swccgo.logic.timing.Action;

/**
 * This effect sets the flag that indicates whether a given card is a "curse".
 * Text on Endor Will Bow To Me cancels the game text of cards that have curses attached to them.
 *
 * YAGNI: cancel the game text of the card that itself is the curse?
 * 
 */
public class CurseAssignmentEffect extends AbstractSuccessfulEffect {
    private PhysicalCard _card;
    private boolean _newCurseStatus;

    /**
     * Creates an effect that marks/unmarks a card as a "curse"
     * @param action the action performing this effect
     * @param card the card to update
     * @param newCurseStatus whether or not the card should be considered a "curse"
     */
    public CurseAssignmentEffect(Action action, PhysicalCard card, boolean newCurseStatus) {
        super(action);
        _card = card;
        _newCurseStatus = newCurseStatus;
    }

    @Override
    public void doPlayEffect(SwccgGame game) {
        _card.setIsCurse(_newCurseStatus);

        if (_newCurseStatus) {
            game.getGameState().sendMessage(GameUtils.getCardLink(_action.getActionSource()) + " makes a curse out of " + GameUtils.getCardLink(_card) + "!");
        } else {
            game.getGameState().sendMessage(GameUtils.getCardLink(_card) + " is no longer a curse");
        }
    }
}
