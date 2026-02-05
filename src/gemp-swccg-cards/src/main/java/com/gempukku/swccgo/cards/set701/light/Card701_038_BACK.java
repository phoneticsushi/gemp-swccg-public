package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractCreature;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerGameEffect;
import com.gempukku.swccgo.cards.effects.usage.OncePerTurnEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.ModelType;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Phase;
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
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromOffTableEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromTableEffect;
import com.gempukku.swccgo.logic.effects.RelocateBetweenLocationsEffect;
import com.gempukku.swccgo.logic.effects.RetrieveForceEffect;
import com.gempukku.swccgo.logic.effects.choose.ChooseCardOnTableEffect;
import com.gempukku.swccgo.logic.modifiers.DefinedByGameTextFerocityModifier;
import com.gempukku.swccgo.logic.modifiers.ForceRetrievalImmuneToSecretPlansModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotMoveModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.results.DefeatedResult;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Beezer Bowl 2025
 * Type: Creature
 * Subtype: Gigantic Predator
 * Title: Gorax, The Great Devourer
 */
public class Card701_038_BACK extends AbstractCreature {
    public Card701_038_BACK() {
        // Side, destiny, deployCost, ferocity (null = defined by game text), defenseValue, forfeit, title, uniqueness, expansionSet, rarity
        super(Side.LIGHT, 7, 0, null, 11, 0, Title.The_Great_Devourer, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Once awakened, the Gorax becomes an unstoppable force of destruction.");
        setGameText("Ferocity = 10 + destiny. Habitat: Exterior Endor sites. May not be placed in Reserve Deck. If lost, place out of play. " +
                "Dark Side player controls movement. " +
                "Once per game, during any deploy phase, may relocate The Great Devourer to an adjacent site. " +
                "If defeated, place out of play and attacking player retrieves X Force (where X = twice the number of cards beneath Pile Of Bones). " +
                "Force retrieval from defeating The Great Devourer is immune to Secret Plans.");
        addIcons(Icon.BEEZER_BOWL_2025, Icon.CREATURE);
        addModelType(ModelType.GIGANTIC_PREDATOR);
        addPersona(Persona.GORAX);
        setMayNotBePlacedInReserveDeck(true);
    }

    @Override
    protected Filter getGameTextHabitatFilter(String playerId, SwccgGame game, PhysicalCard self) {
        // Habitat: Exterior Endor sites
        return Filters.and(Filters.exterior_site, Filters.Endor_site);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();

        // Ferocity = 10 + destiny
        modifiers.add(new DefinedByGameTextFerocityModifier(self, 10, 1));

        // Force retrieval from defeating The Great Devourer is immune to Secret Plans
        modifiers.add(new ForceRetrievalImmuneToSecretPlansModifier(self, self));

        // Dark Side player controls movement - block all normal movement
        // (DS moves via explicit relocate actions in getOpponentsCardGameTextTopLevelActions)
        modifiers.add(new MayNotMoveModifier(self));

        return modifiers;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        List<RequiredGameTextTriggerAction> actions = new LinkedList<>();
        String playerId = self.getOwner();

        // If lost, place out of play
        if (TriggerConditions.justLost(game, effectResult, self)) {
            RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Place out of play");
            action.setActionMsg("Place " + GameUtils.getCardLink(self) + " out of play");
            action.appendEffect(
                    new PlaceCardOutOfPlayFromOffTableEffect(action, self));
            actions.add(action);
        }

        // If defeated, place out of play and attacking player retrieves X Force
        // (where X = twice the number of cards beneath Pile Of Bones)
        if (TriggerConditions.justDefeatedBy(game, effectResult, self, Filters.any)) {
            DefeatedResult defeatedResult = (DefeatedResult) effectResult;
            Collection<PhysicalCard> defeatedByCards = defeatedResult.getDefeatedByCards();

            String attackingPlayer = null;
            if (defeatedByCards != null && !defeatedByCards.isEmpty()) {
                PhysicalCard firstDefeater = defeatedByCards.iterator().next();
                attackingPlayer = firstDefeater.getOwner();
            }

            RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Place out of play and retrieve Force");

            // Calculate X = twice the number of cards beneath Pile of Bones
            int cardsUnderPileOfBones = 0;
            PhysicalCard pileOfBones = Filters.findFirstActive(game, self, Filters.title(Title.Pile_Of_Bones));
            if (pileOfBones != null) {
                cardsUnderPileOfBones = game.getGameState().getStackedCards(pileOfBones).size();
            }
            final int forceToRetrieve = cardsUnderPileOfBones * 2;

            action.setActionMsg("Place " + GameUtils.getCardLink(self) + " out of play" +
                    (attackingPlayer != null && forceToRetrieve > 0 ? " and make " + attackingPlayer + " retrieve " + forceToRetrieve + " Force" : ""));

            // Place out of play
            action.appendEffect(
                    new PlaceCardOutOfPlayFromTableEffect(action, self));

            // Attacking player retrieves X Force
            if (attackingPlayer != null && forceToRetrieve > 0) {
                final String retriever = attackingPlayer;
                action.appendEffect(
                        new RetrieveForceEffect(action, retriever, forceToRetrieve));
            }

            actions.add(action);
        }

        return actions;
    }

    /**
     * Dark Side player controls all movement of The Great Devourer.
     * - During Dark Side's move phase, may move to an adjacent exterior Endor site (once per turn).
     * - Once per game, during any deploy phase, may relocate to an adjacent site.
     *
     * NOTE: If this method does not compile (method not found in creature hierarchy),
     * move these actions to Card701_044_BACK (the objective) instead, which supports opponent actions.
     */
    @Override
    protected List<TopLevelGameTextAction> getOpponentsCardGameTextTopLevelActions(String playerId, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<>();

        // 1) During Dark Side's move phase, may move to an adjacent exterior Endor site (once per turn)
        GameTextActionId moveActionId = GameTextActionId.OTHER_CARD_ACTION_1;

        if (GameConditions.isDuringYourPhase(game, playerId, Phase.MOVE)
                && GameConditions.isOncePerTurn(game, self, playerId, gameTextSourceCardId, moveActionId)) {

            PhysicalCard currentLocation = game.getModifiersQuerying().getLocationHere(game.getGameState(), self);
            if (currentLocation != null) {
                Filter adjacentValidSite = Filters.and(
                        Filters.adjacentSite(currentLocation),
                        Filters.exterior_site,
                        Filters.Endor_site
                );

                if (GameConditions.canSpot(game, self, adjacentValidSite)) {
                    final TopLevelGameTextAction action = new TopLevelGameTextAction(self, playerId, gameTextSourceCardId, moveActionId);
                    action.setText("Move The Great Devourer to adjacent site");
                    action.setActionMsg("Move " + GameUtils.getCardLink(self) + " to an adjacent exterior Endor site");

                    action.appendUsage(
                            new OncePerTurnEffect(action));

                    action.appendEffect(
                            new ChooseCardOnTableEffect(action, playerId, "Choose adjacent site to move The Great Devourer to", adjacentValidSite) {
                                @Override
                                protected void cardSelected(PhysicalCard selectedCard) {
                                    action.appendEffect(
                                            new RelocateBetweenLocationsEffect(action, self, selectedCard));
                                }
                            }
                    );

                    actions.add(action);
                }
            }
        }

        // 2) Once per game, during any deploy phase, may relocate to an adjacent site
        GameTextActionId relocateActionId = GameTextActionId.GORAX_THE_GREAT_DEVOURER__RELOCATE_TO_ADJACENT_SITE;

        if (GameConditions.isOncePerGame(game, self, relocateActionId)
                && GameConditions.isDuringEitherPlayersPhase(game, Phase.DEPLOY)) {

            PhysicalCard currentLocation = game.getModifiersQuerying().getLocationHere(game.getGameState(), self);
            if (currentLocation != null) {
                // Note: once-per-game relocate goes to any adjacent site, not restricted to habitat
                Filter adjacentSite = Filters.adjacentSite(currentLocation);

                if (GameConditions.canSpot(game, self, adjacentSite)) {
                    final TopLevelGameTextAction action = new TopLevelGameTextAction(self, playerId, gameTextSourceCardId, relocateActionId);
                    action.setText("Relocate to adjacent site (once per game)");
                    action.setActionMsg("Relocate " + GameUtils.getCardLink(self) + " to an adjacent site");

                    action.appendUsage(
                            new OncePerGameEffect(action));

                    action.appendEffect(
                            new ChooseCardOnTableEffect(action, playerId, "Choose adjacent site to relocate to", adjacentSite) {
                                @Override
                                protected void cardSelected(PhysicalCard selectedCard) {
                                    action.appendEffect(
                                            new RelocateBetweenLocationsEffect(action, self, selectedCard));
                                }
                            }
                    );

                    actions.add(action);
                }
            }
        }

        return actions;
    }
}
