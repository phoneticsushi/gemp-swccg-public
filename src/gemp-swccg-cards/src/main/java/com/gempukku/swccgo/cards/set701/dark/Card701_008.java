package com.gempukku.swccgo.cards.set701.dark;

import com.gempukku.swccgo.cards.AbstractSorceryTest;
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
import com.gempukku.swccgo.logic.effects.AttachCardFromTableEffect;
import com.gempukku.swccgo.logic.effects.CompleteSorceryTestEffect;
import com.gempukku.swccgo.logic.effects.LoseForceEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromTableEffect;
import com.gempukku.swccgo.logic.effects.PlaceTopCardOfLostPileOutOfPlayEffect;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
* Set: BEEZER_BOWL_2025
* Type: SORCERY_TEST
* Title: Kiss Of Death
*/
public class Card701_008 extends AbstractSorceryTest {
    public Card701_008() {
        super(Side.DARK, 4, "Kiss Of Death", ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploy on Dark Tree Village if Morag alone at an Endor site. Target an apprentice who has completed Sorcery Test #3. Attempt when Morag and apprentice are present: Initiate 'Duel of the Fates' (compare power totals). Relocate Spellbook to character with greater power total; place other out of play and opponent loses 4 force. Cast Kiss Of Death: At the end of opponent's battle phase, 'blow a kiss' (opponent must place top card of Lost Pile out of play).");
        addIcon(Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.SORCERY_TEST_4);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        // Deploy on Dark Tree Village...
        return Filters.Dark_Tree_Village;
    }
    @Override
    protected boolean checkGameTextDeployRequirements(String playerId, SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        // ...if Morag alone at an Endor site
        return Filters.canSpotFromAllOnTable(
            game,
            Filters.and(
                Filters.Morag,
                Filters.alone,
                Filters.at(Filters.Endor_site)
            )
        );
    }

    // Target an apprentice who has completed Sorcery Test #3
    @Override
    protected boolean targetsMentor() {
        return false;
    }
    @Override
    protected Filter getGameTextAdditionalMentorFilter(String playerId, SwccgGame game, PhysicalCard self, PhysicalCard deployTarget) {
        return Filters.none;  // does not apply
    }
    @Override
    protected Filter getGameTextAdditionalApprenticeFilter(String playerId, SwccgGame game, PhysicalCard self, PhysicalCard deployTarget) {
        return Filters.apprenticeTargetedBySorceryTest(Filters.and(Filters.completed_Sorcery_Test, Filters.Sorcery_Test_3));
    }

    @Override
    protected OptionalGameTextTriggerAction tryGetGameTextSorceryTestAttemptAction(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self) {
        final PhysicalCard morag = Filters.findFirstActive(game, self, Filters.Morag);  // Persona filter, so there can be only one
        final PhysicalCard apprentice = self.getTargetedCard(game.getGameState(), TargetId.SORCERY_TEST_APPRENTICE);

        if (morag == null || apprentice == null) {
            // not active -> not present
            return null;
        }

        // Attempt when Morag and apprentice are present
        if (Filters.present(self).accepts(game, morag) && Filters.present(self).accepts(game, apprentice)) {
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, self.getCardId(), GameTextActionId.SORCERY_TEST__ATTEMPT_TEST);
            action.setText("Initiate 'Duel of the Fates'");
            action.addAnimationGroup(morag, apprentice);

            // (compare power totals)
            final float apprenticePower = game.getModifiersQuerying().getPower(game.getGameState(), apprentice);
            final float moragPower = game.getModifiersQuerying().getPower(game.getGameState(), morag);

            final int comparison = Float.compare(apprenticePower, moragPower);
            final PhysicalCard winner;
            final PhysicalCard loser;
            if (comparison < 0) {
                winner = morag;
                loser = apprentice;
            } else if (comparison > 0) {
                winner = apprentice;
                loser = morag;
            } else {
                // Duel is a tie, so nothing happens
                action.setActionMsg("Duel of the Fates: ended in a tie");
                return action;
            }

            action.setActionMsg("Duel of the Fates: " + GameUtils.getFullName(winner) + " is victorious!");

            final PhysicalCard spellbook = Filters.findFirstActive(game, self, Filters.Spellbook);
            if (spellbook != null) {
                // Relocate Spellbook to character with greater power total
                action.appendEffect(new AttachCardFromTableEffect(action, spellbook, winner));
            }

            // place other out of play
            action.appendEffect(new PlaceCardOutOfPlayFromTableEffect(action, loser));

            // opponent loses 4 force
            action.appendEffect(new LoseForceEffect(action, game.getOpponent(playerId), 4));

            // Regardless of who wins, this test is 'completed'
            action.appendEffect(new CompleteSorceryTestEffect(action, self));

            return action;
        }

        return null;
    }

    @Override
    public OptionalGameTextTriggerAction getGameTextSpellcastingAction(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self, PhysicalCard spellbook, PhysicalCard spellcaster, Filter spellcasterEffectivePresenceFilter) {
        // At the end of opponent's battle phase...
        if (TriggerConditions.isEndOfOpponentsPhase(game, self, effectResult, Phase.BATTLE)) {
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(spellbook, self.getCardId(), GameTextActionId.SPELLCASTING_ACTION);
            action.setText("'blow a kiss' (Cast '" + self.getTitle() + "')");

            // ...'blow a kiss' (opponent must place top card of Lost Pile out of play).
            action.appendEffect(new PlaceTopCardOfLostPileOutOfPlayEffect(action, playerId));
            return action;
        }

        return null;
    }
}
