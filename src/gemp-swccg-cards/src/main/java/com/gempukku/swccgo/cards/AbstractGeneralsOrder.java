package com.gempukku.swccgo.cards;

import com.gempukku.swccgo.common.*;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.*;

/**
 * The abstract class providing the common implementation for General's Orders.
 * Beezer Bowl 2025: New card type similar to Admiral's Orders but requires presence at battleground site.
 */
public abstract class AbstractGeneralsOrder extends AbstractNonLocationPlaysToTable {

    /**
     * Creates a blueprint for a General's Order card.
     * @param side the side of the Force
     * @param destiny the destiny value
     * @param title the card title
     * @param uniqueness the uniqueness
     * @param expansionSet the expansionSet
     * @param rarity the rarity
     */
    protected AbstractGeneralsOrder(Side side, float destiny, String title, Uniqueness uniqueness, ExpansionSet expansionSet, Rarity rarity) {
        super(side, destiny, PlayCardZoneOption.YOUR_SIDE_OF_TABLE, 0f, title, uniqueness, expansionSet, rarity);
        setCardCategory(CardCategory.GENERALS_ORDER);
        addCardType(CardType.GENERALS_ORDER);
        addIcon(Icon.GENERALS_ORDER);
    }

    /**
     * Determines if the card can be played.
     * @param playerId the player
     * @param game the game
     * @param self the card
     * @param deploymentRestrictionsOption specifies which deployment restrictions are to be ignored, or null
     * @param playCardOption the play card option, or null
     * @param reactActionOption a 'react' action option, or null if not a 'react'
     * @return true if card can be played, otherwise false
     */
    @Override
    protected boolean checkPlayRequirements(String playerId, SwccgGame game, PhysicalCard self, DeploymentRestrictionsOption deploymentRestrictionsOption, PlayCardOption playCardOption, ReactActionOption reactActionOption) {
        // General's Order requires presence at a battleground site (unlike Admiral's Order which requires battleground system)
        return super.checkPlayRequirements(playerId, game, self, deploymentRestrictionsOption, playCardOption, reactActionOption)
                && Filters.canSpotFromTopLocationsOnTable(game, Filters.and(Filters.battleground_site, Filters.occupies(playerId)));
    }

    /**
     * Determines if the card can be played during the current phase.
     * @param playerId the player
     * @param game the game
     * @param self the card
     * @return true if card can be played during the current phase, otherwise false
     */
    @Override
    protected boolean canPlayCardDuringCurrentPhase(String playerId, SwccgGame game, PhysicalCard self) {
        return GameConditions.isDuringYourPhase(game, playerId, Phase.DEPLOY);
    }

    /**
     * Determines if the card type, subtype, etc. always plays for free.
     * @return true if card type card type, subtype, etc. always plays for free, otherwise false
     */
    @Override
    protected final boolean isCardTypeAlwaysPlayedForFree() {
        return true;
    }

    /**
     * Determines if this type of card is deployed or played
     * @return true if card is "deployed", false if card is "played"
     */
    @Override
    public final boolean isCardTypeDeployed() {
        return true;
    }
}
