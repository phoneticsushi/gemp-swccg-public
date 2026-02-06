package com.gempukku.swccgo.cards.set701.light;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractObjective;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.CancelForceDrainEffect;
import com.gempukku.swccgo.cards.effects.usage.NumTimesPerTurnEffect;
import com.gempukku.swccgo.cards.effects.usage.OncePerTurnEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Zone;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.FlipCardEffect;
import com.gempukku.swccgo.logic.effects.LoseForceEffect;
import com.gempukku.swccgo.logic.effects.RelocateBetweenLocationsEffect;
import com.gempukku.swccgo.logic.effects.ShuffleReserveDeckEffect;
import com.gempukku.swccgo.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.swccgo.logic.decisions.DecisionResultInvalidException;
import com.gempukku.swccgo.logic.decisions.YesNoDecision;
import com.gempukku.swccgo.logic.effects.PlayoutDecisionEffect;
import com.gempukku.swccgo.logic.effects.choose.ChooseCardsFromLostPileEffect;
import com.gempukku.swccgo.logic.effects.choose.ChooseCardOnTableEffect;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.PassthruEffect;

/**
 * Set: Beezer Bowl 2025
 * Type: Objective
 * Title: Back To Base (Card 38 back)
 */
public class Card701_044_BACK extends AbstractObjective {
    public Card701_044_BACK() {
        super(Side.LIGHT, 7, Title.Back_To_Base, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Immediately cause each player to place their Used Pile and hand (if possible) onto Reserve Deck; reshuffle. " +
                "Each player then draws 8 cards from Reserve Deck. Opponent loses 3 Force and may relocate Gorax to an exterior Endor site. " +
                "While this side up, twice per turn, may lose 1 Force to cancel a Force drain. " +
                "During your move phase, may search your Lost Pile and choose two cards; opponent places one card on your Used Pile (place the other card out of play). " +
                "During your draw phase, opponent loses 1 Force for each battleground occupied by your Ewok/Rebel pair (2 if they control). " +
                "Flip this card if [Beezer Bowl 2025] Scrambled Transmission not present at a site.");
        addIcons(Icon.BEEZER_BOWL_2025);
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        List<RequiredGameTextTriggerAction> actions = new LinkedList<>();
        final String playerId = self.getOwner();
        final String opponent = game.getOpponent(playerId);

        // "Immediately" when flipped to this side
        if (TriggerConditions.cardFlipped(game, effectResult, self)) {
            RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Perform immediate flip effects");
            action.setActionMsg("Cause each player to place Used Pile and hand onto Reserve Deck, reshuffle, draw 8 cards, opponent loses 3 Force");

            // 1. Place each player's Used Pile onto Reserve Deck
            action.appendEffect(
                    new PassthruEffect(action) {
                        @Override
                        protected void doPlayEffect(SwccgGame game) {
                            GameState gameState = game.getGameState();
                            // Place player's Used Pile on Reserve Deck
                            gameState.placeCardPileOnCardPile(playerId, Zone.USED_PILE, Zone.RESERVE_DECK);
                            // Place opponent's Used Pile on Reserve Deck
                            gameState.placeCardPileOnCardPile(opponent, Zone.USED_PILE, Zone.RESERVE_DECK);
                            gameState.sendMessage("Each player's Used Pile is placed on their Reserve Deck");
                        }
                    });

            // 2. Place each player's hand onto Reserve Deck (if possible)
            action.appendEffect(
                    new PassthruEffect(action) {
                        @Override
                        protected void doPlayEffect(SwccgGame game) {
                            GameState gameState = game.getGameState();
                            // Place player's hand on Reserve Deck
                            List<PhysicalCard> playerHand = new ArrayList<>(gameState.getHand(playerId));
                            for (PhysicalCard card : playerHand) {
                                gameState.removeCardsFromZone(Collections.singleton(card));
                                gameState.addCardToTopOfZone(card, Zone.RESERVE_DECK, playerId);
                            }
                            // Place opponent's hand on Reserve Deck
                            List<PhysicalCard> opponentHand = new ArrayList<>(gameState.getHand(opponent));
                            for (PhysicalCard card : opponentHand) {
                                gameState.removeCardsFromZone(Collections.singleton(card));
                                gameState.addCardToTopOfZone(card, Zone.RESERVE_DECK, opponent);
                            }
                            gameState.sendMessage("Each player's hand is placed on their Reserve Deck");
                        }
                    });

            // 3. Reshuffle each player's Reserve Deck
            action.appendEffect(
                    new ShuffleReserveDeckEffect(action, playerId));
            action.appendEffect(
                    new ShuffleReserveDeckEffect(action, opponent));

            // 4. Each player draws 8 cards from Reserve Deck
            action.appendEffect(
                    new PassthruEffect(action) {
                        @Override
                        protected void doPlayEffect(SwccgGame game) {
                            GameState gameState = game.getGameState();
                            // Player draws 8 cards
                            for (int i = 0; i < 8; i++) {
                                List<PhysicalCard> reserveDeck = gameState.getReserveDeck(playerId);
                                if (!reserveDeck.isEmpty()) {
                                    PhysicalCard card = reserveDeck.get(0);
                                    gameState.removeCardsFromZone(Collections.singleton(card));
                                    gameState.addCardToZone(card, Zone.HAND, playerId);
                                }
                            }
                            gameState.sendMessage(playerId + " draws 8 cards from Reserve Deck");
                        }
                    });
            action.appendEffect(
                    new PassthruEffect(action) {
                        @Override
                        protected void doPlayEffect(SwccgGame game) {
                            GameState gameState = game.getGameState();
                            // Opponent draws 8 cards
                            for (int i = 0; i < 8; i++) {
                                List<PhysicalCard> reserveDeck = gameState.getReserveDeck(opponent);
                                if (!reserveDeck.isEmpty()) {
                                    PhysicalCard card = reserveDeck.get(0);
                                    gameState.removeCardsFromZone(Collections.singleton(card));
                                    gameState.addCardToZone(card, Zone.HAND, opponent);
                                }
                            }
                            gameState.sendMessage(opponent + " draws 8 cards from Reserve Deck");
                        }
                    });

            // 5. Opponent loses 3 Force
            action.appendEffect(
                    new LoseForceEffect(action, opponent, 3));

            // 6. Opponent may relocate Gorax to an exterior Endor site
            action.appendEffect(
                    new PassthruEffect(action) {
                        @Override
                        protected void doPlayEffect(SwccgGame game) {
                            Filter goraxFilter = Filters.title("Gorax");
                            Filter exteriorEndorSite = Filters.and(Filters.exterior_site, Filters.Endor_site);

                            final PhysicalCard gorax = Filters.findFirstActive(game, self, goraxFilter);
                            if (gorax != null) {
                                PhysicalCard goraxLocation = game.getModifiersQuerying().getLocationHere(game.getGameState(), gorax);
                                final Filter validDestination = Filters.and(exteriorEndorSite,
                                        goraxLocation != null ? Filters.not(Filters.sameCardId(goraxLocation)) : Filters.any);

                                if (GameConditions.canSpot(game, self, validDestination)) {
                                    // Ask opponent if they want to relocate Gorax
                                    action.appendEffect(
                                            new PlayoutDecisionEffect(action, opponent,
                                                    new YesNoDecision("Do you want to relocate Gorax to an exterior Endor site?") {
                                                        @Override
                                                        protected void yes() {
                                                            action.appendEffect(
                                                                    new ChooseCardOnTableEffect(action, opponent, "Choose exterior Endor site for Gorax", validDestination) {
                                                                        @Override
                                                                        protected void cardSelected(PhysicalCard selectedSite) {
                                                                            action.appendEffect(
                                                                                    new RelocateBetweenLocationsEffect(action, gorax, selectedSite));
                                                                        }
                                                                    });
                                                        }
                                                        @Override
                                                        protected void no() {
                                                            game.getGameState().sendMessage(opponent + " chooses not to relocate Gorax");
                                                        }
                                                    }));
                                }
                            }
                        }
                    });

            actions.add(action);
        }

        // During your draw phase, opponent loses 1 Force for each battleground occupied by your Ewok/Rebel pair
        // (2 if they control)
        if (TriggerConditions.isStartOfYourPhase(game, effectResult, Phase.DRAW, playerId)) {
            // Find all battlegrounds
            Collection<PhysicalCard> battlegrounds = Filters.filterTopLocationsOnTable(game, Filters.battleground);
            int forceLoss = 0;

            for (PhysicalCard battleground : battlegrounds) {
                // Check if your Ewok and your Rebel are both at this location (forming a pair)
                boolean hasYourEwok = GameConditions.canSpot(game, self,
                        Filters.and(Filters.your(playerId), Filters.Ewok, Filters.at(battleground)));
                boolean hasYourRebel = GameConditions.canSpot(game, self,
                        Filters.and(Filters.your(playerId), Filters.Rebel, Filters.at(battleground)));

                if (hasYourEwok && hasYourRebel) {
                    // Check if Light Side (you) controls this battleground
                    boolean youControl = Filters.controls(playerId).accepts(game, battleground);
                    forceLoss += youControl ? 2 : 1;
                }
            }

            if (forceLoss > 0) {
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setText("Make opponent lose " + forceLoss + " Force");
                action.setActionMsg("Make opponent lose " + forceLoss + " Force for Ewok/Rebel pairs at battlegrounds");
                action.appendEffect(
                        new LoseForceEffect(action, opponent, forceLoss));
                actions.add(action);
            }
        }

        // Flip this card if [BB25] Scrambled Transmission not present at a site
        if (TriggerConditions.isTableChanged(game, effectResult)
                && GameConditions.canBeFlipped(game, self)) {

            // Check if [BB25] Scrambled Transmission is NOT present at a site
            Filter bb25ScrambledAtSite = Filters.and(
                    Icon.BEEZER_BOWL_2025,
                    Filters.Scrambled_Transmission,
                    Filters.presentAt(Filters.site)
            );

            if (!GameConditions.canSpot(game, self, bb25ScrambledAtSite)) {
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setSingletonTrigger(true);
                action.setText("Flip");
                action.setActionMsg(null);
                action.appendEffect(
                        new FlipCardEffect(action, self));
                actions.add(action);
            }
        }

        return actions;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        List<OptionalGameTextTriggerAction> actions = new LinkedList<>();
        final String opponent = game.getOpponent(playerId);

        // Twice per turn, may lose 1 Force to cancel a Force drain
        GameTextActionId cancelForceDrainActionId = GameTextActionId.OTHER_CARD_ACTION_1;

        if (TriggerConditions.forceDrainInitiatedBy(game, effectResult, opponent)
                && GameConditions.isNumTimesPerTurn(game, self, playerId, 2, gameTextSourceCardId, cancelForceDrainActionId)
                && GameConditions.canCancelForceDrain(game, self)) {

            OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, cancelForceDrainActionId);
            action.setText("Cancel Force drain");
            action.setActionMsg("Lose 1 Force to cancel Force drain");

            // Update usage limit(s)
            action.appendUsage(
                    new NumTimesPerTurnEffect(action, 2));

            // Pay cost
            action.appendCost(
                    new LoseForceEffect(action, playerId, 1, true));

            // Perform result
            action.appendEffect(
                    new CancelForceDrainEffect(action));

            actions.add(action);
        }

