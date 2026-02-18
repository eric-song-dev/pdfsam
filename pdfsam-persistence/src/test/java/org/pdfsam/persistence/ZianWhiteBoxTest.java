/*
 * White Box Testing - Part 3
 * Author: Zian Xu
 * Module: pdfsam-persistence
 *
 * This test class targets uncovered lines in pdfsam-persistence classes,
 * focusing on DefaultEntityRepository (primitive type methods),
 * PreferencesRepository (additional operations), and PersistenceException.
 */
package org.pdfsam.persistence;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * White-box tests for pdfsam-persistence module.
 * Covers previously uncovered primitive-type accessors, keys retrieval,
 * and PersistenceException constructors.
 *
 * @author Zian Xu
 */
public class ZianWhiteBoxTest {

    private static DefaultEntityRepository<TestEntity> entityRepo;
    private static PreferencesRepository prefsRepo;

    @BeforeAll
    static void setUp() {
        var mapper = JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .visibility(PropertyAccessor.FIELD, Visibility.ANY)
                .configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, false)
                .serializationInclusion(Include.ALWAYS)
                .build();
        entityRepo = new DefaultEntityRepository<>(
                "/test/org/pdfsam/zian/entity", mapper, TestEntity.class);
        prefsRepo = new PreferencesRepository("/test/org/pdfsam/zian/prefs");
    }

    @AfterAll
    static void tearDown() throws PersistenceException {
        entityRepo.clean();
        prefsRepo.clean();
    }

    // ===== PersistenceException constructors =====

    @Test
    @DisplayName("PersistenceException: message-only constructor")
    void persistenceExceptionMessageOnly() {
        PersistenceException ex = new PersistenceException("test error");
        assertEquals("test error", ex.getMessage());
        assertNull(ex.getCause());
    }


    @Test
    @DisplayName("PersistenceException: cause-only constructor")
    void persistenceExceptionCauseOnly() {
        Throwable cause = new RuntimeException("root cause");
        PersistenceException ex = new PersistenceException(cause);
        assertEquals(cause, ex.getCause());
    }

    // ===== DefaultEntityRepository: primitive type accessors =====

    @Test
    @DisplayName("DefaultEntityRepository: saveInt and getInt")
    void entityRepoSaveGetInt() throws PersistenceException {
        entityRepo.saveInt("intKey", 42);
        assertEquals(42, entityRepo.getInt("intKey", 0));
    }

    @Test
    @DisplayName("DefaultEntityRepository: getInt default value")
    void entityRepoGetIntDefault() throws PersistenceException {
        entityRepo.delete("intMissing");
        assertEquals(-1, entityRepo.getInt("intMissing", -1));
    }

    @Test
    @DisplayName("DefaultEntityRepository: saveLong and getLong")
    void entityRepoSaveGetLong() throws PersistenceException {
        entityRepo.saveLong("longKey", 123456789L);
        assertEquals(123456789L, entityRepo.getLong("longKey", 0L));
    }

    @Test
    @DisplayName("DefaultEntityRepository: getLong default value")
    void entityRepoGetLongDefault() throws PersistenceException {
        entityRepo.delete("longMissing");
        assertEquals(-1L, entityRepo.getLong("longMissing", -1L));
    }

    @Test
    @DisplayName("DefaultEntityRepository: saveString and getString")
    void entityRepoSaveGetString() throws PersistenceException {
        entityRepo.saveString("strKey", "hello");
        assertEquals("hello", entityRepo.getString("strKey", (String) null));
    }

    @Test
    @DisplayName("DefaultEntityRepository: getString default value")
    void entityRepoGetStringDefault() throws PersistenceException {
        entityRepo.delete("strMissing");
        assertEquals("default", entityRepo.getString("strMissing", "default"));
    }

    @Test
    @DisplayName("DefaultEntityRepository: saveBoolean and getBoolean")
    void entityRepoSaveGetBoolean() throws PersistenceException {
        entityRepo.saveBoolean("boolKey", true);
        assertTrue(entityRepo.getBoolean("boolKey", false));
    }

    @Test
    @DisplayName("DefaultEntityRepository: getBoolean default value")
    void entityRepoGetBooleanDefault() throws PersistenceException {
        entityRepo.delete("boolMissing");
        assertFalse(entityRepo.getBoolean("boolMissing", false));
    }

    @Test
    @DisplayName("DefaultEntityRepository: keys returns stored keys")
    void entityRepoKeys() throws PersistenceException {
        entityRepo.clean();
        entityRepo.save("testKey1", new TestEntity("a", 1));
        entityRepo.save("testKey2", new TestEntity("b", 2));
        var keys = entityRepo.keys();
        assertThat(keys).hasSize(2);
        assertThat(keys).containsOnly("testKey1", "testKey2");
    }

    @Test
    @DisplayName("DefaultEntityRepository: keys returns empty after clean")
    void entityRepoKeysAfterClean() throws PersistenceException {
        entityRepo.save("tempKey", new TestEntity("temp", 0));
        entityRepo.clean();
        var keys = entityRepo.keys();
        assertThat(keys).isEmpty();
    }

    @Test
    @DisplayName("getString(Supplier) coverage")
    void entityRepoGetStringWithSupplier() {
        entityRepo.delete("missingKey");
        assertEquals("fallback", entityRepo.getString("missingKey", () -> "fallback"));
    }

    @Test
    @DisplayName("get(key) triggers PersistenceException on corrupted JSON")
    void entityRepoGetThrowsOnCorruptedData() throws PersistenceException {
        PreferencesRepository rawRepo = new PreferencesRepository("/test/org/pdfsam/zian/entity");

        rawRepo.saveString("corruptKey", "{ not valid json ... }");

        assertThrows(PersistenceException.class, () -> entityRepo.get("corruptKey"));
    }

    @Test
    @DisplayName("save(entity) triggers PersistenceException on serialization error")
    void entityRepoSaveThrowsOnSerializationError() throws Exception {
        ObjectMapper mockMapper = org.mockito.Mockito.mock(ObjectMapper.class);

        com.fasterxml.jackson.core.JsonProcessingException fakeException =
                new com.fasterxml.jackson.core.JsonProcessingException("Simulated error") {};

        org.mockito.Mockito.when(mockMapper.writeValueAsString(org.mockito.ArgumentMatchers.any()))
                .thenThrow(fakeException);

        DefaultEntityRepository<TestEntity> brokenEntityRepo = new DefaultEntityRepository<>(
                "/test/org/pdfsam/zian/entity_mock", mockMapper, TestEntity.class);

        assertThrows(PersistenceException.class,
                () -> brokenEntityRepo.save("anyKey", new TestEntity("name", 1)));
    }

    // ===== Repository Interface Default Methods Coverage =====

    @Test
    @DisplayName("Repository: getInt uses Supplier for default value")
    void repositoryGetIntWithSupplier() throws PersistenceException {
        entityRepo.delete("intSuppMissing");

        assertEquals(100, entityRepo.getInt("intSuppMissing", () -> 100));
    }

    @Test
    @DisplayName("Repository: getLong uses Supplier for default value")
    void repositoryGetLongWithSupplier() throws PersistenceException {
        entityRepo.delete("longSuppMissing");

        assertEquals(999L, entityRepo.getLong("longSuppMissing", () -> 999L));
    }

    public record TestEntity(String name, Integer value) {
    }

    // ===== PreferencesRepository additional coverage =====

    @Nested
    class ZianExceptionCoverageTest {
        private final PreferencesRepository brokenRepo = new PreferencesRepository("/invalid//double/slash/path");

        @Test
        @DisplayName("getInt triggers PersistenceException on invalid path")
        void getIntException() {
            assertThrows(PersistenceException.class, () -> brokenRepo.getInt("anyKey", 0));
        }

        @Test
        @DisplayName("getLong triggers PersistenceException on invalid path")
        void getLongException() {
            assertThrows(PersistenceException.class, () -> brokenRepo.getLong("anyKey", 0L));
        }

        @Test
        @DisplayName("getBoolean triggers PersistenceException on invalid path")
        void getBooleanException() {
            assertThrows(PersistenceException.class, () -> brokenRepo.getBoolean("anyKey", false));
        }

        @Test
        @DisplayName("getString triggers PersistenceException on invalid path")
        void getStringException() {
            assertThrows(PersistenceException.class, () -> brokenRepo.getString("anyKey", () -> "default"));
        }

        @Test
        @DisplayName("saveInt triggers PersistenceException on invalid path")
        void saveIntException() {
            assertThrows(PersistenceException.class, () -> brokenRepo.saveInt("anyKey", 1));
        }

        @Test
        @DisplayName("saveLong triggers PersistenceException on invalid path")
        void saveLongException() {
            assertThrows(PersistenceException.class, () -> brokenRepo.saveLong("anyKey", 1L));
        }

        @Test
        @DisplayName("saveBoolean triggers PersistenceException on invalid path")
        void saveBooleanException() {
            assertThrows(PersistenceException.class, () -> brokenRepo.saveBoolean("anyKey", true));
        }

        @Test
        @DisplayName("saveString triggers PersistenceException on invalid path")
        void saveStringException() {
            assertThrows(PersistenceException.class, () -> brokenRepo.saveString("anyKey", "val"));
        }

        @Test
        @DisplayName("saveString (null) triggers PersistenceException via delete on invalid path")
        void saveStringNullException() {
            assertThrows(PersistenceException.class, () -> brokenRepo.saveString("anyKey", null));
        }

        @Test
        @DisplayName("delete triggers PersistenceException on invalid path")
        void deleteException() {
            assertThrows(PersistenceException.class, () -> brokenRepo.delete("anyKey"));
        }

    }
}
