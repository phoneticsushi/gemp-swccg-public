package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractEffect;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerGameEffect;
import com.gempukku.swccgo.cards.evaluators.StackedEvaluator;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Phase;
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
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.RelocateBetweenLocationsEffect;
import com.gempukku.swccgo.logic.effects.choose.ChooseCardOnTableEffect;
import com.gempukku.swccgo.logic.modifiers.FerocityModifier;
import com.gempukku.swccgo.logic.modifiers.LandspeedModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.PassthruEffect;
import com.gempukku.swccgo.logic.timing.results.DefeatedResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Beezer Bowl 2025
 * Type: Effect
 * Title: Pile Of Bones
 *
 * REQUIRED ADDITIONS TO COMPILE:
 * Title.java:
 *   String Goraxs_Lair = "Mt Krana: Gorax's Lair";
 *   String Gorax = "Gorax";
 *   String The_Great_Devourer = "The Great Devourer";
 *
 * Persona.java:
 *   GORAX("Gorax"),
 *   THE_GREAT_DEVOURER("The Great Devourer"),
 *   // Add to getRelatedPersona(): if (equals(THE_GREAT_DEVOURER)) return GORAX;
 *
 * TESTING NOTES:
 * - Code checks defeatedCard.getZone().isInPlay() before stacking
 * - If cards are NOT in play when justDefeatedBy fires, the trigger won't stack anything
 * - In that case, may need to use isAboutToBeLost trigger instead to intercept before going to lost pile
 * - Cards stacked here are "out of play" per game text definition
 */
