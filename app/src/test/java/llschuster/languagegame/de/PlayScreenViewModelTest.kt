package llschuster.languagegame.de

import android.app.ActivityManager
import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class PlayScreenViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun startNewRound_shouldGenerate_new_word_and_possible_solutions(){
        var app = ApplicationProvider.getApplicationContext<Application>()

        var playScreenViewModel = PlayScreenViewModel()
        playScreenViewModel.wordListInputStream = app.assets.open("words_v2.json")
        playScreenViewModel.getListOfWords()

        playScreenViewModel.startNewRound()
        val value = playScreenViewModel.currentWord.getOrAwaitValue()
        val solutions = playScreenViewModel.possibleSolutions.getOrAwaitValue()

        assertNotNull(value)
        assertEquals(solutions.size, 15)
    }

    @Test
    fun getListOfWords_shouldLoadWordsFromJsonData(){
        var app = ApplicationProvider.getApplicationContext<Application>()

        var playScreenViewModel = PlayScreenViewModel()
        playScreenViewModel.wordListInputStream = app.assets.open("words_v2.json")
        playScreenViewModel.getListOfWords()

        val words = playScreenViewModel.listOfWords.getOrAwaitValue()

        assertNotNull(words)
    }
}