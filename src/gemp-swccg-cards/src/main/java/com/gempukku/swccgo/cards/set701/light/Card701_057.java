package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractNormalEffect;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
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
import com.gempukku.swccgo.logic.effects.AddCardsToMoveUsingLandspeedSimultaneouslyEffect;
import com.gempukku.swccgo.logic.effects.MoveUsingLocationTextEffect;
import com.gempukku.swccgo.logic.effects.TargetCardsOnTableEffect;
import com.gempukku.swccgo.logic.effects.UnrespondableEffect;
import com.gempukku.swccgo.logic.modifiers.FireWeaponFiredAtCostModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.TotalPowerModifier;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.results.MovingResult;
import com.gempukku.swccgo.logic.timing.results.MovingUsingLandspeedResult;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Set 701 (Beezer Bowl 2025)
 * Type: Effect
 * Title: Yuf Tu Churi!
 * Gemp ID: 701_057
 */
public class Card701_057 extends AbstractNormalEffect {
    public Card701_057() {
        super(Side.LIGHT, 5, PlayCardZoneOption.YOUR_SIDE_OF_TABLE, "Yuf Tu Churi!", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Climb the mountain! The Ewoks of Bright Tree Village often sought adventure beyond their quiet home tucked away in the Endor forest.");
        setGameText("Deploy on table. Opponent must use +1 Force to fire a weapon targeting your Rebel present with your Ewok. Your Ewok/Rebel pairs may move for 1 Force together. At sites where you have an Ewok and a mountaineer, your total power is +2. Immune to Alter.");
        addIcons(Icon.BEEZER_BOWL_2025);
        addImmuneToCardTitle(Title.Alter);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        String playerId = self.getOwner();

        // Your Rebel present with your Ewok
        Filter yourRebelPresentWithYourEwok = Filters.and(
                Filters.your(playerId),
                Filters.Rebel,
                Filters.presentWith(self, Filters.and(Filters.your(playerId), Filters.Ewok)));

        // Sites where you have an Ewok and a mountaineer
        Filter sitesWithEwokAndMountaineer = Filters.and(
                Filters.site,
                Filters.sameLocationAs(self, Filters.and(Filters.your(playerId), Filters.Ewok)),
                Filters.sameLocationAs(self, Filters.and(Filters.your(playerId), Keyword.MOUNTAINEER)));

        List<Modifier> modifiers = new LinkedList<Modifier>();

        // Opponent must use +1 Force to fire a weapon targeting your Rebel present with your Ewok
        // Using Filters.any for affectFilter and weaponFilter so this works for both regular weapons
        // and permanent weapons (SwccgBuiltInCardBlueprint). The targetFilter provides the real constraint.
        modifiers.add(new FireWeaponFiredAtCostModifier(self,
                Filters.any,
                1,
                Filters.any,
                yourRebelPresentWithYourEwok));

        // At sites where you have an Ewok and a mountaineer, your total power is +2
        modifiers.add(new TotalPowerModifier(self, sitesWithEwokAndMountaineer, 2, playerId));

        return modifiers;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        List<OptionalGameTextTriggerAction> actions = new LinkedList<OptionalGameTextTriggerAction>();

        Filter yourEwoks = Filters.and(Filters.your(playerId), Filters.Ewok);
        Filter yourRebels = Filters.and(Filters.your(playerId), Filters.Rebel);

        // ===== LANDSPEED MOVEMENT =====
        if (TriggerConditions.movingUsingLandspeed(game, effectResult, Filters.and(Filters.your(playerId), Filters.or(Filters.Ewok, Filters.Rebel)))) {
            final MovingUsingLandspeedResult movingResult = (MovingUsingLandspeedResult) effectResult;
            final PhysicalCard toLocation = movingResult.getMovingTo();
            Collection<PhysicalCard> cardsMoving = movingResult.getAllCardsMoving();

            boolean ewokMoving = Filters.canSpot(cardsMoving, game, yourEwoks);
            boolean rebelMoving = Filters.canSpot(cardsMoving, game, yourRebels);

            // If Ewok moving without Rebel, allow Rebel to join
            if (ewokMoving && !rebelMoving) {
                PhysicalCard movingEwok = Filters.findFirstActive(game, self, Filters.and(yourEwoks, Filters.in(cardsMoving)));
                if (movingEwok != null) {
                    Collection<PhysicalCard> rebelsToJoin = Filters.filterActive(game, self,
                            Filters.and(yourRebels,
                                    Filters.not(Filters.in(cardsMoving)),
                                    Filters.present(movingEwok),
                                    Filters.movableAsRegularMoveUsingLandspeed(playerId, movingResult.isReact(), movingResult.isMoveAway(), true, 0, null, Filters.sameCardId(toLocation))));

                    if (movingResult.isReact()) {
                        rebelsToJoin = Filters.filter(rebelsToJoin, game, Filters.isCardEligibleToJoinMoveAsReact);
                    } else if (!GameConditions.isPhaseForPlayer(game, Phase.MOVE, playerId)) {
                        rebelsToJoin.clear();
                    }

                    if (!rebelsToJoin.isEmpty()) {
                        final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
                        action.setText("Move Rebel with Ewok");
                        action.appendTargeting(
                                new TargetCardsOnTableEffect(action, playerId, "Choose Rebel to move with Ewok", 1, 1, Filters.in(rebelsToJoin)) {
                                    @Override
                                    protected void cardsTargeted(int targetGroupId, final Collection<PhysicalCard> targetedRebels) {
                                        action.addAnimationGroup(targetedRebels);
                                        action.allowResponses("Move " + GameUtils.getAppendedNames(targetedRebels) + " with Ewok to " + GameUtils.getCardLink(toLocation),
                                                new UnrespondableEffect(action) {
                                                    @Override
                                                    protected void performActionResults(Action targetingAction) {
                                                        action.appendEffect(
                                                                new AddCardsToMoveUsingLandspeedSimultaneouslyEffect(action, targetedRebels, movingResult));
                                                    }
                                                }
                                        );
                                    }
                                }
                        );
                        actions.add(action);
                    }
                }
            }

            // If Rebel moving without Ewok, allow Ewok to join
            if (rebelMoving && !ewokMoving) {
                PhysicalCard movingRebel = Filters.findFirstActive(game, self, Filters.and(yourRebels, Filters.in(cardsMoving)));
                if (movingRebel != null) {
                    Collection<PhysicalCard> ewoksToJoin = Filters.filterActive(game, self,
                            Filters.and(yourEwoks,
                                    Filters.not(Filters.in(cardsMoving)),
                                    Filters.present(movingRebel),
                                    Filters.movableAsRegularMoveUsingLandspeed(playerId, movingResult.isReact(), movingResult.isMoveAway(), true, 0, null, Filters.sameCardId(toLocation))));

                    if (movingResult.isReact()) {
                        ewoksToJoin = Filters.filter(ewoksToJoin, game, Filters.isCardEligibleToJoinMoveAsReact);
                    } else if (!GameConditions.isPhaseForPlayer(game, Phase.MOVE, playerId)) {
                        ewoksToJoin.clear();
                    }

                    if (!ewoksToJoin.isEmpty()) {
                        final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
                        action.setText("Move Ewok with Rebel");
                        action.appendTargeting(
                                new TargetCardsOnTableEffect(action, playerId, "Choose Ewok to move with Rebel", 1, 1, Filters.in(ewoksToJoin)) {
                                    @Override
                                    protected void cardsTargeted(int targetGroupId, final Collection<PhysicalCard> targetedEwoks) {
                                        action.addAnimationGroup(targetedEwoks);
                                        action.allowResponses("Move " + GameUtils.getAppendedNames(targetedEwoks) + " with Rebel to " + GameUtils.getCardLink(toLocation),
                                                new UnrespondableEffect(action) {
                                                    @Override
                                                    protected void performActionResults(Action targetingAction) {
                                                        action.appendEffect(
                                                                new AddCardsToMoveUsingLandspeedSimultaneouslyEffect(action, targetedEwoks, movingResult));
                                                    }
                                                }
                                        );
                                    }
                                }
                        );
                        actions.add(action);
                    }
                }
            }
        }

        // ===== LOCATION TEXT MOVEMENT (e.g. Mt. Krana Pass, Generator Chamber) =====
        // MoveUsingLocationTextEffect moves one card at a time, so we perform a separate free move for the companion.
        // Recursion guard: MoveUsingLocationTextEffect calls regularMovePerformed() BEFORE emitting
        // MovingUsingLocationTextResult, so hasNotPerformedRegularMove prevents the companion from re-triggering.
        if (effectResult.getType() == EffectResult.Type.MOVING_USING_LOCATION_TEXT) {
            final MovingResult movingResult = (MovingResult) effectResult;
            final PhysicalCard cardMoving = movingResult.getCardMoving();
            final PhysicalCard toLocation = movingResult.getMovingTo();

            if (cardMoving != null && toLocation != null
                    && Filters.and(Filters.your(playerId), Filters.or(Filters.Ewok, Filters.Rebel)).accepts(game, cardMoving)) {

                boolean ewokMoving = yourEwoks.accepts(game, cardMoving);
                boolean rebelMoving = yourRebels.accepts(game, cardMoving);

                // If Ewok moving, allow a Rebel present to move free to same destination
                if (ewokMoving) {
                    Collection<PhysicalCard> rebelsToJoin = Filters.filterActive(game, self,
                            Filters.and(yourRebels,
                                    Filters.hasNotPerformedRegularMove,
                                    Filters.present(cardMoving)));

                    if (!rebelsToJoin.isEmpty()) {
                        final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
                        action.setText("Move Rebel with Ewok");
                        action.appendTargeting(
                                new TargetCardsOnTableEffect(action, playerId, "Choose Rebel to move with Ewok", 1, 1, Filters.in(rebelsToJoin)) {
                                    @Override
                                    protected void cardsTargeted(int targetGroupId, final Collection<PhysicalCard> targetedRebels) {
                                        final PhysicalCard rebelToMove = targetedRebels.iterator().next();
                                        action.addAnimationGroup(targetedRebels);
                                        action.allowResponses("Move " + GameUtils.getCardLink(rebelToMove) + " with Ewok to " + GameUtils.getCardLink(toLocation),
                                                new UnrespondableEffect(action) {
                                                    @Override
                                                    protected void performActionResults(Action targetingAction) {
                                                        action.appendEffect(
                                                                new MoveUsingLocationTextEffect(action, rebelToMove, toLocation, false, false));
                                                    }
                                                }
                                        );
                                    }
                                }
                        );
                        actions.add(action);
                    }
                }

                // If Rebel moving, allow an Ewok present to move free to same destination
                if (rebelMoving) {
                    Collection<PhysicalCard> ewoksToJoin = Filters.filterActive(game, self,
                            Filters.and(yourEwoks,
                                    Filters.hasNotPerformedRegularMove,
                                    Filters.present(cardMoving)));

                    if (!ewoksToJoin.isEmpty()) {
                        final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
                        action.setText("Move Ewok with Rebel");
                        action.appendTargeting(
                                new TargetCardsOnTableEffect(action, playerId, "Choose Ewok to move with Rebel", 1, 1, Filters.in(ewoksToJoin)) {
                                    @Override
                                    protected void cardsTargeted(int targetGroupId, final Collection<PhysicalCard> targetedEwoks) {
                                        final PhysicalCard ewokToMove = targetedEwoks.iterator().next();
                                        action.addAnimationGroup(targetedEwoks);
                                        action.allowResponses("Move " + GameUtils.getCardLink(ewokToMove) + " with Rebel to " + GameUtils.getCardLink(toLocation),
                                                new UnrespondableEffect(action) {
                                                    @Override
                                                    protected void performActionResults(Action targetingAction) {
                                                        action.appendEffect(
                                                                new MoveUsingLocationTextEffect(action, ewokToMove, toLocation, false, false));
                                                    }
                                                }
                                        );
                                    }
                                }
                        );
                        actions.add(action);
                    }
                }
            }
        }

        return actions;
    }
}
