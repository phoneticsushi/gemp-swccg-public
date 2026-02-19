package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractDroid;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.ModelType;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.PlayCardOptionId;
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
import com.gempukku.swccgo.logic.effects.PlaceCardInUsedPileFromTableEffect;
import com.gempukku.swccgo.logic.effects.PutCardFromHandOnUsedPileEffect;
import com.gempukku.swccgo.logic.effects.choose.DrawCardIntoHandFromReserveDeckEffect;
import com.gempukku.swccgo.logic.modifiers.EachSearchPartyDestinyModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Set 701 (Beezer Bowl 25)
 * Type: Character
 * Subtype: Droid
 * Title: EZ-PZ, Security Droid
 * Gemp ID: 701_037
 */
public class Card701_037 extends AbstractDroid {
    public Card701_037() {
        super(Side.LIGHT, 3, 2, 1, 4, "\u2022EZ-PZ, Security Droid", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setArmor(4);
        setLore("One of Dansra Beezer's creations. Made from rare materials brought back from Appenza Peak on Alderaan by Dansra herself.");
        setGameText("Deploys only to Back Door or Theed Palace Courtyard. During any control phase, may say \"easy-peasy lemon squeezy\" and place EZ-PZ in Used Pile. If you just deployed a character here, may place one card from hand in Used Pile to draw top card from Reserve Deck. Adds 3 to search party destiny here.");
        addPersona(Persona.EZ_PZ);
        addModelType(ModelType.SECURITY);
        addIcons(Icon.BEEZER_BOWL_2025);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.or(Filters.Back_Door, Filters.Theed_Palace_Courtyard);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        String playerId = self.getOwner();

        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new EachSearchPartyDestinyModifier(self, Filters.wherePresent(self), 3, playerId));
        return modifiers;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_1;

        // Check condition(s) - During any control phase, may place EZ-PZ in Used Pile
        if (GameConditions.isDuringEitherPlayersPhase(game, Phase.CONTROL)) {

            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Place EZ-PZ in Used Pile");
            action.setActionMsg("Say \"easy-peasy lemon squeezy\" out loud and place EZ-PZ in Used Pile");
            // Perform result(s)
            action.appendEffect(
                    new PlaceCardInUsedPileFromTableEffect(action, self));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_2;

        // Check condition(s) - If you just deployed a character here
        if (TriggerConditions.justDeployedTo(game, effectResult, playerId, Filters.character, Filters.here(self))
                && GameConditions.hasHand(game, playerId)
                && GameConditions.hasReserveDeck(game, playerId)) {

            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Place card from hand in Used Pile to draw");
            action.setActionMsg("Place a card from hand in Used Pile to draw top card from Reserve Deck");
            // Pay cost(s)
            action.appendCost(
                    new PutCardFromHandOnUsedPileEffect(action, playerId));
            // Perform result(s)
            action.appendEffect(
                    new DrawCardIntoHandFromReserveDeckEffect(action, playerId));
            return Collections.singletonList(action);
        }
        return null;
    }
}
