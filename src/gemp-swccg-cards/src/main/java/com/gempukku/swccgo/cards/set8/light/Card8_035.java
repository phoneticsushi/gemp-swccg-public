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
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
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

        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new MayInitiateBattlesForFreeModifier(self, player));
        modifiers.add(new InitiateForceDrainCostModifier(self, new UnlessCondition(playerSatisfies), 3, player));
        modifiers.add(new InitiateForceDrainCostModifier(self, new UnlessCondition(opponentSatisfies), 3, opponent));
        return modifiers;
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
