package com.gempukku.swccgo.cards.set701.light;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractSite;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.LoseCardsFromTableEffect;
import com.gempukku.swccgo.logic.effects.PlaceCardOutOfPlayFromTableEffect;
import com.gempukku.swccgo.logic.effects.choose.DeployCardFromOutsideTheGameEffect;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
 * Set: Beezer Bowl 2025
 * Type: Location
 * Subtype: Site
 * Title: Mt. Krana: Gorax's Lair
 */
public class Card701_047 extends AbstractSite {
    public Card701_047() {
        super(Side.LIGHT, Title.Goraxs_Lair, Title.Endor, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLocationLightSideGameText("When deployed, take Gorax, The Mighty and Pile Of Bones from outside your deck and deploy both here. If unable, place Gorax's Lair and Mt. Krana: Apex out of play.");
        setLocationDarkSideGameText("Only deploys if Mt. Krana Pass on table. Vehicles and starships here are lost.");
        addIcon(Icon.DARK_FORCE, 1);
        addIcon(Icon.LIGHT_FORCE, 1);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.EXTERIOR_SITE, Icon.INTERIOR_SITE, Icon.MOUNTAIN_SITE, Icon.PLANET);
        addKeyword(Keyword.MT_KRANA_SITE);
    }

    @Override
    protected boolean checkGameTextDeployRequirements(String playerId, SwccgGame game, PhysicalCard self) {
        // Only deploys if Mt. Krana Pass on table
        return GameConditions.canSpotLocation(game, Filters.title(Title.Endor_Mt_Krana_Pass));
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextLightSideRequiredAfterTriggers(String playerOnLightSideOfLocation, SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        // When deployed, take Gorax, The Mighty and Pile Of Bones from outside your deck and deploy both here.
        // If unable, place Gorax's Lair and Mt. Krana: Apex out of play.
        if (TriggerConditions.justDeployed(game, effectResult, self)) {

            // Check if both cards are available in Light Side player's outside-of-deck zone
            boolean goraxAvailable = false;
            boolean pileAvailable = false;

            List<PhysicalCard> outsideOfDeck = game.getGameState().getOutsideOfDeck(playerOnLightSideOfLocation);
            if (outsideOfDeck != null) {
                for (PhysicalCard card : outsideOfDeck) {
                    if (Filters.title(Title.Gorax_The_Mighty).accepts(game, card)) {
                        goraxAvailable = true;
                    }
                    if (Filters.title(Title.Pile_Of_Bones).accepts(game, card)) {
                        pileAvailable = true;
                    }
                }
            }

            if (goraxAvailable && pileAvailable) {
                // Deploy both cards from outside the game to this location
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setText("Deploy Gorax, The Mighty and Pile Of Bones");
                action.setActionMsg("Deploy Gorax, The Mighty and Pile Of Bones from outside deck to " + GameUtils.getCardLink(self));
                action.appendEffect(
                        new DeployCardFromOutsideTheGameEffect(action, Filters.title(Title.Gorax_The_Mighty), Filters.sameLocation(self), 0));
                action.appendEffect(
                        new DeployCardFromOutsideTheGameEffect(action, Filters.title(Title.Pile_Of_Bones), Filters.sameLocation(self), 0));
                return Collections.singletonList(action);
            } else {
                // Unable to deploy both — place Gorax's Lair and Mt. Krana: Apex out of play
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setText("Place Gorax's Lair and Apex out of play");
                action.setActionMsg("Place " + GameUtils.getCardLink(self) + " and Mt. Krana: Apex out of play (unable to deploy Gorax and Pile Of Bones)");

                // Place Gorax's Lair (self) out of play
                action.appendEffect(
                        new PlaceCardOutOfPlayFromTableEffect(action, self));

                // Place Mt. Krana: Apex out of play (if on table)
                PhysicalCard apex = Filters.findFirstActive(game, self, Filters.title(Title.Apex));
                if (apex != null) {
                    action.appendEffect(
                            new PlaceCardOutOfPlayFromTableEffect(action, apex));
                }

                return Collections.singletonList(action);
            }
        }
        return null;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextDarkSideRequiredAfterTriggers(String playerOnDarkSideOfLocation, SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        // Vehicles and starships here are lost
        if (TriggerConditions.isTableChanged(game, effectResult)) {
            Filter vehiclesAndStarshipsHere = Filters.and(
                    Filters.or(Filters.vehicle, Filters.starship),
                    Filters.here(self)
            );

            Collection<PhysicalCard> cardsToLose = Filters.filterActive(game, self, vehiclesAndStarshipsHere);
            if (!cardsToLose.isEmpty()) {
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setSingletonTrigger(true);
                action.setText("Make vehicles and starships lost");
                action.setActionMsg("Make vehicles and starships here lost");
                action.appendEffect(
                        new LoseCardsFromTableEffect(action, cardsToLose));
                return Collections.singletonList(action);
            }
        }
        return null;
    }
}
