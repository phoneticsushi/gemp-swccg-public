package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractDevice;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.PlayCardOptionId;
import com.gempukku.swccgo.common.PlayCardZoneOption;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.AttachCardFromTableEffect;
import com.gempukku.swccgo.logic.effects.DrawDestinyEffect;
import com.gempukku.swccgo.logic.effects.choose.ChooseCardOnTableEffect;
import com.gempukku.swccgo.logic.timing.GuiUtils;

import java.util.Collections;
import java.util.List;

/**
 * Set: Set 701 (Beezer Bowl 2025)
 * Type: Device
 * Title: Antenna
 * Gemp ID: 701_027
 */
public class Card701_027 extends AbstractDevice {
    public Card701_027() {
        super(Side.LIGHT, 4, PlayCardZoneOption.ATTACHED, "Antenna", Uniqueness.UNRESTRICTED, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Used to securely transmit information long distances, although a skilled technician could hack into its mainframe.");
        setGameText("Deploy on Apex. During your turn, your mountaineer here may attempt to 'intercept' the Imperial transmission. (Each player draws 2 destiny. Add 1 to your total for each of your devices here. If your total destiny > opponent's total destiny, relocate [BEEZER_BOWL_2025] Scrambled Transmission to your mountaineer at Apex.)");
        addIcons(Icon.SCOMP_LINK, Icon.BEEZER_BOWL_2025);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.Apex;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_1;

        final Filter yourMountaineerHere = Filters.and(Filters.your(self), Keyword.MOUNTAINEER, Filters.here(self));
        final Filter scrambledTransmission = Filters.and(Icon.BEEZER_BOWL_2025, Filters.title("Scrambled Transmission"));

        // Check condition(s)
        if (GameConditions.isDuringYourTurn(game, self)
                && GameConditions.canSpot(game, self, yourMountaineerHere)
                && GameConditions.canSpot(game, self, scrambledTransmission)
                && !GameConditions.canSpot(game, self, Filters.and(scrambledTransmission, Filters.attachedTo(yourMountaineerHere)))) {

            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Attempt to 'intercept' transmission");
            action.setActionMsg("Attempt to 'intercept' the Imperial transmission");

            // Choose target mountaineer to receive Scrambled Transmission if successful
            action.appendEffect(
                    new ChooseCardOnTableEffect(action, playerId, "Choose your mountaineer", yourMountaineerHere) {
                        @Override
                        protected void cardSelected(final PhysicalCard targetedMountaineer) {
                            action.addAnimationGroup(targetedMountaineer);

                            // Draw destiny - player draws 2
                            action.appendEffect(
                                    new DrawDestinyEffect(action, playerId, 2) {
                                        @Override
                                        protected void destinyDraws(SwccgGame game, List<PhysicalCard> playersDestinyCardDraws, List<Float> playersDestinyDrawValues, final Float playersTotalDestiny) {

                                            // Add bonus for devices here
                                            final int devicesHere = Filters.countActive(game, self, Filters.and(Filters.your(self), Filters.device, Filters.here(self)));
                                            final float playersTotal = (playersTotalDestiny != null ? playersTotalDestiny : 0) + devicesHere;

                                            // Opponent draws 2 destiny
                                            final String opponent = game.getOpponent(playerId);
                                            action.appendEffect(
                                                    new DrawDestinyEffect(action, opponent, 2) {
                                                        @Override
                                                        protected void destinyDraws(SwccgGame game, List<PhysicalCard> opponentsDestinyCardDraws, List<Float> opponentsDestinyDrawValues, Float opponentsTotalDestiny) {
                                                            final float opponentsTotal = (opponentsTotalDestiny != null ? opponentsTotalDestiny : 0);

                                                            game.getGameState().sendMessage(playerId + "'s total destiny: " + GuiUtils.formatAsString(playersTotal) + " (including +" + devicesHere + " for devices)");
                                                            game.getGameState().sendMessage(opponent + "'s total destiny: " + GuiUtils.formatAsString(opponentsTotal));

                                                            // Check if player wins
                                                            if (playersTotal > opponentsTotal) {
                                                                game.getGameState().sendMessage(playerId + " successfully 'intercepts' the Imperial transmission");

                                                                // Find Scrambled Transmission and relocate it
                                                                final PhysicalCard scrambledTransmissionCard = Filters.findFirstActive(game, self, scrambledTransmission);
                                                                if (scrambledTransmissionCard != null) {
                                                                    action.appendEffect(
                                                                            new AttachCardFromTableEffect(action, scrambledTransmissionCard, targetedMountaineer));
                                                                }
                                                            } else {
                                                                game.getGameState().sendMessage(playerId + " fails to 'intercept' the Imperial transmission");
                                                            }
                                                        }
                                                    }
                                            );
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
}
