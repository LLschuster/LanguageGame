package llschuster.languagegame.de

import android.animation.Animator
import android.animation.ObjectAnimator
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
import java.lang.Exception

enum class DecisionOutcomeType {
    skip,
    match,
    ignore
}

class MainActivity : AppCompatActivity() {
    lateinit var mainLayout: ConstraintLayout
    lateinit var wordToTest: TextView
    lateinit var confirmBtn: View
    lateinit var denyBtn: View
    lateinit var progressBar: ProgressBar
    lateinit var gameScoreTxt: TextView
    var displaySize: Point = Point()
    private val playScreenViewModel: PlayScreenViewModel by viewModels()
    var animationFinalValue = 800f
    var animatedWord: AnimatedComponent? = null
    var decision= DecisionOutcomeType.ignore


    fun onDecision(){
        mainLayout?.removeView(animatedWord?.viewToAnimate)
        when{
            decision == DecisionOutcomeType.match -> {
                playScreenViewModel.getRandomWord()
                decision = DecisionOutcomeType.ignore
            }
            decision == DecisionOutcomeType.skip -> {
                playScreenViewModel.getNextSolution()
                decision = DecisionOutcomeType.ignore
            }
            decision == DecisionOutcomeType.ignore -> {
                playScreenViewModel.denyMatch()
                playScreenViewModel.getNextSolution()
            }
        }
    }

    fun renderNextWord(text: String){
        var wordView: View = inflateWord(text) ?: return
        animatedWord = AnimatedComponent(wordView, endAnimationCallback = {onDecision()} ,
            animateTo = animationFinalValue, startAnimationCallback = {startTimeOutBar()})
        animatedWord?.objectAnim?.start()
    }

    fun startTimeOutBar(){
        var animatedProgressBar = ObjectAnimator.ofInt(progressBar, "progress", 100, 0).apply {
            this.duration = 4800
        }
        animatedProgressBar.start()
    }

    fun inflateWord(text: String): View?{
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
        progressBar = findViewById(R.id.progressBar)

        confirmBtn.setOnClickListener {
            var confirmResult = playScreenViewModel.confirmMatch()
            decision = if (confirmResult) DecisionOutcomeType.match else DecisionOutcomeType.skip
            animatedWord?.objectAnim?.cancel()
        }

        denyBtn.setOnClickListener {
            try {
                var denyResult = playScreenViewModel.denyMatch()
                decision = if (!denyResult) DecisionOutcomeType.match else DecisionOutcomeType.skip
                animatedWord?.objectAnim?.cancel()
            } catch (e: Exception){
                Log.i("error", e.toString())
            }
        }

        windowManager.defaultDisplay.getSize(displaySize)
        animationFinalValue = displaySize.y * 1.10f

        playScreenViewModel.wordListInputStream = assets.open("words_v2.json")
        playScreenViewModel.getListOfWords()
        playScreenViewModel.isGameStarted.observe(this) {
            when{
                it == false -> Toast.makeText(this@MainActivity, "Loading", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this@MainActivity, "started", Toast.LENGTH_SHORT).show()
            }
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
    }

    override fun onResume() {
        super.onResume()
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


