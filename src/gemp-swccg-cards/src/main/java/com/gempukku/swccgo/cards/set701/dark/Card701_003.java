package com.gempukku.swccgo.cards.set701.dark;

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
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;

/**
* Set: BEEZER_BOWL_2025
* Type: LOCATION_SITE
* Title: Endor: Mt. Thunderstone Castle
*/
public class Card701_003 extends AbstractSite {
    public Card701_003() {
        super(Side.DARK, "Endor: Mt. Thunderstone Castle", Title.Endor, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLocationDarkSideGameText("During your move phase, ‘dark arts’ characters may move between here and any Endor battleground site.");
        addIcon(Icon.DARK_FORCE, 2);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.EXTERIOR_SITE, Icon.MOUNTAIN_SITE, Icon.PLANET);
    }
    
    @Override
    protected List<TopLevelGameTextAction> getGameTextDarkSideTopLevelActions(String playerOnDarkSideOfLocation, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();
        Filter otherEndorBattlegroundSites = Filters.and(Filters.other(self), Filters.Endor_site, Filters.battleground);

        // During your move phase, ‘dark arts’ characters may move between here and any Endor battleground site.
        if (GameConditions.isDuringYourPhase(game, playerOnDarkSideOfLocation, Phase.MOVE)
                && GameConditions.canSpotLocation(game, otherEndorBattlegroundSites)) {
            if (GameConditions.canPerformMovementUsingLocationText(playerOnDarkSideOfLocation, game, Filters.Dark_Arts, self, otherEndorBattlegroundSites, false)) {

                MoveUsingLocationTextAction action = new MoveUsingLocationTextAction(playerOnDarkSideOfLocation, game, self, gameTextSourceCardId, Filters.Dark_Arts, self, otherEndorBattlegroundSites, false);
                action.setText("Move from here to other Endor battleground site");
                actions.add(action);
            }
        }
        return actions;
    }
}