        return actions;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<>();
        final String opponent = game.getOpponent(playerId);

        // Combo persona replacement: During deploy phase, if Sergeant Beezer and Sergeant Junkin are present at same location,
        // may deploy Sergeant Beezer & Sergeant Junkin from hand replacing both
        if (GameConditions.isPhaseForPlayer(game, Phase.DEPLOY, playerId)) {
            
            // Check if player has the combo card in hand (card with title "Sergeant Beezer & Sergeant Junkin")
            Collection<PhysicalCard> comboCardsInHand = Filters.filter(game.getGameState().getHand(playerId), game,
                    Filters.title("Sergeant Beezer & Sergeant Junkin"));
            
            if (!comboCardsInHand.isEmpty()) {
                // Find Sergeant Beezer on table
                Collection<PhysicalCard> beezers = Filters.filterActive(game, self,
                        Filters.and(Filters.your(playerId), Filters.title("Sergeant Beezer"), Filters.presentAt(Filters.location)));
                
                for (final PhysicalCard beezer : beezers) {
                    // Get the location where Beezer is
                    final PhysicalCard beezerLocation = game.getModifiersQuerying().getLocationThatCardIsAt(game.getGameState(), beezer);
                    if (beezerLocation == null) continue;
                    
                    // Check if Sergeant Junkin is present at the same location
                    Collection<PhysicalCard> junkins = Filters.filterActive(game, self,
                            Filters.and(Filters.your(playerId), Filters.title("Sergeant Junkin"), Filters.present(beezer)));
                    
                    for (final PhysicalCard junkin : junkins) {
                        // Found a valid pair! Create an action for each combo card in hand
                        for (final PhysicalCard comboCard : comboCardsInHand) {
                            final TopLevelGameTextAction comboAction = new TopLevelGameTextAction(self, gameTextSourceCardId);
                            comboAction.setText("Dansra proposes to Carl");
                            comboAction.setActionMsg("Deploy " + GameUtils.getCardLink(comboCard) + " replacing " + GameUtils.getCardLink(beezer) + " and " + GameUtils.getCardLink(junkin));
                            
                            // Perform the combo replacement
                            comboAction.appendEffect(
                                    new PassthruEffect(comboAction) {
                                        @Override
                                        protected void doPlayEffect(SwccgGame game) {
                                            performComboReplacement(game, playerId, comboCard, beezer, junkin, beezerLocation);
                                            comboAction.setText("Sergeant Beezer is placed out of play");
                                            comboAction.setText("Sergeant Junkin is placed in Lost Pile");
                                            comboAction.setText("Congratulations to the happy couple!");
                                        }
                                    });
                            
                            actions.add(comboAction);
                        }
                    }
                }
            }
        }

