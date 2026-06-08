package com.gempukku.swccgo.cards.set701.light;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractPermanentWeapon;
import com.gempukku.swccgo.cards.AbstractRebel;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerGameEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.FireWeaponAction;
import com.gempukku.swccgo.logic.actions.FireWeaponActionBuilder;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.choose.StealCardAndAttachFromLostPileEffect;
import com.gempukku.swccgo.logic.modifiers.MayDeployToTargetModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;

/**
* Set: BEEZER_BOWL_2025
* Type: CHARACTER_REBEL
* Title: Sergeant Junkin
*/
public class Card701_052 extends AbstractRebel {
    public Card701_052() {
        super(Side.LIGHT, 2, 3, 3, 3, 5, Title.Sergeant_Junkin, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Any stolen blaster may deploy on Junkin. Once per game, Junkin may steal a blaster from opponent's Lost Pile. Permanent weapon is Concussion Grenade (may 'throw' at same or adjacent site; draw destiny; all characters present with that destiny number are 'hit').");
        addIcons(Icon.BEEZER_BOWL_2025, Icon.PERMANENT_WEAPON, Icon.WARRIOR, Icon.WARRIOR);
        addKeywords(Keyword.MALE, Keyword.MOUNTAINEER);
        addPersonas(Persona.JUNKIN);
    }

    // Any stolen blaster may deploy on Junkin
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new ArrayList<>();
        modifiers.add(new MayDeployToTargetModifier(self, Filters.and(Filters.stolen, Filters.blaster), self));
        return modifiers;
    }

    // Once per game, Junkin may steal a blaster from opponent's Lost Pile
    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.SERGEANT_JUNKIN__STEAL_A_BLASTER;
        if (
            GameConditions.isOncePerGame(game, self, gameTextActionId)
            && GameConditions.hasLostPile(game, game.getOpponent(playerId))
            && GameConditions.canSearchOpponentsLostPile(game, playerId, self, gameTextActionId)
        ) {
            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Steal a blaster from opponent's Lost Pile");
            action.setActionMsg("Steal a blaster from opponent's Lost Pile and attach to Junkin");
            action.appendUsage(
                    new OncePerGameEffect(action)
            );
            // Perform result(s)
            action.appendEffect(
                    new StealCardAndAttachFromLostPileEffect(action, playerId, self, Filters.blaster));
            return Collections.singletonList(action);
        }

        return null;
    }

    // Permanent weapon is Concussion Grenade
    @Override
    protected AbstractPermanentWeapon getGameTextPermanentWeapon() {
        AbstractPermanentWeapon permanentWeapon = new AbstractPermanentWeapon("Concussion Grenade") {
            @Override
            public List<FireWeaponAction> getGameTextFireWeaponActions(String playerId, SwccgGame game, PhysicalCard self, boolean forFree, int extraForceRequired, PhysicalCard sourceCard, boolean repeatedFiring, Filter targetedAsCharacter, Float defenseValueAsCharacter, Filter fireAtTargetFilter, boolean ignorePerAttackOrBattleLimit) {
                FireWeaponActionBuilder actionBuilder = FireWeaponActionBuilder.startBuildPrep(playerId, game, sourceCard, self, this, forFree, extraForceRequired, repeatedFiring, targetedAsCharacter, defenseValueAsCharacter, fireAtTargetFilter, ignorePerAttackOrBattleLimit)
                        .firesWithoutTargetingAtSameOrAdjacentSite().finishBuildPrep();
                if (actionBuilder != null) {
                    // Build action using common utility
                    FireWeaponAction action = actionBuilder.buildFirePermamentWeaponJunkinConcussionGrenadeAction();
                    return Collections.singletonList(action);
                }
                return null;
            }
        };
        return permanentWeapon;
    }
}
