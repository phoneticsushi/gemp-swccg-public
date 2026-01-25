package com.gempukku.swccgo.cards.set701.light;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractRebel;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerBattleEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.CancelDestinyAndCauseRedrawEffect;
import com.gempukku.swccgo.logic.modifiers.FireWeaponCostModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
* Set: BEEZER_BOWL_2025
* Type: CHARACTER_REBEL
* Title: Sergeant Squalls
*/
public class Card701_053 extends AbstractRebel {
    public Card701_053() {
        super(Side.LIGHT, 3, 3, 3, 2, 4, Title.Sergeant_Squalls, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("At same site, opponent must first use 1 Force to fire a weapon (or 2 force if a [Permanent Weapon] ). Once during battle, if present with Junkin, may cancel and redraw your just drawn weapon destiny.");
        addIcons(Icon.BEEZER_BOWL_2025, Icon.WARRIOR, Icon.WARRIOR);
        addKeywords(Keyword.MALE, Keyword.MOUNTAINEER);
        addPersonas(Persona.SQUALLS);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // At same site, opponent must first use 1 Force to fire a weapon...
        modifiers.add(new FireWeaponCostModifier(self, Filters.and(Filters.atSameSite(self), Filters.opponents(self), Filters.weapon), 1));
        // ...or 2 force if a [Permanent Weapon]
        modifiers.add(new FireWeaponCostModifier(self, Filters.and(Filters.atSameSite(self), Filters.opponents(self), Filters.hasPermanentWeapon), 2));
        return modifiers;
    }

    // Once during battle, if present with Junkin, may cancel and redraw your just drawn weapon destiny.
    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.ANY_CARD__CANCEL_AND_REDRAW_A_DESTINY;

        // Check condition(s)
        if (TriggerConditions.isWeaponDestinyJustDrawnBy(game, effectResult, playerId)
                && GameConditions.isInBattle(game, self)
                && GameConditions.isPresentWith(game, self, Filters.Junkin)
                && GameConditions.isOncePerBattle(game, self, playerId, gameTextSourceCardId, gameTextActionId)
                && GameConditions.canCancelDestinyAndCauseRedraw(game, playerId)) {
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Cancel and re-draw weapon destiny");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerBattleEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new CancelDestinyAndCauseRedrawEffect(action));
            return Collections.singletonList(action);
        }
        return null;
    }
}
