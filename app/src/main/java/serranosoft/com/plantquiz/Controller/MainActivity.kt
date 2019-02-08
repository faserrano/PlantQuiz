package serranosoft.com.plantquiz.Controller

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity;
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import serranosoft.com.plantquiz.Model.DownloadingObject
import serranosoft.com.plantquiz.Model.ParsePlantUtility
import serranosoft.com.plantquiz.Model.Plant
import serranosoft.com.plantquiz.R
import java.lang.Exception
import android.util.TypedValue
import android.content.Context
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo


class MainActivity : AppCompatActivity() {

    private var cameraButton: Button? = null
    private var photoGalleryButton: Button? = null
    private var imageTaken: ImageView? = null

    //Instance Variables
    var correctAnswerIndex: Int = 0
    var correctPlant: Plant? = null
    var numberOfTimesUserAnsweredCorrectly: Int = 0
    var numberOfTimesUserAnsweredIncorrectly: Int = 0
    var answerChosen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        showProgressBar(false)
        displayUIWidgets(false)

        playAnimationOnView(btnNextPlant, 5, Techniques.Pulse)

        imageTaken = findViewById<ImageView>(R.id.imgTaken)

        btnNextPlant.setOnClickListener(View.OnClickListener {
            if(checkForInternetConnection()){
                showProgressBar(true)
                try {
                    val innerClassObject = DownloadingPlantTask()
                    innerClassObject.execute()
                } catch (e: Exception){
                    e.printStackTrace()
                }

                answerChosen = false
                colorAllButtons()
            }
        })


    }

    fun button1isClicked(buttonView: View) { specifyTheRightAndWrongAnswer(0) }

    fun button2isClicked(buttonView: View) { specifyTheRightAndWrongAnswer(1) }

    fun button3isClicked(buttonView: View) { specifyTheRightAndWrongAnswer(2) }

    fun button4isClicked(buttonView: View) { specifyTheRightAndWrongAnswer(3) }

    inner class DownloadingPlantTask: AsyncTask<String, Int, List<Plant>>() {
        override fun doInBackground(vararg p0: String?): List<Plant>? {

            val parsePlantUtility: ParsePlantUtility = ParsePlantUtility()

            return parsePlantUtility.parsePlantObjectsFromJSONData()
        }

        override fun onPostExecute(result: List<Plant>?) {
            super.onPostExecute(result)

            var numberOfPlants = result?.size ?: 0

            if (numberOfPlants > 0) {
                var randomPlantIndexForButton1: Int = (Math.random() * result!!.size).toInt()
                var randomPlantIndexForButton2: Int = (Math.random() * result!!.size).toInt()
                var randomPlantIndexForButton3: Int = (Math.random() * result!!.size).toInt()
                var randomPlantIndexForButton4: Int = (Math.random() * result!!.size).toInt()

                var allRandomPlants = ArrayList<Plant>()
                allRandomPlants.add(result.get(randomPlantIndexForButton1))
                allRandomPlants.add(result.get(randomPlantIndexForButton2))
                allRandomPlants.add(result.get(randomPlantIndexForButton3))
                allRandomPlants.add(result.get(randomPlantIndexForButton4))

                button1.text = result.get(randomPlantIndexForButton1).toString()
                button2.text = result.get(randomPlantIndexForButton2).toString()
                button3.text = result.get(randomPlantIndexForButton3).toString()
                button4.text = result.get(randomPlantIndexForButton4).toString()

                correctAnswerIndex = (Math.random() * allRandomPlants.size).toInt()
                correctPlant = allRandomPlants.get(correctAnswerIndex)

                val downloadingImageTask =  DownloadingImageTask()
                downloadingImageTask.execute(allRandomPlants.get(correctAnswerIndex).pictureName)
            }


        }

    }

    private fun checkForInternetConnection(): Boolean {
        val connectivityManager: ConnectivityManager = this.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        val isDeviceConnectedToInternet = networkInfo != null && networkInfo.isConnectedOrConnecting

        if (isDeviceConnectedToInternet){
            return true
        } else {
            showNetworkErrorAlert()
            return false
        }

    }

    private fun showNetworkErrorAlert() {
        val alertDialog: AlertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle("Network Error")
        alertDialog.setMessage("Please check for internet connection")

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", {
            dialog:DialogInterface?, which: Int ->

            startActivity(Intent(Settings.ACTION_SETTINGS))
        })

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", {
                dialog:DialogInterface?, which: Int ->

            Toast.makeText(this@MainActivity, "You must be connected to the internet", Toast.LENGTH_SHORT).show()
            finish()
        })

        alertDialog.show()
    }

    private fun specifyTheRightAndWrongAnswer(userGuess: Int) {
        if (answerChosen) {
            return
        }

        answerChosen = true
        val correctColor = gradientColorForCorrectButton()
        when (correctAnswerIndex) {
            0 -> button1.background = correctColor
            1 -> button2.background = correctColor
            2 -> button3.background = correctColor
            3 -> button4.background = correctColor
        }

        if (userGuess == correctAnswerIndex) {
            txtState.setText("Correct!")
            numberOfTimesUserAnsweredCorrectly++
            txtRightAnswers.setText("$numberOfTimesUserAnsweredCorrectly")
        } else {
            var correctPlantName = correctPlant.toString()
            txtState.setText("Incorrect, answer is: $correctPlantName")
            numberOfTimesUserAnsweredIncorrectly++
            txtWrongAnswers.setText("$numberOfTimesUserAnsweredIncorrectly")
        }
    }

    inner class DownloadingImageTask: AsyncTask<String, Int, Bitmap?>(){
        override fun doInBackground(vararg pictureName: String?): Bitmap? {

            try {
                val downloadingObject = DownloadingObject()
                val plantBitmap: Bitmap? = downloadingObject.downloadPlantPicture(pictureName[0])

                return plantBitmap
            } catch (e: Exception){
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)

            txtState.text = ""
            imgTaken.setImageBitmap(result)
            showProgressBar(false)
            displayUIWidgets(true)
            playAnimationOnView(imgTaken,0, Techniques.Tada)
            playAnimationOnView(button1,0, Techniques.RollIn)
            playAnimationOnView(button2,0, Techniques.RollIn)
            playAnimationOnView(button3,0, Techniques.RollIn)
            playAnimationOnView(button4,0, Techniques.RollIn)
            playAnimationOnView(txtState,0, Techniques.Swing)
            playAnimationOnView(txtRightAnswers,0, Techniques.FlipInX)
            playAnimationOnView(txtWrongAnswers,0, Techniques.Landing)
        }
    }

    private fun showProgressBar(show: Boolean){
        if (show){
            progressBar.visibility = View.VISIBLE
            loadingLayout.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } else {
            progressBar.visibility = View.GONE
            loadingLayout.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun displayUIWidgets(display: Boolean){
        if (display){
            imgTaken.visibility = View.VISIBLE
            button1.visibility = View.VISIBLE
            button2.visibility = View.VISIBLE
            button3.visibility = View.VISIBLE
            button4.visibility = View.VISIBLE
            txtState.visibility = View.VISIBLE
            txtWrongAnswers.visibility = View.VISIBLE
            txtRightAnswers.visibility = View.VISIBLE
            imgWrong.visibility = View.VISIBLE
            imgRight.visibility = View.VISIBLE
        } else {
            imgTaken.visibility = View.INVISIBLE
            button1.visibility = View.INVISIBLE
            button2.visibility = View.INVISIBLE
            button3.visibility = View.INVISIBLE
            button4.visibility = View.INVISIBLE
            txtState.visibility = View.INVISIBLE
            txtWrongAnswers.visibility = View.INVISIBLE
            txtRightAnswers.visibility = View.INVISIBLE
            imgWrong.visibility = View.INVISIBLE
            imgRight.visibility = View.INVISIBLE
        }
    }

    fun dipToFloat(context: Context, dipValue: Float): Float {
        val metrics = context.getResources().getDisplayMetrics()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics)
    }

    private fun playAnimationOnView(view: View?, repeat: Int, technique: Techniques) {
        YoYo.with(technique)
            .duration(700)
            .repeat(repeat)
            .playOn(view)
    }

    private fun colorAllButtons(){

        val startColor: Int = resources.getColor(R.color.colorButtonStartGradient)
        val endColor: Int = resources.getColor(R.color.colorButtonEndGradient)
        val borderColor: Int = resources.getColor(R.color.colorButtonGradientBorder)

        var gradientColors: IntArray = IntArray(2)
        gradientColors.set(0, startColor)
        gradientColors.set(1, endColor)

        var gradientDrawable: GradientDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors)
        gradientDrawable.setStroke(5, borderColor)

        val convertedDp = dipToFloat(applicationContext, 3f)
        gradientDrawable.cornerRadius = convertedDp

        button1.background = gradientDrawable
        button2.background = gradientDrawable
        button3.background = gradientDrawable
        button4.background = gradientDrawable
    }

    private fun gradientColorForCorrectButton(): GradientDrawable {
        val startColor: Int = resources.getColor(R.color.colorCorrectButtonStartGradient)
        val endColor: Int = resources.getColor(R.color.colorCorrectButtonEndGradient)
        val borderColor: Int = resources.getColor(R.color.colorButtonGradientBorder)

        var gradientColors: IntArray = IntArray(2)
        gradientColors.set(0, startColor)
        gradientColors.set(1, endColor)

        var gradientDrawable: GradientDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors)
        gradientDrawable.setStroke(5, borderColor)

        val convertedDp = dipToFloat(applicationContext, 3f)
        gradientDrawable.cornerRadius = convertedDp

        return gradientDrawable;
    }
}
