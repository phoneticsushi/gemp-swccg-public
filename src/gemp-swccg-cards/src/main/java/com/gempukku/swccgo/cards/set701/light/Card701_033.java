package com.gempukku.swccgo.cards.set701.light;

import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractSite;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.actions.MoveUsingLocationTextAction;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.modifiers.InitiateBattleCostModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;

/**
 * Set: Beezer Bowl 2025
 * Type: Location
 * Subtype: Site
 * Title: Endor: Generator Chamber
 */
public class Card701_033 extends AbstractSite {
    public Card701_033() {
        super(Side.LIGHT, Title.Generator_Chamber, Title.Endor, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLocationLightSideGameText("During your move phase, you may move between here and Back Door.");
        setLocationDarkSideGameText("To initiate battle here, you must use +3 force.");
        addIcon(Icon.LIGHT_FORCE, 2);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.INTERIOR_SITE, Icon.PLANET, Icon.SCOMP_LINK);
    }

    @Override
    protected List<Modifier> getGameTextDarkSideWhileActiveModifiers(String playerOnDarkSideOfLocation, SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // To initiate battle here, you must use +3 force.
        modifiers.add(new InitiateBattleCostModifier(self, self, 3, playerOnDarkSideOfLocation));
        return modifiers;
    }

    // During your move phase, you may move between here and Back Door
    @Override
    protected List<TopLevelGameTextAction> getGameTextLightSideTopLevelActions(String playerOnLightSideOfLocation, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();

        if (GameConditions.isDuringYourPhase(game, playerOnLightSideOfLocation, Phase.MOVE)) {

            // Move FROM here to Back Door
            if (GameConditions.canSpotLocation(game, Filters.Back_Door)
                    && GameConditions.canPerformMovementUsingLocationText(playerOnLightSideOfLocation, game, Filters.your(playerOnLightSideOfLocation), self, Filters.Back_Door, false)) {

                MoveUsingLocationTextAction action = new MoveUsingLocationTextAction(playerOnLightSideOfLocation, game, self, gameTextSourceCardId, Filters.your(playerOnLightSideOfLocation), self, Filters.Back_Door, false);
                action.setText("Move from here to Back Door");
                actions.add(action);
            }

            // Move TO this site from Back Door
            if (GameConditions.canSpotLocation(game, Filters.Back_Door)
                    && GameConditions.canPerformMovementUsingLocationText(playerOnLightSideOfLocation, game, Filters.your(playerOnLightSideOfLocation), Filters.Back_Door, self, false)) {

                MoveUsingLocationTextAction action = new MoveUsingLocationTextAction(playerOnLightSideOfLocation, game, self, gameTextSourceCardId, Filters.your(playerOnLightSideOfLocation), Filters.Back_Door, self, false);
                action.setText("Move from Back Door to here");
                actions.add(action);
            }
        }

        return actions;
    }
}
