package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractAlien;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerGameEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Species;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.LoseForceEffect;
import com.gempukku.swccgo.logic.effects.ModifyPowerUntilEndOfTurnEffect;
import com.gempukku.swccgo.logic.effects.choose.DeployCardFromLostPileEffect;
import com.gempukku.swccgo.logic.effects.choose.DeployCardFromReserveDeckEffect;
import com.gempukku.swccgo.logic.modifiers.DeployCostToLocationModifier;
import com.gempukku.swccgo.logic.modifiers.ImmuneToAttritionLessThanModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
* Set: BEEZER_BOWL_2025
* Type: CHARACTER_ALIEN
* Title: Morag, Sorceress
*/
public class Card701_012 extends AbstractAlien {
    public Card701_012() {
        super(Side.DARK, 1, 6, 4, 5, 7, Title.Morag_Sorceress, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploys -1 to Mt. Thunderstone sites. Once per game, may use 2 Force to [download] Shadowstone Staff (or deploy it from Lost Pile). If opponent's attempt to 'hit' Morag is not successful, opponent loses 1 Force and Morag is cumulatively power +2 for remainder of turn. Immune to Rebel Barrier and attrition < 5.");
        addIcons(Icon.BEEZER_BOWL_2025, Icon.WARRIOR);
        addKeywords(Keyword.DARK_ARTS, Keyword.SORCERER);
        addPersonas(Persona.MORAG);
        setSpecies(Species.TULGAH);
        // Immune to Rebel Barrier
        addImmuneToCardTitle(Title.Rebel_Barrier);
    }
    
    @Override
    protected List<Modifier> getGameTextAlwaysOnModifiers(SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Deploys -1 to Mt. Thunderstone sites
        modifiers.add(new DeployCostToLocationModifier(self, -1, Filters.Mt_Thunderstone_site));
        // Immune to attrition < 5
        modifiers.add(new ImmuneToAttritionLessThanModifier(self, 5));
        return modifiers;
    }
   
    // Once per game, may use 2 Force to [download] Shadowstone Staff (or deploy it from Lost Pile)
    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();

        // Note the two cases are not mutually exclusive, so we might want to offer both actions at the same time,
        // but they both contribute to the same once-per-game condition, so they share this gameTextActionId:
        final GameTextActionId gameTextActionId = GameTextActionId.MORAG_SORCERESS__DOWNLOAD_SHADOWSTONE_STAFF_OR_DEPLOY_FROM_LOST_PILE;

        // Check condition(s)
        if (GameConditions.isOncePerGame(game, self, gameTextActionId)
                && GameConditions.isDuringYourPhase(game, self, Phase.DEPLOY)
                && GameConditions.canUseForce(game, playerId, 2)) {

            // First case: Shadowstone Staff is in reserve deck
            if (GameConditions.canDeployCardFromReserveDeck(game, playerId, self, gameTextActionId, Title.Shadowstone_Staff)) {
                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("Deploy Shadowstone Staff from Reserve Deck");
                action.setActionMsg("Deploy Shadowstone Staff from Reserve Deck");
                // Update usage limit(s)
                action.appendUsage(
                        new OncePerGameEffect(action));
                // Perform result(s)
                action.appendEffect(
                        new DeployCardFromReserveDeckEffect(action, Filters.title(Title.Shadowstone_Staff), true));
                actions.add(action);
            }
            
            // Second case: Shadowstone Staff is in lost pile
            if (GameConditions.canDeployCardFromLostPile(game, playerId, self, gameTextActionId, Title.Shadowstone_Staff)) {
                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
                action.setText("Deploy Shadowstone Staff from Lost Pile");
                action.setActionMsg("Deploy Shadowstone Staff from Lost Pile");
                // Update usage limit(s)
                action.appendUsage(
                        new OncePerGameEffect(action));
                // Pay cost(s)
                action.appendCost(
                    new LoseForceEffect(action, playerId, 2));
                // Perform result(s)
                action.appendEffect(
                        new DeployCardFromLostPileEffect(action, Filters.title(Title.Shadowstone_Staff), true));
                actions.add(action);
            }
        }

        return actions;
    }

    // If opponent’s attempt to 'hit' Morag is not successful, opponent loses 1 Force and Morag is cumulatively power +2 for remainder of turn
    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        // Check condition(s)
        if (TriggerConditions.justAvertedBeingHit(game, effectResult, self)) {
            final String opponent = game.getOpponent(self.getOwner());

            final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText(opponent + " loses 1 Force and " + GameUtils.getCardLink(self) + " is cumulatively power +2 for remainder of turn");
            action.setActionMsg(opponent + " loses 1 Force and " + GameUtils.getCardLink(self) + " is cumulatively power +2 for remainder of turn");
            // Update usage limit(s)
            action.appendEffect(
                    new LoseForceEffect(action, opponent, 1));
            // Perform result(s)
            action.appendEffect(
                    new ModifyPowerUntilEndOfTurnEffect(action, self, 2, true));
            return Collections.singletonList(action);
        }

        return null;
    }
}
