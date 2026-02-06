package com.gempukku.swccgo.cards.set701.light;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractSite;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerGameEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.LoseCardsFromTableEffect;
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
        setLocationLightSideGameText("When deployed, may take Gorax, The Mighty and Pile Of Bones into hand from outside your deck and deploy them simultaneously.");
        setLocationDarkSideGameText("Only deploys if Mt. Krana Pass on table. Vehicles and starships here are lost.");
        addIcon(Icon.DARK_FORCE, 1);
        addIcon(Icon.LIGHT_FORCE, 1);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.EXTERIOR_SITE, Icon.INTERIOR_SITE, Icon.MOUNTAIN_SITE, Icon.PLANET);
    }

    @Override
    protected boolean checkGameTextDeployRequirements(String playerId, SwccgGame game, PhysicalCard self) {
        // Only deploys if Mt. Krana Pass on table
        return GameConditions.canSpotLocation(game, Filters.title(Title.Endor_Mt_Krana_Pass));
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextLightSideOptionalAfterTriggers(String playerOnLightSideOfLocation, SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.GORAXS_LAIR__DEPLOY_GORAX_AND_PILE_OF_BONES;

        // When deployed, may take Gorax and Pile Of Bones into hand from outside your deck and deploy them simultaneously
        if (TriggerConditions.justDeployed(game, effectResult, self)
                && GameConditions.isOncePerGame(game, self, gameTextActionId)) {

            OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, playerOnLightSideOfLocation, gameTextSourceCardId, gameTextActionId);
            action.setText("Deploy Gorax, The Mighty and Pile Of Bones");
            action.setActionMsg("Take Gorax, The Mighty and Pile Of Bones from outside deck and deploy them here");
            action.appendUsage(
                    new OncePerGameEffect(action));
            action.appendEffect(
                    new DeployCardFromOutsideTheGameEffect(action, Filters.title(Title.Gorax_The_Mighty), Filters.sameLocation(self), 0));
            action.appendEffect(
                    new DeployCardFromOutsideTheGameEffect(action, Filters.title(Title.Pile_Of_Bones), Filters.sameLocation(self), 0));

            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextDarkSideOptionalAfterTriggers(String playerOnDarkSideOfLocation, SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.GORAXS_LAIR__DEPLOY_GORAX_AND_PILE_OF_BONES;

        // When deployed, may take Gorax and Pile Of Bones into hand from outside your deck and deploy them simultaneously
        // (Dark Side gets this option if Light Side declines)
        if (TriggerConditions.justDeployed(game, effectResult, self)
                && GameConditions.isOncePerGame(game, self, gameTextActionId)) {

            OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, playerOnDarkSideOfLocation, gameTextSourceCardId, gameTextActionId);
            action.setText("Deploy Gorax, The Mighty and Pile Of Bones");
            action.setActionMsg("Take Gorax, The Mighty and Pile Of Bones from outside deck and deploy them here");
            action.appendUsage(
                    new OncePerGameEffect(action));
            action.appendEffect(
                    new DeployCardFromOutsideTheGameEffect(action, Filters.title(Title.Gorax_The_Mighty), Filters.sameLocation(self), 0));
            action.appendEffect(
                    new DeployCardFromOutsideTheGameEffect(action, Filters.title(Title.Pile_Of_Bones), Filters.sameLocation(self), 0));

            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextLightSideRequiredAfterTriggers(String playerOnLightSideOfLocation, SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        // ANY vehicles and starships here (light or dark) are lost
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

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextDarkSideRequiredAfterTriggers(String playerOnDarkSideOfLocation, SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        // ANY vehicles and starships here (light or dark) are lost
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
