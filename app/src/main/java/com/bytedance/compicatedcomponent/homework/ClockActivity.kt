package com.bytedance.compicatedcomponent.homework

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import com.bytedance.compicatedcomponent.R
import java.util.*

/**
 *  author : neo
 *  time   : 2021/10/30
 *  desc   :
 */
class ClockActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)

        val clock = findViewById<ClockViewTouchable>(R.id.clock)

        findViewById<Button>(R.id.btn1).setOnClickListener {

            val calendar: Calendar = Calendar.getInstance()
            val now: Date = calendar.time
            clock.nowHours = now.hours
            clock.nowMinutes = now.minutes
            clock.nowSeconds = now.seconds
            clock.invalidate()
        }


        findViewById<Button>(R.id.btn2).setOnClickListener {
            if(clock.nowHours >= 13){
                clock.nowHours-=12
            }
            else{
                clock.nowHours+=12
            }
            clock.invalidate()
        }
    }


}