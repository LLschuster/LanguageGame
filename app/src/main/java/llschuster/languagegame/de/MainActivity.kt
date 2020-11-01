package llschuster.languagegame.de

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.observe


class MainActivity : AppCompatActivity() {
    lateinit var mainLayout: ConstraintLayout
    lateinit var wordToTest: TextView
    lateinit var blackHole: View
    var displaySize: Point = Point()
    private val playScreenViewModel: PlayScreenViewModel by viewModels()
    var animationFinalValue = 800f


    fun onEndAnimation(){
        playScreenViewModel.getNextSolution()
    }

    fun renderNextWord(text: String){
        var wordView = inflateWord(text)
        var animatedWord = getAnimatedComponent(wordView, endAnimationCallback = {onEndAnimation()}, animateTo = animationFinalValue)
        animatedWord?.start()
    }

    fun inflateWord(text: String): View?{
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLayout = findViewById(R.id.mainLayout)
        wordToTest = findViewById(R.id.wordToTest)
        blackHole = findViewById(R.id.black_hole)

        windowManager.defaultDisplay.getSize(displaySize)
        animationFinalValue = displaySize.y * 0.80f

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
    }

    override fun onResume() {
        super.onResume()
    }

    fun getAnimatedComponent(animatedView: View?, endAnimationCallback: ()->Unit, animateTo: Float): ObjectAnimator? {
        if (animatedView ==  null){
            return null
        }

        var objectAnim = ObjectAnimator.ofFloat(animatedView, "translationY", animateTo).apply {
            this.duration = 2000

            this.addListener(object: Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {
                    return
                }

                override fun onAnimationEnd(animation: Animator?) {
                    mainLayout?.removeView(animatedView)
                    return endAnimationCallback()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    return
                }

                override fun onAnimationStart(animation: Animator?) {
                    return
                }

            })
        }

        return objectAnim
    }
}


