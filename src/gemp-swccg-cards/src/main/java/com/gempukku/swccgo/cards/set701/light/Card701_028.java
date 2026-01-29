package com.gempukku.swccgo.cards.set701.light;

import com.gempukku.swccgo.cards.AbstractCharacterDevice;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.PlayCardOptionId;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.DrawDestinyEffect;
import com.gempukku.swccgo.logic.effects.ExcludeFromBattleEffect;
import com.gempukku.swccgo.logic.effects.TargetCardOnTableEffect;
import com.gempukku.swccgo.logic.modifiers.DefenseValueModifier;
import com.gempukku.swccgo.logic.modifiers.EachWeaponDestinyModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.GuiUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Set 701 (Beezer Bowl 25)
 * Type: Device
 * Title: Beezer's Helmet
 * Gemp ID: 701_028
 */
public class Card701_028 extends AbstractCharacterDevice {
    public Card701_028() {
        super(Side.LIGHT, 5, "Beezer's Helmet", Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setLore("The Rebel Alliance didn't have a helmet that fit Beezer's head, so she used her technical prowess to fashion her own. Made with kevlar.");
        setGameText("Deploy on Beezer. Adds 2 to defense value. Subtracts 1 from opponent's weapon destiny draws here. During battle, may target opponent's character and yell \"CHAAAAAARGE!!!\" (both players draw one destiny; if your destiny > opponent's, Beezer and target are excluded from battle).");
        addIcons(Icon.BEEZER_BOWL_2025);
        addKeywords(Keyword.DEPLOYS_ON_CHARACTERS);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.persona(Persona.BEEZER);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        Filter whereAttached = Filters.hasAttached(self);

        List<Modifier> modifiers = new LinkedList<Modifier>();
        // Adds 2 to defense value (of the character this is attached to)
        modifiers.add(new DefenseValueModifier(self, whereAttached, 2));
        // Subtracts 1 from opponent's weapon destiny draws here
        modifiers.add(new EachWeaponDestinyModifier(self, Filters.and(Filters.opponents(self), Filters.weapon, Filters.here(self)), -1));
        return modifiers;
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, final SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        final String opponent = game.getOpponent(playerId);
        final Filter opponentsCharacterInBattle = Filters.and(Filters.opponents(self), Filters.character, Filters.participatingInBattle);

        // Check condition(s) - During battle, may target opponent's character
        if (GameConditions.isDuringBattle(game)
                && GameConditions.canTarget(game, self, opponentsCharacterInBattle)) {

            // Get Beezer (the character this device is attached to)
            final PhysicalCard beezer = self.getAttachedTo();
            if (beezer != null && GameConditions.isInBattle(game, beezer)) {

                final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId);
                action.setText("Yell \"CHAAAAAARGE!!!\"");
                action.setActionMsg("Target opponent's character and yell \"CHAAAAAARGE!!!\"");

                // Choose target
                action.appendTargeting(
                        new TargetCardOnTableEffect(action, playerId, "Choose opponent's character", opponentsCharacterInBattle) {
                            @Override
                            protected void cardTargeted(int targetGroupId, final PhysicalCard targetedCharacter) {
                                action.addAnimationGroup(targetedCharacter);

                                // Player draws 1 destiny
                                action.appendEffect(
                                        new DrawDestinyEffect(action, playerId, 1) {
                                            @Override
                                            protected void destinyDraws(SwccgGame game, List<PhysicalCard> playersDestinyCardDraws, List<Float> playersDestinyDrawValues, final Float playersTotalDestiny) {
                                                final float playersTotal = (playersTotalDestiny != null ? playersTotalDestiny : 0);

                                                // Opponent draws 1 destiny
                                                action.appendEffect(
                                                        new DrawDestinyEffect(action, opponent, 1) {
                                                            @Override
                                                            protected void destinyDraws(SwccgGame game, List<PhysicalCard> opponentsDestinyCardDraws, List<Float> opponentsDestinyDrawValues, Float opponentsTotalDestiny) {
                                                                final float opponentsTotal = (opponentsTotalDestiny != null ? opponentsTotalDestiny : 0);

                                                                game.getGameState().sendMessage(playerId + "'s destiny: " + GuiUtils.formatAsString(playersTotal));
                                                                game.getGameState().sendMessage(opponent + "'s destiny: " + GuiUtils.formatAsString(opponentsTotal));

                                                                // Check if player wins
                                                                if (playersTotal > opponentsTotal) {
                                                                    game.getGameState().sendMessage("CHAAAAAARGE!!! succeeds - " + GameUtils.getCardLink(beezer) + " and " + GameUtils.getCardLink(targetedCharacter) + " are excluded from battle");

                                                                    // Exclude Beezer from battle
                                                                    action.appendEffect(
                                                                            new ExcludeFromBattleEffect(action, beezer));
                                                                    // Exclude target from battle
                                                                    action.appendEffect(
                                                                            new ExcludeFromBattleEffect(action, targetedCharacter));
                                                                } else {
                                                                    game.getGameState().sendMessage("CHAAAAAARGE!!! fails");
                                                                }
                                                            }
                                                        }
                                                );
                                            }
                                        }
                                );
                            }
                        }
                );

                return Collections.singletonList(action);
            }
        }

        return null;
    }
}
