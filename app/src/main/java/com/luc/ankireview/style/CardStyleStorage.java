package com.luc.ankireview.style;

import java.io.Serializable;
import java.util.HashMap;

public class CardStyleStorage  implements Serializable {
    public static final long serialVersionUID = 1L; // increment this in case of schema changes
    public HashMap<CardTemplateKey, CardTemplate> cardTemplateMap;
}