public class Card701_050 extends AbstractEffect {
    public Card701_050() {
        super(Side.LIGHT, 0f, PlayCardZoneOption.ATTACHED, Title.Pile_Of_Bones, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Place under Starting Effect. May not be placed in Reserve Deck. Deploys only on Gorax's Lair. Cards defeated by Gorax are stacked here face up and are out of play. Gorax is ferocity +1 for each card stacked here. The Great Devourer is landspeed +1 for each card stacked here.");
        addIcons(Icon.BEEZER_BOWL_2025);
        setMayNotBePlacedInReserveDeck(true);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.title(Title.Goraxs_Lair);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        // Gorax is ferocity +1 for each card stacked here
        modifiers.add(new FerocityModifier(self, Filters.persona(Persona.GORAX), null, new StackedEvaluator(self), false));
        // The Great Devourer is landspeed +1 for each card stacked here
        modifiers.add(new LandspeedModifier(self, Filters.title(Title.The_Great_Devourer), new StackedEvaluator(self)));
        return modifiers;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        // Cards defeated by Gorax are stacked here face up and are out of play
        if (TriggerConditions.justDefeatedBy(game, effectResult, Filters.any,
                Filters.persona(Persona.GORAX))) {

            DefeatedResult defeatedResult = (DefeatedResult) effectResult;
            final PhysicalCard defeatedCard = defeatedResult.getCardDefeated();

            if (defeatedCard != null && defeatedCard.getZone() != null) {
                // Capture the defeated card and all its attachments NOW (before game engine moves them)
                final List<PhysicalCard> cardsToStack = new ArrayList<>();
                cardsToStack.add(defeatedCard);
                cardsToStack.addAll(game.getGameState().getAllAttachedRecursively(defeatedCard));

                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setText("Stack defeated card");
                action.setActionMsg("Stack " + GameUtils.getCardLink(defeatedCard) + " and attachments on " + GameUtils.getCardLink(self));
                // Use PassthruEffect to manually stack all cards, handling cards that may have already moved to Lost Pile
                action.appendEffect(
                        new PassthruEffect(action) {
                            @Override
                            protected void doPlayEffect(SwccgGame game) {
                                GameState gameState = game.getGameState();
                                for (PhysicalCard card : cardsToStack) {
                                    if (card.getZone() != null) {
                                        gameState.removeCardFromZone(card);
                                        gameState.stackCard(card, self, false, false, false);
                                    }
                                }
                                gameState.sendMessage(GameUtils.getCardLink(defeatedCard) + " and attachments stacked on " + GameUtils.getCardLink(self));
                            }
                        });
                return Collections.singletonList(action);
            }
        }
        return null;
    }

    @Override
    protected List<TopLevelGameTextAction> getOpponentsCardGameTextTopLevelActions(String playerId, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<>();

        // Once per game, DS player may stack topmost character from opponent's (LS) Lost Pile under Pile of Bones
        // (This action is from Gorax, The Mighty's game text but hosted here since creatures don't support opponent actions)
        GameTextActionId stackActionId = GameTextActionId.GORAX_THE_MIGHTY__STACK_CHARACTER_UNDER_PILE_OF_BONES;

        if (GameConditions.isOncePerGame(game, self, stackActionId)
                && GameConditions.canSpot(game, self, Filters.persona(Persona.GORAX))) {

            // playerId is DS (opponent of LS card owner); opponent = LS whose lost pile we search
            String opponent = game.getOpponent(playerId);
            List<PhysicalCard> opponentLostPile = game.getGameState().getLostPile(opponent);
            PhysicalCard topmostCharacter = null;
            for (PhysicalCard card : opponentLostPile) {
                if (Filters.character.accepts(game, card)) {
                    topmostCharacter = card;
                    break;
                }
            }

            if (topmostCharacter != null) {
                final PhysicalCard cardToStack = topmostCharacter;

                TopLevelGameTextAction action = new TopLevelGameTextAction(self, playerId, gameTextSourceCardId, stackActionId);
                action.setText("Stack character under Pile of Bones");
                action.setActionMsg("Stack " + GameUtils.getCardLink(cardToStack) + " from opponent's Lost Pile on " + GameUtils.getCardLink(self));

                action.appendUsage(
                        new OncePerGameEffect(action));

                action.appendEffect(
                        new PassthruEffect(action) {
                            @Override
                            protected void doPlayEffect(SwccgGame game) {
                                game.getGameState().removeCardFromZone(cardToStack);
                                game.getGameState().stackCard(cardToStack, self, false, false, false);
                                game.getGameState().sendMessage(GameUtils.getCardLink(cardToStack) + " is stacked on " + GameUtils.getCardLink(self));
                            }
                        });

                actions.add(action);
            }
        }

        // The following action is from The Great Devourer's (Card701_038_BACK) game text,
        // hosted here since creatures don't support opponent actions.
        // Only available when The Great Devourer is on table (Gorax has flipped).
        PhysicalCard greatDevourer = Filters.findFirstActive(game, self, Filters.title(Title.The_Great_Devourer));

        if (greatDevourer != null) {

            // Once per game, during any deploy phase, may relocate The Great Devourer to an adjacent site
            GameTextActionId relocateActionId = GameTextActionId.GORAX_THE_GREAT_DEVOURER__RELOCATE_TO_ADJACENT_SITE;

            if (GameConditions.isOncePerGame(game, self, relocateActionId)
                    && GameConditions.isDuringEitherPlayersPhase(game, Phase.DEPLOY)) {

                PhysicalCard currentLocation = game.getModifiersQuerying().getLocationHere(game.getGameState(), greatDevourer);
                if (currentLocation != null) {
                    // Once-per-game relocate goes to any adjacent site, not restricted to habitat
                    Filter adjacentSite = Filters.adjacentSite(currentLocation);

                    if (GameConditions.canSpot(game, self, adjacentSite)) {
                        final PhysicalCard creatureToMove = greatDevourer;
                        final TopLevelGameTextAction action = new TopLevelGameTextAction(self, playerId, gameTextSourceCardId, relocateActionId);
                        action.setText("Relocate to adjacent site (once per game)");
                        action.setActionMsg("Relocate " + GameUtils.getCardLink(creatureToMove) + " to an adjacent site");

                        action.appendUsage(
                                new OncePerGameEffect(action));

                        action.appendEffect(
                                new ChooseCardOnTableEffect(action, playerId, "Choose adjacent site to relocate to", adjacentSite) {
                                    @Override
                                    protected void cardSelected(PhysicalCard selectedCard) {
                                        action.appendEffect(
                                                new RelocateBetweenLocationsEffect(action, creatureToMove, selectedCard));
                                    }
                                }
                        );

                        actions.add(action);
                    }
                }
            }
        }

        return actions;
    }
}
