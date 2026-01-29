package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.timing.AbstractStandardEffect;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.Effect;

import java.util.Collections;

/**
 * An effect that stacks the top card from a player's Used Pile onto a specified card.
 */
public class StackTopCardOfUsedPileEffect extends AbstractStandardEffect {
    private String _playerId;
    private PhysicalCard _stackOn;
    private boolean _faceDown;

    /**
     * Creates an effect that stacks the top card from a player's Used Pile onto a specified card.
     * @param action the action performing this effect
     * @param playerId the player whose Used Pile to take from
     * @param stackOn the card to stack on
     * @param faceDown true if card should be stacked face down, false if face up
     */
    public StackTopCardOfUsedPileEffect(Action action, String playerId, PhysicalCard stackOn, boolean faceDown) {
        super(action);
        _playerId = playerId;
        _stackOn = stackOn;
        _faceDown = faceDown;
    }

    @Override
    public boolean isPlayableInFull(SwccgGame game) {
        return !game.getGameState().getUsedPile(_playerId).isEmpty();
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
            PhysicalCard card = game.getGameState().getTopOfUsedPile(_playerId);
            if (card != null) {
                if (_faceDown)
                    game.getGameState().sendMessage(_playerId + " stacks a card from Used Pile on " + GameUtils.getCardLink(_stackOn));
                else
                    game.getGameState().sendMessage(_playerId + " stacks " + GameUtils.getCardLink(card) + " from Used Pile on " + GameUtils.getCardLink(_stackOn));
                game.getGameState().removeCardsFromZone(Collections.singleton(card));
                game.getGameState().stackCard(card, _stackOn, _faceDown, false, false);
                return new FullEffectResult(true);
            }
        }
        return new FullEffectResult(false);
    }
}
