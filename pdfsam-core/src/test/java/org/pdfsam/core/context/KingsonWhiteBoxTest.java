/*
 * White Box Testing - Kingson Zhang (kxzhang@uci.edu)
 * SWE 261P Part 3: Structural Testing
 * Module: pdfsam-core
 * Target: ConversionUtils (edge cases), ApplicationRuntimeState,
 *         ObjectCollectionWriter, validation utilities
 */
package org.pdfsam.core.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pdfsam.core.support.params.ConversionUtils;
import org.pdfsam.core.support.io.ObjectCollectionWriter;
import org.sejda.conversion.exception.ConversionException;
import org.sejda.model.pdf.page.PageRange;
import org.sejda.model.pdf.page.PagesSelection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * White-box tests by Kingson Zhang targeting uncovered code in pdfsam-core.
 */
public class KingsonWhiteBoxTest {

    // ==================== ConversionUtils Tests (uncovered branches)
    // ====================

    @Test
    void toPageRangeSetBlankReturnsEmpty() {
        var result = ConversionUtils.toPageRangeSet("");
        assertTrue(result.isEmpty());
    }

    @Test
    void toPageRangeSetNullReturnsEmpty() {
        var result = ConversionUtils.toPageRangeSet(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void toPagesSelectionSetBlankReturnsEmpty() {
        var result = ConversionUtils.toPagesSelectionSet("");
        assertTrue(result.isEmpty());
    }

    @Test
    void toPagesSelectionSetNullReturnsEmpty() {
        var result = ConversionUtils.toPagesSelectionSet(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void toPagesSelectionSetLastPage() {
        Set<PagesSelection> result = ConversionUtils.toPagesSelectionSet("last");
        assertEquals(1, result.size());
        assertTrue(result.contains(PagesSelection.LAST_PAGE));
    }

    @Test
    void toPagesSelectionSetMixedWithLast() {
        Set<PagesSelection> result = ConversionUtils.toPagesSelectionSet("1-3,last");
        assertEquals(2, result.size());
        assertTrue(result.contains(PagesSelection.LAST_PAGE));
        assertTrue(result.contains(new PageRange(1, 3)));
    }

    @Test
    void toPageRangeSetWithSpaces() {
        Set<PageRange> result = ConversionUtils.toPageRangeSet(" 3 , 7 ");
        assertEquals(2, result.size());
    }

    @Test
    void toPageRangeSetAmbiguousThrows() {
        assertThrows(ConversionException.class, () -> ConversionUtils.toPageRangeSet("1-2-3"));
    }

    @Test
    void toPageRangeSetInvalidNumberThrows() {
        assertThrows(ConversionException.class, () -> ConversionUtils.toPageRangeSet("abc"));
    }

    @Test
    void toPageRangeSetInvalidRangeEndLower() {
        assertThrows(ConversionException.class, () -> ConversionUtils.toPageRangeSet("10-5"));
    }

    @Test
    void toPageRangeSetOpenEndedRange() {
        Set<PageRange> result = ConversionUtils.toPageRangeSet("5-");
        assertEquals(1, result.size());
        PageRange range = result.iterator().next();
        assertEquals(5, range.getStart());
        assertTrue(range.isUnbounded());
    }

    @Test
    void toPageRangeSetPrefixedDash() {
        Set<PageRange> result = ConversionUtils.toPageRangeSet("-8");
        assertEquals(1, result.size());
        PageRange range = result.iterator().next();
        assertEquals(1, range.getStart());
        assertEquals(8, range.getEnd());
    }

    @Test
    void toPagesSelectionSetSinglePage() {
        Set<PagesSelection> result = ConversionUtils.toPagesSelectionSet("5");
        assertEquals(1, result.size());
    }

    @Test
    void toPagesSelectionSetRange() {
        Set<PagesSelection> result = ConversionUtils.toPagesSelectionSet("2-6");
        assertEquals(1, result.size());
    }

    // ==================== ApplicationRuntimeState Tests ====================

    @Test
    void workingPathInitiallyEmpty() {
        ApplicationRuntimeState state = new ApplicationRuntimeState();
        assertEquals(Optional.empty(), state.workingPathValue());
    }

    @Test
    void maybeWorkingPathWithNull() {
        ApplicationRuntimeState state = new ApplicationRuntimeState();
        state.maybeWorkingPath((Path) null);
        assertEquals(Optional.empty(), state.workingPathValue());
    }

    @Test
    void maybeWorkingPathWithBlankString() {
        ApplicationRuntimeState state = new ApplicationRuntimeState();
        state.maybeWorkingPath("");
        assertEquals(Optional.empty(), state.workingPathValue());
    }

    @Test
    void maybeWorkingPathWithNullString() {
        ApplicationRuntimeState state = new ApplicationRuntimeState();
        state.maybeWorkingPath((String) null);
        assertEquals(Optional.empty(), state.workingPathValue());
    }

    @Test
    void maybeWorkingPathWithValidDirectory(@TempDir Path tempDir) {
        ApplicationRuntimeState state = new ApplicationRuntimeState();
        state.maybeWorkingPath(tempDir);
        assertEquals(Optional.of(tempDir), state.workingPathValue());
    }

    @Test
    void maybeWorkingPathWithRegularFile(@TempDir Path tempDir) throws IOException {
        Path file = Files.createTempFile(tempDir, "test", ".txt");
        ApplicationRuntimeState state = new ApplicationRuntimeState();
        state.maybeWorkingPath(file);
        // Should resolve to parent directory
        assertEquals(Optional.of(tempDir), state.workingPathValue());
    }

    @Test
    void maybeWorkingPathStringValid(@TempDir Path tempDir) {
        ApplicationRuntimeState state = new ApplicationRuntimeState();
        state.maybeWorkingPath(tempDir.toString());
        assertEquals(Optional.of(tempDir), state.workingPathValue());
    }

    @Test
    void activeToolInitiallyEmpty() {
        ApplicationRuntimeState state = new ApplicationRuntimeState();
        assertEquals(Optional.empty(), state.activeToolValue());
        assertNotNull(state.activeTool());
    }

    @Test
    void workspaceInitiallyNull() {
        ApplicationRuntimeState state = new ApplicationRuntimeState();
        assertNull(state.workspace());
    }

    @Test
    void themeObservable() {
        ApplicationRuntimeState state = new ApplicationRuntimeState();
        assertNotNull(state.theme());
    }

    @Test
    void themeSetNull() {
        ApplicationRuntimeState state = new ApplicationRuntimeState();
        state.theme(null); // should not throw
        assertNotNull(state.theme());
    }

    @Test
    void workingPathObservable() {
        ApplicationRuntimeState state = new ApplicationRuntimeState();
        assertNotNull(state.workingPath());
    }

    // ==================== ObjectCollectionWriter Tests ====================

    @Test
    void objectCollectionWriterWriteContent(@TempDir Path tempDir) throws IOException {
        Path output = tempDir.resolve("output.txt");
        ObjectCollectionWriter.writeContent(java.util.List.of("line1", "line2"))
                .to(output);
        String content = Files.readString(output);
        assertTrue(content.contains("line1"));
        assertTrue(content.contains("line2"));
    }

    @Test
    void objectCollectionWriterEmptyList(@TempDir Path tempDir) throws IOException {
        Path output = tempDir.resolve("empty.txt");
        ObjectCollectionWriter.writeContent(java.util.List.of())
                .to(output);
        String content = Files.readString(output);
        assertTrue(content.isEmpty());
    }
}
