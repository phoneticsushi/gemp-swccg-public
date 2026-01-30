package com.gempukku.swccgo.cards.set701.light;

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
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.CancelDestinyEffect;
import com.gempukku.swccgo.logic.effects.choose.TakeCardIntoHandFromReserveDeckEffect;
import com.gempukku.swccgo.logic.modifiers.HyperspeedModifier;
import com.gempukku.swccgo.logic.modifiers.ImmuneToAttritionLessThanModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Set 701 (Beezer Bowl 2025)
 * Type: Character
 * Subtype: Rebel
 * Title: Nik Sant
 * Gemp ID: 701_048
 */
public class Card701_048 extends AbstractRebel {
    public Card701_048() {
        super(Side.LIGHT, 1, 4, 3, 3, 6, "\u2022Nik Sant", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Former Lieutenant, now major and survival expert of the Rebel strike team. Nearly indistinguishable from a typical nav computer. Never dyes his beard. Mountaineer.");
        setGameText("When deployed, may [upload] one starfighter or vehicle. While aboard a starship, adds 2 to hyperspeed. During battle with a mountaineer, may cancel one opponent's just drawn weapon destiny. Immune to attrition < 4.");
        addPersona(Persona.NIK_SANT);
        addIcons(Icon.WARRIOR, Icon.NAV_COMPUTER, Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.MOUNTAINEER, Keyword.MAJOR);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new HyperspeedModifier(self, Filters.and(Filters.starship, Filters.hasAboard(self)), 2));
        modifiers.add(new ImmuneToAttritionLessThanModifier(self, 4));
        return modifiers;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        List<OptionalGameTextTriggerAction> actions = new LinkedList<OptionalGameTextTriggerAction>();

        GameTextActionId gameTextActionId = GameTextActionId.NIK_SANT__UPLOAD_STARFIGHTER_OR_VEHICLE;

        // Check condition(s) - When deployed, may upload a starfighter or vehicle
        if (TriggerConditions.justDeployed(game, effectResult, self)
                && GameConditions.canTakeCardsIntoHandFromReserveDeck(game, playerId, self, gameTextActionId)) {

            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Take starfighter or vehicle into hand from Reserve Deck");
            action.setActionMsg("Take a starfighter or vehicle into hand from Reserve Deck");
            // Perform result(s)
            action.appendEffect(
                    new TakeCardIntoHandFromReserveDeckEffect(action, playerId, Filters.or(Filters.starfighter, Filters.vehicle), true));
            actions.add(action);
        }

        String opponent = game.getOpponent(playerId);
        gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_2;

        // Check condition(s) - During battle with a mountaineer, may cancel opponent's just drawn weapon destiny
        if (TriggerConditions.isWeaponDestinyJustDrawnBy(game, effectResult, opponent, Filters.any)
                && GameConditions.isInBattleWith(game, self, Filters.and(Keyword.MOUNTAINEER))
                && GameConditions.canCancelDestiny(game, playerId)
                && GameConditions.isOncePerBattle(game, self, playerId, gameTextSourceCardId, gameTextActionId)) {

            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Cancel weapon destiny");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerBattleEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new CancelDestinyEffect(action));
            actions.add(action);
        }

        return actions;
    }
}
