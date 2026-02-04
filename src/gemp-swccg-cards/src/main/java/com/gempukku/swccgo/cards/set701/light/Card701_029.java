package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractCharacterDevice;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerTurnEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.PlayCardOptionId;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.common.Zone;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.PutStackedCardUnderPileEffect;
import com.gempukku.swccgo.logic.effects.StackTopCardFromPileEffect;
import com.gempukku.swccgo.logic.effects.choose.ChooseStackedCardEffect;
import com.gempukku.swccgo.logic.effects.choose.TakeStackedCardIntoHandEffect;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Set 701 (Beezer Bowl 2025)
 * Type: Device
 * Title: Beezer's Tweezers
 * Gemp ID: 701_029
 */
public class Card701_029 extends AbstractCharacterDevice {
    public Card701_029() {
        super(Side.LIGHT, 3, "Beezer's Tweezers", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Beezer's tool for electronic repair, diagnosis, and sometimes sabotage. Capable of manipulating objects at a microscopic level.");
        setGameText("Deploy on Beezer. Each turn, may 'tweeze' one card: Place the top card of your Force Pile, Used Pile, or Reserve Deck here face up. OR Place a card here on bottom of your Force Pile, Used Pile, or Reserve Deck. OR Take a card here into your hand. Holds one card at a time.");
        addIcons(Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.DEPLOYS_ON_CHARACTERS);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.persona(Persona.BEEZER);
    }

    @Override
    protected Filter getGameTextValidToUseDeviceFilter(final SwccgGame game, final PhysicalCard self) {
        return Filters.persona(Persona.BEEZER);
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();

        GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_1;

        // Check if once per turn is available
        if (GameConditions.isOncePerTurn(game, self, playerId, gameTextSourceCardId, gameTextActionId)) {

            boolean hasCardStacked = GameConditions.hasStackedCards(game, self);

            // Option 1: Place top card of Force Pile here (only if no card stacked)
            if (!hasCardStacked && GameConditions.hasForcePile(game, playerId)) {
                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("'Tweeze' top card of Force Pile");
                action.setActionMsg("Place top card of Force Pile here face up");
                action.appendUsage(new OncePerTurnEffect(action));
                action.appendEffect(new StackTopCardFromPileEffect(action, playerId, Zone.FORCE_PILE, self, false));
                actions.add(action);
            }

            // Option 2: Place top card of Used Pile here (only if no card stacked)
            if (!hasCardStacked && GameConditions.hasUsedPile(game, playerId)) {
                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("'Tweeze' top card of Used Pile");
                action.setActionMsg("Place top card of Used Pile here face up");
                action.appendUsage(new OncePerTurnEffect(action));
                action.appendEffect(new StackTopCardFromPileEffect(action, playerId, Zone.USED_PILE, self, false));
                actions.add(action);
            }

            // Option 3: Place top card of Reserve Deck here (only if no card stacked)
            if (!hasCardStacked && GameConditions.hasReserveDeck(game, playerId)) {
                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("'Tweeze' top card of Reserve Deck");
                action.setActionMsg("Place top card of Reserve Deck here face up");
                action.appendUsage(new OncePerTurnEffect(action));
                action.appendEffect(new StackTopCardFromPileEffect(action, playerId, Zone.RESERVE_DECK, self, false));
                actions.add(action);
            }

            // Option 4: Place card here on bottom of Force Pile (only if card stacked)
            if (hasCardStacked) {
                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("'Tweeze' card to bottom of Force Pile");
                action.setActionMsg("Place card here on bottom of Force Pile");
                action.appendUsage(new OncePerTurnEffect(action));
                action.appendTargeting(
                        new ChooseStackedCardEffect(action, playerId, self, Filters.any) {
                            @Override
                            protected void cardSelected(final PhysicalCard stackedCard) {
                                action.appendEffect(new PutStackedCardUnderPileEffect(action, playerId, stackedCard, Zone.FORCE_PILE));
                            }
                        }
                );
                actions.add(action);
            }

            // Option 5: Place card here on bottom of Used Pile (only if card stacked)
            if (hasCardStacked) {
                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("'Tweeze' card to bottom of Used Pile");
                action.setActionMsg("Place card here on bottom of Used Pile");
                action.appendUsage(new OncePerTurnEffect(action));
                action.appendTargeting(
                        new ChooseStackedCardEffect(action, playerId, self, Filters.any) {
                            @Override
                            protected void cardSelected(final PhysicalCard stackedCard) {
                                action.appendEffect(new PutStackedCardUnderPileEffect(action, playerId, stackedCard, Zone.USED_PILE));
                            }
                        }
                );
                actions.add(action);
            }

            // Option 6: Place card here on bottom of Reserve Deck (only if card stacked)
            if (hasCardStacked) {
                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("'Tweeze' card to bottom of Reserve Deck");
                action.setActionMsg("Place card here on bottom of Reserve Deck");
                action.appendUsage(new OncePerTurnEffect(action));
                action.appendTargeting(
                        new ChooseStackedCardEffect(action, playerId, self, Filters.any) {
                            @Override
                            protected void cardSelected(final PhysicalCard stackedCard) {
                                action.appendEffect(new PutStackedCardUnderPileEffect(action, playerId, stackedCard, Zone.RESERVE_DECK));
                            }
                        }
                );
                actions.add(action);
            }

            // Option 7: Take card here into hand (only if card stacked)
            if (hasCardStacked) {
                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("'Tweeze' card into hand");
                action.setActionMsg("Take card here into hand");
                action.appendUsage(new OncePerTurnEffect(action));
                action.appendEffect(new TakeStackedCardIntoHandEffect(action, playerId, self, Filters.any));
                actions.add(action);
            }
        }

        return actions;
    }
}
