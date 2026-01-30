package com.gempukku.swccgo.cards.set701.light;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractAlien;
import com.gempukku.swccgo.cards.effects.CancelAttackEffect;
import com.gempukku.swccgo.cards.evaluators.MinEvaluator;
import com.gempukku.swccgo.cards.evaluators.PresentEvaluator;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.PlayCardOptionId;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Species;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.DrawDestinyEffect;
import com.gempukku.swccgo.logic.effects.LoseForceEffect;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.TotalBattleDestinyModifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
 * Set: Beezer Bowl 2025
 * Type: Character
 * Subtype: Alien
 * Title: Latara
 */
public class Card701_043 extends AbstractAlien {
    public Card701_043() {
        super(Side.LIGHT, 3, 2, 2, 2, 4, "Latara", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("Best friends with princess Kneesaa. Uses her flirtatious personality to get what she wants. Musician and fashion expert.");
        setGameText("Deploys only on Endor. Your total battle destiny at same site is +1 for each of your Ewok/Rebel pairs present. Permanent device is \u2022Latara's Flute (if an attack was just initiated at same site, may draw destiny; if destiny < 3, attack is canceled and you must lose 1 Force).");
        addIcons(Icon.PERMANENT_DEVICE, Icon.BEEZER_BOWL_2025);
        setSpecies(Species.EWOK);
        addKeyword(Keyword.MUSICIAN);
        addKeyword(Keyword.FEMALE);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.Deploys_on_Endor;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        // Your total battle destiny at same site is +1 for each of your Ewok/Rebel pairs present
        modifiers.add(new TotalBattleDestinyModifier(self, Filters.sameSite(self),
                new MinEvaluator(new PresentEvaluator(self, Filters.and(Filters.your(self), Filters.Ewok)),
                        new PresentEvaluator(self, Filters.and(Filters.your(self), Filters.Rebel))), self.getOwner()));
        return modifiers;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, final SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.OTHER_CARD_ACTION_1;

        // Check if an attack was just initiated at same site
        if (TriggerConditions.attackInitiatedAt(game, effectResult, Filters.sameSite(self))) {
            final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Draw destiny to cancel attack");
            action.setActionMsg("Draw destiny to cancel the attack");

            // Draw destiny
            action.appendEffect(
                    new DrawDestinyEffect(action, playerId) {
                        @Override
                        protected void destinyDraws(SwccgGame game, List<PhysicalCard> destinyCardDraws, List<Float> destinyDrawValues, Float totalDestiny) {
                            GameState gameState = game.getGameState();
                            if (totalDestiny == null) {
                                gameState.sendMessage("Result: Failed due to failed destiny draw");
                                return;
                            }

                            gameState.sendMessage("Destiny: " + totalDestiny);

                            if (totalDestiny < 3) {
                                gameState.sendMessage("Result: Attack canceled");
                                // Cancel the attack
                                action.appendEffect(new CancelAttackEffect(action));
                                // Must lose 1 Force
                                action.appendEffect(new LoseForceEffect(action, playerId, 1));
                            } else {
                                gameState.sendMessage("Result: Attack not canceled");
                            }
                        }
                    }
            );
            return Collections.singletonList(action);
        }
        return null;
    }
}
