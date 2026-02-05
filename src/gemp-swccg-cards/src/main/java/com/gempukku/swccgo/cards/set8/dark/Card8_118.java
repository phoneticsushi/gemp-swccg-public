package com.gempukku.swccgo.cards.set8.dark;

import com.gempukku.swccgo.cards.AbstractNormalEffect;
import com.gempukku.swccgo.cards.conditions.OccupiesCondition;
import com.gempukku.swccgo.cards.conditions.OnTableCondition;
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
import com.gempukku.swccgo.logic.conditions.AndCondition;
import com.gempukku.swccgo.logic.conditions.Condition;
import com.gempukku.swccgo.logic.conditions.OrCondition;
import com.gempukku.swccgo.logic.conditions.UnlessCondition;
import com.gempukku.swccgo.logic.modifiers.InitiateForceDrainCostModifier;
import com.gempukku.swccgo.logic.modifiers.MayInitiateBattlesForFreeModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.ModifierType;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.modifiers.querying.ModifiersQuerying;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Endor
 * Type: Effect
 * Title: Battle Order
 */
public class Card8_118 extends AbstractNormalEffect {
    public Card8_118() {
        super(Side.DARK, 5, PlayCardZoneOption.YOUR_SIDE_OF_TABLE, Title.Battle_Order, Uniqueness.UNIQUE, ExpansionSet.ENDOR, Rarity.U);
        setLore("Administration of the Imperial installation on Endor includes coordination of troops on the ground and tight security provided by the Empire's space fleet.");
        setGameText("Deploy on table. You may initiate battles for free. Also (unless Battle Plan on table), for either player to initiate a Force drain, that player must first use 3 Force unless that player occupies a battleground site (except a holosite) and a battleground system. (Immune to Alter.)");
        addIcons(Icon.ENDOR);
        addImmuneToCardTitle(Title.Alter);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        String player = self.getOwner();
        String opponent = game.getOpponent(player);
        Condition battlePlanOnTable = new OnTableCondition(self, Filters.Battle_Plan);

        // Condition: Player occupies a location with SATISFIES_BATTLE_ORDER modifier
        Condition playerSatisfiesBattleOrder = new OccupiesSatisfiesBattleOrderCondition(player);
        Condition opponentSatisfiesBattleOrder = new OccupiesSatisfiesBattleOrderCondition(opponent);

        // Normal satisfaction: occupies battleground site AND battleground system
        Condition playerNormalSatisfaction = new AndCondition(
                new OccupiesCondition(player, Filters.battleground_site),
                new OccupiesCondition(player, Filters.battleground_system));
        Condition opponentNormalSatisfaction = new AndCondition(
                new OccupiesCondition(opponent, Filters.battleground_site),
                new OccupiesCondition(opponent, Filters.battleground_system));

        // Combined satisfaction: normal OR via modifier
        Condition playerSatisfies = new OrCondition(playerNormalSatisfaction, playerSatisfiesBattleOrder);
        Condition opponentSatisfies = new OrCondition(opponentNormalSatisfaction, opponentSatisfiesBattleOrder);

        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new MayInitiateBattlesForFreeModifier(self, Filters.not(Filters.immuneToCardTitle(Title.Battle_Order)), player));
        modifiers.add(new InitiateForceDrainCostModifier(self, Filters.not(Filters.immuneToCardTitle(Title.Battle_Order)),
                new UnlessCondition(new OrCondition(battlePlanOnTable, playerSatisfies)), 3, player));
        modifiers.add(new InitiateForceDrainCostModifier(self, Filters.not(Filters.immuneToCardTitle(Title.Battle_Order)),
                new UnlessCondition(new OrCondition(battlePlanOnTable, opponentSatisfies)), 3, opponent));
        return modifiers;
    }

    /**
     * A condition that checks if a player occupies a location with the SATISFIES_BATTLE_ORDER modifier.
     */
    private class OccupiesSatisfiesBattleOrderCondition implements Condition {
        private String _playerId;

        public OccupiesSatisfiesBattleOrderCondition(String playerId) {
            _playerId = playerId;
        }

        @Override
        public boolean isFulfilled(GameState gameState, ModifiersQuerying modifiersQuerying) {
            // Find all locations the player occupies
            Collection<PhysicalCard> occupiedLocations = Filters.filterTopLocationsOnTable(gameState.getGame(),
                    Filters.occupies(_playerId));

            // Check if any of those locations have the SATISFIES_BATTLE_ORDER modifier
            for (PhysicalCard location : occupiedLocations) {
                if (modifiersQuerying.hasModifierType(gameState, location, ModifierType.SATISFIES_BATTLE_ORDER)) {
                    return true;
                }
            }
            return false;
        }
    }
}
