package org.pdfsam.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pdfsam.model.io.OpenType;
import org.pdfsam.model.news.LatestNewsResponse;
import org.pdfsam.model.news.NewsData;
import org.pdfsam.model.pdf.PdfDocumentDescriptor;
import org.pdfsam.model.tool.BaseToolBound;
import org.pdfsam.model.tool.ClearToolRequest;
import org.pdfsam.model.tool.RequiredPdfData;
import org.pdfsam.model.tool.ToolCategory;
import org.pdfsam.model.tool.ToolDescriptor;
import org.pdfsam.model.tool.ToolDescriptorBuilder;
import org.pdfsam.model.tool.ToolInputOutputType;
import org.pdfsam.model.tool.ToolPriority;
import org.pdfsam.model.ui.ChangedSelectedPdfVersionEvent;
import org.pdfsam.model.ui.ComboItem;
import org.pdfsam.model.ui.NonExistingOutputDirectoryEvent;
import org.pdfsam.model.ui.SetTitleRequest;
import org.pdfsam.model.ui.dnd.FilesDroppedEvent;
import org.pdfsam.model.update.UpdateCheckRequest;
import org.sejda.model.pdf.PdfVersion;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * White-box tests for pdfsam-model module.
 * Covers previously uncovered classes and branches.
 *
 * @author Zhenyu Song
 */
public class ZhenyuWhiteBoxTest {

    private File mockFile;

    @BeforeEach
    void setUp() {
        mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("test.pdf");
        when(mockFile.isFile()).thenReturn(true);
    }

    // ===== PdfDocumentDescriptor =====

    @Test
    @DisplayName("hasPassword returns true when password set")
    void descriptorHasPassword() {
        var desc = PdfDocumentDescriptor.newDescriptor(mockFile, "secret");
        assertTrue(desc.hasPassword());
    }

    @Test
    @DisplayName("hasPassword returns false when no password")
    void descriptorHasNoPassword() {
        var desc = PdfDocumentDescriptor.newDescriptorNoPassword(mockFile);
        assertFalse(desc.hasPassword());
    }

    @Test
    @DisplayName("pages observable")
    void descriptorPages() {
        var desc = PdfDocumentDescriptor.newDescriptorNoPassword(mockFile);
        desc.pages(42);
        assertEquals(42, desc.pages().getValue());
    }

    @Test
    @DisplayName("getFile returns the file")
    void descriptorGetFile() {
        var desc = PdfDocumentDescriptor.newDescriptorNoPassword(mockFile);
        assertEquals(mockFile, desc.getFile());
    }

    @Test
    @DisplayName("setPassword and getPassword")
    void descriptorSetPassword() {
        var desc = PdfDocumentDescriptor.newDescriptorNoPassword(mockFile);
        assertNull(desc.getPassword());
        desc.setPassword("newpwd");
        assertEquals("newpwd", desc.getPassword());
    }

    @Test
    @DisplayName("getVersion returns null initially")
    void descriptorVersionNull() {
        var desc = PdfDocumentDescriptor.newDescriptorNoPassword(mockFile);
        assertNull(desc.getVersion());
    }

    @Test
    @DisplayName("setVersion and getVersion")
    void descriptorVersion() {
        var desc = PdfDocumentDescriptor.newDescriptorNoPassword(mockFile);
        desc.setVersion(PdfVersion.VERSION_1_7);
        assertEquals(PdfVersion.VERSION_1_7, desc.getVersion());
    }

    @Test
    @DisplayName("getInformation for nonexistent key")
    void descriptorGetInfoNonexistent() {
        var desc = PdfDocumentDescriptor.newDescriptorNoPassword(mockFile);
        assertEquals("", desc.getInformation("nonexistent"));
    }

    // ===== ToolDescriptor =====

    @Test
    @DisplayName("blank name throws")
    void toolDescriptorBlankName() {
        assertThrows(IllegalArgumentException.class, () -> ToolDescriptorBuilder.builder().name("").description("desc")
                .category(ToolCategory.MERGE).build());
    }

    @Test
    @DisplayName("blank description throws")
    void toolDescriptorBlankDesc() {
        assertThrows(IllegalArgumentException.class, () -> ToolDescriptorBuilder.builder().name("name").description("")
                .category(ToolCategory.MERGE).build());
    }

    @Test
    @DisplayName("hasInputType returns false for missing type")
    void toolDescriptorHasInputTypeFalse() {
        ToolDescriptor td = ToolDescriptorBuilder.builder()
                .name("test").description("desc")
                .category(ToolCategory.SPLIT)
                .inputTypes(ToolInputOutputType.SINGLE_PDF)
                .build();
        assertFalse(td.hasInputType(ToolInputOutputType.MULTIPLE_PDF));
    }

