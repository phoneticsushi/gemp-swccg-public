package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractTransportVehicle;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.CancelWeaponTargetingEffect;
import com.gempukku.swccgo.cards.effects.usage.OncePerGameEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.PlaceCardInLostPileFromTableEffect;
import com.gempukku.swccgo.logic.modifiers.CharactersAboardMayJumpOffModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.MovesForFreeModifier;
import com.gempukku.swccgo.logic.timing.Effect;

/**
* Set: BEEZER_BOWL_2025
* Type: VEHICLE_TRANSPORT
* Title: Zarrak’s Hang Glider
*/
public class Card701_025 extends AbstractTransportVehicle {
    public Card701_025() {
        super(Side.DARK, 4, 2, 3, null, 3, 3, 4, Title.Zarraks_Hang_Glider, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("May add 1 driver (must be Zarrak). Moves for free. Once per game during battle, may 'shield' Zarrak (cancel an attempt to target Zarrak with a weapon; place Zarrak's Hang Glider in Lost Pile). If lost, Zarrak may 'jump off' (disembark).");
        addIcons(Icon.BEEZER_BOWL_2025);
        // May add 1 driver
        setDriverCapacity(1);
    }
    
    @Override
    protected Filter getGameTextValidDriverFilter(String playerId, SwccgGame game, PhysicalCard self) {
        // (must be Zarrak)
        return Filters.Zarrak;
    }
    
    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Moves for free
        modifiers.add(new MovesForFreeModifier(self));
        // If lost, Zarrak may 'jump off' (disembark).
        modifiers.add(new CharactersAboardMayJumpOffModifier(self));
        return modifiers;
    }
    
    // Assuming references to "Zarrak" refer to "the driver", since only Zarrak can board this vehicle,
    // and it doesn't make lore sense for the vehicle to act on him otherwise...

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalBeforeTriggers(final String playerId, SwccgGame game, final Effect effect, final PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.ZARRAKS_HANG_GLIDER__SHIELD_ZARRAK;

        // Check condition(s)
        // Once per game during battle...
        if (GameConditions.isOncePerGame(game, self, gameTextActionId) && GameConditions.isDuringBattle(game)) {
            if (TriggerConditions.isTargetedByWeapon(game, effect, Filters.aboard(self), Filters.any)) {
                final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
                // ...may 'shield' Zarrak
                action.setText("'shield' the driver");
                action.setActionMsg("Cancel weapon targeting");
                // Update usage limit(s)
                action.appendUsage(
                        new OncePerGameEffect(action));
                // Pay cost(s)
                action.appendCost(
                        // place Zarrak's Hang Glider in Lost Pile)
                        new PlaceCardInLostPileFromTableEffect(action, self));
                // Perform result(s)
                action.appendEffect(
                        // cancel an attempt to target Zarrak with a weapon
                        new CancelWeaponTargetingEffect(action));
                return Collections.singletonList(action);
            }
        }
        
        return null;
    }
}
