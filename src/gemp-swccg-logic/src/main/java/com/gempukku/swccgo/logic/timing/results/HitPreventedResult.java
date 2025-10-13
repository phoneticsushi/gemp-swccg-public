package com.gempukku.swccgo.logic.timing.results;

import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgBuiltInCardBlueprint;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
 * The effect result that is emitted when a card was about to be 'hit', but the hit was prevented from happening.
 * Flow is: AboutToBeHitResult -> (HitResult OR HitAvertedResult)
 */
public class HitPreventedResult extends EffectResult {
    private PhysicalCard _cardPreventedFromBeingHit;
    private PhysicalCard _cardAttemptingHit;
    private SwccgBuiltInCardBlueprint _permanentWeaponAttemptingHit;
    private PhysicalCard _cardFiringWeapon;

    /**
     * Creates an effect result that is emitted when a card is 'hit'.
     * @param cardPreventedFromBeingHit the card that was almost hit
     * @param cardAttemptingHit the card that attempted the hit
     * @param permanentWeaponAttemptingHit the permanent weapon that attempted the hit, or null
     * @param cardFiringWeapon the card that fired the weapon that attempted the hit, or null
     * @param hitByRepeatedFiring true if the weapon was firing repeatedly when it hit
     */
    public HitPreventedResult(PhysicalCard cardPreventedFromBeingHit, PhysicalCard cardAttemptingHit, SwccgBuiltInCardBlueprint permanentWeaponAttemptingHit, PhysicalCard cardFiringWeapon) {
        super(Type.HIT_PREVENTED, cardAttemptingHit.getOwner());
        _cardPreventedFromBeingHit = cardPreventedFromBeingHit;
        _cardAttemptingHit = cardAttemptingHit;
        _permanentWeaponAttemptingHit = permanentWeaponAttemptingHit;
        _cardFiringWeapon = cardFiringWeapon;
    }

    /**
     * Gets the card that was 'hit'.
     * @return the card that was 'hit'
     */
    public PhysicalCard getCardPreventedFromBeingHit() {
        return _cardPreventedFromBeingHit;
    }

    /**
     * Gets the card that attempted the hit.
     * @return the card that attempted the hit
     */
    public PhysicalCard getCardAttemptingHit() {
        return _cardAttemptingHit;
    }

    /**
     * Gets the permanent weapon that attempted the hit, or null.
     * @return the permanent weapon that attempted the hit, or null
     */
    public SwccgBuiltInCardBlueprint getPermanentWeaponAttemptingHit() {
        return _permanentWeaponAttemptingHit;
    }

    /**
     * Gets the card that fired the weapon that attempted the hit, or null
     * @return the card that fired the weapon that attempted the hit, or null
     */
    public PhysicalCard getCardFiringWeapon() {
        return _cardFiringWeapon;
    }

    /**
     * Gets the text to show to describe the effect result.
     * @param game the game
     * @return the text
     */
    @Override
    public String getText(SwccgGame game) {
        return "'Hit Prevented' " + GameUtils.getCardLink(_cardPreventedFromBeingHit);
    }
}
