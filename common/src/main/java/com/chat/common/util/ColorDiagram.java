package com.chat.common.util;

import java.util.Random;

public class ColorDiagram {

    private final String[] colors = {
            "#0715cd",
            "#b536da",
            "#e00707",
            "#4ac925",
            "#00d5f2",
            "#1f9400",
            "#ff6ff2",
            "#f2a400",
            "#a10000",
            "#a15000",
            "#a1a100",
            "#626262",
            "#416600",
            "#008141",
            "#008282",
            "#005682",
            "#000056",
            "#2b0057",
            "#6a006a",
            "#77003c",
    };

    private final Random random = new Random();
    
    public String getColor(String sessionId) {
        int hash = Math.abs(sessionId.hashCode());
        int idx = hash % colors.length;
        return colors[idx];
    }
}
