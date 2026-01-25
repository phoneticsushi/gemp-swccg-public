package com.gempukku.swccgo.cards.set701.light;

import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractSite;
import com.gempukku.swccgo.cards.conditions.HereCondition;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.conditions.Condition;
import com.gempukku.swccgo.logic.conditions.UnlessCondition;
import com.gempukku.swccgo.logic.modifiers.ForceDrainModifier;
import com.gempukku.swccgo.logic.modifiers.ImmuneToAttritionModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;

/**
* Set: BEEZER_BOWL_2025
* Type: LOCATION_SITE
* Title: Endor: Twin Trees
*/
public class Card701_035 extends AbstractSite {
    public Card701_035() {
        super(Side.LIGHT, Title.Twin_Trees, Title.Endor, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLocationLightSideGameText("Your Ewoks here are immune to attrition.");
        setLocationDarkSideGameText("Unless your Ewok here, Force drain -1.");
        addIcon(Icon.DARK_FORCE, 1);
        addIcon(Icon.LIGHT_FORCE, 2);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.EXTERIOR_SITE, Icon.PLANET);
        setAsHorizontal(false);
    }

    @Override
    protected List<Modifier> getGameTextDarkSideWhileActiveModifiers(String playerOnDarkSideOfLocation, SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        final Filter yourEwoks = Filters.and(Filters.your(playerOnDarkSideOfLocation), Filters.Ewok);
        // Unless Your Ewoks here
        final Condition unlessYourEwoksHere = new UnlessCondition(new HereCondition(self, yourEwoks));
        // Force Drain -1
        modifiers.add(new ForceDrainModifier(self, self, unlessYourEwoksHere, -1, playerOnDarkSideOfLocation));
        return modifiers;
    }

    @Override
    protected List<Modifier> getGameTextLightSideWhileActiveModifiers(String playerOnLightSideOfLocation, SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Your Ewoks here
        final Filter yourEowksHere = Filters.and(Filters.your(playerOnLightSideOfLocation), Filters.Ewok, Filters.here(self));
        // are Immune to Attrition
        modifiers.add(new ImmuneToAttritionModifier(self, yourEowksHere));
        return modifiers;
    }
}
