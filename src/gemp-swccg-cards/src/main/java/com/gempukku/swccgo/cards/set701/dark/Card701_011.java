package com.gempukku.swccgo.cards.set701.dark;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractAlien;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerGameEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.GameTextActionId;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.Keyword;
import com.gempukku.swccgo.common.Persona;
import com.gempukku.swccgo.common.PlayCardOptionId;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Species;
import com.gempukku.swccgo.common.TargetId;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.choose.TakeCardIntoHandFromReserveDeckEffect;
import com.gempukku.swccgo.logic.modifiers.ImmuneToAttritionLessThanModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotBeTargetedByModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotBeTargetedByWeaponsModifier;
import com.gempukku.swccgo.logic.modifiers.MayNotDrawMoreThanBattleDestinyModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;

/**
* Set: BEEZER_BOWL_2025
* Type: CHARACTER_ALIEN
* Title: Master Zarrak
*/
public class Card701_011 extends AbstractAlien {
    public Card701_011() {
        super(Side.DARK, 1, 4, 4, 5, 6, Title.Master_Zarrak, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploys only to your Endor mountain sites. When mentor, opponent may not target your apprentice with weapons or interrupts. Once per game, may [upload] Zarrak's Hang Glider or Zarrak's Medallion. Opponent may draw no more than one battle destiny here. Immune to attrition < 4.");
        addIcons(Icon.BEEZER_BOWL_2025, Icon.WARRIOR);
        addKeywords(Keyword.DARK_ARTS, Keyword.MALE, Keyword.SORCERER);
        addPersonas(Persona.ZARRAK);
        setSpecies(Species.EWOK);
    }

    // Deploys only to your Endor mountain sites
    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.and(Filters.your(self.getOwner()), Filters.Endor_site, Filters.mountain_site);
    }

    // Once per game, may [upload] Zarrak’s Hang Glider or Zarrak’s Medallion
    @Override
    protected List<TopLevelGameTextAction> getGameTextTopLevelActions(final String playerId, SwccgGame game, final PhysicalCard self, int gameTextSourceCardId) {
        GameTextActionId gameTextActionId = GameTextActionId.MASTER_ZARRAK__UPLOAD_HANG_GLIDER_OR_MEDALLION;

        // Check condition(s)
        if (GameConditions.isOncePerGame(game, self, gameTextActionId)
                && GameConditions.canTakeCardsIntoHandFromReserveDeck(game, playerId, self, gameTextActionId)) {

            final TopLevelGameTextAction action = new TopLevelGameTextAction(self, gameTextSourceCardId, gameTextActionId);
            action.setText("Take card into hand from Reserve Deck");
            action.setActionMsg("Take Zarrak's Hang Glider or Zarrak's Medallion into hand from Reserve Deck");
            // Update usage limit(s)
            action.appendUsage(
                    new OncePerGameEffect(action));
            // Perform result(s)
            action.appendEffect(
                    new TakeCardIntoHandFromReserveDeckEffect(action, playerId, Filters.or(Filters.title(Title.Zarraks_Hang_Glider), Filters.title(Title.Zarraks_Medallion)), true));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        
        // When mentor, opponent may not target your apprentice with weapons or interrupts
        modifiers.add(new MayNotBeTargetedByModifier(
            self,
            Filters.zarraks_apprentice(),
            null,
            Filters.and(
                Filters.opponents(self.getOwner()),
                Filters.or(Filters.weapon, Filters.Interrupt)
            )
        ));

        // Opponent may draw no more than one battle destiny here
        modifiers.add(new MayNotDrawMoreThanBattleDestinyModifier(self, Filters.here(self), 1, game.getOpponent(self.getOwner())));

        // Immune to attrition < 4
        modifiers.add(new ImmuneToAttritionLessThanModifier(self, 4));

        return modifiers;
    }
}