        // During your move phase, may search your Lost Pile and choose two cards;
        // opponent places one card on your Used Pile (place the other card out of play)
        GameTextActionId gameTextActionId = GameTextActionId.BACK_TO_BASE__SEARCH_LOST_PILE;

        if (GameConditions.isDuringYourPhase(game, playerId, Phase.MOVE)
                && GameConditions.isOncePerTurn(game, self, playerId, gameTextSourceCardId, gameTextActionId)
                && GameConditions.hasLostPile(game, playerId)) {

            // Check if there are at least 2 cards in Lost Pile
            int lostPileSize = game.getGameState().getLostPile(playerId).size();
            if (lostPileSize >= 2) {

                TopLevelGameTextAction action = new TopLevelGameTextAction(self, playerId, gameTextSourceCardId, gameTextActionId);
                action.setText("Search Lost Pile for two cards");
                action.setActionMsg("Search Lost Pile and choose two cards; opponent places one on Used Pile, other is placed out of play");

                // Update usage limit
                action.appendUsage(
                        new OncePerTurnEffect(action));

                // Search Lost Pile and choose two cards
                action.appendEffect(
                        new ChooseCardsFromLostPileEffect(action, playerId, 2, 2) {
                            @Override
                            protected void cardsSelected(SwccgGame game, final Collection<PhysicalCard> selectedCards) {
                                if (selectedCards.size() == 2) {
                                    final List<PhysicalCard> cardList = new ArrayList<>(selectedCards);

                                    // Send message about the selected cards
                                    game.getGameState().sendMessage(playerId + " selects " + GameUtils.getAppendedNames(cardList) + " from Lost Pile");

                                    // Let opponent choose one card to place on Used Pile (using ArbitraryCardsSelectionDecision since cards are off-table)
                                    action.appendEffect(
                                            new PlayoutDecisionEffect(action, opponent,
                                                    new ArbitraryCardsSelectionDecision("Choose card to place on " + playerId + "'s Used Pile", cardList, 1, 1) {
                                                        @Override
                                                        public void decisionMade(String result) throws DecisionResultInvalidException {
                                                            List<PhysicalCard> chosenCards = getSelectedCardsByResponse(result);
                                                            final PhysicalCard cardForUsedPile = chosenCards.get(0);
                                                            final PhysicalCard cardOutOfPlay = cardList.get(0).getCardId() == cardForUsedPile.getCardId() ? cardList.get(1) : cardList.get(0);

                                                            game.getGameState().sendMessage(opponent + " chooses to place " + GameUtils.getCardLink(cardForUsedPile) + " on " + playerId + "'s Used Pile");

                                                            // Place one card on Used Pile (must remove from Lost Pile first since ChooseCardsFromLostPileEffect only selects, doesn't remove)
                                                            action.appendEffect(
                                                                    new PassthruEffect(action) {
                                                                        @Override
                                                                        protected void doPlayEffect(SwccgGame game) {
                                                                            GameState gameState = game.getGameState();
                                                                            gameState.removeCardFromZone(cardForUsedPile);
                                                                            gameState.addCardToTopOfZone(cardForUsedPile, Zone.USED_PILE, playerId);
                                                                        }
                                                                    });

                                                            // Place other card out of play (must remove from Lost Pile first)
                                                            action.appendEffect(
                                                                    new PassthruEffect(action) {
                                                                        @Override
                                                                        protected void doPlayEffect(SwccgGame game) {
                                                                            GameState gameState = game.getGameState();
                                                                            gameState.removeCardFromZone(cardOutOfPlay);
                                                                            gameState.addCardToTopOfZone(cardOutOfPlay, Zone.OUT_OF_PLAY, cardOutOfPlay.getOwner());
                                                                            game.getGameState().sendMessage(GameUtils.getCardLink(cardOutOfPlay) + " is placed out of play");
                                                                        }
                                                                    });
                                                        }
                                                    }));
                                }
                            }
                        });

                actions.add(action);
            }
        }

