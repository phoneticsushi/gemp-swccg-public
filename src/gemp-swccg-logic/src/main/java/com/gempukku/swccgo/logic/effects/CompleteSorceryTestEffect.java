package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.common.CharacterTestStatus;
import com.gempukku.swccgo.common.TargetId;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.timing.AbstractSuccessfulEffect;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.results.SorceryTestCompletedResult;


/**
 * An effect that completes the specified Sorcery Test.
 */
public class CompleteSorceryTestEffect extends AbstractSuccessfulEffect {
    private PhysicalCard _sorceryTest;

    /**
     * Creates an effect that completes the specified Sorcery Test.
     * @param action the action performing this effect
     * @param sorceryTest the Sorcery Test to complete
     */
    public CompleteSorceryTestEffect(Action action, PhysicalCard sorceryTest) {
        super(action);
        _sorceryTest = sorceryTest;
    }

    @Override
    protected void doPlayEffect(SwccgGame game) {
        GameState gameState = game.getGameState();

        if (_sorceryTest.getCharacterTestStatus() != CharacterTestStatus.COMPLETED) {
            PhysicalCard apprentice = _sorceryTest.getTargetedCard(gameState, TargetId.SORCERY_TEST_APPRENTICE);
            gameState.sendMessage(GameUtils.getCardLink(_sorceryTest) + " is 'completed' by " + GameUtils.getCardLink(apprentice));
            _sorceryTest.setCharacterTestStatus(CharacterTestStatus.COMPLETED);

            // Jedi Tests update this global in the modifiers, but it seems to be never read.
            // pretty sure it doesn't apply to Sorcery Tests, so didn't bother to implement it...
            // modifiersQuerying.completedSorceryTest(_sorceryTest, apprentice);

            game.getActionsEnvironment().emitEffectResult(new SorceryTestCompletedResult(_action, _sorceryTest, apprentice));
        }
    }
}
