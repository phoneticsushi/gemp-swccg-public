package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractAlien;
import com.gempukku.swccgo.cards.AbstractPermanentDevice;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.UsePermanentDeviceEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.DrawDestinyEffect;
import com.gempukku.swccgo.logic.effects.LoseCardFromTableEffect;
import com.gempukku.swccgo.logic.effects.RespondableEffect;
import com.gempukku.swccgo.logic.effects.TargetCardOnTableEffect;
import com.gempukku.swccgo.logic.modifiers.DefinedByGameTextLandspeedModifier;
import com.gempukku.swccgo.logic.modifiers.DeployCostToLocationModifier;
import com.gempukku.swccgo.logic.modifiers.ImmuneToAttritionLessThanModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotBeTargetedByPermanentWeaponsModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.GuiUtils;

/**
* Set: BEEZER_BOWL_2025
* Type: CHARACTER_ALIEN
* Title: Gracca
*/
public class Card701_006 extends AbstractAlien {
    public Card701_006() {
        super(Side.DARK, 2, 5, 5, 3, 6, "Gracca", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Winged sorcerer. Invaded the Bright Tree Village and stole the Crystal Cloak, enabling him to crystalize anything he touched.");
        setGameText("Deploys -1 to any Endor mountain site.  May 'fly' (landspeed = 2).  Permanent device is •Crystal Cloak (during battle, may target opponent's device or character weapon present; draw destiny; if destiny = target's destiny number, target is lost). Immune to [Permanent Weapon] and attrition < 4.");
        addIcons(Icon.BEEZER_BOWL_2025, Icon.PERMANENT_DEVICE, Icon.WARRIOR);
        addKeywords(Keyword.SORCEROR);
        addPersonas(Persona.GRACCA);
        // Note that Gracca has no known species in any canon
    }

    @Override
    protected List<Modifier> getGameTextAlwaysOnModifiers(SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Deploys -1 to any Endor mountain site
        modifiers.add(new DeployCostToLocationModifier(self, -1, Filters.and(Filters.partOfSystem(Title.Endor), Filters.icon(Icon.MOUNTAIN_SITE))));
        return modifiers;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        // May 'fly' (landspeed = 2)
        modifiers.add(new DefinedByGameTextLandspeedModifier(self, 2));
        // Immune to [Permanent Weapon]
        modifiers.add(new MayNotBeTargetedByPermanentWeaponsModifier(self));
        // Immune to attrition < 4
        modifiers.add(new ImmuneToAttritionLessThanModifier(self, Filters.hasAttached(self), 4));
        return modifiers;
    }

    @Override
    protected AbstractPermanentDevice getGameTextPermanentDevice() {

        // Permanent device is •Crystal Cloak
        AbstractPermanentDevice permanentDevice = new AbstractPermanentDevice(Persona.CRYSTAL_CLOAK) {
            @Override
            public List<TopLevelGameTextAction> getPermanentDeviceTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self) {
                GameTextActionId gameTextActionId = GameTextActionId.GRACCA__PERMANENT_DEVICE_CRYSTAL_CLOAK;

                Filter targetFilter = Filters.and(
                    // may target opponent's
                    Filters.opponents(self),
                    // device or character weapon
                    Filters.or(Filters.device, Filters.character_weapon),
                    // present
                    Filters.present(self)
                );

                // during battle
                if (GameConditions.isInBattle(game, self) && GameConditions.canTarget(game, self, targetFilter)) {
                    final TopLevelGameTextAction action = new TopLevelGameTextAction(self, self.getCardId(), gameTextActionId);
                    action.setText("Use Crystal Cloak");

                    action.appendTargeting(
                        new TargetCardOnTableEffect(action, playerId, "Choose device or character weapon", targetFilter) {
                            @Override
                            protected void cardTargeted(final int targetGroupId, final PhysicalCard targetedCard) {
                                action.addAnimationGroup(targetedCard);
                                // Allow response(s)
                                action.allowResponses("Targeting " + GameUtils.getCardLink(targetedCard),
                                    new RespondableEffect(action) {
                                        @Override
                                        protected void performActionResults(Action targetingAction) {
                                            // Get the targeted card(s) from the action using the targetGroupId.
                                            // This needs to be done in case the target(s) were changed during the responses.
                                            final PhysicalCard finalTarget = action.getPrimaryTargetCard(targetGroupId);
                                            // Update usage limit(s)
                                            action.appendUsage(
                                                    new UsePermanentDeviceEffect(action, self));
                                            // Perform result(s)
                                            action.appendEffect(
                                                // draw destiny
                                                new DrawDestinyEffect(action, playerId) {
                                                    @Override
                                                    protected void destinyDraws(SwccgGame game, List<PhysicalCard> destinyCardDraws, List<Float> destinyDrawValues, Float totalDestiny) {
                                                        GameState gameState = game.getGameState();
                                                        if (totalDestiny == null) {
                                                            gameState.sendMessage("Result: Failed due to failed destiny draw");
                                                            return;
                                                        }

                                                        float targetDestiny = game.getModifiersQuerying().getDestiny(gameState, finalTarget);
                                                        gameState.sendMessage("Drawn Destiny: " + GuiUtils.formatAsString(totalDestiny));
                                                        gameState.sendMessage("Target's Destiny: " + GuiUtils.formatAsString(targetDestiny));

                                                        // if destiny = target's destiny number,
                                                        if (totalDestiny == targetDestiny) {
                                                            gameState.sendMessage("Result: Succeeded");
                                                            // target is lost
                                                            action.appendEffect(new LoseCardFromTableEffect(action, finalTarget));
                                                        } else {
                                                            gameState.sendMessage("Result: Failed");
                                                        }
                                                    }
                                                }
                                            );
                                        }
                                    }
                                );
                            }
                        }
                    );
                    return Collections.singletonList(action);
                }
                return null;
            }
        };

        return permanentDevice;
    }
}
