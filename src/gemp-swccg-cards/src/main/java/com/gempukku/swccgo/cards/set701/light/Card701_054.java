package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractImmediateEffect;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerPhaseEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.PlayCardZoneOption;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.PlayCardAction;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.DrawDestinyEffect;
import com.gempukku.swccgo.logic.effects.LoseCardFromTableEffect;
import com.gempukku.swccgo.logic.effects.RestoreCardToNormalEffect;
import com.gempukku.swccgo.logic.effects.RetrieveCardEffect;
import com.gempukku.swccgo.logic.effects.choose.PlaceCardsOutOfPlayFromLostPileEffect;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.ResetForfeitModifier;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.GuiUtils;
import com.gempukku.swccgo.logic.timing.results.HitResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Beezer Bowl 2025
 * Type: Effect
 * Subtype: Immediate
 * Title: Son Of A Beezer
 */
public class Card701_054 extends AbstractImmediateEffect {
    public Card701_054() {
        super(Side.LIGHT, 5, PlayCardZoneOption.ATTACHED, "Son Of A Beezer", Uniqueness.RESTRICTED_2, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Beezer was comfortable with Han's first mate... but it doesn't take much divergence in DNA to make inter-species breeding impossible.");
        setGameText("Deploy on your character just 'hit'. Character is restored to normal and is forfeit = 0 (even if forfeit may not be reduced). During your next move phase, draw destiny and lose Son Of A Beezer. If destiny = 5, retrieve any one male character. If destiny > 5, place all cards in opponent's Lost Pile out of play (except for characters).");
        addIcons(Icon.BEEZER_BOWL_2025);
    }

    @Override
    protected List<PlayCardAction> getGameTextOptionalAfterActions(final String playerId, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        // Check if your character was just hit
        if (TriggerConditions.justHitBy(game, effectResult, Filters.and(Filters.your(self), Filters.character), Filters.any)) {
            PhysicalCard cardHit = ((HitResult) effectResult).getCardHit();
            PlayCardAction action = getPlayCardAction(playerId, game, self, self, false, 0, null, null, null, null, null, false, 0, Filters.sameCardId(cardHit), null);
            if (action != null) {
                return Collections.singletonList(action);
            }
        }
        return null;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(final SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        List<RequiredGameTextTriggerAction> actions = new LinkedList<>();

        // When just deployed, restore the character to normal
        if (TriggerConditions.justDeployed(game, effectResult, self)) {
            PhysicalCard character = self.getAttachedTo();
            if (character != null) {
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setText("Restore " + GameUtils.getFullName(character) + " to normal");
                action.setActionMsg("Restore " + GameUtils.getCardLink(character) + " to normal");
                action.appendEffect(
                        new RestoreCardToNormalEffect(action, character));
                actions.add(action);
            }
        }

        // End of move phase - force the action if not already done
        final String playerId = self.getOwner();
        final String opponent = game.getOpponent(playerId);
        GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_1;

        if (TriggerConditions.isEndOfYourPhase(game, effectResult, Phase.MOVE, playerId)
                && GameConditions.isOnceDuringYourPhase(game, self, playerId, gameTextSourceCardId, gameTextActionId, Phase.MOVE)
                && GameConditions.canDrawDestiny(game, playerId)) {

            final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Draw destiny and lose Son Of A Beezer");
            action.setActionMsg("Draw destiny and lose " + GameUtils.getCardLink(self));
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerPhaseEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new DrawDestinyEffect(action, playerId, 1) {
                        @Override
                        protected void destinyDraws(SwccgGame game, List<PhysicalCard> destinyCardDraws, List<Float> destinyDrawValues, Float totalDestiny) {
                            GameState gameState = game.getGameState();

                            // Lose Son Of A Beezer
                            action.appendEffect(
                                    new LoseCardFromTableEffect(action, self));

                            if (totalDestiny == null) {
                                gameState.sendMessage("Result: Failed due to failed destiny draw");
                                return;
                            }

                            gameState.sendMessage("Destiny: " + GuiUtils.formatAsString(totalDestiny));

                            if (totalDestiny == 5) {
                                gameState.sendMessage("Result: Destiny = 5, retrieve any one male character");
                                action.appendEffect(
                                        new RetrieveCardEffect(action, playerId, Filters.and(Filters.character, Keyword.MALE)));
                            } else if (totalDestiny > 5) {
                                gameState.sendMessage("Result: Destiny > 5, place all non-character cards in opponent's Lost Pile out of play");
                                action.appendEffect(
                                        new PlaceCardsOutOfPlayFromLostPileEffect(action, playerId, opponent, 0, Integer.MAX_VALUE, Filters.not(Filters.character), false));
                            } else {
                                gameState.sendMessage("Result: Destiny < 5, no additional effect");
                            }
                        }
                    });
            actions.add(action);
        }

        return actions.isEmpty() ? null : actions;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(new ResetForfeitModifier(self, Filters.hasAttached(self), 0));
        return modifiers;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        final String opponent = game.getOpponent(playerId);
        GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_1;

        // Check condition(s)
        if (GameConditions.isOnceDuringYourPhase(game, self, playerId, gameTextSourceCardId, gameTextActionId, Phase.MOVE)
                && GameConditions.canDrawDestiny(game, playerId)) {

            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Draw destiny and lose Son Of A Beezer");
            action.setActionMsg("Draw destiny and lose " + GameUtils.getCardLink(self));
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerPhaseEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new DrawDestinyEffect(action, playerId, 1) {
                        @Override
                        protected void destinyDraws(SwccgGame game, List<PhysicalCard> destinyCardDraws, List<Float> destinyDrawValues, Float totalDestiny) {
                            GameState gameState = game.getGameState();

                            // Lose Son Of A Beezer
                            action.appendEffect(
                                    new LoseCardFromTableEffect(action, self));

                            if (totalDestiny == null) {
                                gameState.sendMessage("Result: Failed due to failed destiny draw");
                                return;
                            }

                            gameState.sendMessage("Destiny: " + GuiUtils.formatAsString(totalDestiny));

                            if (totalDestiny == 5) {
                                gameState.sendMessage("Result: Destiny = 5, retrieve any one male character");
                                action.appendEffect(
                                        new RetrieveCardEffect(action, playerId, Filters.and(Filters.character, Keyword.MALE)));
                            } else if (totalDestiny > 5) {
                                gameState.sendMessage("Result: Destiny > 5, place all non-character cards in opponent's Lost Pile out of play");
                                action.appendEffect(
                                        new PlaceCardsOutOfPlayFromLostPileEffect(action, playerId, opponent, 0, Integer.MAX_VALUE, Filters.not(Filters.character), false));
                            } else {
                                gameState.sendMessage("Result: Destiny < 5, no additional effect");
                            }
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
