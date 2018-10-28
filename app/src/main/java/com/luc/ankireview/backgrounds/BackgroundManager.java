package com.luc.ankireview.backgrounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

public class BackgroundManager {

    public BackgroundManager(long deckId) {
        m_deckId = deckId;

        String[] backgroundImageUrls = {
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301931/ankireview_backgrounds/chinese_women/dreamstimemaximum_52491159.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301930/ankireview_backgrounds/chinese_women/dreamstimemaximum_51242767.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301928/ankireview_backgrounds/chinese_women/dreamstimemaximum_46084453.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301927/ankireview_backgrounds/chinese_women/dreamstimemaximum_45547181.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301925/ankireview_backgrounds/chinese_women/dreamstimemaximum_45193806.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301923/ankireview_backgrounds/chinese_women/dreamstimemaximum_41211514.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301922/ankireview_backgrounds/chinese_women/dreamstimemaximum_41171330.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301921/ankireview_backgrounds/chinese_women/dreamstimemaximum_40065466.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301920/ankireview_backgrounds/chinese_women/dreamstimemaximum_33367818.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301917/ankireview_backgrounds/chinese_women/dreamstimemaximum_33112734.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301912/ankireview_backgrounds/chinese_women/dreamstimeextralarge_54834109.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301911/ankireview_backgrounds/chinese_women/dreamstimeextralarge_53686849.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301911/ankireview_backgrounds/chinese_women/dreamstimeextralarge_54833749.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301909/ankireview_backgrounds/chinese_women/dreamstimeextralarge_53686790.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301900/ankireview_backgrounds/chinese_women/dreamstimeextralarge_48563750.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301900/ankireview_backgrounds/chinese_women/dreamstimeextralarge_51136353.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301900/ankireview_backgrounds/chinese_women/dreamstimeextralarge_51136341.jpg"
        };

        m_backgroundUrlList = new Vector<String>(Arrays.asList(backgroundImageUrls));
        Collections.shuffle(m_backgroundUrlList);
    }


    public String getBackgroundUrl() {

        // get current URL
        m_currentBackgroundIndex++;
        if(m_currentBackgroundIndex > m_backgroundUrlList.size() - 1) {
            m_currentBackgroundIndex = 0;
        }
        String imgUrl = m_backgroundUrlList.get(m_currentBackgroundIndex);

        return imgUrl;
    }


    private long m_deckId;

    private Vector<String> m_backgroundUrlList;
    private int m_currentBackgroundIndex = 0;

}
