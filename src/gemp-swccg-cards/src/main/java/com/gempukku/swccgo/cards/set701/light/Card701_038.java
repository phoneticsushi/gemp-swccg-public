package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractCreature;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
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
import com.gempukku.swccgo.game.state.WhileInPlayData;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.conditions.Condition;
import com.gempukku.swccgo.logic.conditions.NotCondition;
import com.gempukku.swccgo.logic.effects.FlipCardEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromOffTableEffect;
import com.gempukku.swccgo.logic.effects.RetrieveForceEffect;
import com.gempukku.swccgo.logic.modifiers.KeywordModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotAttackModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotMoveModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.MovedOnlyByOpponentModifier;
import com.gempukku.swccgo.logic.modifiers.querying.ModifiersQuerying;
import com.gempukku.swccgo.logic.timing.EffectResult;
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
     * Helper method to check if Light Side player currently has presence at Gorax's location.
     * This is the live check used to TRIGGER suspicion. Once suspicion is gained, it is permanent
     * and tracked via WhileInPlayData.
     */
    private boolean hasLightSidePresence(SwccgGame game, PhysicalCard self) {
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

        // Condition: Gorax has permanently gained suspicion (stored in WhileInPlayData)
        Condition hasSuspicionCondition = new Condition() {
            @Override
            public boolean isFulfilled(GameState gameState, ModifiersQuerying modifiersQuerying) {
                PhysicalCard self = gameState.findCardByPermanentId(permCardId);
                if (self == null) {
                    return false;
                }
                return GameConditions.cardHasWhileInPlayDataEquals(self, true);
            }
        };

        // Inverse: no suspicion yet
        Condition noSuspicionCondition = new NotCondition(hasSuspicionCondition);

        // Display "Suspicion" keyword on card info when suspicion has been gained
        modifiers.add(new KeywordModifier(self, self, hasSuspicionCondition, Keyword.SUSPICION));

        // Unless Gorax has suspicion, Gorax may not move
        modifiers.add(new MayNotMoveModifier(self, noSuspicionCondition));

        // When suspicion is active, movement is controlled by opponent (Dark Side)
        // DS player can move Gorax using landspeed during their move phase and DS pays
        modifiers.add(new MovedOnlyByOpponentModifier(self, Filters.sameCardId(self), hasSuspicionCondition));

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

        // Suspicion gain check + flip check
        if (TriggerConditions.isTableChanged(game, effectResult)) {

            // Check if Light Side now has presence at Gorax's location.
            // Once suspicion is gained, it is permanent for the remainder of the game.
            boolean alreadyHasSuspicion = GameConditions.cardHasWhileInPlayDataEquals(self, true);

            if (!alreadyHasSuspicion && hasLightSidePresence(game, self)) {
                self.setWhileInPlayData(new WhileInPlayData(true));
                game.getGameState().sendMessage(GameUtils.getCardLink(self) + " gains suspicion \u2014 movement now controlled by Dark Side player for remainder of game");
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

        return actions;
    }


}
