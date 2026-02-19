package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractRebel;
import com.gempukku.swccgo.cards.AbstractPermanentWeapon;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.TargetingReason;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.FireWeaponAction;
import com.gempukku.swccgo.logic.actions.FireWeaponActionBuilder;
import com.gempukku.swccgo.logic.modifiers.MayNotHaveForfeitValueReducedModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotHavePowerReducedModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Set 701 (Beezer Bowl 2025)
 * Type: Character
 * Subtype: Rebel
 * Title: Corporal 'DELEVARY' Delevar
 */
public class Card701_030 extends AbstractRebel {
    public Card701_030() {
        super(Side.LIGHT, 2, 3, 3, 2, 5, "Corporal 'DELEVARY' Delevar", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Trained as a medic and a scout. Threw a smoke canister into the cockpit of an approaching AT-ST during the battle of Endor. Mountaineer.");
        setGameText("Rebels here may not have their power or forfeit reduced. Permanent weapon is •Smoke Canister (may 'throw' at character or vehicle by yelling \"DELEVARY!!!\"; for remainder of turn, character's power and weapon destiny draws are -1. OR vehicle's game text is canceled).");
        addPersona(Persona.DELEVAR);
        addIcons(Icon.WARRIOR, Icon.PERMANENT_WEAPON, Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.MOUNTAINEER, Keyword.MALE, Keyword.SCOUT, Keyword.CORPORAL);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        Filter rebelsHere = Filters.and(Filters.Rebel, Filters.here(self));

        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new MayNotHavePowerReducedModifier(self, rebelsHere, null));
        modifiers.add(new MayNotHaveForfeitValueReducedModifier(self, rebelsHere, null));
        return modifiers;
    }

    // Define "Smoke Canister" permanent weapon
    @Override
    protected AbstractPermanentWeapon getGameTextPermanentWeapon() {
        AbstractPermanentWeapon permanentWeapon = new AbstractPermanentWeapon(Persona.SMOKE_CANISTER) {
            @Override
            public List<FireWeaponAction> getGameTextFireWeaponActions(String playerId, SwccgGame game, PhysicalCard self, boolean forFree, int extraForceRequired, PhysicalCard sourceCard, boolean repeatedFiring, Filter targetedAsCharacter, Float defenseValueAsCharacter, Filter fireAtTargetFilter, boolean ignorePerAttackOrBattleLimit) {
                List<FireWeaponAction> actions = new LinkedList<FireWeaponAction>();

                // Action 1: Target a character (power -1 and weapon destiny draws -1 for remainder of turn)
                FireWeaponActionBuilder actionBuilder1 = FireWeaponActionBuilder.startBuildPrep(playerId, game, sourceCard, self, this, forFree, extraForceRequired, repeatedFiring, targetedAsCharacter, defenseValueAsCharacter, fireAtTargetFilter, ignorePerAttackOrBattleLimit)
                        .targetForFree(Filters.or(Filters.character, targetedAsCharacter), TargetingReason.OTHER)
                        .noWeaponDestinyNeeded()
                        .finishBuildPrep();
                if (actionBuilder1 != null) {
                    FireWeaponAction action1 = actionBuilder1.buildFireWeaponSmokeCanisterAction();
                    actions.add(action1);
                }

                // Action 2: Target a vehicle (cancel game text for remainder of turn)
                FireWeaponActionBuilder actionBuilder2 = FireWeaponActionBuilder.startBuildPrep(playerId, game, sourceCard, self, this, forFree, extraForceRequired, repeatedFiring, targetedAsCharacter, defenseValueAsCharacter, fireAtTargetFilter, ignorePerAttackOrBattleLimit)
                        .targetForFree(Filters.vehicle, TargetingReason.OTHER)
                        .noWeaponDestinyNeeded()
                        .finishBuildPrep();
                if (actionBuilder2 != null) {
                    FireWeaponAction action2 = actionBuilder2.buildFireWeaponCancelGameTextAction(true);
                    actions.add(action2);
                }

                return actions.isEmpty() ? null : actions;
            }
        };
        return permanentWeapon;
    }
}
