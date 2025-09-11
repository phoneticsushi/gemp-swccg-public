package com.gempukku.swccgo.cards;

import com.gempukku.swccgo.common.CardCategory;
import com.gempukku.swccgo.common.CardType;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.PlayCardZoneOption;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Uniqueness;

/**
 * The abstract class providing the common implementation for Artifacts.
 * 
 * An Artifact behaves much like an Effect, except that it is not an effect.
 */
public abstract class AbstractArtifact extends AbstractDeployable {

    /**
     * Creates a blueprint for an Effect.
     * @param side the side of the Force
     * @param destiny the destiny value
     * @param playCardZoneOption the zone option for playing the card, or null if card has multiple play options
     * @param title the card title
     * @param uniqueness the uniqueness
     * @param expansionSet the expansionSet
     * @param rarity the rarity
     */
    protected AbstractArtifact(Side side, Float destiny, String title, Uniqueness uniqueness, ExpansionSet expansionSet, Rarity rarity) {
        super(side, destiny, PlayCardZoneOption.ATTACHED, null, title, uniqueness, expansionSet, rarity);
        setCardCategory(CardCategory.ARTIFACT);
        addCardType(CardType.ARTIFACT);
        addIcon(Icon.ARTIFACT);
    }
}
