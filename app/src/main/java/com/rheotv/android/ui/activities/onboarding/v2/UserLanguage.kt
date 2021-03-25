package com.rheotv.android.ui.activities.onboarding.v2

import java.util.*

sealed class UserLanguage {
    object Hindi : UserLanguage() {
        override val people_watching_live = "5000 se adhik log live dekh rahe hain"
        override val login_to_chat = "Inse chat karne ke liye abhi login karen"
        override val pick_username_title = "Rheo ki gaming community apko iss naam se jaanegi"
        override val pick_username_subtitle = "Apni pasand ka username chuniye \uD83D\uDE0E"
        override val top_streamer = "Hamari behtareen recommendation sirf apke liye ! kinhi teen ya usase adhik streamers ko follow kar aagae badhein."
        override val most_awaited_shows = "⏰ Arre wo aa gya jisaka tha intejaar srif Rheo par"
        override val select_at_least_3 = "Aage badne ke liye ek ya ek se adhik shows chune"
        override val welcome_message: String = "%s, Rheo par apni pehli stream mein aapka swagath hain!"
    }

    object Tamil : UserLanguage() {
        override val people_watching_live: String = "5000thikum merpatta parvai aalargal neralaiyei paathukondu irukirargal"
        override val login_to_chat: String = "avargaludan chat muliyamaga thodara login seiungal "
        override val pick_username_title: String = "Rheo vil ulla gaming community indha peyaril ungalai arindhu kollum!"
        override val pick_username_subtitle: String = "fun aana peiyarai therndheydunga \uD83D\uDE0E"
        override val top_streamer: String = "Ungalukkana sirandha parinthuraigal ! melum thodara kuraindhadhu 3 streamergalai pinthodaravum"
        override val most_awaited_shows: String = "⏰ rheo vil migavum ethirpaarkapatta  nigazhtchigal"
        override val select_at_least_3: String = "thodara  kuraindhathu 1 aavadhu therntheydukavum"
        override val welcome_message: String = "vanakam %s, Rheo ungaladhu mudhal stream ku varverkiradhu!"
    }


    object English : UserLanguage() {
        override val people_watching_live: String = "5K+ people are watching live now"
        override val login_to_chat: String = "Login to watch & chat with them"
        override val pick_username_title: String = "Gaming community in Rheo will get to know you by this name"
        override val pick_username_subtitle: String = "Pick a cool username \uD83D\uDE0E"
        override val top_streamer: String = "Our Top recommendations for you! follow at least 3 streamers to continue(Description below language selected)"
        override val most_awaited_shows: String = "⏰ Must watch live game shows"
        override val select_at_least_3: String = "Select at least 1 for best experience"
        override val welcome_message: String = "Welcome %s to your first stream on Rheo!"
    }


    object Malayalam : UserLanguage() {
        override val people_watching_live: String = "5000 thil kooduthal aalukal ippol tatsamayam kaanunnu"
        override val login_to_chat: String = "Avarumayi chat cheyyan ipol thanne login cheyyuka"
        override val pick_username_title: String = "Rheo le gaming community ningale ee peril ariyappedum"
        override val pick_username_subtitle: String = "rasakaramaya oru username thiranjedukkuka \uD83D\uDE0E"
        override val top_streamer: String = "Ningalkkayi njangalude migacha shubarshakal! Thudarunnathinu kuranjath 3 streamukal pinthudaruka"
        override val most_awaited_shows: String = "Ningal ettavum kooduthal kaathirunna shows ippol rheo il"
        override val select_at_least_3: String = "Thudaran kuranjath 1 thiranjedukkuka"
        override val welcome_message: String = "Rheo ila ningalude aadyathe streamilekku swagatham %s!"
    }


    object Telugu : UserLanguage() {
        override val people_watching_live: String = "5000 kante ekkuva mandi ippuḍu Live chustunnaru"
        override val login_to_chat: String = "Varito chaṭ cheyaḍaniki login avvaṇḍi"
        override val pick_username_title: String = "Rheo lo gaming community ki ee perutho parichayam avutharu"
        override val pick_username_subtitle: String = "Meeku tagina username enchukondi \uD83D\uDE0E"
        override val top_streamer: String = "Mi kosam ma agra sipharsulu! Konasaginacaḍaniki kanisaṁ 3 sṭreamaranu anusarinncaṇḍi"
        override val most_awaited_shows: String = "Rheolo ekkuva mandi eduruchustunna shows"
        override val select_at_least_3: String = "Konasaginchaḍaniki kanisam 1 shows enchukondi"
        override val welcome_message: String = "%s, Rheo par apni pehli stream mein aapka swagath hain!"
    }

    abstract val people_watching_live: String
    abstract val login_to_chat: String
    abstract val pick_username_title: String
    abstract val pick_username_subtitle: String
    abstract val top_streamer: String
    abstract val most_awaited_shows: String
    abstract val select_at_least_3: String
    abstract val welcome_message: String

    companion object {

        fun toUserLanguage(lang: String): UserLanguage {
            return when (lang.toLowerCase(Locale.getDefault())) {
                "hindi" -> Hindi
                "tamil" -> Tamil
                "telugu" -> Telugu
                "malayalam" -> Malayalam
                else -> English
            }
        }
    }
}