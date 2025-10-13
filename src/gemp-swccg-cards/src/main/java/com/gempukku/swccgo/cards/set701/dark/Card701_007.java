package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collections;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractAlien;
import com.gempukku.swccgo.cards.AbstractPermanentDevice;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.RevealCardFromOwnHandEffect;
import com.gempukku.swccgo.cards.effects.usage.OncePerPhaseEffect;
import com.gempukku.swccgo.common.CardType;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.PlayCardOptionId;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Species;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromOffTableEffect;
import com.gempukku.swccgo.logic.effects.PlayoutDecisionEffect;
import com.gempukku.swccgo.logic.effects.choose.ChooseCardFromHandEffect;

/**
* Set: BEEZER_BOWL_2025
* Type: CHARACTER_ALIEN
* Title: Jadru, Enchantress
*/
public class Card701_007 extends AbstractAlien {
    public Card701_007() {
        super(Side.DARK, 3, 3, 3, 4, 6, Title.Jadru_Enchantress, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploys only on Endor. Permanent device is •Jadru's Crystal Ball (once during your control phase, close your eyes and have opponent place a card from their hand face down on the table. Guess its destiny number, then open your eyes and have opponent reveal card. If correctly guessed, opponent places their card out of play).");
        addIcons(Icon.BEEZER_BOWL_2025, Icon.PERMANENT_DEVICE, Icon.WARRIOR);
        addKeywords(Keyword.SORCERER);
        addPersonas(Persona.JADRU);
        setSpecies(Species.CATHAR);
        addCardType(CardType.ARTIFACT);  // The permanent device is an artifact, but it's incorporated into this card.  Does this make sense?
    }
    
    // Deploys only on Endor
    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.Deploys_on_Endor;
    }

    @Override
    protected AbstractPermanentDevice getGameTextPermanentDevice() {
        // Permanent device is •Jadru’s Crystal Ball
        AbstractPermanentDevice permanentDevice = new AbstractPermanentDevice(Persona.JADRUS_CRYSTAL_BALL) {

            // Once during your control phase, close your eyes and have opponent place a card from their hand face down on the table.
            // Guess its destiny number, then open your eyes and have opponent reveal card.
            // If correctly guessed, opponent places their card out of play).
            @Override
            public List<TopLevelGameTextAction> getPermanentDeviceTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self) {
                final String opponent = game.getOpponent(playerId);

                // Check condition(s)
                if (GameConditions.isOnceDuringYourPhase(game, self, playerId, self.getCardId(), Phase.CONTROL)
                        && GameConditions.hasHand(game, opponent)) {

                    final TopLevelGameTextAction action = new TopLevelGameTextAction(self, self.getCardId());
                    action.setText("Guess destiny of card opponent selects from their hand");
                    // Update usage limit(s)
                    action.appendUsage(
                            new OncePerPhaseEffect(action));
                    // Perform result(s)
                    action.appendTargeting(
                        new ChooseCardFromHandEffect(action, opponent) {
                            @Override
                            protected void cardSelected(final SwccgGame game, final PhysicalCard selectedCard) {
                                action.appendEffect(
                                    new PlayoutDecisionEffect(
                                        action,
                                        playerId,
                                        new IntegerAwaitingDecision("Guess the destiny of a card", 0, null, null) {
                                            @Override
                                            public void decisionMade(int destinyGuess) {
                                                action.appendEffect(
                                                    new RevealCardFromOwnHandEffect(action, opponent, selectedCard) {
                                                        @Override
                                                        protected void cardRevealed(PhysicalCard revealedCard) {
                                                            // Assumption is that card destinies are non-negative whole numbers,
                                                            // and that the intent of the game text is to compare to the number printed on the card,
                                                            // without any modifiers applied to it:
                                                            final int destinyFromCard = revealedCard.getBlueprint().getDestiny().intValue();

                                                            // Perform result(s)
                                                            game.getGameState().sendMessage(playerId + " guesses destiny " + destinyGuess);
                                                            game.getGameState().sendMessage(opponent + " reveals " + GameUtils.getCardLink(revealedCard) + " from hand with destiny " + destinyFromCard);

                                                            if (destinyFromCard == destinyGuess) {
                                                                game.getGameState().sendMessage(GameUtils.getCardLink(revealedCard) + " is placed out of play");
                                                                action.appendEffect(
                                                                        new PlaceCardOutOfPlayFromOffTableEffect(action, revealedCard));
                                                            }
                                                        }
                                                    }
                                                );
                                            }
                                        }
                                    )
                                );
                            }
                        }
                    );

                    return Collections.singletonList(action);
                }
                return null;
            }
        };

        return permanentDevice;
    }
}
