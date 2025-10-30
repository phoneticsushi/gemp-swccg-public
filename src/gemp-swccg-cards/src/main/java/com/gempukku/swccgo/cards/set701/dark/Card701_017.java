package com.gempukku.swccgo.cards.set701.dark;

import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractCreatureVehicle;
import com.gempukku.swccgo.cards.conditions.HasAboardCondition;
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
import com.gempukku.swccgo.logic.modifiers.DefenseValueModifier;
import com.gempukku.swccgo.logic.modifiers.DefinedByGameTextAbilityModifier;
import com.gempukku.swccgo.logic.modifiers.MayMoveAsReactModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.PowerModifier;

/**
* Set: BEEZER_BOWL_2025
* Type: VEHICLE_CREATURE
* Title: Rakazzak Beast
*/
public class Card701_017 extends AbstractCreatureVehicle {
    public Card701_017() {
        super(Side.DARK, 4, 3, 4, null, 3, 3, 3, Title.Rakazzak_Beast, Uniqueness.UNRESTRICTED, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Ability = 3/4. May add one 'rider' (passenger). Ewoks are defense value -1 at same site. Power +3 when 'ridden' by a Yuzzum. May move as a 'react.'");
        addIcons(Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.ARACHNID);
        // May add one ‘rider’ (passenger)
        setPassengerCapacity(1);
    }

    @Override
    protected List<Modifier> getGameTextAlwaysOnModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Ability = 3/4
        modifiers.add(new DefinedByGameTextAbilityModifier(self, 3.0/4.0));
        return modifiers;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Ewoks are defense value -1 at same site
        modifiers.add(new DefenseValueModifier(self, Filters.and(Filters.Ewok, Filters.atSameSite(self)), -1));
        // Power +3 when 'ridden' by a Yuzzum
        modifiers.add(new PowerModifier(self, new HasAboardCondition(self, Filters.Yuzzum), 3));
        // May move as a ‘react.’
        modifiers.add(new MayMoveAsReactModifier(self));
        return modifiers;
    }
}
