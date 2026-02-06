package com.gempukku.swccgo.cards.set701.light;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractSite;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.ExpansionSet;
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
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.LoseCardsFromTableEffect;
import com.gempukku.swccgo.logic.modifiers.MayTargetAtAnyExteriorSiteOnPlanetModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.ForceDrainImmuneToBattleOrderModifier;
import com.gempukku.swccgo.logic.modifiers.ForceDrainImmuneToBattlePlanModifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
 * Set: Beezer Bowl 2025
 * Type: Location
 * Subtype: Site
 * Title: Mt. Krana: Apex
 */
public class Card701_046 extends AbstractSite {
    public Card701_046() {
        super(Side.LIGHT, Title.Apex, Title.Endor, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLocationLightSideGameText("Deploys only if Gorax's Lair on table. Rifles here may fire at targets at any exterior Endor site. Light Side Force drains here are immune to Battle Order.");
        setLocationDarkSideGameText("Combat vehicles and starships here are lost. Dark Side Force drains here are immune to Battle Plan.");
        addIcon(Icon.DARK_FORCE, 1);
        addIcon(Icon.LIGHT_FORCE, 2);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.EXTERIOR_SITE, Icon.MOUNTAIN_SITE, Icon.PLANET);
    }

    @Override
    protected boolean checkGameTextDeployRequirements(String playerId, SwccgGame game, PhysicalCard self) {
        // Deploys only if Gorax's Lair on table
        return GameConditions.canSpotLocation(game, Filters.title(Title.Goraxs_Lair));
    }

    @Override
    protected List<Modifier> getGameTextLightSideWhileActiveModifiers(String playerOnLightSideOfLocation, SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();

        // ANY rifle here (light or dark) may fire at targets at any exterior Endor site
        Filter riflesHere = Filters.and(Filters.rifle, Filters.here(self));
        modifiers.add(new MayTargetAtAnyExteriorSiteOnPlanetModifier(self, riflesHere, Title.Endor));

        // Light Side Force drains here are immune to Battle Order
        modifiers.add(new ForceDrainImmuneToBattleOrderModifier(self, self, game.getLightPlayer()));

        return modifiers;
    }

    @Override
    protected List<Modifier> getGameTextDarkSideWhileActiveModifiers(String playerOnDarkSideOfLocation, SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();

        // ANY rifle here (light or dark) may fire at targets at any exterior Endor site
        Filter riflesHere = Filters.and(Filters.rifle, Filters.here(self));
        modifiers.add(new MayTargetAtAnyExteriorSiteOnPlanetModifier(self, riflesHere, Title.Endor));

        // Dark Side Force drains here are immune to Battle Plan
        modifiers.add(new ForceDrainImmuneToBattlePlanModifier(self, self, game.getDarkPlayer()));

        return modifiers;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextLightSideRequiredAfterTriggers(String playerOnLightSideOfLocation, SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        // ANY combat vehicles and starships here (light or dark) are lost
        if (TriggerConditions.isTableChanged(game, effectResult)) {
            Filter combatVehiclesAndStarshipsHere = Filters.and(
                    Filters.or(Filters.combat_vehicle, Filters.starship),
                    Filters.here(self)
            );

            Collection<PhysicalCard> cardsToLose = Filters.filterActive(game, self, combatVehiclesAndStarshipsHere);
            if (!cardsToLose.isEmpty()) {
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setSingletonTrigger(true);
                action.setText("Make combat vehicles and starships lost");
                action.setActionMsg("Make combat vehicles and starships here lost");
                action.appendEffect(
                        new LoseCardsFromTableEffect(action, cardsToLose));
                return Collections.singletonList(action);
            }
        }
        return null;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextDarkSideRequiredAfterTriggers(String playerOnDarkSideOfLocation, SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        // ANY combat vehicles and starships here (light or dark) are lost
        if (TriggerConditions.isTableChanged(game, effectResult)) {
            Filter combatVehiclesAndStarshipsHere = Filters.and(
                    Filters.or(Filters.combat_vehicle, Filters.starship),
                    Filters.here(self)
            );

            Collection<PhysicalCard> cardsToLose = Filters.filterActive(game, self, combatVehiclesAndStarshipsHere);
            if (!cardsToLose.isEmpty()) {
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setSingletonTrigger(true);
                action.setText("Make combat vehicles and starships lost");
                action.setActionMsg("Make combat vehicles and starships here lost");
                action.appendEffect(
                        new LoseCardsFromTableEffect(action, cardsToLose));
                return Collections.singletonList(action);
            }
        }
        return null;
    }
}
