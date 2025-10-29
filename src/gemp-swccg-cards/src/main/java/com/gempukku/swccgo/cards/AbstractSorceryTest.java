package com.gempukku.swccgo.cards;

import com.gempukku.swccgo.common.CardCategory;
import com.gempukku.swccgo.common.CardType;
import com.gempukku.swccgo.common.CharacterTestStatus;
import com.gempukku.swccgo.common.DestinyType;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.PlayCardZoneOption;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.TargetId;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.PlayCardOption;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.CompleteSorceryTestEffect;
import com.gempukku.swccgo.logic.effects.DrawDestinyEffect;
import com.gempukku.swccgo.logic.effects.LoseForceEffect;
import com.gempukku.swccgo.logic.effects.TargetCardOnTableEffect;
import com.gempukku.swccgo.logic.effects.choose.ExchangeCardInHandWithCardInLostPileEffect;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.FailCostEffect;
import com.gempukku.swccgo.logic.timing.GuiUtils;
import com.gempukku.swccgo.logic.timing.TargetingEffect;
import java.util.LinkedList;
import java.util.List;


/**
 * The abstract class providing the common implementation for Sorcery Tests.
 */
public abstract class AbstractSorceryTest extends AbstractDeployable {

    /**
     * Creates a blueprint for a Sorcery Test.
     * @param side the side of the Force
     * @param destiny the destiny value
     * @param title the card title
     * @param expansionSet the expansionSet
     * @param rarity the rarity
     */
    protected AbstractSorceryTest(Side side, float destiny, String title, ExpansionSet expansionSet, Rarity rarity) {
        this(side, destiny, PlayCardZoneOption.ATTACHED, title, expansionSet, rarity);
    }

    /**
     * Creates a blueprint for a Sorcery Test.
     * @param side the side of the Force
     * @param destiny the destiny value
     * @param playCardZoneOption the zone option for playing the card, or null if card has multiple play options
     * @param title the card title
     * @param expansionSet the expansionSet
     * @param rarity the rarity
     */
    protected AbstractSorceryTest(Side side, float destiny, PlayCardZoneOption playCardZoneOption, String title, ExpansionSet expansionSet, Rarity rarity) {
        super(side, destiny, playCardZoneOption, 0f, title, Uniqueness.UNRESTRICTED, expansionSet, rarity);
        setCardCategory(CardCategory.SORCERY_TEST);
        addCardType(CardType.SORCERY_TEST);
        addIcon(Icon.SORCERY_TEST);
    }

    /**
     * Determines if the given sorcery test targets a mentor.
     * @return true or false
     */
    protected abstract boolean targetsMentor();

    /**
    /**
     * Gets effects (to be performed in order) that set any targeted cards when the card is being deployed.
     * @param action the action to perform the effect
     * @param playerId the performing player
     * @param game the game
     * @param self the card
     * @param target the target to where the card is being deployed, or null (if side of table or card pile)
     * @param playCardOption the play card option chosen
     * @return the targeting effects, or null
     */
    @Override
    public List<TargetingEffect> getTargetCardsWhenDeployedEffects(final Action action, final String playerId, final SwccgGame game, final PhysicalCard self, final PhysicalCard target, PlayCardOption playCardOption) {
        final GameState gameState = game.getGameState();

        List<TargetingEffect> targetingEffects = new LinkedList<TargetingEffect>();

        // Not all sorcery tests target a mentor, so some cards may skip this section...
        if (targetsMentor()) {
            // Target mentor
            final Filter mentorTargetFilter = getValidMentorFilter(playerId, game, self, target);

            final TargetingEffect targetingEffect = new TargetCardOnTableEffect(action, playerId, "Choose mentor", mentorTargetFilter) {
                @Override
                protected void cardTargeted(int targetGroupId1, final PhysicalCard mentor) {
                    action.addAnimationGroup(mentor);
                    self.setTargetedCard(TargetId.SORCERY_TEST_MENTOR, targetGroupId1, mentor, mentorTargetFilter);

                    // ...then check if a valid apprentice to target can be found
                    final Filter apprenticeToTargetFilter = getValidApprenticeFilter(playerId, game, self, target, mentor);

                    if (Filters.canSpot(game, self, apprenticeToTargetFilter)) {
                        action.appendTargeting(
                                new TargetCardOnTableEffect(action, playerId, "Choose apprentice", apprenticeToTargetFilter) {
                                    @Override
                                    protected void cardTargeted(int targetGroupId2, PhysicalCard apprentice) {
                                        action.addAnimationGroup(apprentice);
                                        self.setTargetedCard(TargetId.SORCERY_TEST_APPRENTICE, targetGroupId2, apprentice, apprenticeToTargetFilter);
                                        gameState.addApprentice(apprentice);
                                    }
                                }
                        );
                    } else {
                        action.appendTargeting(
                                new FailCostEffect(action));
                    }
                }
            };
            targetingEffects.add(targetingEffect);
        }
        else {
            // Target an apprentice only, and not the mentor
            final Filter apprenticeToTargetFilter = getValidApprenticeFilter(playerId, game, self, target, null);

            final TargetingEffect targetingEffect = new TargetCardOnTableEffect(action, playerId, "Choose apprentice", apprenticeToTargetFilter) {
                @Override
                protected void cardTargeted(int targetGroupId2, PhysicalCard apprentice) {
                    action.addAnimationGroup(apprentice);
                    self.setTargetedCard(TargetId.SORCERY_TEST_APPRENTICE, targetGroupId2, apprentice, apprenticeToTargetFilter);
                    gameState.addApprentice(apprentice);
                }
            };
            targetingEffects.add(targetingEffect);
        }

        return targetingEffects;
    }

