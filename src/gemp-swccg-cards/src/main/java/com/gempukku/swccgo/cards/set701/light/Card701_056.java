package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractAlien;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.evaluators.MultiplyEvaluator;
import com.gempukku.swccgo.cards.evaluators.StackedEvaluator;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
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
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.cards.effects.PeekAtTopCardsOfReserveDeckAndStackEffect;
import com.gempukku.swccgo.logic.modifiers.DeployCostToLocationModifier;
import com.gempukku.swccgo.logic.modifiers.DefenseValueModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Set 701 (Beezer Bowl 2025)
 * Type: Character
 * Subtype: Alien
 * Title: Wicket Warrick
 * Gemp ID: 701_056
 */
public class Card701_056 extends AbstractAlien {
    public Card701_056() {
        super(Side.LIGHT, 2, 3, 3, 2, 5, "\u2022Wicket Warrick", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Brave warrior of the Bright Tree Village. Uses the stars to help him on journeys. Married Chief Chirpa's daughter, Kneesaa, shortly after the battle of Endor.");
        setGameText("Deploys only on Endor (-1 to same site as any Ewok). Permanent device is \u2022Wicket's Belt of Honor (if Wicket just initiated a Force drain or won a battle, stack top card of opponent's Reserve Deck here; Wicket is defense value +2 for each card stacked here).");
        addPersona(Persona.WICKET);
        addIcons(Icon.WARRIOR, Icon.DEVICE, Icon.BEEZER_BOWL_2025);
        setSpecies(Species.EWOK);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.Deploys_on_Endor;
    }

    @Override
    protected List<Modifier> getGameTextAlwaysOnModifiers(SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Deploys -1 to same site as any Ewok
        modifiers.add(new DeployCostToLocationModifier(self, -1, Filters.wherePresent(self, Filters.Ewok)));
        return modifiers;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Wicket is defense value +2 for each card stacked here
        modifiers.add(new DefenseValueModifier(self, new MultiplyEvaluator(2, new StackedEvaluator(self))));
        return modifiers;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, final EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        String playerId = self.getOwner();
        String opponent = game.getOpponent(playerId);

        // Check condition(s) - If Wicket just initiated a Force drain or won a battle
        if ((TriggerConditions.forceDrainInitiatedBy(game, effectResult, playerId, Filters.wherePresent(self))
                || TriggerConditions.wonBattleAt(game, effectResult, playerId, Filters.wherePresent(self)))
                && GameConditions.hasReserveDeck(game, opponent)) {

            final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Stack top card of opponent's Reserve Deck here");
            action.setActionMsg("Stack top card of opponent's Reserve Deck on " + GameUtils.getCardLink(self));
            // Perform result(s)
            action.appendEffect(
                    new PeekAtTopCardsOfReserveDeckAndStackEffect(action, opponent, 1, self));
            return Collections.singletonList(action);
        }
        return null;
    }
}
