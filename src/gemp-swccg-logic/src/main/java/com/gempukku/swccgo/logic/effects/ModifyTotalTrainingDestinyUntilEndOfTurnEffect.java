package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.common.DestinyType;
import com.gempukku.swccgo.common.Filterable;
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
public class ModifyTotalTrainingDestinyUntilEndOfTurnEffect extends AbstractSuccessfulEffect {
    private float _modifierAmount;
    private Filterable _testFilter;

    /**
     * Creates an effect that modifies the total training destiny.
     * @param action the action performing this effect.
     * @param filter the cards whose training destiny draws are to be modified
     * @param modifierAmount the amount to modify
     */
    public ModifyTotalTrainingDestinyUntilEndOfTurnEffect(Action action, Filterable testFilter, float modifierAmount) {
        super(action);
        _modifierAmount = modifierAmount;
        _testFilter = testFilter;
    }

    @Override
    protected void doPlayEffect(SwccgGame game) {
        GameState gameState = game.getGameState();
        DrawDestinyState drawDestinyState = gameState.getTopDrawDestinyState();
        if (drawDestinyState != null) {
            DrawDestinyEffect drawDestinyEffect = drawDestinyState.getDrawDestinyEffect();
            if (drawDestinyEffect.getDestinyType() == DestinyType.TRAINING_DESTINY) {

                game.getModifiersEnvironment().addUntilEndOfTurnModifier(
                        new TotalTrainingDestinyModifier(_action.getActionSource(), _testFilter, Math.round(_modifierAmount)));
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
