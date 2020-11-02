package llschuster.languagegame.de.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.observe
import llschuster.languagegame.de.viewmodels.PlayScreenViewModel
import llschuster.languagegame.de.R
import llschuster.languagegame.de.utils.DecisionOutcomeType
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    lateinit var mainLayout: ConstraintLayout
    lateinit var wordToTest: TextView
    lateinit var confirmBtn: View
    lateinit var denyBtn: View
    lateinit var progressBar: ProgressBar
    lateinit var gameScoreTxt: TextView
    lateinit var feedbackTxt: TextView
    var displaySize: Point = Point()
    var animationFinalValue = 800f
    var animatedWord: AnimatedComponent? = null
    var decision= DecisionOutcomeType.ignore
    var playLock: Boolean = false

    val playScreenViewModel: PlayScreenViewModel by viewModels()

    // onDecision is triggered on the end of a falling answer animation
    // and it decides if should render the next answer or start a complete new round
    fun onDecision(){
        mainLayout?.removeView(animatedWord?.viewToAnimate)
        when{
            decision == DecisionOutcomeType.match -> {
                playScreenViewModel.startNewRound()
                decision = DecisionOutcomeType.ignore
            }
            decision == DecisionOutcomeType.skip -> {
                playScreenViewModel.getNextSolution()
                decision = DecisionOutcomeType.ignore
            }
            decision == DecisionOutcomeType.ignore -> {
                playScreenViewModel.denyMatch()  // ignoring an answer is considered as denying it
                playScreenViewModel.getNextSolution()
            }
        }
    }

    fun renderNextWord(text: String){
        var wordView: View = renderAnswer(text) ?: return
        animatedWord = AnimatedComponent(
            wordView,
            endAnimationCallback = { onDecision() },
            animateTo = animationFinalValue,
            startAnimationCallback = { startTimeOutBar() })
        animatedWord?.objectAnim?.start()
    }

    fun startTimeOutBar(){
        playLock = false
        var animatedProgressBar = ObjectAnimator.ofInt(progressBar, "progress", 100, 0).apply {
            this.duration = 4800
        }
        animatedProgressBar.start()
    }

    fun animateFeedback(text: String, color: Int){
        feedbackTxt.setText(text)
        feedbackTxt.setTextColor(color)
        var animatedFeebackTxt = ObjectAnimator.ofFloat(feedbackTxt, "alpha", 0.5f, 1.0f).apply {
            this.duration = 1500
            this.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {
                    return
                }

                override fun onAnimationEnd(animation: Animator?) {
                    feedbackTxt.visibility = View.INVISIBLE
                }

                override fun onAnimationCancel(animation: Animator?) {
                    return
                }

                override fun onAnimationStart(animation: Animator?) {
                    feedbackTxt.visibility = View.VISIBLE
                }

            })
        }
        animatedFeebackTxt.start()
    }

    fun renderAnswer(text: String): View?{
        try {
            if (mainLayout != null){
                val newTextView = TextView(this)
                newTextView.setText(text)
                newTextView.id = View.generateViewId()
                newTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                newTextView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                val constraintSet = ConstraintSet()
                constraintSet.clone(mainLayout)

                mainLayout?.addView(newTextView)

                constraintSet.connect(newTextView.id, ConstraintSet.TOP, mainLayout.id, ConstraintSet.TOP)
                constraintSet.connect(newTextView.id, ConstraintSet.BOTTOM, mainLayout.id, ConstraintSet.BOTTOM)
                constraintSet.connect(newTextView.id, ConstraintSet.START, mainLayout.id, ConstraintSet.START)
                constraintSet.connect(newTextView.id, ConstraintSet.END, mainLayout.id, ConstraintSet.END)

                constraintSet.applyTo(mainLayout)

                newTextView.translationY = -800f
                newTextView.translationX = displaySize.x * 0.4f

                return newTextView
            }
            return null
        } catch (e: Exception){
            Log.i("Error", e.toString())
            return null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLayout = findViewById(R.id.mainLayout)
        wordToTest = findViewById(R.id.wordToTest)
        confirmBtn = findViewById(R.id.correctBtn)
        denyBtn = findViewById(R.id.incorrectBtn)
        gameScoreTxt = findViewById(R.id.gameScoreTxt)
        feedbackTxt = findViewById(R.id.feedbackTxt)
        progressBar = findViewById(R.id.progressBar)

        confirmBtn.setOnClickListener {
            if (!playLock) {
                playLock = true

                var confirmResult = playScreenViewModel.confirmMatch()
                decision =
                    if (confirmResult) DecisionOutcomeType.match else DecisionOutcomeType.skip

                var feedbackText = if (confirmResult) "Well done" else "Bad luck"
                var feedbackColor = if (confirmResult) Color.GREEN else Color.RED
                animateFeedback(feedbackText, feedbackColor)

                animatedWord?.objectAnim?.cancel()
            }
        }

        denyBtn.setOnClickListener {
            try {
                if (!playLock){
                    playLock = true

                    var denyResult = playScreenViewModel.denyMatch()
                    decision = if (!denyResult) DecisionOutcomeType.match else DecisionOutcomeType.skip
                    if (!denyResult) animateFeedback("That was actually right", Color.RED)

                    animatedWord?.objectAnim?.cancel()
                }
            } catch (e: Exception){
                Log.i("error", e.toString())
            }
        }

        windowManager.defaultDisplay.getSize(displaySize)
        animationFinalValue = displaySize.y * 1.05f //animate until little further than screen size

        playScreenViewModel.wordListInputStream = assets.open("words_v2.json")
        playScreenViewModel.startGame()

        playScreenViewModel.isGameStarted.observe(this) {
            when{
                it == false -> Toast.makeText(this@MainActivity, "Loading", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this@MainActivity, "started", Toast.LENGTH_SHORT).show()
            }
        }

        playScreenViewModel.listOfWords.observe(this){
            if (it.size >= 1) playScreenViewModel.startNewRound()
        }

        playScreenViewModel.currentWord.observe(this){
            wordToTest.setText(it.original)
        }

        playScreenViewModel.currentSolution.observe(this){
            renderNextWord(it.translation)
        }

        playScreenViewModel.gameScore.observe(this) {
            gameScoreTxt.setText(it.toString())
        }

        playScreenViewModel.isGameOver.observe(this){
            if (it){
                var endScreen = Intent(this, GameEnd::class.java)
                startActivity(endScreen)
            }
        }
    }
}

data class AnimatedComponent(var viewToAnimate: View?, var endAnimationCallback: ()->Unit,  var startAnimationCallback: ()->Unit, var animateTo: Float) {
    lateinit var objectAnim: ObjectAnimator
    init {
        var objectAnimator = ObjectAnimator.ofFloat(viewToAnimate, "translationY", animateTo).apply {
            this.duration = 5000

            this.addListener(object: Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {
                    return
                }

                override fun onAnimationEnd(animation: Animator?) {
                    animation?.duration
                    return endAnimationCallback()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    return
                }

                override fun onAnimationStart(animation: Animator?) {
                    return startAnimationCallback()
                }
            })
        }
        objectAnim = objectAnimator
    }
}


