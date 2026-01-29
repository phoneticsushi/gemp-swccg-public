package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.common.Zone;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.timing.AbstractStandardEffect;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.Effect;
import com.gempukku.swccgo.logic.timing.results.PutCardInCardPileFromOffTableResult;

import java.util.Collections;

/**
 * An effect to put a stacked card on the bottom of the Used Pile.
 */
public class PutStackedCardOnBottomOfUsedPileEffect extends AbstractStandardEffect {
    private String _playerId;
    private PhysicalCard _stackedCard;
    private boolean _hidden;

    /**
     * Creates an effect that puts a stacked card on the bottom of the Used Pile.
     * @param action the action performing this effect
     * @param playerId the player
     * @param stackedCard the stacked card
     * @param hidden true if card is not revealed when put in pile, otherwise false
     */
    public PutStackedCardOnBottomOfUsedPileEffect(Action action, String playerId, PhysicalCard stackedCard, boolean hidden) {
        super(action);
        _playerId = playerId;
        _stackedCard = stackedCard;
        _hidden = hidden;
    }

    @Override
    public boolean isPlayableInFull(SwccgGame game) {
        return _stackedCard.getZone() == Zone.STACKED;
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
            String cardInfo = _hidden ? "a card" : GameUtils.getCardLink(_stackedCard);
            
            game.getGameState().removeCardsFromZone(Collections.singleton(_stackedCard));
            game.getGameState().addCardToZone(_stackedCard, Zone.USED_PILE, _stackedCard.getOwner());
            game.getGameState().sendMessage(_playerId + " puts " + cardInfo + " on bottom of Used Pile");
            
            game.getActionsEnvironment().emitEffectResult(
                    new PutCardInCardPileFromOffTableResult(_action, _stackedCard, _playerId, Zone.USED_PILE, false));
            
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
