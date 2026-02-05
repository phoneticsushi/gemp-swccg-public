package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractRebel;
import com.gempukku.swccgo.cards.AbstractPermanentWeapon;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Statistic;
import com.gempukku.swccgo.common.TargetingReason;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.FireWeaponAction;
import com.gempukku.swccgo.logic.actions.FireWeaponActionBuilder;
import com.gempukku.swccgo.logic.modifiers.MayDeployAsReactToLocationModifier;
import com.gempukku.swccgo.logic.modifiers.MayDeployAsReactToLocationForFreeModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Set 701 (Beezer Bowl 2025)
 * Type: Character
 * Subtype: Rebel
 * Title: Midge, Marksman
 * Gemp ID: 701_045
 */
public class Card701_045 extends AbstractRebel {
    public Card701_045() {
        super(Side.LIGHT, 3, 3, 3, 2, 4, "\u2022Midge, Marksman", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Mountaineer. Recruited into General Crix Madine's commando unit as a field technician and earned the rank of corporal.");
        setGameText("May deploy as a 'react' to same site as your mountaineer (free if Janse). Permanent weapon is ••A280 Sharpshooter Rifle (may target a character or creature at same or adjacent site for free; draw destiny; target hit, and its forfeit = 0, if destiny +1 > defense value).");
        addPersona(Persona.MIDGE);
        addIcons(Icon.WARRIOR, Icon.PERMANENT_WEAPON, Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.MOUNTAINEER, Keyword.CORPORAL, Keyword.MALE);
    }

    @Override
    protected List<Modifier> getGameTextAlwaysOnModifiers(SwccgGame game, PhysicalCard self) {
        Filter sameLocationAsJanse = Filters.sameLocationAs(self, Filters.and(Filters.your(self), Filters.persona(Persona.JANSE)));
        Filter sameLocationAsYourMountaineer = Filters.sameLocationAs(self, Filters.and(Filters.your(self), Keyword.MOUNTAINEER));

        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Normal react (costs Force) - to same site as your mountaineer (but NOT Janse)
        modifiers.add(new MayDeployAsReactToLocationModifier(self, 
                Filters.and(Filters.site, sameLocationAsYourMountaineer, Filters.not(sameLocationAsJanse))));
        // Free react - to same site as Janse
        modifiers.add(new MayDeployAsReactToLocationForFreeModifier(self, 
                Filters.and(Filters.site, sameLocationAsJanse)));
        return modifiers;
    }

    // Define "A280 Sharpshooter Rifle" permanent weapon
    @Override
    protected AbstractPermanentWeapon getGameTextPermanentWeapon() {
        AbstractPermanentWeapon permanentWeapon = new AbstractPermanentWeapon("A280 Sharpshooter Rifle", Uniqueness.DIAMOND_2) {
            @Override
            public List<FireWeaponAction> getGameTextFireWeaponActions(String playerId, SwccgGame game, PhysicalCard self, boolean forFree, int extraForceRequired, PhysicalCard sourceCard, boolean repeatedFiring, Filter targetedAsCharacter, Float defenseValueAsCharacter, Filter fireAtTargetFilter, boolean ignorePerAttackOrBattleLimit) {
                FireWeaponActionBuilder actionBuilder = FireWeaponActionBuilder.startBuildPrep(playerId, game, sourceCard, self, this, forFree, extraForceRequired, repeatedFiring, targetedAsCharacter, defenseValueAsCharacter, fireAtTargetFilter, ignorePerAttackOrBattleLimit)
                        .targetAtSameOrAdjacentSiteForFree(Filters.or(Filters.character, Filters.creature, targetedAsCharacter), TargetingReason.TO_BE_HIT)
                        .finishBuildPrep();
                if (actionBuilder != null) {
                    // Build action using common utility
                    // Draw 1 destiny, add 1 to total, hit + forfeit = 0 if total > defense value
                    FireWeaponAction action = actionBuilder.buildFireWeaponWithHitAction(1, 1, Statistic.DEFENSE_VALUE, true, 0);
                    return Collections.singletonList(action);
                }
                return null;
            }
        };
        return permanentWeapon;
    }
}
