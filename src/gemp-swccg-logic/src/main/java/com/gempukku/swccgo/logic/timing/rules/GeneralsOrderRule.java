package com.gempukku.swccgo.logic.timing.rules;

import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.AbstractActionProxy;
import com.gempukku.swccgo.game.ActionsEnvironment;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredRuleTriggerAction;
import com.gempukku.swccgo.logic.actions.TriggerAction;
import com.gempukku.swccgo.logic.effects.PlaceCardInUsedPileFromTableEffect;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.results.PlayCardResult;

import java.util.Collections;
import java.util.List;

/**
 * Enforces the game rule that when a General's Order is deployed, a General's Order already on the table (if any)
 * is placed in its owner's Used Pile.
 * Beezer Bowl 2025: New rule for General's Order card type.
 */
public class GeneralsOrderRule implements Rule {
    private ActionsEnvironment _actionsEnvironment;
    private Rule _that;

    /**
     * Creates a rule that when a General's Order is deployed, a General's Order already on the table (if any) is
     * placed in its owner's Used Pile.
     * @param actionsEnvironment the actions environment
     */
    public GeneralsOrderRule(ActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
        _that = this;
    }

    public void applyRule() {
        _actionsEnvironment.addUntilEndOfGameActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<TriggerAction> getRequiredAfterTriggers(SwccgGame game, EffectResult effectResult) {
                        // Check condition(s)
                        if (TriggerConditions.justDeployed(game, effectResult, Filters.Generals_Order)) {
                            PhysicalCard playedCard = ((PlayCardResult) effectResult).getPlayedCard();
                            PhysicalCard otherGeneralsOrder = Filters.findFirstFromAllOnTable(game, Filters.and(Filters.Generals_Order, Filters.not(playedCard)));
                            if (otherGeneralsOrder != null) {

                                RequiredRuleTriggerAction action = new RequiredRuleTriggerAction(_that, otherGeneralsOrder);
                                action.setText("Place in Used Pile");
                                // Perform result(s)
                                action.appendEffect(
                                        new PlaceCardInUsedPileFromTableEffect(action, otherGeneralsOrder));
                                return Collections.singletonList((TriggerAction) action);
                            }
                        }
                        return null;
                    }
                });
    }
}
