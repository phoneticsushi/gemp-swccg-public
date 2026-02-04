package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.common.Zone;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.timing.AbstractStandardEffect;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.Effect;
import com.gempukku.swccgo.logic.timing.results.StackedFromCardPileResult;

import java.util.Collections;

/**
 * An effect that stacks the top card from a player's card pile onto a specified card.
 */
public class StackTopCardFromPileEffect extends AbstractStandardEffect {
    private String _playerId;
    private PhysicalCard _stackOn;
    private Zone _cardPile;
    private boolean _faceDown;

    /**
     * Creates an effect that stacks the top card from a player's card pile onto a specified card.
     * @param action the action performing this effect
     * @param playerId the player whose card pile to take from
     * @param cardPile the card pile (Zone.RESERVE_DECK, Zone.FORCE_PILE, or Zone.USED_PILE)
     * @param stackOn the card to stack on
     * @param faceDown true if card should be stacked face down, false if face up
     */
    public StackTopCardFromPileEffect(Action action, String playerId, Zone cardPile, PhysicalCard stackOn, boolean faceDown) {
        super(action);
        _playerId = playerId;
        _cardPile = cardPile;
        _stackOn = stackOn;
        _faceDown = faceDown;
    }

    @Override
    public boolean isPlayableInFull(SwccgGame game) {
        return !game.getGameState().getCardPile(_playerId, _cardPile).isEmpty();
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
            GameState gameState = game.getGameState();
            PhysicalCard card = gameState.getTopOfCardPile(_playerId, _cardPile);
            if (card != null) {
                if (_faceDown)
                    gameState.sendMessage(_playerId + " stacks a card from " + _cardPile.getHumanReadable() + " on " + GameUtils.getCardLink(_stackOn));
                else
                    gameState.sendMessage(_playerId + " stacks " + GameUtils.getCardLink(card) + " from " + _cardPile.getHumanReadable() + " on " + GameUtils.getCardLink(_stackOn));
                
                gameState.removeCardsFromZone(Collections.singleton(card));
                gameState.stackCard(card, _stackOn, _faceDown, false, false);

                // Emit effect result
                game.getActionsEnvironment().emitEffectResult(
                        new StackedFromCardPileResult(_action, card, _stackOn));

                return new FullEffectResult(true);
            }
        }
        return new FullEffectResult(false);
    }
}