        return actions;
    }

    /**
     * Performs the combo persona replacement.
     * Beezer goes out of play, Junkin goes to Lost Pile, combo card deploys to location.
     */
    private void performComboReplacement(SwccgGame game, String playerId, PhysicalCard comboCard, PhysicalCard beezer, PhysicalCard junkin, PhysicalCard location) {
        GameState gameState = game.getGameState();
        
        // Collect attached cards from both characters before we start moving things
        List<PhysicalCard> attachedToBeezer = new ArrayList<>(gameState.getAttachedCards(beezer, true));
        List<PhysicalCard> attachedToJunkin = new ArrayList<>(gameState.getAttachedCards(junkin, true));

        // Send message
        gameState.sendMessage(playerId + " deploys " + GameUtils.getCardLink(comboCard) +
                " replacing " + GameUtils.getCardLink(beezer) + " and " + GameUtils.getCardLink(junkin));

        // 1. Remove combo card from hand and deploy to location
        gameState.removeCardFromZone(comboCard);
        comboCard.setOwner(playerId);
        gameState.playCardToLocation(comboCard, location, playerId);

        // 2. Handle Beezer's attached cards first
        for (PhysicalCard attachedCard : attachedToBeezer) {
            Filter validTransferFilter = attachedCard.getBlueprint().getValidTransferDuringCharacterReplacementTargetFilter(game, attachedCard);
            if (validTransferFilter != null && validTransferFilter.accepts(game, comboCard)) {
                // Transfer to combo card
                gameState.moveCardToAttached(attachedCard, comboCard);
            } else {
                // Goes to Lost Pile
                gameState.removeCardFromZone(attachedCard);
                gameState.addCardToTopOfZone(attachedCard, Zone.LOST_PILE, attachedCard.getOwner());
            }
        }

        // Place Beezer out of play (per her card text)
        gameState.removeCardFromZone(beezer);
        gameState.addCardToTopOfZone(beezer, Zone.OUT_OF_PLAY, beezer.getOwner());

        // 3. Handle Junkin's attached cards
        for (PhysicalCard attachedCard : attachedToJunkin) {
            Filter validTransferFilter = attachedCard.getBlueprint().getValidTransferDuringCharacterReplacementTargetFilter(game, attachedCard);
            if (validTransferFilter != null && validTransferFilter.accepts(game, comboCard)) {
                // Transfer to combo card
                gameState.moveCardToAttached(attachedCard, comboCard);
            } else {
                // Goes to Lost Pile
                gameState.removeCardFromZone(attachedCard);
                gameState.addCardToTopOfZone(attachedCard, Zone.LOST_PILE, attachedCard.getOwner());
            }
        }

        // Place Junkin in Lost Pile (normal persona replacement behavior)
        gameState.removeCardFromZone(junkin);
        gameState.addCardToTopOfZone(junkin, Zone.LOST_PILE, junkin.getOwner());

        // 4. Emit the play card result
        game.getActionsEnvironment().emitEffectResult(
                new com.gempukku.swccgo.logic.timing.results.PlayCardResult(playerId, comboCard, Zone.HAND, null, location, null, false, false));
    }
}
