package com.gempukku.swccgo.cards.set701.light;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractNormalEffect;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.PlayCardZoneOption;
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
import com.gempukku.swccgo.logic.effects.AttachCardFromTableEffect;
import com.gempukku.swccgo.logic.modifiers.MayNotBeChokedModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.PassthruEffect;
import com.gempukku.swccgo.logic.timing.results.AboutToLeaveTableResult;

/**
* Set: BEEZER_BOWL_2025
* Type: EFFECT
* Title: Scrambled Transmission
*/
public class Card701_051 extends AbstractNormalEffect {
    public Card701_051() {
        // Deploy on table
        super(Side.LIGHT, 4, PlayCardZoneOption.YOUR_SIDE_OF_TABLE, Title.Scrambled_Transmission, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploy on table. If present with Han, may relocate Scrambled Transmission to him. Rebels here may not be 'choked.' If about to leave table (for any reason, even if inactive), relocate to Mount Krana: Apex. (Immune to Alter.)");
        addIcons(Icon.BEEZER_BOWL_2025);
        // Immune to Alter
        addImmuneToCardTitle(Title.Alter);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new ArrayList<Modifier>();
        // Rebels here may not be 'choked.'
        modifiers.add(new MayNotBeChokedModifier(self, Filters.and(Filters.Rebel, Filters.here(self))));
        return modifiers;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggersWhenInactiveInPlay(SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        return getRequiredRelocateToApexAction(game, effectResult, self, gameTextSourceCardId);
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(final SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        return getRequiredRelocateToApexAction(game, effectResult, self, gameTextSourceCardId);
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();

        // Han is a Persona filter, so there can be at most one...
        final PhysicalCard han = Filters.findFirstActive(game, self, Filters.Han);

        // If present with Han...
        if (
            han != null
            // (...and not already attached, for player's sanity's sake...)
            && !GameConditions.isAttachedTo(game, self, Filters.Han)  // isAttachedTo takes Filter, not Filterable...
            && GameConditions.isPresentWith(game, self, han)
        ) {
            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId);

            // ...may relocate Scrambled Transmission to him
            action.appendEffect(new AttachCardFromTableEffect(action, self, han));
            actions.add(action);
        }

        return actions;
    }

    private List<RequiredGameTextTriggerAction> getRequiredRelocateToApexAction(final SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        List<RequiredGameTextTriggerAction> actions = new ArrayList<RequiredGameTextTriggerAction>();

        // If about to leave table (for any reason, even if inactive), relocate to Mount Krana: Apex
        if (TriggerConditions.isAboutToLeaveTable(game, effectResult, self)) {
            final PhysicalCard mountKranaApex = Filters.findFirstActive(game, self, Filters.Apex);
            if (mountKranaApex != null) {
                final AboutToLeaveTableResult result = (AboutToLeaveTableResult) effectResult;
                final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);

                action.setText("Relocate to Mount Krana: Apex");
                action.setActionMsg("Relocate " + GameUtils.getCardLink(self) + " to Mount Krana: Apex");
                // Prevent the original leave-table effect
                action.appendEffect(
                        new PassthruEffect(action) {
                            @Override
                            protected void doPlayEffect(SwccgGame game) {
                                result.getPreventableCardEffect().preventEffectOnCard(self);
                                for (PhysicalCard attachedCards : game.getGameState().getAllAttachedRecursively(self)) {
                                    result.getPreventableCardEffect().preventEffectOnCard(attachedCards);
                                }
                            }
                        });
                // FIX: Relocate to Apex by attaching to the Apex location (instead of placing out of play,
                // which was causing an infinite loop by re-triggering "about to leave table")
                action.appendEffect(
                        new AttachCardFromTableEffect(action, self, mountKranaApex));
                actions.add(action);
            } else {
                game.getGameState().sendMessage("Unable to find Mount Krana: Apex; can't relocate " + GameUtils.getCardLink(self));
            }
        }

        return actions;
    }
}
