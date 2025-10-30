package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collections;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractNormalEffect;
import com.gempukku.swccgo.cards.effects.RevealBottomCardOfReserveDeckEffect;
import com.gempukku.swccgo.common.CardType;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.PlayCardZoneOption;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.decisions.CardTypeAwaitingDecision;
import com.gempukku.swccgo.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.swccgo.logic.effects.LoseCardFromBottomOfReserveDeckEffect;
import com.gempukku.swccgo.logic.effects.ModifyTotalTrainingDestinyUntilEndOfDrawEffect;
import com.gempukku.swccgo.logic.effects.ModifyTotalTrainingDestinyUntilEndOfTurnEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromTableEffect;
import com.gempukku.swccgo.logic.effects.PlayoutDecisionEffect;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
* Set: BEEZER_BOWL_2025
* Type: EFFECT
* Title: Prophecy Pool
*/
public class Card701_015 extends AbstractNormalEffect {
    final String TRAINING_DESTINY = "Add 2 to your sorcery training destinies this turn";
    final String GUESS_CARD = "Name a card type and reveal bottom card of opponent's Reserve Deck";
    final String[] possibleActions = new String[] {TRAINING_DESTINY, GUESS_CARD};

    public Card701_015() {
        // Deploy on table
        super(Side.DARK, 3, PlayCardZoneOption.YOUR_SIDE_OF_TABLE, Title.Prophecy_Pool, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploy on table. At the start of your control phase, choose: Add 2 to your sorcery training destinies this turn. OR name a card type and reveal bottom card of opponent's Reserve Deck. If that card matches the named card type, it is lost. May place this Effect out of play to add 3 to your just-drawn training destiny. (Immune to Alter.)");
        addIcons(Icon.BEEZER_BOWL_2025);
        // Immune to Alter
        addImmuneToCardTitle(Title.Alter);
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, final EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        if (TriggerConditions.isStartOfYourPhase(game, effectResult, Phase.CONTROL, self.getOwner())) {
            final String playerId = self.getOwner();
            final String opponent = game.getOpponent(playerId);

            final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Activate Prophecy Pool");
            action.appendEffect(
                // At the start of your control phase, choose:
                new PlayoutDecisionEffect(
                    action,
                    playerId,
                    new MultipleChoiceAwaitingDecision("Choose option", possibleActions) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            switch (result) {
                                case TRAINING_DESTINY:
                                    // Add 2 to your sorcery training destinies this turn
                                    game.getGameState().sendMessage(GameUtils.getCardLink(self) + " adds 2 to sorcery training destinies this turn");
                                    action.appendEffect(new ModifyTotalTrainingDestinyUntilEndOfTurnEffect(action, Filters.Sorcery_Test, 2));
                                    break;
                                case GUESS_CARD:
                                    action.appendEffect(
                                        new PlayoutDecisionEffect(action, playerId,
                                                new CardTypeAwaitingDecision(game, "Name a card type") {
                                                    @Override
                                                    protected void cardTypeChosen(final CardType cardType) {
                                                        final GameState gameState = game.getGameState();
                                                        gameState.sendMessage(playerId + " guesses " + cardType.getHumanReadable() + " card type");
                                                        action.appendEffect(
                                                            // Reveal bottom card of opponent's Reserve Deck
                                                            new RevealBottomCardOfReserveDeckEffect(action, playerId, opponent) {
                                                                @Override
                                                                protected void cardRevealed(final PhysicalCard revealedCard) {
                                                                    if (game.getModifiersQuerying().getCardTypes(gameState, revealedCard).contains(cardType)) {
                                                                        action.appendEffect(
                                                                            // If that card matches the named card type, it is lost
                                                                            new LoseCardFromBottomOfReserveDeckEffect(action, opponent, false));
                                                                    }
                                                                }
                                                            }
                                                        );
                                                    }
                                                }
                                            )
                                        );
                                    break;
                                default:
                            }
                        }
                    }
                )
            );

            return Collections.singletonList(action);
        }

        return null;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, final SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        if ((TriggerConditions.isAboutToCompleteYourOwnTrainingDestinyDraw(game, effectResult))) {
            // May place this Effect out of play to add 3 to your just-drawn training destiny
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Place " + GameUtils.getFullName(self) + " out of play");
            action.setActionMsg("Place " + GameUtils.getCardLink(self) + " out of play to to add 3 to just-drawn training destiny");
            // Pay cost(s)
            action.appendCost(new PlaceCardOutOfPlayFromTableEffect(action, self));
            // Perform result(s)
            action.appendEffect(new ModifyTotalTrainingDestinyUntilEndOfDrawEffect(action, 3));
            return Collections.singletonList(action);
        }

        return null;
    }

}
