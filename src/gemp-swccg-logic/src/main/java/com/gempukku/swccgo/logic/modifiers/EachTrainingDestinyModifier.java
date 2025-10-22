package com.gempukku.swccgo.logic.modifiers;

import com.gempukku.swccgo.common.Filterable;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.conditions.Condition;
import com.gempukku.swccgo.logic.evaluators.ConstantEvaluator;
import com.gempukku.swccgo.logic.evaluators.Evaluator;
import com.gempukku.swccgo.logic.modifiers.querying.ModifiersQuerying;
import com.gempukku.swccgo.logic.timing.GuiUtils;

/**
 * A modifier that affects each training destiny.
 */
public class EachTrainingDestinyModifier extends AbstractModifier {
    private Evaluator _evaluator;

    /**
     * Creates a modifier that affects each training destiny.
     * @param source the source of the modifier
     * @param testFilter the filter for cards whose training destiny is modified, i.e. Jedi or Sorcery Tests
     * @param modifierAmount the amount of the modifier
     */
    public EachTrainingDestinyModifier(PhysicalCard source, Filterable testFilter, int modifierAmount) {
        this(source, testFilter, null, new ConstantEvaluator(modifierAmount));
    }

    /**
     * Creates a modifier that affects each training destiny.
     * @param source the source of the modifier
     * @param testFilter the filter for cards whose training destiny is modified, i.e. Jedi or Sorcery Tests
     * @param condition the condition that must be fulfilled for the modifier to be in effect
     * @param modifierAmount the amount of the modifier
     */
    public EachTrainingDestinyModifier(PhysicalCard source, Filterable testFilter, Condition condition, int modifierAmount) {
        this(source, testFilter, condition, new ConstantEvaluator(modifierAmount));
    }

    /**
     * Creates a modifier that affects each training destiny.
     * @param source the source of the modifier
     * @param testFilter the filter for cards whose training destiny is modified, i.e. Jedi or Sorcery Tests
     * @param evaluator the evaluator that calculates the amount of the modifier
     */
    public EachTrainingDestinyModifier(PhysicalCard source, Filterable testFilter, Evaluator evaluator) {
        this(source, testFilter, null, evaluator);
    }

    /**
     * Creates a modifier that affects each training destiny.
     * @param source the source of the modifier
     * @param testFilter the filter for cards whose training destiny is modified, i.e. Jedi or Sorcery Tests
     * @param condition the condition that must be fulfilled for the modifier to be in effect
     * @param evaluator the evaluator that calculates the amount of the modifier
     */
    public EachTrainingDestinyModifier(PhysicalCard source, Filterable testFilter, Condition condition, Evaluator evaluator) {
        super(source, null, testFilter, condition, ModifierType.EACH_TRAINING_DESTINY, false);
        _evaluator = evaluator;
    }

    @Override
    public String getText(GameState gameState, ModifiersQuerying modifiersQuerying, PhysicalCard self) {
        final float value = _evaluator.evaluateExpression(gameState, modifiersQuerying, self);

        if (value >= 0)
            return "Each training destiny +" + GuiUtils.formatAsString(value);
        else
            return "Each training destiny " + GuiUtils.formatAsString(value);
    }

    @Override
    public float getValue(GameState gameState, ModifiersQuerying modifiersQuerying, PhysicalCard physicalCard) {
        return _evaluator.evaluateExpression(gameState, modifiersQuerying, physicalCard);
    }
}
