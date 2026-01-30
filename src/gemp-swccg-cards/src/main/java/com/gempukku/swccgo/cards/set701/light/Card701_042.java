package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractAlien;
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
import com.gempukku.swccgo.common.Species;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.choose.TakeCardIntoHandFromLostPileEffect;
import com.gempukku.swccgo.logic.effects.choose.TakeCardIntoHandFromReserveDeckEffect;
import com.gempukku.swccgo.logic.modifiers.AddsPowerToPilotedBySelfModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Set 701 (Beezer Bowl 2025)
 * Type: Character
 * Subtype: Alien
 * Title: Kneesaa
 * Gemp ID: 701_042
 */
public class Card701_042 extends AbstractAlien {
    public Card701_042() {
        super(Side.LIGHT, 2, 2, 3, 2, 4, "\u2022Kneesaa", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Daughter of Chief Chirpa. Helped pathfinder Kes Dameron with tactical maneuvers at the battle of Endor. Experienced in gorax defense tactics. Princess.");
        setGameText("Adds 2 to anything she pilots (3 if Ghost). Deploys only on Endor or Ghost. During your turn, may [upload] one Ewok Glider or non-unique Ewok. If opponent just initiated battle here, may take into hand one Ewok or device from Lost Pile.");
        addPersona(Persona.KNEESAA);
        addIcons(Icon.PILOT, Icon.WARRIOR, Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.PRINCESS, Keyword.FEMALE);
        setSpecies(Species.EWOK);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.or(Filters.Deploys_on_Endor, Filters.Ghost);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Adds 2 to anything she pilots (3 if Ghost)
        modifiers.add(new AddsPowerToPilotedBySelfModifier(self, 2, Filters.not(Filters.Ghost)));
        modifiers.add(new AddsPowerToPilotedBySelfModifier(self, 3, Filters.Ghost));
        return modifiers;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.KNEESAA__UPLOAD_EWOK_GLIDER_OR_EWOK;

        // Check condition(s) - During your turn, may upload one Ewok Glider or non-unique Ewok
        if (GameConditions.isOnceDuringYourTurn(game, self, playerId, gameTextSourceCardId, gameTextActionId)
                && GameConditions.canTakeCardsIntoHandFromReserveDeck(game, playerId, self, gameTextActionId)) {

            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Take Ewok Glider or Ewok into hand from Reserve Deck");
            action.setActionMsg("Take an Ewok Glider or non-unique Ewok into hand from Reserve Deck");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerTurnEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new TakeCardIntoHandFromReserveDeckEffect(action, playerId, Filters.or(Filters.Ewok_glider, Filters.and(Filters.non_unique, Filters.Ewok)), true));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        String opponent = game.getOpponent(playerId);
        GameTextActionId gameTextActionId = GameTextActionId.KNEESAA__TAKE_CARD_INTO_HAND_FROM_LOST_PILE;

        // Check condition(s) - If opponent just initiated battle here
        if (TriggerConditions.battleInitiatedAt(game, effectResult, opponent, Filters.here(self))
                && GameConditions.canTakeCardsIntoHandFromLostPile(game, playerId, self, gameTextActionId)) {

            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Take Ewok or device into hand from Lost Pile");
            action.setActionMsg("Take an Ewok or device into hand from Lost Pile");
            // Perform result(s)
            action.appendEffect(
                    new TakeCardIntoHandFromLostPileEffect(action, playerId, Filters.or(Filters.Ewok, Filters.device), true));
            return Collections.singletonList(action);
        }
        return null;
    }
}
