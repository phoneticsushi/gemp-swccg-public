package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractAlien;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.conditions.AtSameSiteAsCondition;
import com.gempukku.swccgo.cards.effects.usage.OncePerGameEffect;
import com.gempukku.swccgo.cards.evaluators.ForceIconsAtLocationEvaluator;
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
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.choose.DeployCardToTargetFromLostPileEffect;
import com.gempukku.swccgo.logic.modifiers.DeployCostToLocationModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.PowerModifier;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.results.LostFromTableResult;

/**
* Set: BEEZER_BOWL_2025
* Type: CHARACTER_ALIEN
* Title: Teebo, Young Apprentice
*/
public class Card701_022 extends AbstractAlien {
    public Card701_022() {
        super(Side.DARK, 2, 2, 2, 3, 5, "Teebo, Young Apprentice", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Ewok shaman and musician. Childhood crush on Latara. Student of Logray's. Tempted by the dark arts into learning flashy magic.");
        setGameText("Deploys only on Endor. Power +1 for each [LS] icon at same Endor site. Once per game, if your character present was just lost, may use 'potion of life' to deploy that character from Lost Pile to same site for free. Opponent’s non-alien characters deploy +1 here.");
        addIcons(Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.DARK_ARTS, Keyword.MUSICIAN, Keyword.SHAMAN);
        addPersonas(Persona.TEEBO);
        setSpecies(Species.EWOK);
    }

    // Deploys only on Endor
    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.locationAndCardsAtLocation(Filters.Endor_site);
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, SwccgGame game, final EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.TEEBO_YOUNG_APPRENTICE__POTION_OF_LIFE;

        // Once per game, if your character present was just lost
        if (GameConditions.isOncePerGame(game, self, gameTextActionId)
                && TriggerConditions.justLostFromLocation(game, effectResult, Filters.and(Filters.your(self), Filters.character), Filters.wherePresent(self))) {
            final PhysicalCard justLostCard = ((LostFromTableResult) effectResult).getCard();
            final PhysicalCard siteWherePresent = game.getModifiersQuerying().getLocationThatCardIsPresentAt(game.getGameState(), self);

            // may use 'potion of life'
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Use 'potion of life'");
            action.setActionMsg("Deploy " + GameUtils.getCardLink(justLostCard) + " from Lost Pile to " + GameUtils.getCardLink(siteWherePresent));
            action.appendUsage(
                    new OncePerGameEffect(action));

            // to deploy that character from Lost Pile to same site for free
            action.appendEffect(
                    new DeployCardToTargetFromLostPileEffect(action, justLostCard, Filters.sameLocationId(self), true, false));
                    
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();

        // Power +1 for each [LS] icon at same Endor site
        final PhysicalCard siteWherePresent = game.getModifiersQuerying().getLocationThatCardIsPresentAt(game.getGameState(), self);
        modifiers.add(new PowerModifier(
            self,
            new AtSameSiteAsCondition(self, Filters.Endor_site),
            new ForceIconsAtLocationEvaluator(siteWherePresent, false, true)
        ));

        // Opponent’s non-alien characters deploy +1 here
        modifiers.add(new DeployCostToLocationModifier(self,
            Filters.and(Filters.opponents(self), Filters.not(Filters.alien)),
            1,
            Filters.sameLocation(self)));

        return modifiers;
    }
}
