package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractRebel;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerPhaseEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Species;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.FlipCardEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromTableEffect;
import com.gempukku.swccgo.logic.effects.UseForceEffect;
import com.gempukku.swccgo.logic.effects.choose.DeployCardFromReserveDeckEffect;
import com.gempukku.swccgo.logic.modifiers.ImmuneToAttritionLessThanModifier;
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
 * Title: Corporal Beezer
 */
public class Card701_031 extends AbstractRebel {
    public Card701_031() {
        super(Side.LIGHT, 3, 0, 2, 3, 4, "Corporal Beezer", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setFrontOfDoubleSidedCard(true);
        setLore("Alderaanian slicer, technician, and mountaineer. Part of the Renegade Squadron division of the Rebel Strike Team. Capable of intercepting any transmission with the right tools.");
        setGameText("May not be placed in Reserve Deck. If Beezer about to leave table, place her out of play. Once during each of your deploy phases, may use 1 Force to [upload] one device; reshuffle. Immune to opponent's Interrupts and attrition < 3. Flip this card if [Beezer Bowl 2025] Scrambled Transmission relocated to Han.");
        addPersona(Persona.BEEZER);
        addIcons(Icon.WARRIOR, Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.CORPORAL, Keyword.MOUNTAINEER, Keyword.FEMALE);
        setSpecies(Species.ALDERAANIAN);
        setMayNotBePlacedInReserveDeck(true);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        Filter opponentsInterrupts = Filters.and(Filters.opponents(self), Filters.Interrupt);

        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new ImmuneToAttritionLessThanModifier(self, 3));
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
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        Filter scrambledTransmissionFilter = Filters.and(Filters.title(Title.Scrambled_Transmission), Icon.BEEZER_BOWL_2025);

        // Check condition(s) - Flip if Scrambled Transmission with BEEZER_BOWL_2025 icon relocated to Han
        if (effectResult.getType() == EffectResult.Type.RETARGETED_EFFECT
                || effectResult.getType() == EffectResult.Type.ATTACH_FROM_TABLE) {
            // Check if Scrambled Transmission is now attached to Han
            if (GameConditions.canSpot(game, self, Filters.and(scrambledTransmissionFilter, Filters.attachedTo(Filters.Han)))) {
                if (GameConditions.canBeFlipped(game, self)) {
                    final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                    action.setText("Flip");
                    action.setActionMsg("Flip " + GameUtils.getCardLink(self));
                    // Perform result(s)
                    action.appendEffect(
                            new FlipCardEffect(action, self));
                    return Collections.singletonList(action);
                }
            }
        }

        return null;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        // NOTE: Requires adding CORPORAL_BEEZER__UPLOAD_DEVICE to GameTextActionId.java
        GameTextActionId gameTextActionId = GameTextActionId.CORPORAL_BEEZER__UPLOAD_DEVICE;

        // Check condition(s) - Once during each of your deploy phases, may use 1 Force to upload one device
        if (GameConditions.isOnceDuringYourPhase(game, self, playerId, gameTextSourceCardId, gameTextActionId, Phase.DEPLOY)
                && GameConditions.canUseForce(game, playerId, 1)
                && GameConditions.canDeployCardFromReserveDeck(game, playerId, self, gameTextActionId)) {

            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Deploy device from Reserve Deck");
            action.setActionMsg("Deploy a device from Reserve Deck");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerPhaseEffect(action));
            // Pay cost(s)
            action.appendCost(
                    new UseForceEffect(action, playerId, 1));
            // Perform result(s)
            action.appendEffect(
                    new DeployCardFromReserveDeckEffect(action, Filters.device, true));
            return Collections.singletonList(action);
        }
        return null;
    }
}