    // Note: the game text describing these filters is defined on "Teo... SHA!!!",
    // but it applies to every mentor/apprentice selection for Sorcery Tests...
    private Filter getValidMentorFilter(String playerId, SwccgGame game, PhysicalCard self, PhysicalCard deployTarget) {
        // Mentor: Your sorcerer with ability > 3
        return Filters.and(
            Filters.your(playerId),
            Filters.sorcerer,
            Filters.abilityMoreThan(3),
            getGameTextAdditionalMentorFilter(playerId, game, self, deployTarget)
        );
    }
    private Filter getValidApprenticeFilter(String playerId, SwccgGame game, PhysicalCard self, PhysicalCard deployTarget, PhysicalCard mentor) {
        // Apprentice: Your Ewok shaman with lessor ability than mentor
        return Filters.and(
            Filters.your(playerId),
            Filters.Ewok,
            Filters.Shaman,
            Filters.abilityLessThanCard(mentor),
            getGameTextAdditionalApprenticeFilter(playerId, game, self, deployTarget)
        );
    }
    /**
     * This method is overridden by individual cards to specify the filter for valid mentor targets.
     * @param playerId the player
     * @param game the game
     * @param self the card
     * @param deployTarget the card the Sorcery Test is being deployed on
     * @return the filter
     */
    protected abstract Filter getGameTextAdditionalMentorFilter(String playerId, SwccgGame game, PhysicalCard self, PhysicalCard deployTarget);

    /**
     * This method is overridden by individual cards to specify the filter for valid apprentice targets.
     * @param playerId the player
     * @param game the game
     * @param self the sorcery test
     * @param deployTarget the card the Sorcery Test is being deployed on
     * @param mentor the mentor for the apprentice
     * @return the filter
     */
    protected abstract Filter getGameTextAdditionalApprenticeFilter(String playerId, SwccgGame game, PhysicalCard self, PhysicalCard deployTarget);

    /**
     * Helper method that must be called by Sorcery Tests that do a Training Destiny check to pass.
     * Note that this logic implements the following game text from "Once the Sunstar is Mine...":
    // - "Sorcery Test destiny draws are +1 for each sorcerer on table"
    // - "Whenever you draw sorcery training destiny, draw two and choose one"
     * 
     * @param playerId the player attempting the Sorcery Test
     * @param game the game
     * @param self the sorcery test
     * @param thresholdToPass total destiny must be > this value for the test to be considered "passed"
     * @return the filter
     */
    protected OptionalGameTextTriggerAction getGameTextTrainingDestinyAttemptAction(String playerId, SwccgGame game, PhysicalCard self, PhysicalCard apprentice, float thresholdToPass, float opponentForceLossIfPassed) {
        final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, self.getCardId(), GameTextActionId.SORCERY_TEST__ATTEMPT_TEST);
        final boolean isSunstarObjectiveOnTable = Filters.canSpot(game, null, Filters.Once_The_Sunstar_Is_Mine);
        
        int numDestinyDraws;
        if (isSunstarObjectiveOnTable) {
            numDestinyDraws = 2;
        } else {
            numDestinyDraws = 1;
        }

        // Draw training destiny, computed as the sum of:
        //   drawn destiny
        //   + number of Mt. Thunderstone Sites on Table
        //   + Apprentice's Power
        //   + 1 for each sorcerer on table, if "Once the Sunstar Is Mine..." is on table
        //   + 2, if "Zarrak's Medallion" is on table and attached to the mentor associated with this Sorcery Test
        // FIXME: reimplement those last two via modifiers instead of all here, since the game text appears on those cards
        action.appendEffect(
            new DrawDestinyEffect(action, playerId, numDestinyDraws, 1, DestinyType.TRAINING_DESTINY) {
                @Override
                protected void destinyDraws(SwccgGame game, List<PhysicalCard> destinyCardDraws, List<Float> destinyDrawValues, Float totalDestiny) {
                    final int numThunderstoneSites = Filters.countActive(game, self, Filters.Mt_Thunderstone_site);
                    final float apprenticePower = game.getModifiersQuerying().getPower(game.getGameState(), apprentice);
                    final float numSorcerersOnTable = Filters.countActive(game, self, Filters.sorcerer);

                    float destinyFromObjective;
                    if (isSunstarObjectiveOnTable) {
                        destinyFromObjective = numSorcerersOnTable;
                    } else {
                        destinyFromObjective = 0;
                    }
                    
                    final PhysicalCard mentor = self.getTargetedCard(game.getGameState(), TargetId.SORCERY_TEST_MENTOR);
                    final boolean isMedallionOnTableAndAttachedToMentor = Filters.hasAttached(Filters.Zarraks_Medallion).accepts(game.getGameState(), game.getModifiersQuerying(), mentor);

                    float destinyFromMedallion;
                    if (isMedallionOnTableAndAttachedToMentor) {
                        destinyFromMedallion = 2;
                    } else {
                        destinyFromMedallion = 0;
                    }

                    final float trainingDestiny = totalDestiny + numThunderstoneSites + apprenticePower + destinyFromObjective + destinyFromMedallion;
                    if (Float.compare(trainingDestiny, thresholdToPass) > 0) {
                        // Sorcery Test is 'completed'
                        if (opponentForceLossIfPassed > 0) {
                            action.appendEffect(new LoseForceEffect(action, game.getOpponent(playerId), opponentForceLossIfPassed));
                        }

                        action.appendEffect(new CompleteSorceryTestEffect(action, self));
                    }
                }
            }
        );

