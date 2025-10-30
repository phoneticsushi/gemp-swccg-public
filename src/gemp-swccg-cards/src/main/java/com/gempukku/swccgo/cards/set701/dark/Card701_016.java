package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractCreatureVehicle;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.conditions.AtSameSiteAsCondition;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.DrawDestinyEffect;
import com.gempukku.swccgo.logic.effects.RetrieveForceEffect;
import com.gempukku.swccgo.logic.modifiers.DefinedByGameTextAbilityModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.PowerModifier;

/**
* Set: BEEZER_BOWL_2025
* Type: VEHICLE_CREATURE
* Title: Quarf
*/
public class Card701_016 extends AbstractCreatureVehicle {
    public Card701_016() {
        super(Side.DARK, 5, 2, 3, null, 3, 3, 4, Title.Quarf, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Ability = 1/1,000,000,000,000,000,000,000,000,000. Power +1 at same site as Jadru. During battle may 'Quarf' (draw destiny; if destiny < total number of vehicles and characters here, retrieve 1 Force)");
        addIcons(Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.SHAPESHIFTER);
        // May add one ‘rider’ (passenger)
        // Note: assumed since this is a creature vehicle; it isn't actually in the game text...
        setPassengerCapacity(1);
    }



    @Override
    protected List<Modifier> getGameTextAlwaysOnModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Ability = 1/1,000,000,000,000,000,000,000,000,000
        modifiers.add(new DefinedByGameTextAbilityModifier(self, 1e-27));
        return modifiers;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Power +1 at same site as Jadru
        modifiers.add(new PowerModifier(self, new AtSameSiteAsCondition(self, Filters.Jadru), 1));
        return modifiers;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        if (GameConditions.isDuringBattle(game) && GameConditions.canDrawDestiny(game, playerId)) {
            // During battle may 'Quarf'
            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId);
            action.setText("'Quarf'");
            action.setActionMsg("attempt to retrieve 1 Force");
            action.appendEffect(
                    // draw destiny
                    new DrawDestinyEffect(action, playerId) {
                        @Override
                        protected void destinyDraws(SwccgGame game, List<PhysicalCard> destinyCardDraws, List<Float> destinyDrawValues, Float totalDestiny) {
                            if (totalDestiny == null) {
                                game.getGameState().sendMessage("Result: Failed due to failed destiny draw");
                                return;
                            }

                            // if destiny < total number of vehicles and characters here, retrieve 1 Force)
                            final int totalHere = Filters.countActive(game, self, Filters.and(Filters.or(Filters.vehicle, Filters.character), Filters.here(self)));
                            game.getGameState().sendMessage("Vehicles and Characters Here: " + totalHere);
                            if (totalDestiny < totalHere) {
                                action.appendEffect(new RetrieveForceEffect(action, playerId, 1));
                            }
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
