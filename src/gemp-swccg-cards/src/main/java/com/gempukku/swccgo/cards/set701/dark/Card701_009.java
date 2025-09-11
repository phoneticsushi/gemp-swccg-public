package com.gempukku.swccgo.cards.set701.dark;

import java.util.List;

import com.gempukku.swccgo.cards.AbstractSorceryTest;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.PlayCardOptionId;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.TargetId;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.AddUntilEndOfGameModifierEffect;
import com.gempukku.swccgo.logic.effects.DrawDestinyEffect;
import com.gempukku.swccgo.logic.effects.TargetCardOnTableEffect;
import com.gempukku.swccgo.logic.evaluators.ConstantEvaluator;
import com.gempukku.swccgo.logic.modifiers.DefenseValueModifier;
import com.gempukku.swccgo.logic.modifiers.FerocityModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotAttackChactersOwnedByPlayerModifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
* Set: BEEZER_BOWL_2025
* Type: SORCERY_TEST
* Title: Kroooo... CHA!
*/
public class Card701_009 extends AbstractSorceryTest {
    public Card701_009() {
        super(Side.DARK, 0, "Kroooo... CHA!", ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploy on any site with a creature present. Target a mentor and an apprentice who has completed at least one Sorcery Test. Attempt when mentor, apprentice, and creature are present at the start of your control phase. Draw training destiny. If destiny + apprentice's power > 9, test completed. Cast Kroooo… CHA!: Target a creature present with Spellcaster. Draw destiny. If destiny > ferocity, creature 'mutates' (creature is ferocity and defense value +3 and does not attack your characters).");
        addIcon(Icon.BEEZER_BOWL_2025);
        addKeyword(Keyword.SORCERY_TEST_0);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        // Deploy on any site with a creature present
        return Filters.sameSiteAs(self, Filters.creature);
    }

    // Target a mentor and an apprentice who has completed at least one Sorcery Test
    @Override
    protected boolean targetsMentor() {
        return true;
    }
    @Override
    protected Filter getGameTextAdditionalMentorFilter(String playerId, SwccgGame game, PhysicalCard self, PhysicalCard deployTarget) {
        return Filters.at(Filters.Endor_site);
    }
    @Override
    protected Filter getGameTextAdditionalApprenticeFilter(String playerId, SwccgGame game, PhysicalCard self, PhysicalCard deployTarget) {
        return Filters.apprenticeTargetedBySorceryTest(Filters.completed_Sorcery_Test);
    }

    @Override
    protected OptionalGameTextTriggerAction tryGetGameTextSorceryTestAttemptAction(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self) {
        // at the start of your control phase...
        if (TriggerConditions.isStartOfYourPhase(game, self, effectResult, Phase.CONTROL)) {
            final PhysicalCard mentor = self.getTargetedCard(game.getGameState(), TargetId.SORCERY_TEST_MENTOR);
            final PhysicalCard apprentice = self.getTargetedCard(game.getGameState(), TargetId.SORCERY_TEST_APPRENTICE);

            // ...attempt when mentor, apprentice, and creature are present
            if (
                Filters.present(self).accepts(game, mentor)
                && Filters.present(self).accepts(game, apprentice)
                && Filters.presentWith(self, Filters.creature).accepts(game, self)
            ) {
                // Draw training destiny. If destiny + apprentice's power > 9, test completed
                return getGameTextTrainingDestinyAttemptAction(self.getOwner(), game, self, apprentice, 9, 0);
            }
        }

        return null;
    }

    @Override
    public OptionalGameTextTriggerAction getGameTextSpellcastingAction(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self, PhysicalCard spellbook, PhysicalCard spellcaster, Filter spellcasterEffectivePresenceFilter) {
        final Filter creaturePresentWithSpellcasterFilter = Filters.and(Filters.creature, Filters.presentWith(spellcaster));

        // Target a creature present with Spellcaster
        if (GameConditions.canTarget(game, self, creaturePresentWithSpellcasterFilter)) {
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(spellbook, self.getCardId(), GameTextActionId.SPELLCASTING_ACTION);
            action.setText("Cast '" + self.getTitle() + "'");

            action.appendTargeting(
                new TargetCardOnTableEffect(action, playerId, "Target a creature present with Spellcaster", creaturePresentWithSpellcasterFilter) {
                    @Override
                    protected void cardTargeted(final int targetGroupId, PhysicalCard targetedCreature) {
                        action.addAnimationGroup(targetedCreature);
                        action.appendEffect(
                            // Draw destiny
                            new DrawDestinyEffect(action, playerId) {
                                @Override
                                protected void destinyDraws(SwccgGame game, List<PhysicalCard> destinyCardDraws, List<Float> destinyDrawValues, Float totalDestiny) {
                                    // If destiny > ferocity...
                                    if (totalDestiny > game.getModifiersQuerying().getFerocity(game.getGameState(), targetedCreature, null)) {
                                        // ...creature 'mutates'
                                        // (note that the ferocity/defense buffs are marked "cumulative" as this spell can be cast multiple times)
                                        // (each time a given creature mutates, it becomes harder to draw enough destiny to pass the check)
                                        action.setActionMsg("Result: " + GameUtils.getCardLink(targetedCreature) + " 'mutates'");
                                        action.appendEffect(
                                            // TODO: is this the correct modifier effect?  The card doesn't specify when the effect ends...
                                            // some comments suggest the modifier is removed if the target leaves the table,
                                            // but it's not obvious if "end of game" follows the card across Zones
                                            new AddUntilEndOfGameModifierEffect(
                                                action,
                                                new FerocityModifier(spellbook, targetedCreature, 3, true),
                                                GameUtils.getCardLink(targetedCreature) + " is ferocity +3"
                                            )
                                        );
                                        action.appendEffect(
                                            new AddUntilEndOfGameModifierEffect(
                                                action,
                                                new DefenseValueModifier(spellbook, targetedCreature, null, new ConstantEvaluator(3), true),
                                                GameUtils.getCardLink(targetedCreature) + " is defense +3"
                                            )
                                        );
                                        action.appendEffect(
                                            new AddUntilEndOfGameModifierEffect(
                                                action,
                                                new MayNotAttackChactersOwnedByPlayerModifier(spellbook, playerId),
                                                GameUtils.getCardLink(targetedCreature) + " may not attack characters owned by " + playerId
                                            )
                                        );
                                    } else {
                                        action.setActionMsg("Result: nothing happens");
                                    }
                                }
                            }
                        );
                    }
                }
            );

            return action;
        }

        return null;
    }
}
