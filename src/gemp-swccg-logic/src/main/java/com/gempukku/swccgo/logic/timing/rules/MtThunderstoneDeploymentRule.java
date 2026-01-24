package com.gempukku.swccgo.logic.timing.rules;

import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.logic.modifiers.DeploysAdjacentToLocationModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotDeploySitesBetweenSitesModifier;
import com.gempukku.swccgo.logic.modifiers.querying.ModifiersEnvironment;

/**
 * Enforces the game rule that Mt. Thunderstone Sites may not be separated from each other
 */
public class MtThunderstoneDeploymentRule implements Rule {
    private ModifiersEnvironment _modifiersEnvironment;

    /**
     * Creates a rule that prevents Mt. Thunderstone Sites from being separated from each other
     * @param modifiersEnvironment the modifiers environment
     */
    public MtThunderstoneDeploymentRule(ModifiersEnvironment modifiersEnvironment) {
        _modifiersEnvironment = modifiersEnvironment;
    }

    public void applyRule() {
        _modifiersEnvironment.addAlwaysOnModifier(
            new DeploysAdjacentToLocationModifier(
                null,
                Filters.Mt_Thunderstone_site,
                Filters.Mt_Thunderstone_site,
                true
            )
        );
        _modifiersEnvironment.addAlwaysOnModifier(
            new MayNotDeploySitesBetweenSitesModifier(
                null,
                Filters.Mt_Thunderstone_site,
                Filters.Mt_Thunderstone_site
            )
        );
    }
}
