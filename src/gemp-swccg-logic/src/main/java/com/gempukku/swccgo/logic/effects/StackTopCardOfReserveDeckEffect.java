package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.timing.AbstractStandardEffect;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.Effect;

import java.util.Collections;

/**
 * An effect that stacks the top card from a player's Reserve Deck onto a specified card.
 */
public class StackTopCardOfReserveDeckEffect extends AbstractStandardEffect {
    private String _playerId;
    private PhysicalCard _stackOn;
    private boolean _faceDown;

    /**
     * Creates an effect that stacks the top card from a player's Reserve Deck onto a specified card.
     * @param action the action performing this effect
     * @param playerId the player whose Reserve Deck to take from
     * @param stackOn the card to stack on
     * @param faceDown true if card should be stacked face down, false if face up
     */
    public StackTopCardOfReserveDeckEffect(Action action, String playerId, PhysicalCard stackOn, boolean faceDown) {
        super(action);
        _playerId = playerId;
        _stackOn = stackOn;
        _faceDown = faceDown;
    }

    @Override
    public boolean isPlayableInFull(SwccgGame game) {
        return !game.getGameState().getReserveDeck(_playerId).isEmpty();
    }

    @Override
    public String getText(SwccgGame game) {
        return null;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(SwccgGame game) {
        if (isPlayableInFull(game)) {
            PhysicalCard card = game.getGameState().getTopOfReserveDeck(_playerId);
            if (card != null) {
                if (_faceDown)
                    game.getGameState().sendMessage(_playerId + " stacks a card from Reserve Deck on " + GameUtils.getCardLink(_stackOn));
                else
                    game.getGameState().sendMessage(_playerId + " stacks " + GameUtils.getCardLink(card) + " from Reserve Deck on " + GameUtils.getCardLink(_stackOn));
                game.getGameState().removeCardsFromZone(Collections.singleton(card));
                game.getGameState().stackCard(card, _stackOn, _faceDown, false, false);
                return new FullEffectResult(true);
            }
        }
        return new FullEffectResult(false);
    }
}
