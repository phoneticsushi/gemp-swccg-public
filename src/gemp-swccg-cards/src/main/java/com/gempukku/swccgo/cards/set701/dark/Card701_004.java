package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractSite;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerTurnEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.LoseCardsFromOffTableSimultaneouslyEffect;
import com.gempukku.swccgo.logic.effects.choose.TakeCardIntoHandFromReserveDeckEffect;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
* Set: BEEZER_BOWL_2025
* Type: LOCATION_SITE
* Title: Endor: Zarrak’s Hideout
*/
public class Card701_004 extends AbstractSite {
    public Card701_004() {
        super(Side.DARK, "Endor: Zarrak’s Hideout", Title.Endor, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLocationLightSideGameText("Your non-Ewok characters here are lost.");
        setLocationDarkSideGameText("Once per turn, may [upload] one Sorcery Test.");
        addIcon(Icon.DARK_FORCE, 2);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.EXTERIOR_SITE, Icon.MOUNTAIN_SITE, Icon.PLANET);
    }
    
    protected List<TopLevelGameTextAction> getGameTextDarkSideTopLevelActions(String playerOnDarkSideOfLocation, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.ZARRAKS_HIDEOUT__UPLOAD_SORCERY_TEST;

        // Once per turn...
        if (GameConditions.isOncePerTurn(game, self, playerOnDarkSideOfLocation, gameTextSourceCardId)) {
            // ...may [upload] one Sorcery Test
            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Upload a Sorcery Test");
            action.setActionMsg("Take a Sorcery Test into hand from Reserve Deck");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerTurnEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new TakeCardIntoHandFromReserveDeckEffect(action, playerOnDarkSideOfLocation, Filters.Sorcery_Test, true));
            return Collections.singletonList(action);
        }
        return null;
    }

    protected List<RequiredGameTextTriggerAction> getGameTextLightSideRequiredAfterTriggers(final String playerOnLightSideOfLocation, SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {

        // Your non-Ewok characters here are lost
        if ((TriggerConditions.isTableChanged(game, effectResult))) {
            Collection<PhysicalCard> charactersToLose = Filters.filter(game.getGameState().getCardsAtLocation(self), game, Filters.and(Filters.your(playerOnLightSideOfLocation), Filters.not(Filters.Ewok), Filters.character));
            if (!charactersToLose.isEmpty()) {
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setText("Make character" + (charactersToLose.size()==1?"":"s") + " lost");
                action.setActionMsg("Make " + GameUtils.getAppendedNames(charactersToLose) + " lost");
                // Perform result(s)
                action.appendEffect(
                        new LoseCardsFromOffTableSimultaneouslyEffect(action, charactersToLose, false));
                return Collections.singletonList(action);
            }
        }
        return null;
    }
}
