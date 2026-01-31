package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractCreature;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerGameEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.ModelType;
import com.gempukku.swccgo.common.Persona;
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
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.conditions.Condition;
import com.gempukku.swccgo.logic.effects.FlipCardEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromOffTableEffect;
import com.gempukku.swccgo.logic.effects.RetrieveForceEffect;
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
        super(Side.LIGHT, 0, 0, 10, 10, 0, Title.Gorax, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
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

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        final int permCardId = self.getPermanentCardId();

        // Condition: Light Side player does NOT have presence at Gorax's location (no suspicion)
        // When this condition is true, Gorax may not attack or move
        Condition noSuspicionCondition = new Condition() {
            @Override
            public boolean isFulfilled(GameState gameState, ModifiersQuerying modifiersQuerying) {
                PhysicalCard self = gameState.findCardByPermanentId(permCardId);
                if (self == null) {
                    return true;
                }
                PhysicalCard location = modifiersQuerying.getLocationHere(gameState, self);
                if (location == null) {
                    return true; // No location means no suspicion, so can't attack/move
                }
                // Check if Light Side has presence at this location
                String lightPlayer = gameState.getLightPlayer();
                // If no Light Side cards with ability present here, no suspicion
                return !Filters.canSpot(gameState.getGame(), self, Filters.and(Filters.owner(lightPlayer), Filters.at(location), Filters.hasAbilityOrHasPermanentPilotWithAbility));
            }
        };

        // Unless Gorax has suspicion, Gorax may not attack or move
        modifiers.add(new MayNotAttackModifier(self, noSuspicionCondition, self));
        modifiers.add(new MayNotMoveModifier(self, noSuspicionCondition));

        return modifiers;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        List<RequiredGameTextTriggerAction> actions = new LinkedList<>();
        String playerId = self.getOwner();

        // If lost, place out of play (card is already in Lost Pile when justLost triggers)
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

            // Determine the attacking player from the cards that defeated this creature
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

        // Flip this card if there are three or more cards beneath Pile of Bones
        if (TriggerConditions.isTableChanged(game, effectResult)
                && GameConditions.canBeFlipped(game, self)) {

            // Find Pile of Bones
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

        return actions;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(String playerId, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<>();
        String opponent = game.getOpponent(playerId);

        GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_1;

        // Once per game, may stack topmost character from opponent's Lost Pile under Pile of Bones
        if (GameConditions.isOncePerGame(game, self, gameTextActionId)) {
            // Find Pile of Bones
            PhysicalCard pileOfBones = Filters.findFirstActive(game, self, Filters.title(Title.Pile_Of_Bones));

            if (pileOfBones != null) {
                // Find the topmost character in opponent's Lost Pile (not just the top card)
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

                    // Update usage limit
                    action.appendUsage(
                            new OncePerGameEffect(action));

                    // Stack the topmost character from opponent's Lost Pile onto Pile of Bones
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
}
