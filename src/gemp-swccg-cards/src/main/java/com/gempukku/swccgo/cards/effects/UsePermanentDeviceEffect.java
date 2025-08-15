package com.gempukku.swccgo.cards.effects;

import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.timing.AbstractStandardEffect;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.UsageEffect;


public class UsePermanentDeviceEffect extends AbstractStandardEffect implements UsageEffect {
    // This represents a Permanent Device, so the owner is both the user
    // and the card containing the device's game text
    private PhysicalCard _owner;

    public UsePermanentDeviceEffect(Action action, PhysicalCard owner) {
        super(action);
        _owner = owner;
    }

    @Override
    public boolean isPlayableInFull(SwccgGame game) {
        // Assumptions (see AbstractNonLocationPlaysToTable.getTopLevelActions() for context):
        //   permanent device actions are only presented when the permanent device can be used
        //   the owner of the device is the user, who is able to use it by definition
        //   the device can't be transferred
        //
        // So, we just need to check if the card actually has a permanent device
        return _owner.getBlueprint().getPermanentDevice(_owner) != null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(SwccgGame game) {
        if (isPlayableInFull(game)) {
            game.getModifiersQuerying().markPermanentDeviceUsedByOwner(_owner);
            return new FullEffectResult(true);
        }

        return new FullEffectResult(false);
    }
}
