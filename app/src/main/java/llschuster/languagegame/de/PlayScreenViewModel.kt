package llschuster.languagegame.de

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.lang.reflect.Type
import kotlin.random.Random

class PlayScreenViewModel: ViewModel() {
    var isGameStarted = MutableLiveData<Boolean>(false)
    var gameScore = MutableLiveData<Int>(0)
    var playerLifes = MutableLiveData<Int>()
    var listOfWords = MutableLiveData<List<WordModel>>()
    var currentWord = MutableLiveData<WordModel>()
    var currentSolution = MutableLiveData<WordModel>()
    var possibleSolutions = MutableLiveData<List<WordModel>>()    //list of strings simplify
    lateinit var wordListInputStream: InputStream
    final var SIZE_OF_SOLUTIONS_LIST: Int = 16

    fun startGame(){
        GlobalScope.launch {
            getListOfWords()
            isGameStarted.value = true
        }
        gameScore.value = 0
        playerLifes.value = 3
    }

    fun getListOfWords() {
        var jsonFile = readJsonFromAsset()
        Log.i("json", jsonFile.toString())
        val listType: Type = object : TypeToken<List<WordModel>>() {}.type
        var resultList = Gson().fromJson<List<WordModel>>(jsonFile, listType)
        listOfWords.value = resultList
        startNewRound()
    }

    fun startNewRound() {
        if (listOfWords?.value == null) return
        var rndIndex = Random.nextInt(0, listOfWords.value!!.size)
        possibleSolutions.value = List<WordModel>(SIZE_OF_SOLUTIONS_LIST) {
                index ->
            when {
                index < SIZE_OF_SOLUTIONS_LIST/8 -> listOfWords.value!![rndIndex - index]
                else -> listOfWords.value!![rndIndex + index]
            }
        }
        currentWord.value =  listOfWords.value!![rndIndex]
        getNextSolution()
    }

    fun getNextSolution(){
        if (possibleSolutions?.value == null || possibleSolutions?.value!!.size <= 0) return startNewRound()
        val rndIndex = Random.nextInt(0, possibleSolutions.value!!.size)
        currentSolution.value = possibleSolutions.value!![rndIndex]
        possibleSolutions.value = possibleSolutions.value!!.filterIndexed {
                index, _ ->
                index != rndIndex
        }
    }

    fun confirmMatch(): Boolean{
        if (currentWord.value!!.translation == currentSolution.value!!.translation){
            gameScore.value = gameScore.value?.plus(1)
            return true
        }
        gameScore.value = gameScore.value?.minus(1)
        return false
    }

    fun denyMatch(): Boolean{
        if (currentWord.value!!.translation != currentSolution.value!!.translation){
            return true
        }
        gameScore.value = gameScore.value?.minus(1)
        return false
    }

    fun readJsonFromAsset(): String? {
        if (wordListInputStream == null) return null
        var json: String? = null
        try {
            json = wordListInputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception){
            ex.printStackTrace()
            return null
        }
        return json
    }
}