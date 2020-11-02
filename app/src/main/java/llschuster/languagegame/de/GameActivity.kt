package llschuster.languagegame.de

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class GameActivity : AppCompatActivity() {
    lateinit var startGameBtn:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        startGameBtn = findViewById(R.id.gameStartBtn)
        startGameBtn.setOnClickListener {
            var gotoGameScreen = Intent(this, MainActivity::class.java)
            startActivity(gotoGameScreen)
        }
    }
}