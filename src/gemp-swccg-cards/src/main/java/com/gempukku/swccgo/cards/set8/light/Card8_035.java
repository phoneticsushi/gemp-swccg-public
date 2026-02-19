package com.gempukku.swccgo.cards.set8.light;

import com.gempukku.swccgo.cards.AbstractNormalEffect;
import com.gempukku.swccgo.cards.conditions.OccupiesCondition;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.PlayCardZoneOption;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgBuiltInCardBlueprint;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.conditions.AndCondition;
import com.gempukku.swccgo.logic.conditions.Condition;
import com.gempukku.swccgo.logic.conditions.OrCondition;
import com.gempukku.swccgo.logic.conditions.UnlessCondition;
import com.gempukku.swccgo.logic.modifiers.InitiateForceDrainCostModifier;
import com.gempukku.swccgo.logic.modifiers.MayInitiateBattlesForFreeModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.ModifierType;
import com.gempukku.swccgo.logic.modifiers.querying.ModifiersQuerying;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Endor
 * Type: Effect
 * Title: Battle Plan
 */
public class Card8_035 extends AbstractNormalEffect {
    public Card8_035() {
        super(Side.LIGHT, 5, PlayCardZoneOption.YOUR_SIDE_OF_TABLE, Title.Battle_Plan, Uniqueness.UNIQUE, ExpansionSet.ENDOR, Rarity.U);
        setLore("Even though the landing of the stolen shuttle was successful, the Rebel strike team on Endor was forced to rethink their plans when Leia disappeared.");
        setGameText("Deploy on table. You may initiate battles for free. Also, for either player to initiate a Force drain, that player must first use 3 Force unless that player occupies a battleground site (except a holosite) and a battleground system. (Immune to Alter.)");
        addIcons(Icon.ENDOR);
        addImmuneToCardTitle(Title.Alter);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        String player = self.getOwner();
        String opponent = game.getOpponent(player);

        // Condition: Player occupies a location with SATISFIES_BATTLE_PLAN modifier
        Condition playerSatisfiesBattlePlan = new OccupiesSatisfiesBattlePlanCondition(player);
        Condition opponentSatisfiesBattlePlan = new OccupiesSatisfiesBattlePlanCondition(opponent);

        // Normal satisfaction: occupies battleground site AND battleground system
        Condition playerNormalSatisfaction = new AndCondition(
                new OccupiesCondition(player, Filters.battleground_site),
                new OccupiesCondition(player, Filters.battleground_system));
        Condition opponentNormalSatisfaction = new AndCondition(
                new OccupiesCondition(opponent, Filters.battleground_site),
                new OccupiesCondition(opponent, Filters.battleground_system));

        // Combined satisfaction: normal OR via modifier
        Condition playerSatisfies = new OrCondition(playerNormalSatisfaction, playerSatisfiesBattlePlan);
        Condition opponentSatisfies = new OrCondition(opponentNormalSatisfaction, opponentSatisfiesBattlePlan);

        // Filter for locations where the cost applies (excluding immune locations)
        // Player (Light Side) - exclude locations where player has immunity
        Filter playerLocationFilter = Filters.not(getForceDrainImmuneToBattlePlanFilter(player));

        // Opponent (Dark Side) - exclude locations where opponent has immunity (like Apex)
        Filter opponentLocationFilter = Filters.not(getForceDrainImmuneToBattlePlanFilter(opponent));

        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new MayInitiateBattlesForFreeModifier(self, player));
        modifiers.add(new InitiateForceDrainCostModifier(self, playerLocationFilter,
                new UnlessCondition(playerSatisfies), 3, player));
        modifiers.add(new InitiateForceDrainCostModifier(self, opponentLocationFilter,
                new UnlessCondition(opponentSatisfies), 3, opponent));
        return modifiers;
    }

    /**
     * Creates a filter that accepts locations where the specified player has Force drain immunity to Battle Plan.
     * @param playerId the player
     * @return the filter
     */
    private Filter getForceDrainImmuneToBattlePlanFilter(final String playerId) {
        return new Filter() {
            @Override
            public boolean accepts(GameState gameState, ModifiersQuerying modifiersQuerying, PhysicalCard physicalCard) {
                return modifiersQuerying.isForceDrainImmuneToModifier(gameState, physicalCard, ModifierType.FORCE_DRAIN_IMMUNE_TO_BATTLE_PLAN, playerId);
            }
            @Override
            public boolean accepts(GameState gameState, ModifiersQuerying modifiersQuerying, SwccgBuiltInCardBlueprint builtInCardBlueprint) {
                return false;
            }
        };
    }

    /**
     * A condition that checks if a player occupies a location with the SATISFIES_BATTLE_PLAN modifier.
     */
    private class OccupiesSatisfiesBattlePlanCondition implements Condition {
        private String _playerId;

        public OccupiesSatisfiesBattlePlanCondition(String playerId) {
            _playerId = playerId;
        }

        @Override
        public boolean isFulfilled(GameState gameState, ModifiersQuerying modifiersQuerying) {
            // Find all locations the player occupies
            Collection<PhysicalCard> occupiedLocations = Filters.filterTopLocationsOnTable(gameState.getGame(),
                    Filters.occupies(_playerId));

            // Check if any of those locations have the SATISFIES_BATTLE_PLAN modifier
            for (PhysicalCard location : occupiedLocations) {
                if (modifiersQuerying.hasModifierType(gameState, location, ModifierType.SATISFIES_BATTLE_PLAN)) {
                    return true;
                }
            }
            return false;
        }
    }
}
