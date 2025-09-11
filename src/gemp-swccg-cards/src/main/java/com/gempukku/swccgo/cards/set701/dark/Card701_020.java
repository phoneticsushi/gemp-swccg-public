package com.gempukku.swccgo.cards.set701.dark;

import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractArtifact;
import com.gempukku.swccgo.cards.AbstractSorceryTest;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.usage.OncePerTurnEffect;
import com.gempukku.swccgo.common.ExpansionSet;
import com.gempukku.swccgo.common.Icon;
import com.gempukku.swccgo.common.PlayCardOptionId;
import com.gempukku.swccgo.common.Rarity;
import com.gempukku.swccgo.common.Side;
import com.gempukku.swccgo.common.Title;
import com.gempukku.swccgo.common.Uniqueness;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.AttachCardFromTableEffect;
import com.gempukku.swccgo.logic.modifiers.ImmuneToAttritionModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.results.AboutToLeaveTableResult;
import com.gempukku.swccgo.logic.timing.results.SorceryTestCompletedResult;

/**
* Set: BEEZER_BOWL_2025
* Type: ARTIFACT
* Title: Spellbook
*/
public class Card701_020 extends AbstractArtifact {
    public Card701_020() {
        super(Side.DARK, 0f, Title.Spellbook, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploy on a character if 'Once The Sunstar Is Mine...' on table. Character gains Spellcaster and is immune to attrition. Completed Sorcery Tests are placed here as spells. Spells about to be lost from opponent's side of table are relocated here. Once per turn, may 'cast' one spell from Spellbook (two if Sunstar on this character). Each spell may only be cast once per turn.");
        addIcons(Icon.BEEZER_BOWL_2025);
    }
    
    // Deploy on a character...
    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.character;
    }

    // ...if 'Once The Sunstar Is Mine...' on table
    @Override
    protected boolean checkGameTextDeployRequirements(String playerId, SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return GameConditions.canSpot(game, self, Filters.title(Title.Once_The_Sunstar_Is_Mine));
    }

    // Character gains Spellcaster
    // ^ Note that this is implemented implicitly; see Filters.Spellcaster
    
    // Character is immune to attrition
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new ImmuneToAttritionModifier(self, Filters.Spellcaster));
        return modifiers;
    }
    
    // Relocation checks.
    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        LinkedList<RequiredGameTextTriggerAction> actions = new LinkedList<>();

        final SorceryTestCompletedResult completed = TriggerConditions.tryGetAsSorceryTestCompletedResult(game, effectResult);
        if (completed != null) {
            // Completed Sorcery Tests are placed here as spells (see Filters.Spell)
            final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.appendEffect(new AttachCardFromTableEffect(action, completed.getSorceryTest(), self));
            actions.add(action);
        }

        if (
            TriggerConditions.isTableChanged(game, effectResult)
            // Spells about to be lost from opponent's side of table...
            && TriggerConditions.isAboutToBeLost(game, effectResult, Filters.and(Filters.Spell, Filters.opponents(self)))
        ) {
            final AboutToLeaveTableResult aboutToLeaveTableResult = (AboutToLeaveTableResult) effectResult;
            // ...are relocated here
            final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
            action.appendEffect(new AttachCardFromTableEffect(action, aboutToLeaveTableResult.getCardAboutToLeaveTable(), self));
            actions.add(action);
        }

        return actions;
    }
    
    // Once per turn, may 'cast' one spell from Spellbook (two if Sunstar on this character)
    // (see note on "Sunstar"; This also implements "spells may be cast at adjacent sites as if Spellcaster present there")
    // Note that these are unforunately AfterTriggers and not TopLevel since certain sorcery tests can only be cast at the beginning/end of a phase/turn
    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, final SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        // "Spellcaster" is defined as "the one holding the spellbook"
        final PhysicalCard spellbookHolder = self.getAttachedTo();
        if (spellbookHolder == null) {
            return null;  // spells only available if held by a Spellcaster
        }

        final int castLimitPerTurn;
        final Filter spellcasterEffectivePresenceFilter;
        
        // implement "two if Sunstar on this character" from this card,
        // as well as "spells may be cast at adjacent sites as if Spellcaster present there" from Sunstar
        if (Filters.hasAttached(Filters.Sunstar).accepts(game, spellbookHolder)) {
            castLimitPerTurn = 2;
            spellcasterEffectivePresenceFilter = Filters.sameOrAdjacentSite(spellbookHolder);
        } else {
            castLimitPerTurn = 1;
            spellcasterEffectivePresenceFilter = Filters.sameSite(spellbookHolder);
        }

        // Check if any more spells can be cast; note this is dynamic based on the presence of the Sunstar
        if (GameConditions.isNumTimesPerTurn(game, self, playerId, castLimitPerTurn, gameTextSourceCardId)) {
            LinkedList<OptionalGameTextTriggerAction> spellcastingActions = new LinkedList<OptionalGameTextTriggerAction>();

            // See YAGNI note in AbstractSorceryTest regarding an explicit spell interface; for now:
            // - only AbstractSorceryTests can be Spells
            // - All AbstractSorceryTests attached to the Spellbook must be Spells
            for (PhysicalCard spell : Filters.filterActive(game, self, Filters.and(Filters.Spell, Filters.attachedTo(self)))) {
                AbstractSorceryTest sorceryTest = (AbstractSorceryTest) spell.getBlueprint();

                OptionalGameTextTriggerAction spellAction = sorceryTest.getGameTextSpellcastingAction(playerId, game, effectResult, spell, self, spellbookHolder, spellcasterEffectivePresenceFilter);
                if (spellAction != null) {
                    // Each spell may only be cast once per turn
                    spellAction.appendUsage(new OncePerTurnEffect(spellAction));
                    spellcastingActions.add(spellAction);
                }
            }

            return spellcastingActions;
        }

        return null;
    }
}
