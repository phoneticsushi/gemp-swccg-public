package com.gempukku.swccgo.cards.set701.dark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.gempukku.swccgo.cards.AbstractArtifact;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.conditions.AttachedCondition;
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
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.effects.AttachCardFromTableEffect;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.PowerModifier;
import com.gempukku.swccgo.logic.timing.EffectResult;

/**
* Set: BEEZER_BOWL_2025
* Type: ARTIFACT
* Title: Sunstar
*/
public class Card701_021 extends AbstractArtifact {
    public Card701_021() {
        super(Side.DARK, 0f, Title.Sunstar, Uniqueness.UNIQUE, ExpansionSet.BEEZER_BOWL_2025, Rarity.V);
        setGameText("Deploy on Bright Tree Village. Relocate Sunstar to Spellcaster if you control Dark Tree Village and Sorcery Test #3 is completed. While on Spellcaster, that character is power +2 and Spells may be cast at adjacent sites as if Spellcaster present there. If opponent controls Bright Tree Village, relocate Sunstar to that site.");
        addIcons(Icon.BEEZER_BOWL_2025);
    }

    private boolean isAttachedToSpellcaster(SwccgGame game, PhysicalCard self) {
        return Filters.Spellcaster.accepts(game, self.getAttachedTo());
    }
    
    private boolean isAttachedToBrightTreeVillage(SwccgGame game, PhysicalCard self) {
        return Filters.Bright_Tree_Village.accepts(game, self.getAttachedTo());
    }
    
    // Deploy on Bright Tree Village
    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.Bright_Tree_Village;
    }

    protected List<RequiredGameTextTriggerAction> getGameTextRequiredAfterTriggers(SwccgGame game, EffectResult effectResult, PhysicalCard self, int gameTextSourceCardId) {
        // Relocation checks.
        // Since these could happen in any order, we can't rely on checking for the presence of a trigger, so we check this every time...
        // It should be impossible for Bright Tree Village and Dark Tree Village to be on the table at the same time,
        // but the text specifies both of these operations, so perform them in the given order, even if they both fire...

        if (TriggerConditions.isTableChanged(game, effectResult)) {
            // This allocates every time the table changes, but how better to do this?
            ArrayList<RequiredGameTextTriggerAction> actions = new ArrayList<>();
            
            if (
                !isAttachedToSpellcaster(game, self)
                && GameConditions.canSpot(game, self, Filters.Spellcaster)
                // if you control Dark Tree Village...
                && (GameConditions.controls(game, self.getOwner(), Filters.Dark_Tree_Village))
            ) {
                // ...and Sorcery Test #3 is completed...
                // (There should be only one of these, but for futureproofing, check if any cards match...)
                final Collection<PhysicalCard> sorceryTest3s = Filters.filterActive(game, self, Filters.Sorcery_Test_3);
                if (sorceryTest3s.stream().anyMatch(s -> GameConditions.isCharacterTestCompleted(game, s))) {
                    PhysicalCard spellcaster = Filters.findFirstActive(game, self, Filters.Spellcaster);

                    // checked above in filters but never hurts to be defensive...
                    if (spellcaster != null && spellcaster != self.getAttachedTo()) {
                        // ...relocate Sunstar to Spellcaster
                        final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                        action.appendEffect(new AttachCardFromTableEffect(action, self, spellcaster));
                        actions.add(action);
                    }
                }
            }
            
            if (
                !isAttachedToBrightTreeVillage(game, self)
                // If opponent controls Bright Tree Village...
                && (GameConditions.controls(game, game.getOpponent(self.getOwner()), Filters.Bright_Tree_Village))
            ) {
                PhysicalCard brightTreeVillage = Filters.findFirstActive(game, self, Filters.Bright_Tree_Village);

                if (brightTreeVillage != null) {
                    // ...relocate Sunstar to that site
                    final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);

                    action.appendEffect(new AttachCardFromTableEffect(action, self, brightTreeVillage));
                    actions.add(action);
                }
            }

            return actions;
        }
        return null;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
    
        // While on Spellcaster, that character is power +2...
        modifiers.add(new PowerModifier(self, new AttachedCondition(self, Filters.Spellcaster), 2));
        // ...and Spells may be cast at adjacent sites as if Spellcaster present there
        // ^ Note that this effect is intentionally implemented on the Spellbook, and not here,
        // since casting spells is an action specific to the Spellbook and is otherwise meaningless.
        // FIXME: should that be here?  If something else ever allows casting spells, that will need to be here...
        
        return modifiers;
    }
}
