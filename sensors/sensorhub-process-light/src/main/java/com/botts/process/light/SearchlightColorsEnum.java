package com.botts.process.light;

/**
 * just repeat of SearchLightState
 */
public enum SearchlightColorsEnum {
    OFF,
    WHITE,
    RED,
    MAGENTA,
    BLUE,
    CYAN,
    GREEN,
    YELLOW,
    UNKNOWN;

    public static SearchlightColorsEnum fromString (String name){
        for(SearchlightColorsEnum color: SearchlightColorsEnum.values()){
            if(color.name().equalsIgnoreCase(name)){
                return color;
            }
        }
        return UNKNOWN;
    }
}
