package com.gempukku.swccgo.cards.set701.dark;

import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractAlien;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.conditions.WithCondition;
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
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.CrossOverCharacterEffect;
import com.gempukku.swccgo.logic.effects.DrawDestinyEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardInLostPileFromTableEffect;
import com.gempukku.swccgo.logic.effects.StackCardFromTableEffect;
import com.gempukku.swccgo.logic.effects.TargetCardOnTableEffect;
import com.gempukku.swccgo.logic.effects.UnrespondableEffect;
import com.gempukku.swccgo.logic.modifiers.AddsBattleDestinyModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.GuiUtils;

/**
* Set: BEEZER_BOWL_2025
* Type: CHARACTER_ALIEN
* Title: Makrit
*/
public class Card701_010 extends AbstractAlien {
    public Card701_010() {
        super(Side.DARK, 2, 3, 3, 3, 4, Title.Makrit, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploys only on Endor. May place Wokling in opponent's Lost Pile (or under Pile of Bones). If with Logray, adds one battle destiny. During your move phase, may target opponent's Ewok present here. Each player draws destiny. If your destiny > opponent's destiny, target crosses over to the Dark Side.");
        addIcons(Icon.BEEZER_BOWL_2025, Icon.WARRIOR);
        addKeywords(Keyword.SHAMAN);
        addPersonas(Persona.MAKRIT);
        setSpecies(Species.EWOK);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        // Deploys only on Endor
        return Filters.Deploys_on_Endor;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // If with Logray, adds one battle destiny
        modifiers.add(new AddsBattleDestinyModifier(self, new WithCondition(self, Filters.Logray), 1));
        return modifiers;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();
        
        final String opponent = game.getOpponent(playerId);

        // Both Wokling and Pile of Bones are unique, so there's no need to have the user target them explicitly.
        PhysicalCard wokling = Filters.findFirstActive(game, self, Filters.Wokling);
        PhysicalCard pileOfBones = Filters.findFirstActive(game, self, Filters.Pile_Of_Bones);

        // Generate the actions conditionally based on the presence of the referenced cards
        if (wokling != null) {
            // May place Wokling in opponent’s Lost Pile...
            final TopLevelGameTextAction loseWoklingAction = new TopLevelGameTextAction(self, gameTextSourceCardId);
            loseWoklingAction.setText("Place Wokling in opponent's Lost Pile");
            loseWoklingAction.setActionMsg(GameUtils.getCardLink(wokling) + " is placed in Lost Pile");
            // Ensure that Wokling moves to the zone of the opponent of Makrit's contoller,
            // in the unlikely case that either Makrit and/or Wokling were previously crossed prior to this action's activation.
            // There's no guarantee that Makrit is on the Dark Side, or even that both cards are on opposite sides.
            //
            // FIXME: do we set owner as well?
            // FIXME: does this need to be done from an appendEffect?
            wokling.setZoneOwner(opponent);
            loseWoklingAction.appendEffect(
                new PlaceCardInLostPileFromTableEffect(loseWoklingAction, wokling)
            );
            actions.add(loseWoklingAction);

            // ...or under Pile Of Bones
            if (pileOfBones != null) {
                final TopLevelGameTextAction stackWoklingAction = new TopLevelGameTextAction(self, gameTextSourceCardId);
                loseWoklingAction.setText("Place Wokling under Pile Of Bones");
                loseWoklingAction.setActionMsg(GameUtils.getCardLink(wokling) + " is stacked on " + GameUtils.getCardLink(pileOfBones));

                // Perform result(s)
                stackWoklingAction.appendEffect(
                    new StackCardFromTableEffect(stackWoklingAction, wokling, pileOfBones)
                );
                actions.add(stackWoklingAction);
            }
        }
        
        final Filter opponentsEwokPresentHere = Filters.and(Filters.opponents(playerId), Filters.Ewok, Filters.here(self));

        // During your move phase...
        if (
            GameConditions.isDuringYourPhase(game, playerId, Phase.MOVE)
            && Filters.canSpot(game, self, opponentsEwokPresentHere)
            // Note that we only present the "cross" action if Makrit is owned by the Dark Side player.
            // If Makrit is on the Light Side due to being previously crossed over e.g.,
            // any valid target would already be on the Dark Side, so it wouldn't be valid to cross the target over to the Dark Side.
            && self.getOwner().equals(game.getDarkPlayer())
        ) {
            final TopLevelGameTextAction tryCrossEwokAction = new TopLevelGameTextAction(self, gameTextSourceCardId);
            tryCrossEwokAction.setText("Try to cross opponent's Ewok to the Dark Side");
            tryCrossEwokAction.setActionMsg("Draw destiny");
            // ...may target opponent’s Ewok present here
            tryCrossEwokAction.appendTargeting(
                new TargetCardOnTableEffect(tryCrossEwokAction, playerId, "Target opponent's Ewok present here", opponentsEwokPresentHere) {
                    @Override
                    protected void cardTargeted(int targetGroupId, final PhysicalCard candidateForCrossingOver) {
                        tryCrossEwokAction.allowResponses(
                            "Target " + GameUtils.getCardLink(candidateForCrossingOver),
                            new UnrespondableEffect(tryCrossEwokAction) {
                                @Override
                                protected void performActionResults(Action targetingAction) {
                                    // Each player draws destiny
                                    tryCrossEwokAction.appendEffect(
                                        new DrawDestinyEffect(tryCrossEwokAction, playerId) {
                                            @Override
                                            protected void destinyDraws(final SwccgGame game, List<PhysicalCard> yourDestinyCardDraws, List<Float> yourDestinyDrawValues, final Float yourTotalDestiny) {
                                                tryCrossEwokAction.appendEffect(
                                                    new DrawDestinyEffect(tryCrossEwokAction, opponent) {
                                                        @Override
                                                        protected void destinyDraws(SwccgGame game, List<PhysicalCard> opponentsDestinyCardDraws, List<Float> opponentsDestinyDrawValues, Float opponentsTotalDestiny) {
                                                            GameState gameState = game.getGameState();
                                                            
                                                            String yourDestinyMessage;
                                                            float yourEffectiveDestiny;
                                                            if (yourTotalDestiny == null) {
                                                                yourDestinyMessage = "Failed destiny draw";
                                                                yourEffectiveDestiny = 0f;
                                                            } else {
                                                                yourDestinyMessage = GuiUtils.formatAsString(yourTotalDestiny);
                                                                yourEffectiveDestiny = yourTotalDestiny;
                                                            }
                                                            
                                                            String opponentsDestinyMessage;
                                                            float opponentsEffectiveDestiny;
                                                            if (opponentsTotalDestiny == null) {
                                                                opponentsDestinyMessage = "Failed destiny draw";
                                                                opponentsEffectiveDestiny = 0f;
                                                            } else {
                                                                opponentsDestinyMessage = GuiUtils.formatAsString(opponentsTotalDestiny);
                                                                opponentsEffectiveDestiny = opponentsTotalDestiny;
                                                            }

                                                            gameState.sendMessage(playerId + "'s destiny: " + yourDestinyMessage);
                                                            gameState.sendMessage(opponent + "'s destiny: " + opponentsDestinyMessage);
                                                            
                                                            // If your destiny > opponent’s destiny...
                                                            if (yourEffectiveDestiny > opponentsEffectiveDestiny) {
                                                                // ...Target crosses over to the Dark Side
                                                                if (candidateForCrossingOver.getOwner().equals(game.getLightPlayer())) {
                                                                    tryCrossEwokAction.appendEffect(new CrossOverCharacterEffect(tryCrossEwokAction, candidateForCrossingOver));
                                                                } else {
                                                                    // See relevant comment in the if block that gates the tryCrossEwokAction
                                                                    gameState.sendMessage(
                                                                        "Cannot cross over " + GameUtils.getCardLink(candidateForCrossingOver)
                                                                        + " to the Dark Side as it is owned by " + candidateForCrossingOver.getOwner()
                                                                        + "; please report this error"
                                                                    );
                                                                }
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
                    }
                }
            );

            actions.add(tryCrossEwokAction);
        }

        return actions;
    }
}
