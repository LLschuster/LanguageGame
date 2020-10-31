package llschuster.languagegame.de

import android.animation.Animator
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    lateinit var mainLayout: ConstraintLayout
    var currentWord: String = ""
    var listOfWords = mutableListOf("hi", "morning", "bye").iterator()


    fun onEndAnimation(){
        renderNextWord()
        return Toast.makeText(this, "end animation", Toast.LENGTH_SHORT).show()
    }

    fun renderNextWord(){
        if (listOfWords.hasNext()){
            currentWord = listOfWords.next()
            var wordView = inflateWord(currentWord)
            var animatedWord = getAnimatedComponent(wordView, endAnimationCallback = {onEndAnimation()})
            animatedWord?.start()
        }
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
            constraintSet.connect(newTextView.id, ConstraintSet.START, mainLayout.id, ConstraintSet.START, 300)
            constraintSet.connect(newTextView.id, ConstraintSet.END, mainLayout.id, ConstraintSet.END)

            constraintSet.applyTo(mainLayout)
            return newTextView
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainLayout = findViewById(R.id.mainLayout)

    }

    override fun onResume() {
        super.onResume()
        renderNextWord()
    }
}


fun getAnimatedComponent(animatedView: View?, endAnimationCallback: ()->Unit): ObjectAnimator? {
    if (animatedView ==  null){
        return null
    }

    var objectAnim = ObjectAnimator.ofFloat(animatedView, "translationY", 800f).apply {
        this.duration = 2000

        this.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {
                return
            }

            override fun onAnimationEnd(animation: Animator?) {
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