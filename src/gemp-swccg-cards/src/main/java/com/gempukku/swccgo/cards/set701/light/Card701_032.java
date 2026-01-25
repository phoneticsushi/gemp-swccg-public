package com.gempukku.swccgo.cards.set701.light;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractSite;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.conditions.DuringPlayersTurnNumberCondition;
import com.gempukku.swccgo.cards.conditions.OccupiesCondition;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.conditions.Condition;
import com.gempukku.swccgo.logic.conditions.UnlessCondition;
import com.gempukku.swccgo.logic.effects.choose.DeployCardToLocationFromReserveDeckEffect;
import com.gempukku.swccgo.logic.modifiers.GenerateNoForceModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotBeConvertedModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotInitiateBattleAtLocationModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;

/**
* Set: BEEZER_BOWL_2025
* Type: LOCATION_SITE
* Title: Endor: Back Door (V)
*/
public class Card701_032 extends AbstractSite {
    public Card701_032() {
        super(Side.LIGHT, Title.Back_Door, Title.Endor, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLocationLightSideGameText("May [download] EZ-PZ here. May not be converted.");
        setLocationDarkSideGameText("You may not initiate battle here on your first turn. Unless you occupy, you generate no force here.");
        addIcon(Icon.DARK_FORCE, 2);
        addIcon(Icon.LIGHT_FORCE, 2);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.EXTERIOR_SITE, Icon.PLANET, Icon.SCOMP_LINK);
    }


    @Override
    protected List<Modifier> getGameTextDarkSideWhileActiveModifiers(String playerOnDarkSideOfLocation, SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();

        // You may not initiate battle here on your first turn
        Condition firstTurnCondition = new DuringPlayersTurnNumberCondition(playerOnDarkSideOfLocation, 1);
        modifiers.add(new MayNotInitiateBattleAtLocationModifier(self, self, firstTurnCondition, playerOnDarkSideOfLocation));

        // Unless you occupy, you generate no force here
        Condition unlessYouOccupy = new UnlessCondition(new OccupiesCondition(playerOnDarkSideOfLocation, self));
        modifiers.add(new GenerateNoForceModifier(self, self, unlessYouOccupy, playerOnDarkSideOfLocation));

        return modifiers;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextLightSideTopLevelActions(String playerOnLightSideOfLocation, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.BACK_DOOR__DOWNLOAD_EZ_PZ;

        // May [download] EZ-PZ here
        if (GameConditions.canDeployCardFromReserveDeck(game, playerOnLightSideOfLocation, self, gameTextActionId, Persona.EZ_PZ)) {
            TopLevelGameTextAction action = new TopLevelGameTextAction(self, playerOnLightSideOfLocation, gameTextSourceCardId, gameTextActionId);
            action.setText("Deploy EZ-PZ here from Reserve Deck");
            action.setActionMsg("Deploy EZ-PZ from Reserve Deck");
            action.appendEffect(
                new DeployCardToLocationFromReserveDeckEffect(action, Filters.EZ_PZ, Filters.here(self), true)
            );
            return Collections.singletonList(action);
        }

        return null;
    }


    @Override
    protected List<Modifier> getGameTextLightSideWhileActiveModifiers(String playerOnLightSideOfLocation, SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // May not be converted
        modifiers.add(new MayNotBeConvertedModifier(self));
        return modifiers;
    }
}
