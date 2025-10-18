package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractDevice;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.TwicePerGameEffect;
import com.gempukku.swccgo.common.CardType;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
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
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.DrawDestinyEffect;
import com.gempukku.swccgo.logic.effects.ReturnCardToHandFromTableEffect;
import com.gempukku.swccgo.logic.modifiers.MayNotUseCardToTransportToOrFromLocationModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.GuiUtils;
import com.gempukku.swccgo.logic.timing.results.PlayCardResult;

/**
* Set: BEEZER_BOWL_2025
* Type: DEVICE
* Title: Zarrak’s Medallion
*/
public class Card701_026 extends AbstractDevice {
    public Card701_026() {
        super(Side.DARK, 5, PlayCardZoneOption.ATTACHED, Title.Zarraks_Medallion, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploy on Zarrak. When mentor, adds 2 to sorcery training destiny draws. Opponent may not 'transport' to or from here. Twice per game, if opponent just deployed a character to same site, may shout “RETOW!!!!!!!”; each player draws destiny; character returns to opponent's hand if your destiny > opponent's destiny.");
        addCardType(CardType.ARTIFACT);
        addIcons(Icon.BEEZER_BOWL_2025);
    }

    // Deploy on Zarrak
    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.Zarrak;
    }

    // Opponent may not ‘transport’ to or from here
    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new MayNotUseCardToTransportToOrFromLocationModifier(self, Filters.opponents(self.getOwner()), Filters.here(self)));
        return modifiers;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        final String opponent = game.getOpponent(playerId);
        final GameTextActionId gameTextActionId = GameTextActionId.ZARRAKS_MEDALLION__RETOW_WITH_SEVEN_EXCLAMATION_POINTS_YOU_CAN_COUNT_EM;

        // Twice per game, if opponent just deployed a character to same site...
        if (TriggerConditions.justDeployed(game, effectResult, opponent, Filters.and(Filters.character, Filters.atSameSite(self)))
                && GameConditions.isTwicePerGame(game, self, gameTextActionId)) {

            // ...may shout “RETOW!!!!!!!”; each player draws destiny
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("SHOUT 'RETOW!!!!!!!'");
            action.setActionMsg("Attempt to return character to opponent's hand");
            // Update usage limit(s)
            action.appendUsage(
                    new TwicePerGameEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new DrawDestinyEffect(action, playerId, 1) {
                        @Override
                        protected void destinyDraws(SwccgGame game, final List<PhysicalCard> playerCardDraws, List<Float> playerDestinyDrawValues, final Float playerTotalDestiny) {
                            action.appendEffect(
                                new DrawDestinyEffect(action, opponent, 1) {
                                    @Override
                                    protected void destinyDraws(SwccgGame game, List<PhysicalCard> opponentDestinyCardDraws, List<Float> opponentDestinyDrawValues, Float opponentTotalDestiny) {
                                        final GameState gameState = game.getGameState();

                                        if (playerTotalDestiny == null || opponentTotalDestiny == null) {
                                            gameState.sendMessage("Result: Failed due to failed destiny draw");
                                            return;
                                        }

                                        gameState.sendMessage(playerId + "'s destiny: " + GuiUtils.formatAsString(playerTotalDestiny));
                                        gameState.sendMessage(opponent + "'s destiny: " + GuiUtils.formatAsString(opponentTotalDestiny));

                                        // character returns to opponent's hand if your destiny > opponent’s destiny.
                                        if (playerTotalDestiny > opponentTotalDestiny) {
                                            gameState.sendMessage("Result: Succeeded");

                                            // Type already checked by "justDeployed"
                                            final PlayCardResult playCardResult = (PlayCardResult) effectResult;

                                            action.appendEffect(
                                                    new ReturnCardToHandFromTableEffect(action, playCardResult.getPlayedCard()));
                                        } else {
                                            gameState.sendMessage("Result: Failed");
                                        }
                                    }
                                }
                            );
                        }
                    }
            );
            return Collections.singletonList(action);
        }
        return null;
    }

    // Note: "When mentor, adds 2 to sorcery training destiny draws" is implemented on AbstractSorceryTest.getGameTextTrainingDestinyAttemptAction()
}
