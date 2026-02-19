package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractGeneralsOrder;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.MoveCardAsRegularMoveEffect;
import com.gempukku.swccgo.logic.effects.choose.ChooseCardOnTableEffect;
import com.gempukku.swccgo.logic.modifiers.ImmuneToAttritionLessThanModifier;
import com.gempukku.swccgo.logic.modifiers.ImmunityToAttritionChangeModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.PowerModifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Set: Beezer Bowl 2025
 * Type: General's Order
 * Title: Ground Control
 */
public class Card701_039 extends AbstractGeneralsOrder {
    public Card701_039() {
        super(Side.LIGHT, 6, "Ground Control", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Generals, majors, sergeants, and corporals are power +1 and immune to attrition < 4 (or adds 2 to immunity if character already has immunity). If opponent just initiated battle at same site as your general, may 'radio in' one major, sergeant, or corporal (make a regular move to that site).");
        addIcons(Icon.BEEZER_BOWL_2025);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        // Filter for generals, majors, sergeants, and corporals (applies to both sides)
        Filter affectedCharacters = Filters.or(Keyword.GENERAL, Keyword.MAJOR, Keyword.SERGEANT, Keyword.CORPORAL);
        Filter alreadyHasImmunity = Filters.alreadyHasImmunityToAttrition(self);

        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Power +1
        modifiers.add(new PowerModifier(self, affectedCharacters, 1));
        // Immune to attrition < 4 (if no existing immunity)
        modifiers.add(new ImmuneToAttritionLessThanModifier(self, Filters.and(affectedCharacters, Filters.not(alreadyHasImmunity)), 4));
        // Adds 2 to immunity if character already has immunity
        modifiers.add(new ImmunityToAttritionChangeModifier(self, Filters.and(affectedCharacters, alreadyHasImmunity), 2));
        return modifiers;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        String opponent = game.getOpponent(playerId);

        // If opponent just initiated battle at same site as your general
        if (TriggerConditions.battleInitiated(game, effectResult, opponent)) {
            PhysicalCard battleLocation = game.getGameState().getBattleLocation();
            
            // Check if battle is at a site where player has a general
            if (battleLocation != null 
                    && Filters.site.accepts(game, battleLocation)
                    && GameConditions.canSpot(game, self, Filters.and(Filters.your(self), Keyword.GENERAL, Filters.at(battleLocation)))) {

                // Filter for majors, sergeants, or corporals that can make regular move to that site
                Filter radioInTargets = Filters.and(
                        Filters.your(self),
                        Filters.or(Keyword.MAJOR, Keyword.SERGEANT, Keyword.CORPORAL),
                        Filters.not(Filters.at(battleLocation)),
                        Filters.movableAsRegularMove(playerId, false, 0, false, Filters.sameCardId(battleLocation))
                );

                if (GameConditions.canSpot(game, self, radioInTargets)) {
                    final PhysicalCard locationToMoveTo = battleLocation;

                    final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
                    action.setText("'Radio in' a character");
                    action.setActionMsg("'Radio in' a major, sergeant, or corporal to " + battleLocation.getTitle());
                    // Choose a character to 'radio in'
                    action.appendEffect(
                            new ChooseCardOnTableEffect(action, playerId, "Choose a major, sergeant, or corporal to 'radio in'", radioInTargets) {
                                @Override
                                protected void cardSelected(PhysicalCard selectedCard) {
                                    // Make a regular move to that site
                                    action.appendEffect(
                                            new MoveCardAsRegularMoveEffect(action, playerId, selectedCard, false, false, locationToMoveTo));
                                }
                            });
                    return Collections.singletonList(action);
                }
            }
        }
        return null;
    }
}
