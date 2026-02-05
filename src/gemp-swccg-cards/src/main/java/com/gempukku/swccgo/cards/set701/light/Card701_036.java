package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractTransportVehicle;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.PeekAtTopCardsOfReserveDeckAndChooseCardsToTakeIntoHandEffect;
import com.gempukku.swccgo.cards.effects.usage.OncePerGameEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.PlaceCardInUsedPileFromTableEffect;
import com.gempukku.swccgo.logic.effects.RelocateBetweenLocationsEffect;
import com.gempukku.swccgo.logic.effects.TargetCardOnTableEffect;
import com.gempukku.swccgo.logic.effects.UnrespondableEffect;
import com.gempukku.swccgo.logic.modifiers.CharactersAboardMayJumpOffModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.PassthruEffect;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Set 701 (Beezer Bowl 2025)
 * Type: Vehicle
 * Subtype: Transport
 * Title: Ewok Glider
 * Gemp ID: 701_036
 */
public class Card701_036 extends AbstractTransportVehicle {
    public Card701_036() {
        super(Side.LIGHT, 5, 1, 1, null, 2, 2, 2, Title.Ewok_Glider, Uniqueness.UNRESTRICTED, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Constructed from a number of large animals, the Ewoks used their 'sky-gliders' to gain an advantage over the land-bound stormtroopers.");
        setGameText("May add 1 driver (must be an Ewok or a mountaineer). Once per game, may place in Used Pile and choose: Peek at top two cards of your Reserve Deck and take one into hand. OR Relocate Beezer from Apex to an exterior Endor site (or Back Door). If lost or placed in Used Pile, driver may \"jump off\" (disembark).");
        addIcons(Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.EWOK_VEHICLE);
        setDriverCapacity(1);
    }

    @Override
    protected Filter getGameTextValidDriverFilter(String playerId, SwccgGame game, PhysicalCard self) {
        return Filters.or(Filters.Ewok, Keyword.MOUNTAINEER);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersEvenIfUnpiloted(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // If lost or placed in Used Pile, driver may "jump off" (disembark)
        modifiers.add(new CharactersAboardMayJumpOffModifier(self));
        return modifiers;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActionsEvenIfUnpiloted(final String playerId, final SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();

        GameTextActionId gameTextActionId = GameTextActionId.EWOK_GLIDER__ONCE_PER_GAME_CHOICE;

        // Check if once per game is available
        if (GameConditions.isOncePerGame(game, self, gameTextActionId)) {

            // Option 1: Peek at top two cards of Reserve Deck and take one into hand
            if (GameConditions.hasReserveDeck(game, playerId)) {
                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("Peek at top 2 cards of Reserve Deck");
                action.setActionMsg("Place " + GameUtils.getCardLink(self) + " in Used Pile to peek at top two cards of Reserve Deck and take one into hand");
                // Update usage limit(s)
                action.appendUsage(
                        new OncePerGameEffect(action));
                // Disembark any driver to the current location before placing in Used Pile
                action.appendCost(
                        new PassthruEffect(action) {
                            @Override
                            protected void doPlayEffect(SwccgGame game) {
                                PhysicalCard location = game.getModifiersQuerying().getLocationHere(game.getGameState(), self);
                                if (location != null) {
                                    Collection<PhysicalCard> charactersAboard = Filters.filterActive(game, self, Filters.and(Filters.character, Filters.aboard(self)));
                                    for (PhysicalCard character : charactersAboard) {
                                        game.getGameState().sendMessage(GameUtils.getCardLink(character) + " 'jumps off' " + GameUtils.getCardLink(self));
                                        game.getGameState().moveCardToLocation(character, location);
                                    }
                                }
                            }
                        });
                // Pay cost - place glider in Used Pile (now empty of passengers)
                action.appendCost(
                        new PlaceCardInUsedPileFromTableEffect(action, self));
                // Perform result(s)
                action.appendEffect(
                        new PeekAtTopCardsOfReserveDeckAndChooseCardsToTakeIntoHandEffect(action, playerId, 2, 1, 1));
                actions.add(action);
            }

            // Option 2: Relocate glider (with Beezer aboard) from Apex to an exterior Endor site (or Back Door)
            // Beezer must be aboard (driving) this glider, and the glider must be at Apex
            Filter beezerAboard = Filters.and(Filters.persona(Persona.BEEZER), Filters.aboard(self));
            Filter validDestination = Filters.or(Filters.and(Filters.exterior_site, Filters.Endor_site), Filters.Back_Door);
            boolean gliderAtApex = Filters.at(Filters.Apex).accepts(game.getGameState(), game.getModifiersQuerying(), self);

            if (gliderAtApex
                    && GameConditions.canSpot(game, self, beezerAboard)
                    && GameConditions.canSpot(game, self, validDestination)) {

                final PhysicalCard beezer = Filters.findFirstActive(game, self, beezerAboard);
                if (beezer != null) {

                    final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                    action.setText("Relocate Beezer to exterior Endor site or Back Door");
                    action.setActionMsg("Relocate " + GameUtils.getCardLink(self) + " with Beezer to an exterior Endor site or Back Door, then place glider in Used Pile");
                    // Update usage limit(s)
                    action.appendUsage(
                            new OncePerGameEffect(action));
                    // Choose target location FIRST (appendTargeting must come before appendCost)
                    action.appendTargeting(
                            new TargetCardOnTableEffect(action, playerId, "Choose exterior Endor site or Back Door", validDestination) {
                                @Override
                                protected void cardTargeted(int targetGroupId, final PhysicalCard targetedSite) {
                                    action.addAnimationGroup(targetedSite);
                                    // Allow response(s)
                                    action.allowResponses("Relocate " + GameUtils.getCardLink(self) + " with " + GameUtils.getCardLink(beezer) + " to " + GameUtils.getCardLink(targetedSite),
                                            new UnrespondableEffect(action) {
                                                @Override
                                                protected void performActionResults(Action targetingAction) {
                                                    // Relocate the glider (with Beezer aboard) to the chosen site
                                                    action.appendEffect(
                                                            new RelocateBetweenLocationsEffect(action, self, targetedSite));
                                                    // Disembark Beezer to the destination site before placing glider in Used Pile
                                                    action.appendEffect(
                                                            new PassthruEffect(action) {
                                                                @Override
                                                                protected void doPlayEffect(SwccgGame game) {
                                                                    // Find Beezer (may have changed reference after relocation)
                                                                    PhysicalCard beezerNow = Filters.findFirstActive(game, self, Filters.and(Filters.persona(Persona.BEEZER), Filters.aboard(self)));
                                                                    if (beezerNow != null) {
                                                                        game.getGameState().sendMessage(GameUtils.getCardLink(beezerNow) + " 'jumps off' " + GameUtils.getCardLink(self));
                                                                        game.getGameState().moveCardToLocation(beezerNow, targetedSite);
                                                                    }
                                                                }
                                                            });
                                                    // Place glider in Used Pile (now empty)
                                                    action.appendEffect(
                                                            new PlaceCardInUsedPileFromTableEffect(action, self));
                                                }
                                            }
                                    );
                                }
                            }
                    );
                    actions.add(action);
                }
            }
        }

        return actions;
    }
}
