package com.gempukku.swccgo.cards.set701.dark;

import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractSite;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.modifiers.MayDeployOtherCardsAsReactToLocationModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;

/**
* Set: BEEZER_BOWL_2025
* Type: LOCATION_SITE
* Title: Endor: Mt. Thunderstone Talus
*/
public class Card701_002 extends AbstractSite {
    public Card701_002() {
        super(Side.DARK, "Endor: Mt. Thunderstone Talus", Title.Endor, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLocationLightSideGameText("Your Ewok may deploy here from Reserve Deck as a ‘react’.");
        setLocationDarkSideGameText("Your Rakazzak Beast may deploy here from Reserve Deck as a 'react'.");
        addIcon(Icon.DARK_FORCE, 2);
        addIcon(Icon.LIGHT_FORCE, 1);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.EXTERIOR_SITE, Icon.INTERIOR_SITE, Icon.MOUNTAIN_SITE, Icon.PLANET);
    }
    
    @Override
    protected List<Modifier> getGameTextDarkSideWhileActiveModifiers(String playerOnDarkSideOfLocation, SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Your Rakazzak Beast may deploy here from Reserve Deck as a 'react'
        modifiers.add(new MayDeployOtherCardsAsReactToLocationModifier(self, "Deploy Rakazzak Beast as a 'react'",
                playerOnDarkSideOfLocation, Filters.and(Filters.your(playerOnDarkSideOfLocation), Filters.title(Title.Rakazzak_Beast)), self));
        return modifiers;
    }

    @Override
    protected List<Modifier> getGameTextLightSideWhileActiveModifiers(String playerOnLightSideOfLocation, SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Your Ewok may deploy here from Reserve Deck as a ‘react’
        modifiers.add(new MayDeployOtherCardsAsReactToLocationModifier(self, "Deploy Ewok as a 'react'",
                playerOnLightSideOfLocation, Filters.and(Filters.your(playerOnLightSideOfLocation), Filters.Ewok), self));
        return modifiers;
    }
}
