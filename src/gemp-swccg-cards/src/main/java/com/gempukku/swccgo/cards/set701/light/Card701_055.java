package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractNormalEffect;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.SetWhileInPlayDataEffect;
import com.gempukku.swccgo.cards.effects.usage.OncePerTurnEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.PlayCardZoneOption;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.WhileInPlayData;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.CancelCardActionBuilder;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.choose.TakeCardIntoHandFromReserveDeckEffect;
import com.gempukku.swccgo.logic.timing.Effect;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.PassthruEffect;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Set 701 (Beezer Bowl 2025)
 * Type: Effect
 * Title: Twilight Is Upon Us
 * Gemp ID: 701_055
 */
public class Card701_055 extends AbstractNormalEffect {
    public Card701_055() {
        super(Side.LIGHT, 4, PlayCardZoneOption.YOUR_SIDE_OF_TABLE, "Twilight Is Upon Us", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("The forest moon of Endor orbits a gas giant known to the Ewoks as Tana. Eighteen hour days and two suns cause elongated twilight experiences, perfect for hiding or sneaking around.");
        setGameText("Deploy on table. Like My Father Before Me is canceled. Rotate this Effect 90 degrees at the start of each of your turns. Once per turn during your control phase, when Effect is: Horizontal: May [upload] one Ewok. Vertical: May [upload] one mountaineer. Immune to Alter.");
        addIcons(Icon.BEEZER_BOWL_2025);
        addImmuneToCardTitle(Title.Alter);
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredBeforeTriggers(final SwccgGame game, Effect effect, final PhysicalCard self, int gameTextSourceCardId) {
        Filter likeMyFatherBeforeMe = Filters.title(Title.Like_My_Father_Before_Me);

        // Cancel Like My Father Before Me when being played
        if (TriggerConditions.isPlayingCard(game, effect, likeMyFatherBeforeMe)
                && GameConditions.canCancelCardBeingPlayed(game, self, effect)) {

            RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            // Build action using common utility
            CancelCardActionBuilder.buildCancelCardBeingPlayedAction(action, effect);
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        String playerId = self.getOwner();
        List<RequiredGameTextTriggerAction> actions = new LinkedList<RequiredGameTextTriggerAction>();

        Filter likeMyFatherBeforeMe = Filters.title(Title.Like_My_Father_Before_Me);

        // Initialize WhileInPlayData if not set (0 = vertical/upright, 1 = horizontal/sideways)
        if (!GameConditions.cardHasWhileInPlayDataSet(self)) {
            self.setWhileInPlayData(new WhileInPlayData(0));
        }

        // Cancel Like My Father Before Me if on table
        if (TriggerConditions.isTableChanged(game, effectResult)) {
            if (GameConditions.canTargetToCancel(game, self, likeMyFatherBeforeMe)) {

                final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                // Build action using common utility
                CancelCardActionBuilder.buildCancelCardAction(action, likeMyFatherBeforeMe, Title.Like_My_Father_Before_Me);
                actions.add(action);
            }
        }

        // Rotate this Effect 90 degrees at the start of each of your turns
        if (TriggerConditions.isStartOfYourTurn(game, effectResult, self)) {
            final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Rotate 90 degrees");
            action.setPerformingPlayer(playerId);

            action.appendEffect(new PassthruEffect(action) {
                @Override
                protected void doPlayEffect(SwccgGame game) {
                    int currentPosition = self.getWhileInPlayData().getIntValue();

                    // Toggle between 0 (vertical) and 1 (horizontal)
                    int newPosition = (currentPosition + 1) % 2;

                    // Update card orientation
                    if (newPosition == 0) {
                        // Vertical (upright)
                        self.setSideways(false);
                    } else {
                        // Horizontal (sideways)
                        self.setSideways(true);
                    }

                    game.getGameState().resumeCard(self); // tells the listeners to update the card

                    action.appendEffect(
                            new SetWhileInPlayDataEffect(action, self, new WhileInPlayData(newPosition)));
                }
            });

            actions.add(action);
        }

        return actions;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();

        // Initialize WhileInPlayData if not set
        if (!GameConditions.cardHasWhileInPlayDataSet(self)) {
            self.setWhileInPlayData(new WhileInPlayData(0));
        }

        int currentPosition = self.getWhileInPlayData().getIntValue();

        GameTextActionId gameTextActionId = GameTextActionId.TWILIGHT_IS_UPON_US__UPLOAD_CARD;

        // Once per turn during your control phase
        if (GameConditions.isOnceDuringYourPhase(game, self, playerId, gameTextSourceCardId, gameTextActionId, Phase.CONTROL)
                && GameConditions.canTakeCardsIntoHandFromReserveDeck(game, playerId, self, gameTextActionId)) {

            // Horizontal (position 1): May upload one Ewok
            if (currentPosition == 1) {
                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("Take an Ewok into hand from Reserve Deck");
                action.setActionMsg("Take an Ewok into hand from Reserve Deck");
                // Update usage limit(s)
                action.appendUsage(
                        new OncePerTurnEffect(action));
                // Perform result(s)
                action.appendEffect(
                        new TakeCardIntoHandFromReserveDeckEffect(action, playerId, Filters.Ewok, true));
                actions.add(action);
            }

            // Vertical (position 0): May upload one mountaineer
            if (currentPosition == 0) {
                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("Take a mountaineer into hand from Reserve Deck");
                action.setActionMsg("Take a mountaineer into hand from Reserve Deck");
                // Update usage limit(s)
                action.appendUsage(
                        new OncePerTurnEffect(action));
                // Perform result(s)
                action.appendEffect(
                        new TakeCardIntoHandFromReserveDeckEffect(action, playerId, Filters.and(Keyword.MOUNTAINEER), true));
                actions.add(action);
            }
        }

        return actions;
    }
}
