package org.xpfarm.worldcrud;

public enum WorldTypes {
    NORMAL,
    SKYBLOCK_CLASSIC;
    
    public static WorldTypes fromString(String type) {
        if (type == null || type.trim().isEmpty()) {
            return NORMAL;
        }
        
        try {
            return valueOf(type.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return NORMAL;
        }
    }
    
    public static String[] getValidTypes() {
        WorldTypes[] types = values();
        String[] typeNames = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            typeNames[i] = types[i].name();
        }
        return typeNames;
    }
}
