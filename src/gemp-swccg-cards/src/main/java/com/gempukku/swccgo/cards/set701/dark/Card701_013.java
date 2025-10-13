package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractObjective;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.actions.ObjectiveDeployedTriggerAction;
import com.gempukku.swccgo.cards.conditions.OccupiesCondition;
import com.gempukku.swccgo.cards.effects.usage.OncePerPhaseEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.AbstractActionProxy;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.CancelCardActionBuilder;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.actions.TriggerAction;
import com.gempukku.swccgo.logic.conditions.NotCondition;
import com.gempukku.swccgo.logic.effects.AddUntilEndOfGameActionProxyEffect;
import com.gempukku.swccgo.logic.effects.AddUntilEndOfGameModifierEffect;
import com.gempukku.swccgo.logic.effects.FlipCardEffect;
import com.gempukku.swccgo.logic.effects.UseForceEffect;
import com.gempukku.swccgo.logic.effects.choose.DeployCardFromReserveDeckEffect;
import com.gempukku.swccgo.logic.effects.choose.DeployCardToLocationFromReserveDeckEffect;
import com.gempukku.swccgo.logic.effects.choose.TakeCardIntoHandFromReserveDeckEffect;
import com.gempukku.swccgo.logic.modifiers.ForfeitModifier;
import com.gempukku.swccgo.logic.modifiers.GenerateNoForceModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotPlayModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.PowerModifier;
import com.gempukku.swccgo.logic.timing.Effect;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.results.SorceryTestCompletedResult;

/**
* Set: BEEZER_BOWL_2025
* Type: OBJECTIVE
* Title: Once the Sunstar is Mine...
*/
public class Card701_013 extends AbstractObjective {
    public Card701_013() {
        super(Side.DARK, 0, Title.Once_The_Sunstar_Is_Mine, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setFrontOfDoubleSidedCard(true);
        setGameText("Deploy Bright Tree Village (with Sunstar there) and Zarrak's Hideout (with Zarrak there). For remainder of game, Honor of the Jedi is canceled and you may not deploy Dark Jedi. Your non-alien characters on Endor are power and forfeit -1. Sorcery Test destiny draws are +1 for each sorcerer on table. Unless opponent occupies Bright Tree Village, opponent generates no Force there. While this side up, once during each of your control phases, may [upload] Makrit, Teebo, or one sorcerer. Once during each of your deploy phases, may use 1 Force to [download] a Mt. Thunderstone site. Whenever you draw sorcery training destiny, draw two and choose one. Flip this card if Sorcery Test #4 completed.");
        addIcons(Icon.BEEZER_BOWL_2025);
    }

    // Deploy Bright Tree Village (with Sunstar there) and Zarrak’s Hideout (with Zarrak there)
    @Override
    protected ObjectiveDeployedTriggerAction getGameTextWhenDeployedAction(String playerId, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        ObjectiveDeployedTriggerAction action = new ObjectiveDeployedTriggerAction(self);
        action.appendRequiredEffect(
                new DeployCardFromReserveDeckEffect(action, Filters.Bright_Tree_Village, true, false) {
                    @Override
                    public String getChoiceText() {
                        return "Choose Bright Tree Village to deploy";
                    }
                });
        action.appendRequiredEffect(
                new DeployCardToLocationFromReserveDeckEffect(action, Filters.Sunstar, Filters.Bright_Tree_Village, true, false) {
                    @Override
                    public String getChoiceText() {
                        return "Choose Sunstar to deploy on Bright Tree Village";
                    }
                });
        action.appendRequiredEffect(
                new DeployCardFromReserveDeckEffect(action, Filters.Zarraks_Hideout, true, false) {
                    @Override
                    public String getChoiceText() {
                        return "Choose Zarrak's Hideout to deploy";
                    }
                });
        action.appendRequiredEffect(
                new DeployCardToLocationFromReserveDeckEffect(action, Filters.Zarrak, Filters.Zarraks_Hideout, true, false) {
                    @Override
                    public String getChoiceText() {
                        return "Choose Zarrak to deploy on Zarrak's Hideout";
                    }
                });
        return action;
    }

    @Override
    protected RequiredGameTextTriggerAction getGameTextAfterDeploymentCompletedAction(String playerId, SwccgGame game, final PhysicalCard self, final int gameTextSourceCardId) {
        // For remainder of game...
        RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);

        //...Honor of the Jedi is cancelled...
        final int permCardId = self.getPermanentCardId();
        action.appendEffect(
            new AddUntilEndOfGameActionProxyEffect(
                action,
                new AbstractActionProxy() {
                    @Override
                    public List<TriggerAction> getRequiredBeforeTriggers(SwccgGame game, Effect effect) {
                        PhysicalCard self = game.findCardByPermanentId(permCardId);

                        // Note: Other code seems to implement cancelation twice, once for cards as they are played and once for cards that are on the table
                        // Assumption is that Honor of the Jedi cannot be on the table before this objective is deployed
                        // Check condition(s)
                        if (TriggerConditions.isPlayingCard(game, effect, Filters.Honor_Of_The_Jedi)
                                && GameConditions.canCancelCardBeingPlayed(game, self, effect)) {

                            RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                            // Build action using common utility
                            CancelCardActionBuilder.buildCancelCardBeingPlayedAction(action, effect);

                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                }
            )
        );

        // ... and you may not deploy Dark Jedi
        action.appendEffect(new AddUntilEndOfGameModifierEffect(
            action,
            new MayNotPlayModifier(self, Filters.Dark_Jedi, playerId),
            null
        ));

        return action;
    }

    @Override
    public List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();

        // Your non-alien characters on Endor are power and forfeit -1
        Filter yourNonAlienCharactersOnEndor = Filters.and(Filters.your(self), Filters.not(Filters.alien), Filters.character, Filters.On_Endor);
        modifiers.add(new PowerModifier(self, yourNonAlienCharactersOnEndor, -1));
        modifiers.add(new ForfeitModifier(self, yourNonAlienCharactersOnEndor, -1));

        // Unless opponent occupies Bright Tree Village, opponent generates no Force there
        String opponent = game.getOpponent(self.getOwner());
        modifiers.add(new GenerateNoForceModifier(
            self,
            Filters.Bright_Tree_Village,
            new NotCondition(new OccupiesCondition(opponent, Filters.Bright_Tree_Village)),
            opponent
        ));



        return modifiers;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        List<TopLevelGameTextAction> actions = new LinkedList<TopLevelGameTextAction>();

        // Once during each of your control phases, may [upload] Makrit, Teebo, or one sorcerer
        GameTextActionId uploadActionId = GameTextActionId.ONCE_THE_SUNSTAR_IS_MINE__UPLOAD_MAKRIT_TEEBO_OR_SORCERER;
        if (GameConditions.isOnceDuringYourPhase(game, self, playerId, gameTextSourceCardId, uploadActionId, Phase.CONTROL)
                && GameConditions.canTakeCardsIntoHandFromReserveDeck(game, playerId, self, uploadActionId)) {

            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, uploadActionId);
            action.setText("Take card into hand from Reserve Deck");
            action.setActionMsg("Take Makrit, Teebo, or a Sorcerer into hand from Reserve Deck");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerPhaseEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new TakeCardIntoHandFromReserveDeckEffect(action, playerId, Filters.or(Filters.Makrit, Filters.Teebo, Filters.sorcerer), true));
            actions.add(action);
        }

