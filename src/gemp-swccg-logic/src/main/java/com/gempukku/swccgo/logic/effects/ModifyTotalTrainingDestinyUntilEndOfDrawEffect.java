package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.common.DestinyType;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.DrawDestinyState;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.modifiers.TotalTrainingDestinyModifier;
import com.gempukku.swccgo.logic.timing.AbstractSuccessfulEffect;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.GuiUtils;

/**
 * An effect that modifies the total training destiny until the destiny draw is complete.
 */
public class ModifyTotalTrainingDestinyUntilEndOfDrawEffect extends AbstractSuccessfulEffect {
    private float _modifierAmount;

    /**
     * Creates an effect that modifies the total training destiny.
     * @param action the action performing this effect.
     * @param modifierAmount the amount to modify
     */
    public ModifyTotalTrainingDestinyUntilEndOfDrawEffect(Action action, float modifierAmount) {
        super(action);
        _modifierAmount = modifierAmount;
    }

    @Override
    protected void doPlayEffect(SwccgGame game) {
        GameState gameState = game.getGameState();
        DrawDestinyState drawDestinyState = gameState.getTopDrawDestinyState();
        if (drawDestinyState != null) {
            DrawDestinyEffect drawDestinyEffect = drawDestinyState.getDrawDestinyEffect();
            if (drawDestinyEffect.getDestinyType() == DestinyType.TRAINING_DESTINY) {

                game.getModifiersEnvironment().addUntilEndOfDrawDestinyModifier(
                        // Assumption is that the modifier is used when resolving the training destiny, and then becomes inactive,
                        // so this can only apply to the current draw, hence "Filters.any"...
                        // No idea why this modifier takes an int when destiny is generally a float, but sure...
                        new TotalTrainingDestinyModifier(_action.getActionSource(), Filters.any, Math.round(_modifierAmount)));
                if (_modifierAmount > 0) {
                    gameState.sendMessage(_action.getPerformingPlayer() + " adds " + GuiUtils.formatAsString(_modifierAmount) + " to total training destiny");
                }
                else if (_modifierAmount < 0) {
                    gameState.sendMessage(_action.getPerformingPlayer() + " subtracts " + GuiUtils.formatAsString(-_modifierAmount) + " from total training destiny");
                }
                game.getGameState().sendMessage(drawDestinyEffect.getPlayerDrawingDestiny() + "'s total training destiny drawn is " + GuiUtils.formatAsString(drawDestinyEffect.getTotalDestiny(game)));
            }
        }
    }
}
