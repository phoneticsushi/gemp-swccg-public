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
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.DoubleTotalBattleDestinyEffect;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
* Set: BEEZER_BOWL_2025
* Type: SORCERY_TEST
* Title: FIRESTORM
*/
public class Card701_005 extends AbstractSorceryTest {
    public Card701_005() {
        super(Side.DARK, 3, "FIRESTORM", ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploy on Bright Tree Village. Target a mentor on Endor and an apprentice who has completed Sorcery Test #2. Attempt when mentor and apprentice are present at the end of your move phase. Draw training destiny. If destiny + apprentice's power > 12, test completed and opponent loses 3 Force. Cast FIRESTORM: Double your total battle destiny here.");
        addIcon(Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.SORCERY_TEST_3);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        // Deploy on Bright Tree Village
        return Filters.Bright_Tree_Village;
    }

    // Target a mentor on Endor and an apprentice who has completed Sorcery Test #2
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
        return Filters.apprenticeTargetedBySorceryTest(Filters.and(Filters.completed_Sorcery_Test, Filters.Sorcery_Test_2));
    }

    @Override
    protected OptionalGameTextTriggerAction tryGetGameTextSorceryTestAttemptAction(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self) {
        // at the end of your move phase...
        if (TriggerConditions.isEndOfYourPhase(game, self, effectResult, Phase.MOVE)) {
            final PhysicalCard mentor = self.getTargetedCard(game.getGameState(), TargetId.SORCERY_TEST_MENTOR);
            final PhysicalCard apprentice = self.getTargetedCard(game.getGameState(), TargetId.SORCERY_TEST_APPRENTICE);

            // ...attempt when mentor and apprentice are present
            if (Filters.present(self).accepts(game, mentor) && Filters.present(self).accepts(game, apprentice)) {
                // Draw training destiny.  If destiny + apprentice's power >12, test completed and opponent loses 3 Force
                return getGameTextTrainingDestinyAttemptAction(self.getOwner(), game, self, apprentice, 12, 3);
            }
        }

        return null;
    }

    @Override
    public OptionalGameTextTriggerAction getGameTextSpellcastingAction(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self, PhysicalCard spellbook, PhysicalCard spellcaster, Filter spellcasterEffectivePresenceFilter) {
        // (assumption from context is you can only cast this during battle)
        if (GameConditions.isDuringBattleAt(game, spellcasterEffectivePresenceFilter)) {
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(spellbook, self.getCardId(), GameTextActionId.SPELLCASTING_ACTION);
            action.setText("Cast '" + self.getTitle() + "'");

            // Double your total battle destiny here
            action.appendEffect(new DoubleTotalBattleDestinyEffect(action, playerId));
            return action;
        }

        return null;
    }
}
