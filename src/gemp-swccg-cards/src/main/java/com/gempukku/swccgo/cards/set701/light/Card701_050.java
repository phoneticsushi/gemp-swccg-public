package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractEffect;
import com.gempukku.swccgo.cards.evaluators.StackedEvaluator;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.PlayCardOptionId;
import com.gempukku.swccgo.common.PlayCardZoneOption;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.StackCardsFromTableEffect;
import com.gempukku.swccgo.logic.modifiers.FerocityModifier;
import com.gempukku.swccgo.logic.modifiers.LandspeedModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.results.DefeatedResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Beezer Bowl 2025
 * Type: Effect
 * Title: Pile Of Bones
 *
 * REQUIRED ADDITIONS TO COMPILE:
 * Title.java:
 *   String Goraxs_Lair = "Mt Krana: Gorax's Lair";
 *   String Gorax = "Gorax";
 *   String The_Great_Devourer = "The Great Devourer";
 *
 * Persona.java:
 *   GORAX("Gorax"),
 *   THE_GREAT_DEVOURER("The Great Devourer"),
 *   // Add to getRelatedPersona(): if (equals(THE_GREAT_DEVOURER)) return GORAX;
 *
 * TESTING NOTES:
 * - Code checks defeatedCard.getZone().isInPlay() before stacking
 * - If cards are NOT in play when justDefeatedBy fires, the trigger won't stack anything
 * - In that case, may need to use isAboutToBeLost trigger instead to intercept before going to lost pile
 * - Cards stacked here are "out of play" per game text definition
 */
public class Card701_050 extends AbstractEffect {
    public Card701_050() {
        super(Side.LIGHT, 0f, PlayCardZoneOption.ATTACHED, Title.Pile_Of_Bones, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Place under Starting Effect. May not be placed in Reserve Deck. Deploys only on Gorax's Lair. Cards defeated by Gorax are stacked here face up and are out of play. Gorax is ferocity +1 for each card stacked here. The Great Devourer is landspeed +1 for each card stacked here.");
        addIcons(Icon.BEEZER_BOWL_2025);
        setMayNotBePlacedInReserveDeck(true);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.title(Title.Goraxs_Lair);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        // Gorax is ferocity +1 for each card stacked here
        modifiers.add(new FerocityModifier(self, Filters.title(Title.Gorax), null, new StackedEvaluator(self), false));
        // The Great Devourer is landspeed +1 for each card stacked here
        modifiers.add(new LandspeedModifier(self, Filters.title(Title.The_Great_Devourer), new StackedEvaluator(self)));
        return modifiers;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        // Cards defeated by Gorax are stacked here face up and are out of play
        if (TriggerConditions.justDefeatedBy(game, effectResult, Filters.any,
                Filters.or(Filters.title(Title.Gorax), Filters.title(Title.The_Great_Devourer)))) {

            DefeatedResult defeatedResult = (DefeatedResult) effectResult;
            PhysicalCard defeatedCard = defeatedResult.getCardDefeated();

            if (defeatedCard != null && defeatedCard.getZone().isInPlay()) {
                // Get the defeated card and all its attachments (while still on table)
                List<PhysicalCard> cardsToStack = new ArrayList<>();
                cardsToStack.add(defeatedCard);
                cardsToStack.addAll(game.getGameState().getAllAttachedRecursively(defeatedCard));

                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setText("Stack defeated card");
                action.setActionMsg("Stack " + GameUtils.getCardLink(defeatedCard) + " and attachments on " + GameUtils.getCardLink(self));
                // Stack the cards face up (cards stacked here are out of play per game text)
                action.appendEffect(
                        new StackCardsFromTableEffect(action, cardsToStack, self, false));
                return Collections.singletonList(action);
            }
        }
        return null;
    }
}
