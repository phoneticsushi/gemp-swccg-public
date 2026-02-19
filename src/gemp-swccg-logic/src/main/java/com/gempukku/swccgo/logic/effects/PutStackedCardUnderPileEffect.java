package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.common.Zone;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.timing.AbstractStandardEffect;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.Effect;
import com.gempukku.swccgo.logic.timing.results.PutCardInCardPileFromOffTableResult;

import java.util.Collections;

/**
 * An effect that puts a stacked card on the bottom of a specified card pile.
 */
public class PutStackedCardUnderPileEffect extends AbstractStandardEffect {
    private String _playerId;
    private PhysicalCard _stackedCard;
    private Zone _cardPile;

    /**
     * Creates an effect that puts a stacked card on the bottom of a specified card pile.
     * @param action the action performing this effect
     * @param playerId the player whose card pile to put the card under
     * @param stackedCard the stacked card to put in the pile
     * @param cardPile the card pile (Zone.RESERVE_DECK, Zone.FORCE_PILE, or Zone.USED_PILE)
     */
    public PutStackedCardUnderPileEffect(Action action, String playerId, PhysicalCard stackedCard, Zone cardPile) {
        super(action);
        _playerId = playerId;
        _stackedCard = stackedCard;
        _cardPile = cardPile;
    }

    @Override
    public boolean isPlayableInFull(SwccgGame game) {
        return _stackedCard.getZone() == Zone.STACKED || _stackedCard.getZone() == Zone.STACKED_FACE_DOWN;
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
            
            gameState.sendMessage(_playerId + " places " + GameUtils.getCardLink(_stackedCard) + " on bottom of " + _cardPile.getHumanReadable());
            
            gameState.removeCardsFromZone(Collections.singleton(_stackedCard));
            _stackedCard.setOwner(_playerId);
            gameState.addCardToZone(_stackedCard, _cardPile, _playerId);

            // Emit effect result
            game.getActionsEnvironment().emitEffectResult(
                    new PutCardInCardPileFromOffTableResult(_action, _stackedCard, _playerId, _cardPile, false));

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
