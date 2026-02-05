package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractCreature;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.ModelType;
import com.gempukku.swccgo.common.Persona;
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
import com.gempukku.swccgo.logic.conditions.Condition;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromOffTableEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromTableEffect;
import com.gempukku.swccgo.logic.effects.RetrieveForceEffect;
import com.gempukku.swccgo.logic.modifiers.DefinedByGameTextFerocityModifier;
import com.gempukku.swccgo.logic.modifiers.ForceRetrievalImmuneToSecretPlansModifier;
import com.gempukku.swccgo.logic.modifiers.KeywordModifier;
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
        final int permCardId = self.getPermanentCardId();

        // Ferocity = 10 + destiny
        modifiers.add(new DefinedByGameTextFerocityModifier(self, 10, 1));

        // Force retrieval from defeating The Great Devourer is immune to Secret Plans
        modifiers.add(new ForceRetrievalImmuneToSecretPlansModifier(self, self));

        // Dark Side player controls movement - opponent controls all normal movement
        // DS can move The Great Devourer using landspeed (boosted by Pile of Bones) during their move phase
        modifiers.add(new MovedOnlyByOpponentModifier(self, Filters.sameCardId(self)));

        // Display "Suspicion" keyword if it was gained on the front side.
        // WhileInPlayData persists across flips on the same physical card.
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
        modifiers.add(new KeywordModifier(self, self, hasSuspicionCondition, Keyword.SUSPICION));

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

}
