package com.gempukku.swccgo.cards.set701.dark;

import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractNormalEffect;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.PlayCardZoneOption;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.AttachCardFromTableEffect;
import com.gempukku.swccgo.logic.effects.ModifyTotalTrainingDestinyUntilEndOfDrawEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromTableEffect;
import com.gempukku.swccgo.logic.effects.ReturnCardToHandFromTableEffect;
import com.gempukku.swccgo.logic.effects.UseForceEffect;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.PassthruEffect;
import com.gempukku.swccgo.logic.timing.results.AboutToLeaveTableResult;

/**
* Set: BEEZER_BOWL_2025
* Type: EFFECT
* Title: Realm Of Spirits
*/
public class Card701_018 extends AbstractNormalEffect {
    public Card701_018() {
        // Deploy on table
        super(Side.DARK, 4, PlayCardZoneOption.YOUR_SIDE_OF_TABLE, Title.Realm_Of_Spirits, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploy on table. May place your just lost sorcerer here. Holds one sorcerer at a time.  During your deploy phase, may use x force to release sorcerer from Realm of Spirits into your hand, where x = power of sorcerer. May place this Effect out of play to add 3 to your just-drawn training destiny. (Immune to Alter.)");
        addIcons(Icon.BEEZER_BOWL_2025);
        // Immune to Alter
        addImmuneToCardTitle(Title.Alter);
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, final SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        List<OptionalGameTextTriggerAction> actions = new LinkedList<OptionalGameTextTriggerAction>();

        final Filter yourSorcererFilter = Filters.and(Filters.your(playerId), Filters.sorcerer);

        // Check condition(s)
        if ((TriggerConditions.isAboutToBeLost(game, effectResult, yourSorcererFilter)
                || TriggerConditions.isAboutToBeForfeitedToLostPile(game, effectResult, yourSorcererFilter))
                // Holds one sorcerer at a time
                && !Filters.hasAttached(Filters.sorcerer).accepts(game, self) ) {
            final AboutToLeaveTableResult aboutToLeaveTableResult = (AboutToLeaveTableResult) effectResult;
            final PhysicalCard cardToBeLost = aboutToLeaveTableResult.getCardAboutToLeaveTable();

            // May place your just lost sorcerer here
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Place " + GameUtils.getFullName(cardToBeLost) + " on " + GameUtils.getCardLink(self));
            action.setActionMsg("Place " + GameUtils.getFullName(cardToBeLost) + " on " + GameUtils.getCardLink(self));
            // Perform result(s)
            action.appendEffect(
                    new PassthruEffect(action) {
                        @Override
                        protected void doPlayEffect(SwccgGame game) {
                            aboutToLeaveTableResult.getPreventableCardEffect().preventEffectOnCard(cardToBeLost);
                            action.appendEffect(
                                    new AttachCardFromTableEffect(action, cardToBeLost, self));
                        }
                    }
            );
            actions.add(action);
        }

        if ((TriggerConditions.isAboutToCompleteYourOwnTrainingDestinyDraw(game, effectResult))) {
            // May place this Effect out of play to add 3 to your just-drawn training destiny
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Place " + GameUtils.getFullName(self) + " out of play");
            action.setActionMsg("Place " + GameUtils.getCardLink(self) + " out of play to to add 3 to just-drawn training destiny");
            // Pay cost(s)
            action.appendCost(new PlaceCardOutOfPlayFromTableEffect(action, self));
            // Perform result(s)
            action.appendEffect(new ModifyTotalTrainingDestinyUntilEndOfDrawEffect(action, 3));
            actions.add(action);
        }

        return actions;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();

        // During your deploy phase...
        if (GameConditions.isDuringYourPhase(game, self, Phase.DEPLOY)) {
            // Assumption is there's always 0 or 1 attached cards, but handle the general case as it's more graceful...
            for (PhysicalCard attachedCard : self.getCardsAttached()) {
                if (Filters.sorcerer.accepts(game, attachedCard)) {
                    // Note: assumes that the game text is referring to the power printed on the card,
                    // not the power after any modifiers, since the card would be returned to hand
                    // and the modifiers would be lost...
                    final float sorcererPower = attachedCard.getBlueprint().getPower();

                    // ...may use x force to release sorcerer from Realm of Spirits into your hand, where x = power of sorcerer
                    if (GameConditions.canUseForce(game, playerId, sorcererPower)) {
                        final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId);
                        action.setText("Release '" + GameUtils.getFullName(attachedCard) + "'");
                        action.setActionMsg("Release " + GameUtils.getCardLink(attachedCard) + " from " + GameUtils.getCardLink(self) + " into your hand");
                        // Pay cost(s)
                        action.appendCost(
                                new UseForceEffect(action, playerId, sorcererPower));
                        // Perform result(s)
                        action.appendEffect(
                                new ReturnCardToHandFromTableEffect(action, attachedCard));
                        actions.add(action);
                    }
                }
            }
        }

        return actions;
    }
}
