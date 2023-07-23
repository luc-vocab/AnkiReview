# Install AnkiDroid
after starting up an android emulator
`adb install /volatile_home/luc/apk/AnkiDroid-2.15.6-universal.apk`

# Send collection file
`adb push /volatile_home/luc/anki_decks/anki_collection.colpkg /sdcard/Download`

# Documentation for using AnkiDroid API
## Need to build release version of anki API
Open AnkiDroid module, select build variants, then build the API. Should generate the `Anki-Android/api/build/outputs/aar/api-release.aar`,
which you can copy to `AnkiReview/anki-api/api-release.aar`.

https://github.com/ankidroid/Anki-Android/issues/4617
https://github.com/ankidroid/apisample/pull/5/files
https://github.com/ankidroid/Anki-Android/issues/5613