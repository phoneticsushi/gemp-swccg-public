package com.gempukku.swccgo.logic.timing.results;

import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
 * This effect result is triggered when a Sorcery Test is completed.
 */
public class SorceryTestCompletedResult extends EffectResult {
    private PhysicalCard _sorceryTest;
    private PhysicalCard _apprentice;

    /**
     * Creates an effect result that is triggered during a battle when the battle has just ended.
     * @param action the action performing this effect result
     * @param sorceryTest the Sorcery Test that was completed
     * @param apprentice the apprentice that completed the Sorcery Test
     */
    public SorceryTestCompletedResult(Action action, PhysicalCard sorceryTest, PhysicalCard apprentice) {
        super(Type.SORCERY_TEST_COMPLETED, action.getPerformingPlayer());
        _sorceryTest = sorceryTest;
        _apprentice = apprentice;
    }

    /**
     * Gets the Sorcery Test that was completed.
     * @return the Sorcery Test
     */
    public PhysicalCard getSorceryTest() {
        return _sorceryTest;
    }

    /**
     * Gets the apprentice that completed the Sorcery Test.
     * @return the apprentice that completed the Sorcery Test
     */
    public PhysicalCard getCompletedBy() {
        return _apprentice;
    }

    /**
     * Gets the text to show to describe the effect result.
     * @param game the game
     * @return the text
     */
    @Override
    public String getText(SwccgGame game) {
        return "Sorcery Test, " + GameUtils.getCardLink(_sorceryTest) + ", just completed";
    }
}