        return action;
    }

    /**
     * This method is called by getGameTextOptionalAfterTriggers() to determine if the Sorcery Test can be attempted,
     * and to collect the associated action if it can.
     * 
     * @param playerId the player attempting the Sorcery Test
     * @param game the game
     * @effectResult the result of the triggers
     * @param self the sorcery test
     * @return the action corresponding to the attempt, or null if conditions aren't met to attempt
     */
    protected abstract OptionalGameTextTriggerAction tryGetGameTextSorceryTestAttemptAction(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self);

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        List<OptionalGameTextTriggerAction> actions = super.getGameTextOptionalAfterTriggers(playerId, game, effectResult, self, gameTextSourceCardId);

        // Action to exchange Sorcery Tests with Lost Pile on completion:
        // Note: the game text describing the card exchange is defined on "Teo... SHA!!!",
        // but it applies to the completion of any Sorcery Test...
        final GameTextActionId exchangeActionId = GameTextActionId.SORCERY_TEST__EXCHANGE_SORCERY_TEST_IN_LOST_PILE;
        if (TriggerConditions.tryGetAsSorceryTestCompletedResult(game, effectResult) != null
                && GameConditions.hasHand(game, playerId)
                && GameConditions.canSearchLostPile(game, playerId, self, exchangeActionId)) {

            OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, self.getCardId(), exchangeActionId);
            action.setText("Exchange card in hand for Sorcery Test in Lost Pile");
            action.setActionMsg("Exchange a card in your hand for a Sorcery Test in your Lost Pile");
            // Perform result(s)
            action.appendEffect(
                    new ExchangeCardInHandWithCardInLostPileEffect(action, playerId, Filters.any, Filters.Sorcery_Test));
            actions.add(action);
        }

        // Action to attempt the sorcery test.
        // Sorcery tests can only be attempted once per turn, on your turn, and if not completed yet:
        if (
            self.getCharacterTestStatus() == CharacterTestStatus.NOT_COMPLETED
            && GameConditions.isDuringYourTurn(game, self)
            && GameConditions.isOncePerTurn(game, self, gameTextSourceCardId, GameTextActionId.SORCERY_TEST__ATTEMPT_TEST)
        ) {
            // Remaining checks delgated to the Sorcery Test in question:
            final OptionalGameTextTriggerAction attemptAction = tryGetGameTextSorceryTestAttemptAction(playerId, game, effectResult, self);
            if (attemptAction != null) {
                actions.add(attemptAction);
            }
        }

        return actions;
    }

    // The below methods affect how this card is treated as a spell by the Spellbook.
    // YAGNI: explicit "Spell" interface if something other than a Sorcery Test can become a spell,
    // or if something other than the Spellbook can cast spells.

    /**
     * This method is overridden by individual cards to specify the actions made available by the Spellbook's "cast a spell" mechanic.
     * 
     * As of writing, logic controlling available casts is located on the Spellbook,
     * while the logic describing what happens as a result of the cast is located on each individual Sorcery Test.
     * There is no specific EffectResult issued by casting a spell as there's nothing in the game that explicitly triggers from it.
     * 
     * Note that this has to be an OptionalGameTextTriggerAction and not a TopLevelGameTextAction
     * because Kiss Of Death can only be cast "at the end of your opponents battle phase",
     * which is detected by checking TriggerConditions on the effectResult.
     * 
     * @param playerId the player
     * @param game the game
     * @param self the card containing the game text of the spell (e.g. the Sorcery Test)
     * @param spellbook the card that will ultimately cast the spell (e.g. the Spellbook)
     * @param spellcaster the card holding the spellbook
     * @param spellcasterEffectivePresenceFilter a filter to use when checking for targets who are "present" - see "Sunstar" for context
     * @return the action available to cast, or null if there is none
     */
    public abstract OptionalGameTextTriggerAction getGameTextSpellcastingAction(String playerId, SwccgGame game, EffectResult effectResult, PhysicalCard self, PhysicalCard spellbook, PhysicalCard spellcaster, Filter spellcasterEffectivePresenceFilter);
}