        // Once during each of your deploy phases, may use 1 Force to [download] a Mt. Thunderstone site
        GameTextActionId downloadActionId = GameTextActionId.ONCE_THE_SUNSTAR_IS_MINE__DOWNLOAD_MT_THUNDERSTONE_SITE;
        if (GameConditions.isOnceDuringYourPhase(game, self, playerId, gameTextSourceCardId, downloadActionId, Phase.DEPLOY)
                && GameConditions.canUseForce(game, playerId, 1)
                && GameConditions.canDeployCardFromReserveDeck(game, playerId, self, downloadActionId)) {

            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, downloadActionId);
            action.setText("Deploy a Mt. Thunderstone Site from Reserve Deck");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerPhaseEffect(action));
            // Pay cost(s)
            action.appendCost(
                    new UseForceEffect(action, playerId, 1));
            // Perform result(s)
            action.appendEffect(
                    new DeployCardFromReserveDeckEffect(action, Filters.Mt_Thunderstone_site, true));
            actions.add(action);
        }

        return actions;
    }

    // Flip this card if Sorcery Test #4 completed.
    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        if (effectResult.getType() == EffectResult.Type.SORCERY_TEST_COMPLETED) {
            SorceryTestCompletedResult result = (SorceryTestCompletedResult)effectResult;

            if (Filters.Sorcery_Test_4.accepts(game, result.getSorceryTest())) {
                RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setSingletonTrigger(true);
                action.setText("Flip");
                action.setActionMsg(null);
                // Perform result(s)
                action.appendEffect(
                        new FlipCardEffect(action, self));
                return Collections.singletonList(action);
            }
        }

        return null;
    }

    // Note: the following are currently implemented on AbstractSorceryTest::getGameTextTrainingDestinyAttemptAction():
    // - "Sorcery Test destiny draws are +1 for each sorcerer on table"
    // - "Whenever you draw sorcery training destiny, draw two and choose one"
    // FIXME: this is unlikely to function correctly if the Objective's Game Text is cancelled e.g. but is that even possible?
}
