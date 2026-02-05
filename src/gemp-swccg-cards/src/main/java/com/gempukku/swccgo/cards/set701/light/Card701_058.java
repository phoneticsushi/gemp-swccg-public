package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractRebel;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerTurnEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
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
import com.gempukku.swccgo.logic.effects.ModifyDestinyEffect;
import com.gempukku.swccgo.logic.effects.choose.DeployCardToLocationFromLostPileEffect;
import com.gempukku.swccgo.logic.effects.choose.DeployCardToLocationFromReserveDeckEffect;
import com.gempukku.swccgo.logic.modifiers.MayNotBeTargetedByModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotGoMissingModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

import java.util.LinkedList;
import java.util.List;


/**
 * Set: Beezer Bowl 2025
 * Type: Character
 * Subtype: Rebel
 * Title: Sergeant Beezer & Sergeant Junkin
 */
public class Card701_058 extends AbstractRebel {
    public Card701_058() {
        super(Side.LIGHT, 3, 4, 6, 4, 7, "Sergeant Beezer & Sergeant Junkin", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        addComboCardTitles("Sergeant Beezer", "Sergeant Junkin");
        setLore("Dansra proposed to Carl after completing the mission on Mount Krana, culminating the romance of two mountaineer scout commandos which started during the battle of Endor.");
        setGameText("Deploys only via Back To Base (replaces both Sergeants; Beezer is placed out of play). Once per turn, during your deploy phase or as a 'react', may deploy (for free) any device or weapon here from Reserve Deck or Lost Pile. May add or subtract up to 2 to total destiny of any weapon they fire, and your characters are immune to its effects. Cannot be missing.");
        addPersona(Persona.BEEZER);
        addPersona(Persona.JUNKIN);
        addIcons(Icon.BEEZER_BOWL_2025, Icon.ENDOR, Icon.WARRIOR, Icon.WARRIOR);
        addKeywords(Keyword.MOUNTAINEER, Keyword.SCOUT, Keyword.SERGEANT);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        // Cannot deploy normally - only via Back To Base objective action
        return Filters.none;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Cannot be missing
        modifiers.add(new MayNotGoMissingModifier(self));
        // Your characters are immune to weapons fired by this card
        modifiers.add(new MayNotBeTargetedByModifier(self, Filters.and(Filters.your(self), Filters.character), Filters.weaponBeingFiredBy(self)));
        return modifiers;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();
        
        GameTextActionId gameTextActionId = GameTextActionId.SERGEANT_BEEZER_AND_SERGEANT_JUNKIN__DEPLOY_DEVICE_OR_WEAPON;
        Filter deviceOrWeaponFilter = Filters.or(Filters.device, Filters.weapon);
        
        // During your deploy phase, may deploy (for free) any device or weapon here from Reserve Deck
        if (GameConditions.isOnceDuringYourPhase(game, self, playerId, gameTextSourceCardId, gameTextActionId, Phase.DEPLOY)
                && GameConditions.canDeployCardFromReserveDeck(game, playerId, self, gameTextActionId)) {
            
            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Deploy device/weapon from Reserve Deck");
            action.setActionMsg("Deploy a device or weapon here from Reserve Deck");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerTurnEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new DeployCardToLocationFromReserveDeckEffect(action, deviceOrWeaponFilter, Filters.here(self), true, true));
            actions.add(action);
        }
        
        // During your deploy phase, may deploy (for free) any device or weapon here from Lost Pile
        if (GameConditions.isOnceDuringYourPhase(game, self, playerId, gameTextSourceCardId, gameTextActionId, Phase.DEPLOY)
                && GameConditions.canDeployCardFromLostPile(game, playerId, self, gameTextActionId)) {
            
            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Deploy device/weapon from Lost Pile");
            action.setActionMsg("Deploy a device or weapon here from Lost Pile");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerTurnEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new DeployCardToLocationFromLostPileEffect(action, deviceOrWeaponFilter, Filters.here(self), true));
            actions.add(action);
        }
        
        return actions;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        List<OptionalGameTextTriggerAction> actions = new LinkedList<OptionalGameTextTriggerAction>();
        
        GameTextActionId gameTextActionId1 = GameTextActionId.SERGEANT_BEEZER_AND_SERGEANT_JUNKIN__DEPLOY_DEVICE_OR_WEAPON;
        Filter deviceOrWeaponFilter = Filters.or(Filters.device, Filters.weapon);
        
        // As a 'react', may deploy (for free) any device or weapon here from Reserve Deck
        if (TriggerConditions.battleInitiatedAt(game, effectResult, Filters.here(self))
                && GameConditions.isOncePerTurn(game, self, playerId, gameTextSourceCardId, gameTextActionId1)
                && GameConditions.canDeployCardFromReserveDeck(game, playerId, self, gameTextActionId1, true)) {
            
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId1);
            action.setText("Deploy device/weapon from Reserve Deck as a 'react'");
            action.setActionMsg("Deploy a device or weapon here from Reserve Deck as a 'react'");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerTurnEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new DeployCardToLocationFromReserveDeckEffect(action, deviceOrWeaponFilter, Filters.here(self), true, true));
            actions.add(action);
        }
        
        // As a 'react', may deploy (for free) any device or weapon here from Lost Pile
        if (TriggerConditions.battleInitiatedAt(game, effectResult, Filters.here(self))
                && GameConditions.isOncePerTurn(game, self, playerId, gameTextSourceCardId, gameTextActionId1)
                && GameConditions.canDeployCardFromLostPile(game, playerId, self, gameTextActionId1)) {
            
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId1);
            action.setText("Deploy device/weapon from Lost Pile as a 'react'");
            action.setActionMsg("Deploy a device or weapon here from Lost Pile as a 'react'");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerTurnEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new DeployCardToLocationFromLostPileEffect(action, deviceOrWeaponFilter, Filters.here(self), true));
            actions.add(action);
        }
        
        // May add or subtract up to 2 to total destiny of any weapon they fire
        // Check if weapon destiny just drawn for any weapon fired by this card
        if (TriggerConditions.isWeaponDestinyJustDrawn(game, effectResult, Filters.any, self)) {
            
            // Add 2
            OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Add 2 to weapon destiny");
            action.appendEffect(
                    new ModifyDestinyEffect(action, 2));
            actions.add(action);
            
            // Add 1
            action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Add 1 to weapon destiny");
            action.appendEffect(
                    new ModifyDestinyEffect(action, 1));
            actions.add(action);
            
            // Subtract 1
            action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Subtract 1 from weapon destiny");
            action.appendEffect(
                    new ModifyDestinyEffect(action, -1));
            actions.add(action);
            
            // Subtract 2
            action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
            action.setText("Subtract 2 from weapon destiny");
            action.appendEffect(
                    new ModifyDestinyEffect(action, -2));
            actions.add(action);
        }
        
        return actions;
    }
}
