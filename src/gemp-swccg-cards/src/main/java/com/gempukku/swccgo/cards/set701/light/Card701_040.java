package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractRebel;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerGameEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.CardType;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.PlaceCardsInUsedPileFromTableEffect;
import com.gempukku.swccgo.logic.effects.choose.ChooseCardsOnTableEffect;
import com.gempukku.swccgo.logic.effects.choose.TakeCardIntoHandFromReserveDeckEffect;
import com.gempukku.swccgo.logic.modifiers.AddsPowerToPilotedBySelfModifier;
import com.gempukku.swccgo.logic.modifiers.DeployCostToLocationModifier;
import com.gempukku.swccgo.logic.modifiers.ImmuneToAttritionLessThanModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotBeTargetedByModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Set: Beezer Bowl 2025
 * Type: Character
 * Subtype: Rebel
 * Title: Han, Dependable General
 */
public class Card701_040 extends AbstractRebel {
    public Card701_040() {
        super(Side.LIGHT, 1, 6, 4, 4, 8, "Han, Dependable General", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Founder/ leader of Renegade Squadron. Dependable in pressure situation. Scout.");
        setGameText("Adds 2 to anything he pilots. Deploys -2 to Endor. When deployed, may [upload] one General's Order or Tydirium. Once per game, may place any or all of your devices on table in Used Pile. Immune to opponent's Interrupts and attrition < 4.");
        addPersona(Persona.HAN);
        addIcons(Icon.PILOT, Icon.WARRIOR, Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.LEADER, Keyword.GENERAL, Keyword.MALE, Keyword.SCOUT);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        Filter opponentsInterrupts = Filters.and(Filters.opponents(self), Filters.Interrupt);

        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Adds 2 to anything he pilots
        modifiers.add(new AddsPowerToPilotedBySelfModifier(self, 2));
        // Immune to attrition < 4
        modifiers.add(new ImmuneToAttritionLessThanModifier(self, 4));
        // Immune to opponent's Interrupts
        modifiers.add(new MayNotBeTargetedByModifier(self, opponentsInterrupts));
        return modifiers;
    }

    @Override
    protected List<Modifier> getGameTextAlwaysOnModifiers(SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Deploys -2 to Endor
        modifiers.add(new DeployCostToLocationModifier(self, -2, Filters.Endor_location));
        return modifiers;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.HAN_DEPENDABLE_GENERAL__UPLOAD_CARD;

        // When deployed, may upload one General's Order or Tydirium
        if (TriggerConditions.justDeployed(game, effectResult, self)
                && GameConditions.canTakeCardsIntoHandFromReserveDeck(game, playerId, self, gameTextActionId)) {

            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Take card into hand from Reserve Deck");
            action.setActionMsg("Take a General's Order or Tydirium into hand from Reserve Deck");
            // Perform result(s)
            action.appendEffect(
                    new TakeCardIntoHandFromReserveDeckEffect(action, playerId, Filters.or(Filters.type(CardType.GENERALS_ORDER), Filters.Tydirium), true));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.HAN_DEPENDABLE_GENERAL__PLACE_DEVICES_IN_USED_PILE;

        Filter yourDevicesOnTable = Filters.and(Filters.your(self), Filters.device, Filters.onTable);

        // Once per game, may place any or all of your devices on table in Used Pile
        if (GameConditions.isOncePerGame(game, self, gameTextActionId)
                && GameConditions.canSpot(game, self, yourDevicesOnTable)) {

            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Place devices in Used Pile");
            action.setActionMsg("Place any or all devices on table in Used Pile");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerGameEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new ChooseCardsOnTableEffect(action, playerId, "Choose devices to place in Used Pile", 1, Integer.MAX_VALUE, yourDevicesOnTable) {
                        @Override
                        protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                            action.appendEffect(
                                    new PlaceCardsInUsedPileFromTableEffect(action, selectedCards));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
