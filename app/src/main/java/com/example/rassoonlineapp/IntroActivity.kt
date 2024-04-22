package com.example.rassoonlineapp

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.example.rassoonlineapp.Adapter.IntroSliderAdapter
import com.example.rassoonlineapp.Model.IntroSlider

class IntroActivity : AppCompatActivity() {
    private lateinit var indicatorsContainer: LinearLayout

    private val introSliderAdapter = IntroSliderAdapter(listOf(
        IntroSlider("Sunlight",
            "Sunlighr is very goog",
            R.drawable.save_unfilled_large_icon),
        IntroSlider("Sunlight",
            "Sunlighr is very goog",
            R.drawable.profile),
        IntroSlider("Sunlight",
            "Sunlighr is very goog",
            R.drawable.profile_icon)
    ))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        val viewPager2: ViewPager2 = findViewById(R.id.viewPager2)
        viewPager2.adapter = introSliderAdapter

        // Inicialize indicatorsContainer
        indicatorsContainer = findViewById(R.id.indicatorsContainer)


        setupIndicators()
        setCurrentIndicator(0)

        viewPager2.registerOnPageChangeCallback(object :
        ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

    findViewById<Button>(R.id.buttonNext).setOnClickListener {
        if (viewPager2.currentItem + 1 < introSliderAdapter.itemCount){
            viewPager2.currentItem += 1
        }else{
            Intent(applicationContext, SigninActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
        findViewById<TextView>(R.id.textSkipIntro).setOnClickListener {
            Intent(applicationContext, SigninActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(introSliderAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        layoutParams.setMargins(8,0,8,0)
        for (i in indicators.indices){
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this?.layoutParams = layoutParams
            }
            indicatorsContainer.addView(indicators[i])
        }
    }
    
    private fun setCurrentIndicator(index: Int){
        val childCount = indicatorsContainer.childCount
        for(i in 0 until childCount){
            val imageView = indicatorsContainer[i] as ImageView
            if(i == index){
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            }else{
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }
}