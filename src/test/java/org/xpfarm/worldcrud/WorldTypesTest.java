package org.xpfarm.worldcrud;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WorldTypesTest {
    
    @Test
    void testFromString() {
        assertEquals(WorldTypes.NORMAL, WorldTypes.fromString("NORMAL"));
        assertEquals(WorldTypes.SKYBLOCK_CLASSIC, WorldTypes.fromString("SKYBLOCK_CLASSIC"));
        
        // Test case insensitive
        assertEquals(WorldTypes.NORMAL, WorldTypes.fromString("normal"));
        assertEquals(WorldTypes.SKYBLOCK_CLASSIC, WorldTypes.fromString("skyblock_classic"));
        
        // Test invalid values
        assertEquals(WorldTypes.NORMAL, WorldTypes.fromString("INVALID"));
        assertEquals(WorldTypes.NORMAL, WorldTypes.fromString("FLAT")); // Falls back to NORMAL
        assertEquals(WorldTypes.NORMAL, WorldTypes.fromString("ISLAND")); // Falls back to NORMAL
        assertEquals(WorldTypes.NORMAL, WorldTypes.fromString("VOID")); // Falls back to NORMAL
        assertEquals(WorldTypes.NORMAL, WorldTypes.fromString(null));
        assertEquals(WorldTypes.NORMAL, WorldTypes.fromString(""));
    }
    
    @Test
    void testGetValidTypes() {
        String[] validTypes = WorldTypes.getValidTypes();
        assertNotNull(validTypes);
        assertEquals(2, validTypes.length);
        
        // Check that all skyblock types are present
        assertTrue(containsValue(validTypes, "NORMAL"));
        assertTrue(containsValue(validTypes, "SKYBLOCK_CLASSIC"));
    }
    
    private boolean containsValue(String[] array, String value) {
        for (String item : array) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
