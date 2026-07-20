/*
 * WorldCRUD - a world management utility plugin for Minecraft servers.
 * Copyright (C) 2026 Carmelo Santana
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * See the LICENSE file at the project root for the full license text.
 */
package org.xpfarm.worldcrud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the pure decision functions behind Floodgate-aware player lookup:
 * {@link PlayerLookup#targetNameCandidates(String)} and
 * {@link PlayerLookup#noSuchPlayerMessage(String, List)}.
 *
 * <p>{@link PlayerLookup#resolve(String)} and
 * {@link PlayerLookup#resolveAllowingPartial(String)} are deliberately not tested here:
 * they call {@code Bukkit} statics, which need a running server, and there is no
 * MockBukkit dependency in this project. The candidate ordering those methods iterate
 * is what actually encodes the Floodgate fix, and it is covered exhaustively below.
 */
class PlayerLookupTest {

    @Nested
    @DisplayName("target resolution")
    class TargetResolution {

        @Test
        void bareNameAlsoTriesTheFloodgatePrefixedForm() {
            assertEquals(List.of("carm", ".carm"), PlayerLookup.targetNameCandidates("carm"));
        }

        @Test
        void unprefixedFormIsTriedFirstSoJavaPlayersWinTies() {
            List<String> candidates = PlayerLookup.targetNameCandidates("Notch");
            assertEquals("Notch", candidates.get(0), "the typed name must be tried before the prefixed one");
            assertEquals(".Notch", candidates.get(1));
        }

        @Test
        void alreadyPrefixedNameIsNotPrefixedTwice() {
            assertEquals(List.of(".acarm"), PlayerLookup.targetNameCandidates(".acarm"));
            assertTrue(PlayerLookup.targetNameCandidates(".acarm").stream().noneMatch(c -> c.startsWith("..")),
                    "a Floodgate name must never be double-prefixed");
        }

        @Test
        void surroundingWhitespaceIsTrimmed() {
            assertEquals(List.of("carm", ".carm"), PlayerLookup.targetNameCandidates("  carm  "));
            assertEquals(List.of(".acarm"), PlayerLookup.targetNameCandidates("\t.acarm\n"));
        }

        @Test
        void nullYieldsNoCandidates() {
            assertEquals(List.of(), PlayerLookup.targetNameCandidates(null));
        }

        @Test
        void blankYieldsNoCandidates() {
            assertEquals(List.of(), PlayerLookup.targetNameCandidates(""));
            assertEquals(List.of(), PlayerLookup.targetNameCandidates("   "));
        }

        @Test
        void failureMessageNamesTheTypedNameAndWhoIsOnline() {
            String message = PlayerLookup.noSuchPlayerMessage("carm", List.of(".acarm", "Notch"));
            assertTrue(message.contains("carm"), "message should quote what was typed: " + message);
            assertTrue(message.contains(".acarm"), "message should list online players: " + message);
            assertTrue(message.contains("Notch"), "message should list every online player: " + message);
        }

        @Test
        void failureMessageWithNobodyOnlineSaysSo() {
            String empty = PlayerLookup.noSuchPlayerMessage("carm", List.of());
            assertTrue(empty.contains("no players"), "message should say nobody is online: " + empty);

            String nullList = PlayerLookup.noSuchPlayerMessage("carm", null);
            assertTrue(nullList.contains("no players"), "a null online list reads as nobody online: " + nullList);
        }
    }
}