    @Test
    @DisplayName("hasInputType returns true for existing type")
    void toolDescriptorHasInputTypeTrue() {
        ToolDescriptor td = ToolDescriptorBuilder.builder()
                .name("test").description("desc")
                .category(ToolCategory.SPLIT)
                .inputTypes(ToolInputOutputType.SINGLE_PDF)
                .build();
        assertTrue(td.hasInputType(ToolInputOutputType.SINGLE_PDF));
    }

    @Test
    @DisplayName("priority with ToolPriority enum")
    void toolDescriptorPriorityEnum() {
        ToolDescriptor td = ToolDescriptorBuilder.builder()
                .name("test").description("desc")
                .category(ToolCategory.MERGE)
                .priority(ToolPriority.DEFAULT)
                .build();
        assertEquals(ToolPriority.DEFAULT.getPriority(), td.priority());
    }

    @Test
    @DisplayName("priority with int")
    void toolDescriptorPriorityInt() {
        ToolDescriptor td = ToolDescriptorBuilder.builder()
                .name("test").description("desc")
                .category(ToolCategory.MERGE)
                .priority(99)
                .build();
        assertEquals(99, td.priority());
    }

    @Test
    @DisplayName("supportURL")
    void toolDescriptorSupportURL() {
        ToolDescriptor td = ToolDescriptorBuilder.builder()
                .name("test").description("desc")
                .category(ToolCategory.MERGE)
                .supportURL("https://example.com")
                .build();
        assertEquals("https://example.com", td.supportUrl());
    }

    // ===== ToolCategory =====

    @Test
    @DisplayName("getDescription returns non-null")
    void toolCategoryDescription() {
        for (ToolCategory cat : ToolCategory.values()) {
            assertNotNull(cat.getDescription());
        }
    }

    @Test
    @DisplayName("styleClass returns non-null")
    void toolCategoryStyleClass() {
        for (ToolCategory cat : ToolCategory.values()) {
            assertNotNull(cat.styleClass());
        }
    }

    // ===== BaseToolBound =====

    @Test
    @DisplayName("constructor and toolBinding")
    void baseToolBound() {
        BaseToolBound btb = new BaseToolBound("myToolId");
        assertEquals("myToolId", btb.toolBinding());
    }

    @Test
    @DisplayName("blank toolId throws")
    void baseToolBoundBlank() {
        assertThrows(IllegalArgumentException.class, () -> new BaseToolBound(""));
    }

    // ===== SetTitleRequest =====

    @Test
    @DisplayName("title is trimmed")
    void setTitleRequestTrimmed() {
        SetTitleRequest req = new SetTitleRequest("  hello  ");
        assertEquals("hello", req.title());
    }

    @Test
    @DisplayName("null title is handled")
    void setTitleRequestNull() {
        SetTitleRequest req = new SetTitleRequest(null);
        assertNull(req.title());
    }

    // ===== NonExistingOutputDirectoryEvent =====

    @Test
    @DisplayName("constructor with valid path")
    void nonExistingOutputDir() {
        Path p = Path.of("/tmp/test");
        NonExistingOutputDirectoryEvent evt = new NonExistingOutputDirectoryEvent(p);
        assertEquals(p, evt.outputDirectory());
    }

