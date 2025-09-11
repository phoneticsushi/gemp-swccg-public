package com.gempukku.swccgo.cards.set701.dark;

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
import com.gempukku.swccgo.logic.effects.AddUntilStartOfPlayersNextTurnModifierEffect;
import com.gempukku.swccgo.logic.effects.TargetCardOnTableEffect;
import com.gempukku.swccgo.logic.modifiers.MayNotForceDrainAtLocationModifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
* Set: BEEZER_BOWL_2025
* Type: SORCERY_TEST
* Title: Oomba! Doomba!! Boomba!!!
*/
public class Card701_014 extends AbstractSorceryTest {
    public Card701_014() {
        super(Side.DARK, 2, "Oomba! Doomba!! Boomba!!!", ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploy on a Mt. Thunderstone site. Target a mentor on Endor and an apprentice who has completed Sorcery Test #1. Attempt when apprentice is present at the start of your control phase. Draw training destiny. If destiny + apprentice's power > 11, test completed and opponent loses 2 force. Cast Oomba! Doomba!! Boomba!!!: At the beginning of your turn, target one battleground location you control and one battleground location opponent controls. Until the beginning of your next turn, players may not force drain at either location.");
        addIcon(Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.SORCERY_TEST_2);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        // Deploy on a Mt. Thunderstone site
        return Filters.Mt_Thunderstone_site;
    }

    // Target a mentor on Endor and an apprentice who has completed Sorcery Test #1
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
        return Filters.apprenticeTargetedBySorceryTest(Filters.and(Filters.completed_Sorcery_Test, Filters.Sorcery_Test_1));
    }

    @Override
    protected OptionalGameTextTriggerAction tryGetGameTextSorceryTestAttemptAction(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self) {
        // at the start of your control phase...
        if (TriggerConditions.isStartOfYourPhase(game, self, effectResult, Phase.CONTROL)) {
            final PhysicalCard apprentice = self.getTargetedCard(game.getGameState(), TargetId.SORCERY_TEST_APPRENTICE);

            // ...attempt when apprentice is present
            if (Filters.present(self).accepts(game, apprentice)) {
                // Draw training destiny.  If destiny + apprentice's power > 11, test completed and opponent loses 2 Force
                return getGameTextTrainingDestinyAttemptAction(self.getOwner(), game, self, apprentice, 11, 2);
            }
        }

        return null;
    }

    @Override
    public OptionalGameTextTriggerAction getGameTextSpellcastingAction(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self, PhysicalCard spellbook, PhysicalCard spellcaster, Filter spellcasterEffectivePresenceFilter) {
        final String opponent = game.getOpponent(playerId);
        final Filter yourBattlegroundFilter = Filters.and(Filters.battleground, Filters.controls(playerId));
        final Filter opponentsBattlegroundFilter = Filters.and(Filters.battleground, Filters.controls(opponent));

        // At the beginning of your turn...
        if (TriggerConditions.isStartOfYourTurn(game, effectResult, self)
            && GameConditions.controls(game, playerId, yourBattlegroundFilter)
            && GameConditions.controls(game, opponent, opponentsBattlegroundFilter)
        ) {
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(spellbook, self.getCardId(), GameTextActionId.SPELLCASTING_ACTION);
            action.setText("Cast '" + self.getTitle() + "'");

            // ...target one battleground location you control...
            action.appendTargeting(
                new TargetCardOnTableEffect(action, playerId, "Target one battleground location you control", yourBattlegroundFilter) {
                    @Override
                    protected void cardTargeted(final int targetGroupId, PhysicalCard yourBattleground) {
                        // ...and one battleground location opponent controls
                        action.appendTargeting(
                            new TargetCardOnTableEffect(action, playerId, "Target one battleground location opponent control", opponentsBattlegroundFilter) {
                                @Override
                                protected void cardTargeted(final int targetGroupId, PhysicalCard opponentsBattleground) {
                                    action.addAnimationGroup(yourBattleground, opponentsBattleground);
                                    action.appendEffect(
                                        // Until the beginning of your next turn, players may not force drain at either location
                                        new AddUntilStartOfPlayersNextTurnModifierEffect(
                                            action,
                                            playerId,
                                            new MayNotForceDrainAtLocationModifier(self, Filters.or(yourBattleground, opponentsBattleground)),
                                            "May not force drain at" + GameUtils.getCardLink(yourBattleground)
                                                + " or " + GameUtils.getCardLink(opponentsBattleground)
                                        )
                                    );
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
