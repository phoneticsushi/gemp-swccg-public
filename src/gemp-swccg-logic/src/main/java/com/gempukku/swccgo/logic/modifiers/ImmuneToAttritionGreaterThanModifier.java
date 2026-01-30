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
 * An "Immune to attrition > X" modifier.
 * 
 * Beezer Bowl 2025: Created to support Sergeant Beezer's "Immune to attrition > 2" ability.
 * 
 * This is the inverse of ImmuneToAttritionLessThanModifier:
 * - "Immune to attrition < 3" = immune to 0, 1, 2; vulnerable to 3+
 * - "Immune to attrition > 2" = immune to 3, 4, 5...; vulnerable to 0, 1, 2
 * 
 * This modifier makes a card immune to attrition values greater than X (i.e., immune to X+1, X+2, etc.)
 * but still vulnerable to attrition values X or less.
 */
public class ImmuneToAttritionGreaterThanModifier extends AbstractModifier {
    private Evaluator _evaluator;

    /**
     * Creates a modifier for "Immune to attrition > X".
     * @param source the card that is the source of the modifier and that is given immunity
     * @param modifierAmount the amount of attrition immune to greater than
     */
    public ImmuneToAttritionGreaterThanModifier(PhysicalCard source, float modifierAmount) {
        this(source, source, null, modifierAmount);
    }

    /**
     * Creates a modifier for "Immune to attrition > X".
     * @param source the source of the modifier
     * @param affectFilter the filter for cards whose immunity to attrition is modified
     * @param modifierAmount the amount of attrition immune to greater than
     */
    public ImmuneToAttritionGreaterThanModifier(PhysicalCard source, Filterable affectFilter, float modifierAmount) {
        this(source, affectFilter, null, modifierAmount);
    }

    /**
     * Creates a modifier for "Immune to attrition > X".
     * @param source the card that is the source of the modifier and that is given immunity
     * @param evaluator the evaluator that calculates the attrition immune to greater than
     */
    public ImmuneToAttritionGreaterThanModifier(PhysicalCard source, Evaluator evaluator) {
        this(source, source, null, evaluator);
    }

    /**
     * Creates a modifier for "Immune to attrition > X".
     * @param source the source of the modifier
     * @param affectFilter the filter for cards whose immunity to attrition is modified
     * @param evaluator the evaluator that calculates the attrition immune to greater than
     */
    public ImmuneToAttritionGreaterThanModifier(PhysicalCard source, Filterable affectFilter, Evaluator evaluator) {
        this(source, affectFilter, null, evaluator);
    }

    /**
     * Creates a modifier for "Immune to attrition > X".
     * @param source the card that is the source of the modifier and that is given immunity
     * @param condition the condition that must be fulfilled for the modifier to be in effect
     * @param modifierAmount the amount of attrition immune to greater than
     */
    public ImmuneToAttritionGreaterThanModifier(PhysicalCard source, Condition condition, float modifierAmount) {
        this(source, source, condition, new ConstantEvaluator(modifierAmount));
    }

    /**
     * Creates a modifier for "Immune to attrition > X".
     * @param source the source of the modifier
     * @param affectFilter the filter for cards whose immunity to attrition is modified
     * @param condition the condition that must be fulfilled for the modifier to be in effect
     * @param modifierAmount the amount of attrition immune to greater than
     */
    public ImmuneToAttritionGreaterThanModifier(PhysicalCard source, Filterable affectFilter, Condition condition, float modifierAmount) {
        this(source, affectFilter, condition, new ConstantEvaluator(modifierAmount));
    }

    /**
     * Creates a modifier for "Immune to attrition > X".
     * @param source the card that is the source of the modifier and that is given immunity
     * @param condition the condition that must be fulfilled for the modifier to be in effect
     * @param evaluator the evaluator that calculates the attrition immune to greater than
     */
    public ImmuneToAttritionGreaterThanModifier(PhysicalCard source, Condition condition, Evaluator evaluator) {
        this(source, source, condition, evaluator);
    }

    /**
     * Creates a modifier for "Immune to attrition > X".
     * @param source the source of the modifier
     * @param affectFilter the filter for cards whose immunity to attrition is modified
     * @param condition the condition that must be fulfilled for the modifier to be in effect
     * @param evaluator the evaluator that calculates the attrition immune to greater than
     */
    public ImmuneToAttritionGreaterThanModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        // NOTE: Requires adding IMMUNITY_TO_ATTRITION_GREATER_THAN to ModifierType.java
        super(source, null, affectFilter, condition, ModifierType.IMMUNITY_TO_ATTRITION_GREATER_THAN, true);
        _evaluator = evaluator;
    }

    @Override
    public String getText(GameState gameState, ModifiersQuerying modifiersQuerying, PhysicalCard self) {
        final float value = _evaluator.evaluateExpression(gameState, modifiersQuerying, self);
        if (value >= 0)
            return "Immune to attrition > " + GuiUtils.formatAsString(value);
        else
            return null;
    }

    @Override
    public float getImmunityToAttritionGreaterThanModifier(GameState gameState, ModifiersQuerying modifiersLogic, PhysicalCard physicalCard) {
        return _evaluator.evaluateExpression(gameState, modifiersLogic, physicalCard);
    }
}
