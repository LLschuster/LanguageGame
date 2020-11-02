package llschuster.languagegame.de.models

import com.google.gson.annotations.SerializedName

data class WordModel(
    @SerializedName("text_spa")
    var translation: String,
    @SerializedName("text_eng")
    var original: String
) {}