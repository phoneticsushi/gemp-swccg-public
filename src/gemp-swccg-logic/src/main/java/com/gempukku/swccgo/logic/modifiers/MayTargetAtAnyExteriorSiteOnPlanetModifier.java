package com.gempukku.swccgo.logic.modifiers;

import com.gempukku.swccgo.common.Filterable;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgBuiltInCardBlueprint;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.conditions.Condition;
import com.gempukku.swccgo.logic.modifiers.querying.ModifiersQuerying;

/**
 * A modifier that allows specified weapons to target at any exterior site on a specified planet.
 */
public class MayTargetAtAnyExteriorSiteOnPlanetModifier extends AbstractModifier {
    private Filter _weaponFilter;
    private Filter _siteFilter;

    /**
     * Creates a modifier that allows weapons accepted by the weapon filter to target at any exterior site
     * on the specified planet.
     * @param source the source of the modifier
     * @param weaponFilter the weapon filter
     * @param planetTitle the planet title (e.g., Title.Endor)
     */
    public MayTargetAtAnyExteriorSiteOnPlanetModifier(PhysicalCard source, Filterable weaponFilter, String planetTitle) {
        this(source, weaponFilter, planetTitle, null);
    }

    /**
     * Creates a modifier that allows weapons accepted by the weapon filter to target at any exterior site
     * on the specified planet.
     * @param source the source of the modifier
     * @param weaponFilter the weapon filter
     * @param planetTitle the planet title (e.g., Title.Endor)
     * @param condition the condition under which this modifier is in effect
     */
    public MayTargetAtAnyExteriorSiteOnPlanetModifier(PhysicalCard source, Filterable weaponFilter, String planetTitle, Condition condition) {
        super(source, "May target at any exterior " + planetTitle + " site", null, condition, ModifierType.MAY_TARGET_AT_ANY_EXTERIOR_SITE_ON_PLANET);
        _weaponFilter = Filters.and(weaponFilter);
        _siteFilter = Filters.and(Filters.exterior_site, Filters.partOfSystem(planetTitle));
    }

    @Override
    public boolean isAffectedTarget(GameState gameState, ModifiersQuerying modifiersQuerying, PhysicalCard targetCard) {
        return Filters.and(_weaponFilter).accepts(gameState, modifiersQuerying, targetCard);
    }

    @Override
    public boolean isAffectedTarget(GameState gameState, ModifiersQuerying modifiersQuerying, SwccgBuiltInCardBlueprint targetPermanentWeapon) {
        return Filters.and(_weaponFilter).accepts(gameState, modifiersQuerying, targetPermanentWeapon);
    }

    /**
     * Gets the filter for valid target sites.
     * @return the site filter
     */
    public Filter getSiteFilter() {
        return _siteFilter;
    }
}
