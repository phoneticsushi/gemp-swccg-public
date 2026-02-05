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
import com.gempukku.swccgo.common.PlayCardOptionId;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.game.state.WhileInPlayData;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.conditions.Condition;
import com.gempukku.swccgo.logic.effects.FlipCardEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromOffTableEffect;
import com.gempukku.swccgo.logic.effects.RelocateBetweenLocationsEffect;
import com.gempukku.swccgo.logic.effects.RetrieveForceEffect;
import com.gempukku.swccgo.logic.effects.choose.ChooseCardOnTableEffect;
import com.gempukku.swccgo.logic.modifiers.MayNotAttackModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotMoveModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.querying.ModifiersQuerying;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.PassthruEffect;
import com.gempukku.swccgo.logic.timing.results.DefeatedResult;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Beezer Bowl 2025
 * Type: Creature
 * Subtype: Gigantic Predator
 * Title: Gorax, The Mighty
 */
public class Card701_038 extends AbstractCreature {
    public Card701_038() {
        // Side, destiny, deployCost, ferocity, defenseValue, forfeit, title, uniqueness, expansionSet, rarity
        super(Side.LIGHT, 0, 0, 10, 10, 0, Title.Gorax_The_Mighty, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setFrontOfDoubleSidedCard(true);
        setLore("A towering predator of Endor's forests. Known to collect trophies from its victims.");
        setGameText("Habitat: Exterior Endor sites. May not be placed in Reserve Deck. If lost, place out of play. " +
                "Only deploys if your [Beezer Bowl 2025] objective on table. " +
                "If Light Side player has presence here, Gorax gains 'suspicion' (its movement is controlled by opponent as if a Dark Side card). " +
                "Unless Gorax has suspicion, Gorax may not attack or move. " +
                "Once per game, may stack topmost character from opponent's Lost Pile under Pile of Bones. " +
                "If defeated, attacking player retrieves 4 Force. " +
                "Flip this card if there are three or more cards beneath Pile of Bones.");
        addIcons(Icon.BEEZER_BOWL_2025, Icon.CREATURE);
        addModelType(ModelType.GIGANTIC_PREDATOR);
        addPersona(Persona.GORAX);
        setMayNotBePlacedInReserveDeck(true);
    }

    @Override
    protected Filter getGameTextHabitatFilter(String playerId, final SwccgGame game, final PhysicalCard self) {
        // Habitat: Exterior Endor sites
        return Filters.and(Filters.exterior_site, Filters.Endor_site);
    }

    @Override
    protected boolean checkGameTextDeployRequirements(String playerId, SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        // Only deploys if your [BB25] objective on table
        return Filters.canSpotFromAllOnTable(game, Filters.and(Filters.your(playerId), Filters.icon(Icon.BEEZER_BOWL_2025), Filters.Objective));
    }

    /**
     * Helper method to check if Gorax currently has suspicion (Light Side presence at its location).
     */
    private boolean hasSuspicion(SwccgGame game, PhysicalCard self) {
        PhysicalCard location = game.getModifiersQuerying().getLocationHere(game.getGameState(), self);
        if (location == null) {
            return false;
        }
        String lightPlayer = game.getLightPlayer();
        return Filters.canSpot(game, self, Filters.and(Filters.owner(lightPlayer), Filters.at(location), Filters.hasAbilityOrHasPermanentPilotWithAbility));
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        final int permCardId = self.getPermanentCardId();

        // Condition: Light Side player does NOT have presence at Gorax's location (no suspicion)
        Condition noSuspicionCondition = new Condition() {
            @Override
            public boolean isFulfilled(GameState gameState, ModifiersQuerying modifiersQuerying) {
                PhysicalCard self = gameState.findCardByPermanentId(permCardId);
                if (self == null) {
                    return true;
                }
                PhysicalCard location = modifiersQuerying.getLocationHere(gameState, self);
                if (location == null) {
                    return true;
                }
                String lightPlayer = gameState.getLightPlayer();
                return !Filters.canSpot(gameState.getGame(), self, Filters.and(Filters.owner(lightPlayer), Filters.at(location), Filters.hasAbilityOrHasPermanentPilotWithAbility));
            }
        };

        // Gorax may never use normal movement - Dark Side controls via explicit relocate when suspicion active
        modifiers.add(new MayNotMoveModifier(self));

        // Unless Gorax has suspicion, Gorax may not attack
        modifiers.add(new MayNotAttackModifier(self, noSuspicionCondition, self));

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

        // If defeated, attacking player retrieves 4 Force
        if (TriggerConditions.justDefeatedBy(game, effectResult, self, Filters.any)) {
            DefeatedResult defeatedResult = (DefeatedResult) effectResult;
            Collection<PhysicalCard> defeatedByCards = defeatedResult.getDefeatedByCards();

            String attackingPlayer = null;
            if (defeatedByCards != null && !defeatedByCards.isEmpty()) {
                PhysicalCard firstDefeater = defeatedByCards.iterator().next();
                attackingPlayer = firstDefeater.getOwner();
            }

            if (attackingPlayer != null) {
                final String retriever = attackingPlayer;
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setText("Attacking player retrieves 4 Force");
                action.setActionMsg("Make " + retriever + " retrieve 4 Force");
                action.appendEffect(
                        new RetrieveForceEffect(action, retriever, 4));
                actions.add(action);
            }
        }

        // Suspicion state change notification + flip check
        if (TriggerConditions.isTableChanged(game, effectResult)) {

            // Check and announce suspicion state changes
            boolean currentSuspicion = hasSuspicion(game, self);
            boolean previousSuspicion = GameConditions.cardHasWhileInPlayDataEquals(self, true);

            if (currentSuspicion && !previousSuspicion) {
                self.setWhileInPlayData(new WhileInPlayData(true));
                game.getGameState().sendMessage(GameUtils.getCardLink(self) + " gains suspicion — movement now controlled by Dark Side player");
            } else if (!currentSuspicion && previousSuspicion) {
                self.setWhileInPlayData(new WhileInPlayData(false));
                game.getGameState().sendMessage(GameUtils.getCardLink(self) + " loses suspicion — may not attack or move");
            }

            // Flip this card if there are three or more cards beneath Pile of Bones
            if (GameConditions.canBeFlipped(game, self)) {
                PhysicalCard pileOfBones = Filters.findFirstActive(game, self, Filters.title(Title.Pile_Of_Bones));
                if (pileOfBones != null) {
                    int stackedCount = game.getGameState().getStackedCards(pileOfBones).size();
                    if (stackedCount >= 3) {
                        RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                        action.setSingletonTrigger(true);
                        action.setText("Flip");
                        action.setActionMsg(null);
                        action.appendEffect(
                                new FlipCardEffect(action, self));
                        actions.add(action);
                    }
                }
            }
        }

        return actions;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(String playerId, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<>();
        String opponent = game.getOpponent(playerId);

        GameTextActionId gameTextActionId = GameTextActionId.GORAX_THE_MIGHTY__STACK_CHARACTER_UNDER_PILE_OF_BONES;

        // Once per game, may stack topmost character from opponent's Lost Pile under Pile of Bones
        if (GameConditions.isOncePerGame(game, self, gameTextActionId)) {
            PhysicalCard pileOfBones = Filters.findFirstActive(game, self, Filters.title(Title.Pile_Of_Bones));

            if (pileOfBones != null) {
                List<PhysicalCard> opponentLostPile = game.getGameState().getLostPile(opponent);
                PhysicalCard topmostCharacter = null;
                for (PhysicalCard card : opponentLostPile) {
                    if (Filters.character.accepts(game, card)) {
                        topmostCharacter = card;
                        break;
                    }
                }

                if (topmostCharacter != null) {
                    final PhysicalCard cardToStack = topmostCharacter;
                    final PhysicalCard stackOnCard = pileOfBones;

                    TopLevelGameTextAction action = new TopLevelGameTextAction(self, playerId, gameTextSourceCardId, gameTextActionId);
                    action.setText("Stack character under Pile of Bones");
                    action.setActionMsg("Stack " + GameUtils.getCardLink(cardToStack) + " from opponent's Lost Pile on " + GameUtils.getCardLink(stackOnCard));

                    action.appendUsage(
                            new OncePerGameEffect(action));

                    action.appendEffect(
                            new PassthruEffect(action) {
                                @Override
                                protected void doPlayEffect(SwccgGame game) {
                                    game.getGameState().removeCardFromZone(cardToStack);
                                    game.getGameState().stackCard(cardToStack, stackOnCard, false, false, false);
                                    game.getGameState().sendMessage(GameUtils.getCardLink(cardToStack) + " is stacked on " + GameUtils.getCardLink(stackOnCard));
                                }
                            });

                    actions.add(action);
                }
            }
        }

        return actions;
    }

    /**
     * Dark Side player controls Gorax's movement when suspicion is active.
     * During Dark Side's move phase, may move Gorax to an adjacent exterior Endor site (once per turn).
     *
     * NOTE: If this method does not compile (method not found in creature hierarchy),
     * move this action to Card701_044_BACK (the objective) instead, which supports opponent actions.
     */
    @Override
    protected List<TopLevelGameTextAction> getOpponentsCardGameTextTopLevelActions(String playerId, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<>();

        GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_1;

        // During Dark Side's move phase, if suspicion is active, DS may move Gorax once per turn
        if (GameConditions.isDuringYourPhase(game, playerId, Phase.MOVE)
                && GameConditions.isOncePerTurn(game, self, playerId, gameTextSourceCardId, gameTextActionId)
                && hasSuspicion(game, self)) {

            PhysicalCard currentLocation = game.getModifiersQuerying().getLocationHere(game.getGameState(), self);
            if (currentLocation != null) {
                Filter adjacentValidSite = Filters.and(
                        Filters.adjacentSite(currentLocation),
                        Filters.exterior_site,
                        Filters.Endor_site
                );

                if (GameConditions.canSpot(game, self, adjacentValidSite)) {
                    final TopLevelGameTextAction action = new TopLevelGameTextAction(self, playerId, gameTextSourceCardId, gameTextActionId);
                    action.setText("Move Gorax to adjacent site");
                    action.setActionMsg("Move " + GameUtils.getCardLink(self) + " to an adjacent exterior Endor site");

                    action.appendUsage(
                            new OncePerTurnEffect(action));

                    action.appendEffect(
                            new ChooseCardOnTableEffect(action, playerId, "Choose adjacent site to move Gorax to", adjacentValidSite) {
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
