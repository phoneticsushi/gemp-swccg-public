package com.gempukku.swccgo.cards.set701.light;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractObjective;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.actions.ObjectiveDeployedTriggerAction;
import com.gempukku.swccgo.cards.effects.usage.OncePerTurnEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.AddUntilEndOfGameModifierEffect;
import com.gempukku.swccgo.logic.effects.FlipCardEffect;
import com.gempukku.swccgo.logic.effects.LoseCardsFromTableEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromTableEffect;
import com.gempukku.swccgo.logic.effects.choose.DeployCardFromReserveDeckEffect;
import com.gempukku.swccgo.logic.effects.choose.DeployCardToLocationFromReserveDeckEffect;
import com.gempukku.swccgo.logic.effects.choose.TakeCardIntoHandFromReserveDeckEffect;
import com.gempukku.swccgo.logic.modifiers.DeployCostToLocationModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotDeployModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotUseCardToTransportToOrFromLocationModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.actions.InitiateAttackNonCreatureAction;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.choose.ChooseCardOnTableEffect;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.PassthruEffect;
import com.gempukku.swccgo.logic.timing.results.AttackTargetSelectedResult;
import com.gempukku.swccgo.logic.timing.results.MovedResult;

/**
 * Set: Beezer Bowl 2025
 * Type: Objective
 * Title: Leave It To Beezer
 */
