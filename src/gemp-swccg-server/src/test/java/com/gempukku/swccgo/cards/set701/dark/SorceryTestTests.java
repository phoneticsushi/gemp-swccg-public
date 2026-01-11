package com.gempukku.swccgo.cards.set701.dark;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import com.gempukku.swccgo.common.CharacterTestStatus;
import com.gempukku.swccgo.common.Phase;
import com.gempukku.swccgo.common.TargetId;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.framework.StartingSetup;
import com.gempukku.swccgo.framework.TestBase;
import com.gempukku.swccgo.framework.VirtualTableScenario;
import com.gempukku.swccgo.game.PhysicalCardImpl;

public class SorceryTestTests
{
	protected VirtualTableScenario GetScenario() {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("midge", "9_011");
					put("blount", "9_021");
				}},
				new HashMap<>()
				{{
					put("zarrak", "701_011");
					put("teebo", "701_022");
					put("teosha", "701_023");
					put("oombadoombaboomba", "701_014");
					put("spellbook", "701_020");
				}},
				10,
				10,
				StartingSetup.DefaultLSGroundLocation,
				StartingSetup.DefaultDSGroundLocation,
				StartingSetup.NoLSStartingInterrupts,
				StartingSetup.NoDSStartingInterrupts,
				StartingSetup.NoLSShields,
				StartingSetup.NoDSShields,
				VirtualTableScenario.Open
		);
	}

	@Test
	public void TeoShaOffersOptionToAttemptSorceryTest() {
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl zarrak = scn.GetDSCard("zarrak");
		PhysicalCardImpl teebo = scn.GetDSCard("teebo");
		PhysicalCardImpl teosha = scn.GetDSCard("teosha");
		PhysicalCardImpl spellbook = scn.GetDSCard("spellbook");

		PhysicalCardImpl site = scn.GetDSStartingLocation();

		scn.StartGame();

		// Set up the classic first-sorcery-test dream:
		scn.MoveCardsToLocation(site, zarrak, teebo);
		scn.AttachCardsTo(zarrak, teosha);
		scn.AttachCardsTo(teebo, spellbook);

		// Shoehorn in the state that would normally be set on deploy:
        teosha.setTargetedCard(TargetId.SORCERY_TEST_MENTOR, 6, zarrak, Filters.any);
        teosha.setTargetedCard(TargetId.SORCERY_TEST_APPRENTICE, 7, teebo, Filters.any);

		// Teo Sha can't be attempted at the start of the ACTIVATE phase...
		scn.SkipToPhase(Phase.ACTIVATE);
		assertFalse(scn.ActionAvailable(TestBase.DS, teosha, null));

		// ...but it can at the start of the CONTROL phase:
		scn.SkipToPhase(Phase.CONTROL);
		assertTrue(scn.ActionAvailable(TestBase.DS, teosha, null));
	}

	@Test
	public void TeoShaCanBeCastInBattle() {
		VirtualTableScenario scn = GetScenario();

		// LS
		PhysicalCardImpl midge = scn.GetLSCard("midge");
		PhysicalCardImpl blount = scn.GetLSCard("blount");
		// DS
		PhysicalCardImpl teebo = scn.GetDSCard("teebo");
		PhysicalCardImpl teosha = scn.GetDSCard("teosha");
		PhysicalCardImpl spellbook = scn.GetDSCard("spellbook");
		// Saloon
		PhysicalCardImpl site = scn.GetDSStartingLocation();

		scn.StartGame();

		scn.MoveCardsToLocation(site, midge, blount, teebo);

		// Sorcery Test 1 is Completed and Teebo has the Spellbook
		teosha.setCharacterTestStatus(CharacterTestStatus.COMPLETED);
		scn.AttachCardsTo(teebo, spellbook);
		scn.AttachCardsTo(spellbook, teosha);

		// Teo Sha is not available outside of battle...
		scn.SkipToTurn(TestBase.LS, 1);
		assertFalse(scn.DSAnyActionsAvailable());

		scn.SkipToPhase(Phase.BATTLE);
		assertFalse(scn.DSAnyActionsAvailable());

		// ...but it is within battle in this situation
		scn.LSInitiateBattle(site);
		assertTrue(scn.ActionAvailable(TestBase.DS, spellbook, "Cast 'Teo... SHA!!!'"));
	}

	@Test
	public void OombaDoombaBoomaOffersOptionToAttemptSorceryTest() {
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl teebo = scn.GetDSCard("teebo");
		PhysicalCardImpl teosha = scn.GetDSCard("teosha");
		PhysicalCardImpl oombadoombaboomba = scn.GetDSCard("oombadoombaboomba");
		PhysicalCardImpl spellbook = scn.GetDSCard("spellbook");

		PhysicalCardImpl site = scn.GetDSStartingLocation();

		scn.StartGame();

		// Teebo is at a location and Sorcery Test 2 is attached to that location
		scn.MoveCardsToLocation(site, teebo);
		scn.AttachCardsTo(site, oombadoombaboomba);
		// Shoehorn in the state that would normally be set on deploy:
		// For the purposes of this test, we don't care who the mentor is...
        oombadoombaboomba.setTargetedCard(TargetId.SORCERY_TEST_APPRENTICE, 7, teebo, Filters.any);

		// Sorcery Test 1 is Completed and Teebo has the Spellbook
		teosha.setCharacterTestStatus(CharacterTestStatus.COMPLETED);
		scn.AttachCardsTo(teebo, spellbook);
		scn.AttachCardsTo(spellbook, teosha);

		// OombaBoombaDoomba can't be attempted at the start of the ACTIVATE phase...
		scn.SkipToPhase(Phase.ACTIVATE);
		assertFalse(scn.ActionAvailable(TestBase.DS, oombadoombaboomba, null));

		// ...but it can at the start of the CONTROL phase:
		scn.SkipToPhase(Phase.CONTROL);
		assertTrue(scn.ActionAvailable(TestBase.DS, oombadoombaboomba, null));
	}
}