    @Test
    @DisplayName("null path throws")
    void nonExistingOutputDirNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new NonExistingOutputDirectoryEvent(null));
    }

    // ===== ChangedSelectedPdfVersionEvent =====

    @Test
    @DisplayName("record accessors")
    void changedSelectedPdfVersionEvent() {
        ChangedSelectedPdfVersionEvent evt = new ChangedSelectedPdfVersionEvent(PdfVersion.VERSION_1_5);
        assertEquals(PdfVersion.VERSION_1_5, evt.pdfVersion());
    }

    // ===== LatestNewsResponse =====

    @Test
    @DisplayName("null list throws")
    void latestNewsResponseNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new LatestNewsResponse(null, true));
    }

    @Test
    @DisplayName("valid construction")
    void latestNewsResponseValid() {
        LatestNewsResponse resp = new LatestNewsResponse(
                Collections.emptyList(), false);
        assertNotNull(resp.latestNews());
        assertFalse(resp.isUpToDate());
    }

    // ===== NewsData =====

    @Test
    @DisplayName("string date constructor parses correctly")
    void newsDataStringDate() {
        NewsData nd = new NewsData(1, "Title", "Content", "20230101", "http://link", true);
        assertEquals(1, nd.id());
        assertEquals("Title", nd.title());
        assertEquals("Content", nd.content());
        assertEquals("http://link", nd.link());
        assertTrue(nd.important());
        assertNotNull(nd.date());
        assertEquals(2023, nd.date().getYear());
        assertEquals(1, nd.date().getMonthValue());
        assertEquals(1, nd.date().getDayOfMonth());
    }

    // ===== ObservableAtomicReference =====

    @Test
    @DisplayName("set and getValue")
    void observableAtomicRefSetGet() {
        ObservableAtomicReference<String> ref = new ObservableAtomicReference<>("initial");
        assertEquals("initial", ref.getValue());
        ref.set("updated");
        assertEquals("updated", ref.getValue());
    }

    // ===== UpdateCheckRequest =====

    @Test
    @DisplayName("record creation")
    void updateCheckRequest() {
        UpdateCheckRequest req = new UpdateCheckRequest(true);
        assertTrue(req.notifyIfNoUpdates());
        UpdateCheckRequest req2 = new UpdateCheckRequest(false);
        assertFalse(req2.notifyIfNoUpdates());
    }

    // ===== ComboItem =====

    @Test
    @DisplayName("key and description accessors")
    void comboItemAccessors() {
        ComboItem<String> item = new ComboItem<>("key1", "Description 1");
        assertEquals("key1", item.key());
        assertEquals("Description 1", item.description());
    }

    @Test
    @DisplayName("toString returns description")
    void comboItemToString() {
        ComboItem<String> item = new ComboItem<>("key1", "My Description");
        assertEquals("My Description", item.toString());
    }

    @Test
    @DisplayName("equals with same reference")
    void comboItemEqualsSameRef() {
        ComboItem<String> item = new ComboItem<>("key1", "desc");
        assertEquals(item, item);
    }

    @Test
    @DisplayName("equals with different type")
    void comboItemEqualsDiffType() {
        ComboItem<String> item = new ComboItem<>("key1", "desc");
        assertNotEquals(item, "not a ComboItem");
    }

    @Test
    @DisplayName("equals with same key")
    void comboItemEqualsSameKey() {
        ComboItem<String> a = new ComboItem<>("key1", "desc1");
        ComboItem<String> b = new ComboItem<>("key1", "desc2");
        assertEquals(a, b);
    }

    @Test
    @DisplayName("equals with different key")
    void comboItemEqualsDiffKey() {
        ComboItem<String> a = new ComboItem<>("key1", "desc");
        ComboItem<String> b = new ComboItem<>("key2", "desc");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("hashCode based on key")
    void comboItemHashCode() {
        ComboItem<String> a = new ComboItem<>("key1", "desc1");
        ComboItem<String> b = new ComboItem<>("key1", "desc2");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("fromLocale factory")
    void comboItemFromLocale() {
        ComboItem<String> item = ComboItem.fromLocale(Locale.US);
        assertEquals("en-US", item.key());
        assertNotNull(item.description());
        assertFalse(item.description().isEmpty());
    }

    @Test
    @DisplayName("keyWithEmptyValue factory")
    void comboItemKeyWithEmptyValue() {
        ComboItem<Integer> item = ComboItem.keyWithEmptyValue(42);
        assertEquals(42, item.key());
        assertEquals("", item.description());
    }

    // ===== ClearToolRequest =====

    @Test
    @DisplayName("valid construction")
    void clearToolRequestValid() {
        ClearToolRequest req = new ClearToolRequest("toolId", true, false);
        assertEquals("toolId", req.toolBinding());
        assertTrue(req.clearEverything());
        assertFalse(req.askConfirmation());
    }

    @Test
    @DisplayName("blank toolBinding throws")
    void clearToolRequestBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClearToolRequest("", true, false));
    }

    // ===== FilesDroppedEvent =====

    @Test
    @DisplayName("valid construction")
    void filesDroppedEventValid() {
        File f = new File("test.pdf");
        FilesDroppedEvent evt = new FilesDroppedEvent("toolId", true, List.of(f));
        assertEquals("toolId", evt.toolBinding());
        assertTrue(evt.acceptMultipleFiles());
        assertEquals(1, evt.files().size());
    }

    @Test
    @DisplayName("blank toolBinding throws")
    void filesDroppedEventBlank() {
        assertThrows(IllegalArgumentException.class, () -> new FilesDroppedEvent("", false, List.of()));
    }

    // ===== OpenType =====

    @Test
    @DisplayName("enum values exist")
    void openTypeValues() {
        OpenType[] values = OpenType.values();
        assertEquals(2, values.length);
        assertEquals(OpenType.SAVE, OpenType.valueOf("SAVE"));
        assertEquals(OpenType.OPEN, OpenType.valueOf("OPEN"));
    }

    // ===== RequiredPdfData =====

    @Test
    @DisplayName("enum values exist")
    void requiredPdfDataValues() {
        RequiredPdfData[] values = RequiredPdfData.values();
        assertEquals(2, values.length);
        assertEquals(RequiredPdfData.DEFAULT, RequiredPdfData.valueOf("DEFAULT"));
        assertEquals(RequiredPdfData.BOOMARKS, RequiredPdfData.valueOf("BOOMARKS"));
    }
}
