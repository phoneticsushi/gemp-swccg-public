package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collections;
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
import com.gempukku.swccgo.common.TargetingReason;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.ExcludeFromBattleEffect;
import com.gempukku.swccgo.logic.effects.MayNotBattleUntilEndOfTurnEffect;
import com.gempukku.swccgo.logic.effects.TargetCardOnTableEffect;
import com.gempukku.swccgo.logic.effects.choose.DeployCardFromOutsideTheGameEffect;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
* Set: BEEZER_BOWL_2025
* Type: SORCERY_TEST
* Title: Teo... SHA!!!
*/
public class Card701_023 extends AbstractSorceryTest {
    public Card701_023() {
        super(Side.DARK, 1, "Teo... SHA!!!", ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploy on your sorcerer. Target a mentor and apprentice here. Deploy Spellbook on apprentice from outside your deck. Attempt when targets are present at the start of your control phase. Draw training destiny. If destiny + apprentice's power > 10, test completed and opponent loses 1 Force. Cast Teo... SHA!!!: During battle, target opponent's character of ability < 4 present. Target is 'surrounded by fire' (excluded from battle for remainder of turn). Mentor: Your sorcerer with ability > 3. Apprentice: Your Ewok shaman with lessor ability than mentor. Each time you complete a Sorcery Test, you may exchange one card in hand for one Sorcery Test in Lost Pile.");
        addIcon(Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.SORCERY_TEST_1);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        // Deploy on your sorcerer
        return Filters.and(Filters.your(self), Filters.sorcerer);
    }

    // Target a mentor and apprentice here
    @Override
    protected boolean targetsMentor() {
        return true;
    }
    @Override
    protected Filter getGameTextAdditionalMentorFilter(String playerId, SwccgGame game, PhysicalCard self, PhysicalCard deployTarget) {
        return Filters.here(deployTarget);
    }
    @Override
    protected Filter getGameTextAdditionalApprenticeFilter(String playerId, SwccgGame game, PhysicalCard self, PhysicalCard deployTarget) {
        return Filters.here(deployTarget);
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        // Note: getGameTextRequiredAfterTriggers isn't called by AbstractSorceryTest so we can singleton it up here...

        if (TriggerConditions.justDeployed(game, effectResult, self)) {
            // Deploy Spellbook on apprentice from outside your deck
            final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.appendEffect(
                new DeployCardFromOutsideTheGameEffect(action, Filters.Spellbook,
                    Filters.sameCardId(self.getTargetedCard(game.getGameState(), TargetId.SORCERY_TEST_APPRENTICE)), 0)
            );
            return Collections.singletonList(action);
        }

        return null;
    }

    @Override
    protected OptionalGameTextTriggerAction tryGetGameTextSorceryTestAttemptAction(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self) {
        // at the start of your control phase...
        if (TriggerConditions.isStartOfYourPhase(game, self, effectResult, Phase.CONTROL)) {
            final PhysicalCard mentor = self.getTargetedCard(game.getGameState(), TargetId.SORCERY_TEST_MENTOR);
            final PhysicalCard apprentice = self.getTargetedCard(game.getGameState(), TargetId.SORCERY_TEST_APPRENTICE);

            // ...attempt when targets are present
            // Technically, this implements "present together with each other", not "present with the Sorcery Test".
            // but it's unclear how to check for the latter as the Sorcery Test doesn't match when checking "presence".
            // In practice, this should be no issue, as it's not possible to re-attach the test without deploying it again.
            //
            // Note that mentor and apprentice should never be null here, but might be in tests e.g.
            // and it's better to not crash in that case regardless.
            //
            // FIXME: figure out how to check presence against the Sorcery Test
            if (mentor != null && apprentice != null && GameConditions.isPresentWith(game, mentor, apprentice)) {
                // Draw training destiny.  If destiny + apprentice's power > 10, test completed and opponent loses 1 Force
                return getGameTextTrainingDestinyAttemptAction(self.getOwner(), game, self, apprentice, 10, 1);
            }
        }

        return null;
    }

    @Override
    public OptionalGameTextTriggerAction getGameTextSpellcastingAction(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self, PhysicalCard spellbook, PhysicalCard spellcaster, Filter spellcasterEffectivePresenceFilter) {
        // During battle...
        // (Note that this doesn't say "during battle HERE; see note on spellcasterEffectivePresenceFilter"...)
        if (GameConditions.isDuringBattle(game)) {
            final Filter targetingFilter = Filters.and(
                Filters.opponents(playerId),
                Filters.character,
                Filters.abilityLessThan(4),
                Filters.presentAt(spellcasterEffectivePresenceFilter)
            );
            final TargetingReason targetingReason = TargetingReason.TO_BE_EXCLUDED_FROM_BATTLE;

            if (GameConditions.canTarget(game, self, targetingReason, targetingFilter)) {
                final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(spellbook, self.getCardId(), GameTextActionId.SPELLCASTING_ACTION);
                action.setText("Cast '" + self.getTitle() + "'");

                // Choose target(s)
                action.appendTargeting(
                    // ...target opponent's character of ability < 4 present
                    new TargetCardOnTableEffect(action, playerId, "Choose character to exclude from battle for remainder of turn", targetingReason, targetingFilter) {
                        @Override
                        protected void cardTargeted(final int targetGroupId, PhysicalCard targetedCharacter) {
                            // Target is 'surrounded by fire' (excluded from battle for remainder of turn)
                            action.addAnimationGroup(self, targetedCharacter);
                            action.setActionMsg(GameUtils.getCardLink(targetedCharacter) + "is 'surrounded by fire'");
                            // Perform result(s)
                            action.appendEffect(new ExcludeFromBattleEffect(action, targetedCharacter));
                            action.appendEffect(new MayNotBattleUntilEndOfTurnEffect(action, targetedCharacter));
                        }
                    }
                );
                return action;
            }
        }

        return null;
    }
}
