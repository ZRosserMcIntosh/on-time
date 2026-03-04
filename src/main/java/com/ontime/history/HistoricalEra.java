package com.ontime.history;

/**
 * Represents one of the 12 historical eras that map to Virgil's 12 cognitive principles.
 * Each era provides a unique narrative setting and gameplay lens for its associated principle.
 */
public class HistoricalEra {

    private final String id;
    private final String name;
    private final String yearRange;
    private final String cognitivePrinciple;
    private final String description;
    private final float themeR, themeG, themeB;

    public HistoricalEra(String id, String name, String yearRange,
                         String cognitivePrinciple, String description,
                         float r, float g, float b) {
        this.id = id;
        this.name = name;
        this.yearRange = yearRange;
        this.cognitivePrinciple = cognitivePrinciple;
        this.description = description;
        this.themeR = r;
        this.themeG = g;
        this.themeB = b;
    }

    public String getId()                  { return id; }
    public String getName()                { return name; }
    public String getYearRange()           { return yearRange; }
    public String getCognitivePrinciple()  { return cognitivePrinciple; }
    public String getDescription()         { return description; }

    public float getThemeR() { return themeR; }
    public float getThemeG() { return themeG; }
    public float getThemeB() { return themeB; }

    /**
     * Returns a float[3] RGB theme color for this era.
     */
    public float[] getThemeColor() {
        return new float[]{ themeR, themeG, themeB };
    }
}
