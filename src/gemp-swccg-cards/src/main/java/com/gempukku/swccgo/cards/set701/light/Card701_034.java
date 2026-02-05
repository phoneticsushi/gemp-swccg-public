package com.gempukku.swccgo.cards.set701.light;

import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractSite;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.actions.MoveUsingLocationTextAction;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;

/**
 * Set: Beezer Bowl 2025
 * Type: Location
 * Subtype: Site
 * Title: Endor: Mt. Krana Pass
 */
public class Card701_034 extends AbstractSite {
    public Card701_034() {
        super(Side.LIGHT, Title.Endor_Mt_Krana_Pass, Title.Endor, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLocationLightSideGameText("During your move phase, your cards may move between here and Back Door or Gorax's Lair.");
        setLocationDarkSideGameText("During your move phase, your cards may move between here and Back Door or Gorax's Lair.");
        addIcon(Icon.DARK_FORCE, 1);
        addIcon(Icon.LIGHT_FORCE, 2);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.EXTERIOR_SITE, Icon.MOUNTAIN_SITE, Icon.PLANET);
        addKeyword(Keyword.MT_KRANA_SITE);
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextLightSideTopLevelActions(String playerOnLightSideOfLocation, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();

        // During your move phase, your cards may move between here and Back Door or Gorax's Lair
        if (GameConditions.isDuringYourPhase(game, playerOnLightSideOfLocation, Phase.MOVE)) {
            Filter yourCards = Filters.your(playerOnLightSideOfLocation);
            Filter goraxsLair = Filters.title(Title.Goraxs_Lair);

            // Move FROM here TO Back Door
            if (GameConditions.canSpotLocation(game, Filters.Back_Door)
                    && GameConditions.canPerformMovementUsingLocationText(playerOnLightSideOfLocation, game, yourCards, self, Filters.Back_Door, false)) {
                MoveUsingLocationTextAction action = new MoveUsingLocationTextAction(playerOnLightSideOfLocation, game, self, gameTextSourceCardId, yourCards, self, Filters.Back_Door, false);
                action.setText("Move from here to Back Door");
                actions.add(action);
            }

            // Move FROM here TO Gorax's Lair
            if (GameConditions.canSpotLocation(game, goraxsLair)
                    && GameConditions.canPerformMovementUsingLocationText(playerOnLightSideOfLocation, game, yourCards, self, goraxsLair, false)) {
                MoveUsingLocationTextAction action = new MoveUsingLocationTextAction(playerOnLightSideOfLocation, game, self, gameTextSourceCardId, yourCards, self, goraxsLair, false);
                action.setText("Move from here to Gorax's Lair");
                actions.add(action);
            }

            // Move FROM Back Door TO here
            if (GameConditions.canSpotLocation(game, Filters.Back_Door)
                    && GameConditions.canPerformMovementUsingLocationText(playerOnLightSideOfLocation, game, yourCards, Filters.Back_Door, self, false)) {
                MoveUsingLocationTextAction action = new MoveUsingLocationTextAction(playerOnLightSideOfLocation, game, self, gameTextSourceCardId, yourCards, Filters.Back_Door, self, false);
                action.setText("Move from Back Door to here");
                actions.add(action);
            }

            // Move FROM Gorax's Lair TO here
            if (GameConditions.canSpotLocation(game, goraxsLair)
                    && GameConditions.canPerformMovementUsingLocationText(playerOnLightSideOfLocation, game, yourCards, goraxsLair, self, false)) {
                MoveUsingLocationTextAction action = new MoveUsingLocationTextAction(playerOnLightSideOfLocation, game, self, gameTextSourceCardId, yourCards, goraxsLair, self, false);
                action.setText("Move from Gorax's Lair to here");
                actions.add(action);
            }
        }

        return actions;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextDarkSideTopLevelActions(String playerOnDarkSideOfLocation, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();

        // During your move phase, your cards may move between here and Back Door or Gorax's Lair
        if (GameConditions.isDuringYourPhase(game, playerOnDarkSideOfLocation, Phase.MOVE)) {
            Filter yourCards = Filters.your(playerOnDarkSideOfLocation);
            Filter goraxsLair = Filters.title(Title.Goraxs_Lair);

            // Move FROM here TO Back Door
            if (GameConditions.canSpotLocation(game, Filters.Back_Door)
                    && GameConditions.canPerformMovementUsingLocationText(playerOnDarkSideOfLocation, game, yourCards, self, Filters.Back_Door, false)) {
                MoveUsingLocationTextAction action = new MoveUsingLocationTextAction(playerOnDarkSideOfLocation, game, self, gameTextSourceCardId, yourCards, self, Filters.Back_Door, false);
                action.setText("Move from here to Back Door");
                actions.add(action);
            }

            // Move FROM here TO Gorax's Lair
            if (GameConditions.canSpotLocation(game, goraxsLair)
                    && GameConditions.canPerformMovementUsingLocationText(playerOnDarkSideOfLocation, game, yourCards, self, goraxsLair, false)) {
                MoveUsingLocationTextAction action = new MoveUsingLocationTextAction(playerOnDarkSideOfLocation, game, self, gameTextSourceCardId, yourCards, self, goraxsLair, false);
                action.setText("Move from here to Gorax's Lair");
                actions.add(action);
            }

            // Move FROM Back Door TO here
            if (GameConditions.canSpotLocation(game, Filters.Back_Door)
                    && GameConditions.canPerformMovementUsingLocationText(playerOnDarkSideOfLocation, game, yourCards, Filters.Back_Door, self, false)) {
                MoveUsingLocationTextAction action = new MoveUsingLocationTextAction(playerOnDarkSideOfLocation, game, self, gameTextSourceCardId, yourCards, Filters.Back_Door, self, false);
                action.setText("Move from Back Door to here");
                actions.add(action);
            }

            // Move FROM Gorax's Lair TO here
            if (GameConditions.canSpotLocation(game, goraxsLair)
                    && GameConditions.canPerformMovementUsingLocationText(playerOnDarkSideOfLocation, game, yourCards, goraxsLair, self, false)) {
                MoveUsingLocationTextAction action = new MoveUsingLocationTextAction(playerOnDarkSideOfLocation, game, self, gameTextSourceCardId, yourCards, goraxsLair, self, false);
                action.setText("Move from Gorax's Lair to here");
                actions.add(action);
            }
        }

        return actions;
    }
}
