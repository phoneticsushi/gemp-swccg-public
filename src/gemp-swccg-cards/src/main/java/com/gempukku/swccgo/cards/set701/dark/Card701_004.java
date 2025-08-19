package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractSite;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerTurnEffect;
import com.gempukku.swccgo.common.ExpansionSet;
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
import com.gempukku.swccgo.logic.effects.LoseCardsFromTableSimultaneouslyEffect;
import com.gempukku.swccgo.logic.effects.choose.TakeCardIntoHandFromReserveDeckEffect;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
* Set: BEEZER_BOWL_2025
* Type: LOCATION_SITE
* Title: Endor: Zarrak’s Hideout
*/
public class Card701_004 extends AbstractSite {
    public Card701_004() {
        super(Side.DARK, Title.Zarraks_Hideout, Title.Endor, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLocationLightSideGameText("Your non-Ewok characters here are lost.");
        setLocationDarkSideGameText("Once per turn, may [upload] one Sorcery Test.");
        addIcon(Icon.DARK_FORCE, 2);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.INTERIOR_SITE, Icon.MOUNTAIN_SITE, Icon.PLANET);
    }

    protected List<TopLevelGameTextAction> getGameTextDarkSideTopLevelActions(String playerOnDarkSideOfLocation, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        // Once per turn...
        if (GameConditions.isOncePerTurn(game, self, playerOnDarkSideOfLocation, gameTextSourceCardId)) {
            // ...may [upload] one Sorcery Test
            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, playerOnDarkSideOfLocation, gameTextSourceCardId);
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

    // N.B. logic based on Qui-gon's Lightsaber (221_070)
    protected List<RequiredGameTextTriggerAction> getGameTextLightSideRequiredAfterTriggers(final String playerOnLightSideOfLocation, SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        // Your non-Ewok characters here are lost
        if ((TriggerConditions.isTableChanged(game, effectResult))) {
            Collection<PhysicalCard> charactersToLose = Filters.filter(game.getGameState().getCardsAtLocation(self), game, Filters.and(Filters.your(playerOnLightSideOfLocation), Filters.not(Filters.Ewok), Filters.character));
            if (!charactersToLose.isEmpty()) {
                final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setSingletonTrigger(true);
                action.setText("Make character" + (charactersToLose.size()==1?"":"s") + " lost");
                action.setActionMsg("Make " + GameUtils.getAppendedNames(charactersToLose) + " lost");
                // Perform result(s)
                action.appendEffect(
                    // N.B. it's not clear if captives should be released here, but it makes lore sense that they would be,
                    // as non-ewok characters are presumably disappeared by Zarrak's magic.
                    // If any captives are themselves not ewoks, they'll suffer the same fate, else they remain at the location
                    new LoseCardsFromTableSimultaneouslyEffect(action, charactersToLose, false, true));
                return Collections.singletonList(action);
            }
        }
        return null;
    }
}