public class Card701_044 extends AbstractObjective {
    public Card701_044() {
        super(Side.LIGHT, 0, Title.Leave_It_To_Beezer, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setFrontOfDoubleSidedCard(true);
        setGameText("Deploy Back Door (with [Beezer Bowl 2025] Beezer there), Generator Chamber, and [Beezer Bowl 2025] Scrambled Transmission. " +
                "For remainder of game, you may not deploy [Episode 1] or [Resistance] characters. Leia is lost. " +
                "Once during each of your turns may [upload] an Endor or Mt. Krana battleground site. " +
                "'Transports' to or from Mt. Krana sites are canceled. " +
                "While this side up, at Mt. Krana locations, cards with ability (and [Presence] droids) are deploy +3 (except for Ewoks and mountaineers). " +
                "When a creature attacks your cards, you may select the target. " +
                "Flip this card if Han moves to Generator Chamber with [Beezer Bowl 2025] Scrambled Transmission. " +
                "Place out of play if Beezer not on table.");
        addIcons(Icon.BEEZER_BOWL_2025);
    }

    @Override
    protected ObjectiveDeployedTriggerAction getGameTextWhenDeployedAction(final String playerId, final SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        ObjectiveDeployedTriggerAction action = new ObjectiveDeployedTriggerAction(self);

        // Deploy Back Door
        action.appendRequiredEffect(
                new DeployCardFromReserveDeckEffect(action, Filters.Back_Door, true, false) {
                    @Override
                    public String getChoiceText() {
                        return "Choose Back Door to deploy";
                    }
                });

        // Deploy [BB25] Beezer to Back Door
        action.appendRequiredEffect(
                new DeployCardToLocationFromReserveDeckEffect(action, Filters.and(Icon.BEEZER_BOWL_2025, Filters.persona(Persona.BEEZER)), Filters.Back_Door, true, false) {
                    @Override
                    public String getChoiceText() {
                        return "Choose Beezer to deploy to Back Door";
                    }
                });

        // Deploy Generator Chamber
        action.appendRequiredEffect(
                new DeployCardFromReserveDeckEffect(action, Filters.Generator_Chamber, true, false) {
                    @Override
                    public String getChoiceText() {
                        return "Choose Generator Chamber to deploy";
                    }
                });

        // Deploy [BB25] Scrambled Transmission
        action.appendRequiredEffect(
                new DeployCardFromReserveDeckEffect(action, Filters.and(Icon.BEEZER_BOWL_2025, Filters.Scrambled_Transmission), true, false) {
                    @Override
                    public String getChoiceText() {
                        return "Choose Scrambled Transmission to deploy";
                    }
                });

        return action;
    }

    @Override
    protected RequiredGameTextTriggerAction getGameTextAfterDeploymentCompletedAction(String playerId, SwccgGame game, final PhysicalCard self, final int gameTextSourceCardId) {
        RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);

        // For remainder of game, you may not deploy [Episode 1] or [Resistance] characters
        Filter ep1OrResistanceCharacters = Filters.and(Filters.character, Filters.or(Filters.icon(Icon.EPISODE_I), Filters.icon(Icon.RESISTANCE)));
        action.appendEffect(
                new AddUntilEndOfGameModifierEffect(action,
                        new MayNotDeployModifier(self, ep1OrResistanceCharacters, playerId), null));

        // For remainder of game, 'Transports' to or from Mt. Krana sites are canceled
        // This affects any card that could be used to transport (e.g., Nabrun Leids, Elis Helrot)
        action.appendEffect(
                new AddUntilEndOfGameModifierEffect(action,
                        new MayNotUseCardToTransportToOrFromLocationModifier(self, Filters.any, Filters.mount_krana_site), null));

        return action;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();

        // While this side up, at Mt. Krana locations, cards with ability (and [Presence] droids) are deploy +3
        // (except for Ewoks and mountaineers)
        Filter presenceDroids = Filters.and(Filters.droid, Filters.icon(Icon.PRESENCE));
        Filter exceptions = Filters.or(Filters.Ewok, Keyword.MOUNTAINEER);

        // "Cards with ability" means characters; [Presence] droids are called out specifically
        Filter affectedCards = Filters.and(
                Filters.or(Filters.character, presenceDroids),
                Filters.not(exceptions)
        );

        modifiers.add(new DeployCostToLocationModifier(self, affectedCards, 3, Filters.mount_krana_site));

        return modifiers;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<>();

        // Once during each of your turns may [upload] an Endor or Mt. Krana battleground site
        GameTextActionId gameTextActionId = GameTextActionId.LEAVE_IT_TO_BEEZER__UPLOAD_ENDOR_OR_MOUNT_KRANA_BATTLEGROUND_SITE;

        if (GameConditions.isOncePerTurn(game, self, playerId, gameTextSourceCardId, gameTextActionId)
                && GameConditions.canTakeCardsIntoHandFromReserveDeck(game, playerId, self, gameTextActionId)) {

            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Take battleground site into hand from Reserve Deck");
            action.setActionMsg("Take an Endor or Mt. Krana battleground site into hand from Reserve Deck");

            // Update usage limit(s)
            action.appendUsage(
                    new OncePerTurnEffect(action));

            // Perform result(s)
            Filter battlegroundSiteFilter = Filters.and(
                    Filters.battleground_site,
                    Filters.or(Filters.Endor_site, Filters.mount_krana_site)
            );
            action.appendEffect(
                    new TakeCardIntoHandFromReserveDeckEffect(action, playerId, battlegroundSiteFilter, true));

            actions.add(action);
        }

        return actions;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        List<RequiredGameTextTriggerAction> actions = new LinkedList<>();
        String playerId = self.getOwner();

        // Leia is lost (for remainder of game - triggered when table changes)
        if (TriggerConditions.isTableChanged(game, effectResult)) {
            Collection<PhysicalCard> leiaCards = Filters.filterActive(game, self, Filters.Leia);
            if (!leiaCards.isEmpty()) {
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setSingletonTrigger(true);
                action.setText("Make Leia lost");
                action.setActionMsg("Make " + GameUtils.getAppendedNames(leiaCards) + " lost");
                action.appendEffect(
                        new LoseCardsFromTableEffect(action, leiaCards));
                actions.add(action);
            }
        }

        // Flip this card if Han moves to Generator Chamber with [BB25] Scrambled Transmission attached to him
        if (TriggerConditions.movedToLocation(game, effectResult, Filters.Han, Filters.Generator_Chamber)
                && GameConditions.canBeFlipped(game, self)) {

            MovedResult movedResult = (MovedResult) effectResult;
            Collection<PhysicalCard> movedCards = movedResult.getMovedCards();

            for (PhysicalCard movedCard : movedCards) {
                if (Filters.Han.accepts(game, movedCard)) {
                    // Check if [BB25] Scrambled Transmission is attached to Han
                    if (Filters.hasAttached(Filters.and(Icon.BEEZER_BOWL_2025, Filters.Scrambled_Transmission)).accepts(game, movedCard)) {
                        RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                        action.setSingletonTrigger(true);
                        action.setText("Flip");
                        action.setActionMsg(null);
                        action.appendEffect(
                                new FlipCardEffect(action, self));
                        actions.add(action);
                        break;
                    }
                }
            }
        }

        // Place out of play if Beezer not on table
        if (TriggerConditions.isTableChanged(game, effectResult)
                && !GameConditions.canSpot(game, self, Filters.persona(Persona.BEEZER))) {

            RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.setSingletonTrigger(true);
            action.setText("Place out of play");
            action.setActionMsg("Place " + GameUtils.getCardLink(self) + " out of play");
            action.appendEffect(
                    new PlaceCardOutOfPlayFromTableEffect(action, self));
            actions.add(action);
        }

        return actions;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        // When a creature attacks your cards, you may select the target
        if (effectResult.getType() == EffectResult.Type.ATTACK_TARGET_SELECTED) {
            AttackTargetSelectedResult targetSelectedResult = (AttackTargetSelectedResult) effectResult;
            final InitiateAttackNonCreatureAction creatureAction = targetSelectedResult.getInitiateAttackNonCreatureAction();
            final PhysicalCard creature = targetSelectedResult.getCreature();
            PhysicalCard currentTarget = targetSelectedResult.getTarget();

            // Check: creature action exists, target is chosen, target hasn't already been changed,
            // and the current target belongs to you (creature is attacking YOUR cards)
            if (creatureAction != null
                    && creatureAction.isTargetChosen()
                    && !creatureAction.isTargetChanged()
                    && currentTarget != null
                    && currentTarget.getOwner().equals(playerId)) {

                // Find other valid targets the creature could attack (your cards present with creature)
                Filter validTargets = Filters.and(
                        Filters.your(playerId),
                        Filters.not(currentTarget),
                        Filters.nonCreatureCanBeAttackedByCreature(creature, false)
                );

                if (GameConditions.canTarget(game, self, validTargets)) {
                    final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
                    action.setText("Select creature attack target");
                    action.setActionMsg("Select the target of the creature attack");

                    // Choose a new target (include current target as an option)
                    action.appendEffect(
                            new ChooseCardOnTableEffect(action, playerId, "Choose target for creature attack",
                                    Filters.or(currentTarget, validTargets)) {
                                @Override
                                protected void cardSelected(final PhysicalCard targetedCard) {
                                    action.appendEffect(
                                            new PassthruEffect(action) {
                                                @Override
                                                protected void doPlayEffect(SwccgGame game) {
                                                    creatureAction.setTarget(targetedCard);
                                                }
                                            });
                                }
                            });

                    return Collections.singletonList(action);
                }
            }
        }

        return null;
    }
}
