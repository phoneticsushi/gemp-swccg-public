package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractObjective;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerPhaseEffect;
import com.gempukku.swccgo.cards.effects.usage.OncePerTurnEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.TargetingReason;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.CancelCardActionBuilder;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.AttachCardFromTableEffect;
import com.gempukku.swccgo.logic.effects.CurseAssignmentEffect;
import com.gempukku.swccgo.logic.effects.LoseForceEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromTableEffect;
import com.gempukku.swccgo.logic.effects.RespondableEffect;
import com.gempukku.swccgo.logic.effects.TargetCardOnTableEffect;
import com.gempukku.swccgo.logic.modifiers.CancelsGameTextModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.Effect;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
* Set: BEEZER_BOWL_2025
* Type: OBJECTIVE
* Title: Endor Will Bow TO ME!
*/
public class Card701_013_BACK extends AbstractObjective {
    public Card701_013_BACK() {
        super(Side.DARK, 7, Title.Endor_Will_Bow_To_Me, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("While this side up, Houjix is canceled. During your control phase, for each Endor battleground site your sorcerer occupies, opponent loses 1 Force (2 if controlled). Once per turn, may relocate a Sorcery Test stacked on Spellbook that has not been cast this turn to an opponent's character, vehicle, or weapon at any location. Target is 'cursed' (gametext is canceled). At any time, opponent may lose 2 Force to 'break the curse' (relocate a Sorcery Test from their card to Spellbook). Place out of play and lose 5 Force if Spellcaster leaves table or opponent controls Bright Tree Village.");
        addIcons(Icon.BEEZER_BOWL_2025);
    }
    
    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredBeforeTriggers(SwccgGame game, Effect effect, PhysicalCard self, int gameTextSourceCardId) {
        // While this side up, Houjix is canceled
        if (TriggerConditions.isPlayingCard(game, effect, Filters.Houjix)
                && GameConditions.canCancelCardBeingPlayed(game, self, effect)) {

            RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            CancelCardActionBuilder.buildCancelCardBeingPlayedAction(action, effect);
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, final EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        final String playerId = self.getOwner();
        final String opponent = game.getOpponent(playerId);

        List<RequiredGameTextTriggerAction> actions = new LinkedList<RequiredGameTextTriggerAction>();

        // During your control phase, for each Endor battleground site your sorcerer occupies, opponent loses 1 Force (2 if controlled)
        GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_DEFAULT;
        if (TriggerConditions.isEndOfYourPhase(game, self, effectResult, Phase.CONTROL)
                && GameConditions.isOnceDuringYourPhase(game, self, playerId, gameTextSourceCardId, gameTextActionId, Phase.CONTROL)) {

            Filter yourSorcerorFilter = Filters.and(Filters.your(playerId), Filters.sorcerer);
            Filter relevantSiteFilter = Filters.and(Filters.Endor_location, Filters.battleground_site, Filters.wherePresent(self, yourSorcerorFilter));
            
            // if your sorcerer is present, the site is occupied by definition:
            int numSitesOccupied = Filters.countActive(game, self, relevantSiteFilter);
            // "controlled" sites are by definition "occupied", so this will be a subset of the above:
            int numSitesControlled = Filters.countActive(game, self, Filters.and(relevantSiteFilter, Filters.controls(playerId)));
            // Intentionally count duplicates:
            int numForceToLose = numSitesOccupied + numSitesControlled;

            if (numForceToLose > 0) {
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("Make opponent lose " + numForceToLose + " Force");
                // Update usage limit(s)
                action.appendUsage(
                        new OncePerPhaseEffect(action));
                // Perform result(s)
                action.appendEffect(
                        new LoseForceEffect(action, opponent, numForceToLose));
                actions.add(action);
            }
        }

        // If Spellcaster leaves table or opponent controls Bright Tree Village, place out of play and lose 5 Force
        // FIXME: does the "Spellcaster" filter still work in this case, or is the Spellbook no longer attached by the time this fires so the filter has no effect?
        if (TriggerConditions.leavesTable(game, effectResult, Filters.Spellcaster)
                || GameConditions.controls(game, opponent, Filters.Bright_Tree_Village)) {
            
            RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Place out of play and lose 5 Force");
            action.setActionMsg("Place " + GameUtils.getCardLink(self) + " out of play");
            // Perform result(s)
            action.appendEffect(
                    new PlaceCardOutOfPlayFromTableEffect(action, self));
            action.appendEffect(
                    new LoseForceEffect(action, playerId, 5));
            actions.add(action);
        }

        return actions;
    }

    // Once per turn, may relocate a Sorcery Test stacked on Spellbook that has not been cast this turn to an opponent's character, vehicle, or weapon at any location
    // Target is 'cursed'
    protected List<TopLevelGameTextAction> getTopLevelGameTextActions(final String playerId, SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        final GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_1;
        
        final TargetingReason curseCandidateReason = TargetingReason.OTHER;
        final TargetingReason curseRecipientReason = TargetingReason.TO_BE_CURSED;

        // Note: a "spell" is by definition attached to the Spellbook
        final Filter curseCandidateFilter = Filters.spell_not_cast_this_turn;
        // Note: assumption is these types of cards must be at a location, if they're on the table,
        // and that the intent of the text is to include weapons attached to characters at those locations
        // even though this is not always what being "at" a location means.
        // See comments on Filters.atLocation()
        final Filter curseRecipientFilter = Filters.and(Filters.opponents(playerId), Filters.or(Filters.character, Filters.vehicle, Filters.weapon));
        
        // Check condition(s)
        if (GameConditions.isOncePerTurn(game, self, playerId, gameTextSourceCardId, gameTextActionId)
                && GameConditions.canTarget(game, self, curseCandidateReason, curseCandidateFilter)
                && GameConditions.canTarget(game, self, curseRecipientReason, curseRecipientFilter)) {

            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("'curse' opponent's character, vehicle, or weapon");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerTurnEffect(action));
            // Choose target(s)
            action.appendTargeting(new TargetCardOnTableEffect(action, playerId, "Choose Sorcery Test", curseCandidateReason, curseCandidateFilter) {

                @Override
                protected void cardTargeted(int curseTargetGroupId, PhysicalCard targetedCard) {
                    action.appendTargeting(new TargetCardOnTableEffect(action, playerId, "Choose opponent's character, vehicle, or weapon", curseRecipientReason, curseRecipientFilter) {
                        @Override
                        protected void cardTargeted(int curseRecipientTargetGroupId, PhysicalCard targetedCard) {
                            // Allow response(s)
                            action.allowResponses(
                                    new RespondableEffect(action) {
                                        @Override
                                        protected void performActionResults(Action targetingAction) {
                                            final PhysicalCard finalCurse = action.getPrimaryTargetCard(curseTargetGroupId);
                                            final PhysicalCard finalCurseRecipient = action.getPrimaryTargetCard(curseRecipientTargetGroupId);

                                            action.addAnimationGroup(finalCurse, finalCurseRecipient);
                                            // Perform result(s)
                                            action.appendEffect(
                                                    new CurseAssignmentEffect(action, finalCurse, true));
                                            action.appendEffect(
                                                    new AttachCardFromTableEffect(action, finalCurse, finalCurseRecipient));
                                        }
                                    }
                            );
                        }
                    });
                }
            });
            return Collections.singletonList(action);
        }

        return null;
    }

    // At any time, opponent may lose 2 Force to 'break the curse' (relocate a Sorcery Test from their card to Spellbook)
    @Override
    protected List<TopLevelGameTextAction> getOpponentsCardGameTextTopLevelActions(final String playerId, SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        // Check condition(s)
        if (GameConditions.canSpot(game, self, Filters.curse)
            && GameConditions.canSpot(game, self, Filters.Spellbook)) {
            
            final GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_2;

            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Lose two force to 'break a curse'");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerTurnEffect(action));
            // Choose target(s)
            action.appendTargeting(new TargetCardOnTableEffect(action, playerId, "Choose curse to break", Filters.curse) {
                @Override
                protected void cardTargeted(int curseToBreakTargetGroupId, PhysicalCard targetedCard) {
                    final PhysicalCard curseToBreak = action.getPrimaryTargetCard(curseToBreakTargetGroupId);
                    final PhysicalCard spellbook = Filters.findFirstActive(game, self, Filters.Spellbook);

                    action.addAnimationGroup(curseToBreak);
                    // Pay cost(s)
                    action.appendEffect(
                            new LoseForceEffect(action, playerId, 2));
                    // Perform result(s)
                    action.appendEffect(
                            new CurseAssignmentEffect(action, curseToBreak, false));
                    action.appendEffect(
                            new AttachCardFromTableEffect(action, curseToBreak, spellbook));
                }
            });

            return Collections.singletonList(action);
        }

        return null;
    }

    // All 'cursed' cards have their gametext canceled
    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new CancelsGameTextModifier(self, Filters.hasAttached(Filters.curse)));
        return modifiers;
    }
}
