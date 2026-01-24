package com.gempukku.swccgo.cards.set701.dark;

import com.gempukku.swccgo.cards.AbstractUsedOrLostInterrupt;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.CardSubtype;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.actions.PlayInterruptAction;
import com.gempukku.swccgo.logic.effects.AddUntilEndOfTurnModifierEffect;
import com.gempukku.swccgo.logic.effects.RespondablePlayCardEffect;
import com.gempukku.swccgo.logic.effects.TargetCardOnTableEffect;
import com.gempukku.swccgo.logic.modifiers.ForfeitModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotHaveForfeitValueIncreasedModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotHavePowerIncreasedByCardModifier;
import com.gempukku.swccgo.logic.modifiers.PowerModifier;
import com.gempukku.swccgo.logic.timing.Action;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Set 701
 * Type: Interrupt
 * Subtype: Used Or Lost
 * Title: Thunderstruck
 * Gemp ID: 701_024
 */
public class Card701_024 extends AbstractUsedOrLostInterrupt {
    public Card701_024() {
        super(Side.DARK, 4, "Thunderstruck", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Long before the Ewoks were in possession of the Sunstar, Morag the tulgah witch kept it in her chambers atop Mt. Thunderstone.");
        setGameText("Target any character present with your sorcerer. For remainder of turn: USED: Target is power and forfeit -1. LOST: Target's power and forfeit values may not be increased.");
        addIcons(Icon.BEEZER_BOWL_2025);
    }

    @Override
    protected List<PlayInterruptAction> getGameTextTopLevelActions(final String playerId, SwccgGame game, final PhysicalCard self) {
        List<PlayInterruptAction> actions = new LinkedList<PlayInterruptAction>();

        // Filter for valid targets: any character present with your sorcerer
        Filter targetFilter = Filters.and(Filters.character, Filters.presentWith(self, Filters.and(Filters.your(self), Filters.sorcerer)));

        if (GameConditions.canTarget(game, self, targetFilter)) {
            // Check condition(s) for USED
            final PlayInterruptAction usedAction = new PlayInterruptAction(game, self, CardSubtype.USED);
            usedAction.setText("Make character power and forfeit -1");
            // Choose target(s)
            usedAction.appendTargeting(
                    new TargetCardOnTableEffect(usedAction, playerId, "Choose character present with your sorcerer", targetFilter) {
                        @Override
                        protected void cardTargeted(final int targetGroupId, final PhysicalCard targetedCard) {
                            usedAction.addAnimationGroup(targetedCard);
                            // Allow response(s)
                            usedAction.allowResponses("Make " + GameUtils.getCardLink(targetedCard) + " power and forfeit -1",
                                    new RespondablePlayCardEffect(usedAction) {
                                        @Override
                                        protected void performActionResults(Action targetingAction) {
                                            // Get the targeted card(s) from the action using the targetGroupId.
                                            final PhysicalCard finalTarget = usedAction.getPrimaryTargetCard(targetGroupId);

                                            // Perform result(s)
                                            usedAction.appendEffect(
                                                    new AddUntilEndOfTurnModifierEffect(usedAction,
                                                            new PowerModifier(self, finalTarget, -1),
                                                            "Makes " + GameUtils.getCardLink(finalTarget) + " power -1"));
                                            usedAction.appendEffect(
                                                    new AddUntilEndOfTurnModifierEffect(usedAction,
                                                            new ForfeitModifier(self, finalTarget, -1),
                                                            "Makes " + GameUtils.getCardLink(finalTarget) + " forfeit -1"));
                                        }
                                    }
                            );
                        }
                    }
            );
            actions.add(usedAction);

            // Check condition(s) for LOST
            final PlayInterruptAction lostAction = new PlayInterruptAction(game, self, CardSubtype.LOST);
            lostAction.setText("Prevent power and forfeit from being increased");
            // Choose target(s)
            lostAction.appendTargeting(
                    new TargetCardOnTableEffect(lostAction, playerId, "Choose character present with your sorcerer", targetFilter) {
                        @Override
                        protected void cardTargeted(final int targetGroupId, final PhysicalCard targetedCard) {
                            lostAction.addAnimationGroup(targetedCard);
                            // Allow response(s)
                            lostAction.allowResponses("Prevent " + GameUtils.getCardLink(targetedCard) + "'s power and forfeit from being increased",
                                    new RespondablePlayCardEffect(lostAction) {
                                        @Override
                                        protected void performActionResults(Action targetingAction) {
                                            // Get the targeted card(s) from the action using the targetGroupId.
                                            final PhysicalCard finalTarget = lostAction.getPrimaryTargetCard(targetGroupId);

                                            // Perform result(s)
                                            // Use Filters.any to prevent power increases from ALL sources
                                            lostAction.appendEffect(
                                                    new AddUntilEndOfTurnModifierEffect(lostAction,
                                                            new MayNotHavePowerIncreasedByCardModifier(self, finalTarget, Filters.any),
                                                            "Prevents " + GameUtils.getCardLink(finalTarget) + "'s power from being increased"));
                                            lostAction.appendEffect(
                                                    new AddUntilEndOfTurnModifierEffect(lostAction,
                                                            new MayNotHaveForfeitValueIncreasedModifier(self, finalTarget),
                                                            "Prevents " + GameUtils.getCardLink(finalTarget) + "'s forfeit from being increased"));
                                        }
                                    }
                            );
                        }
                    }
            );
            actions.add(lostAction);
        }

        return actions;
    }
}
