package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractRebel;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.CancelForceDrainEffect;
import com.gempukku.swccgo.cards.effects.usage.OncePerTurnEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Species;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.CancelDestinyEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromTableEffect;
import com.gempukku.swccgo.logic.modifiers.ImmuneToAttritionGreaterThanModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotBeTargetedByModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.PassthruEffect;
import com.gempukku.swccgo.logic.timing.results.AboutToLeaveTableResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Set: Beezer Bowl 2025
 * Type: Character
 * Subtype: Rebel
 * Title: Sergeant Beezer (back side)
 */
public class Card701_031_BACK extends AbstractRebel {
    public Card701_031_BACK() {
        super(Side.LIGHT, 6, 0, 3, 4, 5, "Sergeant Beezer", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Alderaanian slicer, technician, and mountaineer. Promoted to sergeant by General Han Solo. Has an uncanny ability to problem solve on the spot.");
        setGameText("May not be placed in Reserve Deck. If Beezer about to leave table, place her out of play. If present with a scomp link, once per turn, may cancel a Force drain at a related site with a scomp link. During battle with a mountaineer, may cancel one just drawn destiny. Immune to opponent's Interrupts and attrition > 2.");
        addPersona(Persona.BEEZER);
        addIcons(Icon.WARRIOR, Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.SERGEANT, Keyword.MOUNTAINEER, Keyword.FEMALE);
        setSpecies(Species.ALDERAANIAN);
        setMayNotBePlacedInReserveDeck(true);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        Filter opponentsInterrupts = Filters.and(Filters.opponents(self), Filters.Interrupt);

        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Immune to attrition > 2 (immune to attrition 3, 4, 5... but VULNERABLE to attrition 0, 1, 2)
        // NOTE: Requires adding IMMUNITY_TO_ATTRITION_GREATER_THAN to ModifierType.java and updating ModifiersQuerying
        modifiers.add(new ImmuneToAttritionGreaterThanModifier(self, 2));
        modifiers.add(new MayNotBeTargetedByModifier(self, opponentsInterrupts));
        return modifiers;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggersAlwaysWhenInPlay(SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        // Check condition(s) - If Beezer about to leave table, place her out of play
        if (TriggerConditions.isAboutToLeaveTable(game, effectResult, self)
                && !TriggerConditions.isAboutToBePlacedOutOfPlayFromTable(game, effectResult, self)) {
            final AboutToLeaveTableResult result = (AboutToLeaveTableResult) effectResult;

            final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Place out of play");
            action.setActionMsg("Place " + GameUtils.getCardLink(self) + " out of play");
            // Perform result(s)
            action.appendEffect(
                    new PassthruEffect(action) {
                        @Override
                        protected void doPlayEffect(SwccgGame game) {
                            result.getPreventableCardEffect().preventEffectOnCard(self);
                            for (PhysicalCard attachedCards : game.getGameState().getAllAttachedRecursively(self)) {
                                result.getPreventableCardEffect().preventEffectOnCard(attachedCards);
                            }
                        }
                    });
            action.appendEffect(
                    new PlaceCardOutOfPlayFromTableEffect(action, self));
            return Collections.singletonList(action);
        }

        return null;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        List<OptionalGameTextTriggerAction> actions = new LinkedList<OptionalGameTextTriggerAction>();

        GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_1;

        // If present with a scomp link, once per turn, may cancel a Force drain at a related site with a scomp link
        if (TriggerConditions.forceDrainInitiated(game, effectResult)
                && GameConditions.isOncePerTurn(game, self, playerId, gameTextSourceCardId, gameTextActionId)
                && GameConditions.isPresentWith(game, self, Filters.has_Scomp_link)
                && GameConditions.canCancelForceDrain(game, self)) {

            // Check if Force drain is at a related site with a scomp link
            PhysicalCard forceDrainLocation = game.getGameState().getForceDrainState().getLocation();
            Filter relatedSiteWithScompLink = Filters.and(Filters.relatedSite(self), Filters.has_Scomp_link);

            if (forceDrainLocation != null && Filters.and(relatedSiteWithScompLink).accepts(game, forceDrainLocation)) {
                final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("Cancel Force drain");
                action.setActionMsg("Cancel Force drain at " + GameUtils.getCardLink(forceDrainLocation));
                // Update usage limit(s)
                action.appendUsage(
                        new OncePerTurnEffect(action));
                // Perform result(s)
                action.appendEffect(
                        new CancelForceDrainEffect(action));
                actions.add(action);
            }
        }

        GameTextActionId gameTextActionId2 = GameTextActionId.OTHER_CARD_ACTION_2;

        // During battle with another mountaineer, may cancel one just drawn destiny
        if (TriggerConditions.isBattleDestinyJustDrawn(game, effectResult)
                && GameConditions.isInBattleWith(game, self, Filters.and(Filters.other(self), Keyword.MOUNTAINEER))
                && GameConditions.canCancelDestiny(game, playerId)) {

            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId2);
            action.setText("Cancel destiny");
            action.setActionMsg("Cancel a just drawn battle destiny");
            // Perform result(s)
            action.appendEffect(
                    new CancelDestinyEffect(action));
            actions.add(action);
        }

        return actions;
    }
}
