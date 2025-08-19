package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractSite;
import com.gempukku.swccgo.cards.GameConditions;
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
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.FlipCardEffect;
import com.gempukku.swccgo.logic.modifiers.ForfeitModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.PowerModifier;
import com.gempukku.swccgo.logic.timing.Effect;

/**
* Set: BEEZER_BOWL_2025
* Type: LOCATION_SITE
* Title: Endor: Bright Tree Village
*/
public class Card701_001 extends AbstractSite {
    public Card701_001() {
        super(Side.DARK, "Endor: Bright Tree Village", Title.Endor, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLocationDarkSideGameText("Unless opponent controls, flip this site if Sorcery Test #3 completed.");
        setLocationLightSideGameText("Your Ewoks here are power and forfeit +1 (+2 if shaman).");
        addIcon(Icon.DARK_FORCE, 2);
        addIcon(Icon.LIGHT_FORCE, 2);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.EXTERIOR_SITE, Icon.PLANET);
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextDarkSideRequiredBeforeTriggers(String playerOnDarkSideOfLocation, SwccgGame game, Effect effect, PhysicalCard self, int gameTextSourceCardId) {
        // Unless opponent controls...
        if (!GameConditions.controls(game, game.getOpponent(playerOnDarkSideOfLocation), self)
                // ...if Sorcery Test #3 completed...
                && GameConditions.canSpot(game, self, Filters.and(Filters.completed_Jedi_Test, Filters.Sorcery_Test_3))) {

            // ...flip this site
            final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.appendEffect(new FlipCardEffect(action, self));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    protected List<Modifier> getGameTextLightSideWhileActiveModifiers(String playerOnLightSideOfLocation, SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Your Ewoks here
        final Filter yourEowksHere = Filters.and(Filters.your(playerOnLightSideOfLocation), Filters.Ewok, Filters.here(self));
        // are power and forfeit +1
        modifiers.add(new PowerModifier(self, Filters.and(yourEowksHere, Filters.not(Filters.Shaman)), 1));
        modifiers.add(new ForfeitModifier(self, Filters.and(yourEowksHere, Filters.not(Filters.Shaman)), 1));
        // (+2 if shaman)
        modifiers.add(new PowerModifier(self, Filters.and(yourEowksHere, Filters.Shaman), 2));
        modifiers.add(new ForfeitModifier(self, Filters.and(yourEowksHere, Filters.Shaman), 2));
        return modifiers;
    }
}
