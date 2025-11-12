package com.planit

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.planit.alarm.ReminderAlarmManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setupWithNavController(navController)

        // Manejar navegación al crear nuevo recordatorio
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add -> {
                    navController.navigate(R.id.nav_create)
                    true
                }
                R.id.nav_today -> {
                    navController.navigate(R.id.nav_today)
                    true
                }
                R.id.nav_calendar -> {
                    navController.navigate(R.id.nav_calendar)
                    true
                }
                R.id.nav_focus -> {
                    navController.navigate(R.id.nav_focus)
                    true
                }
                else -> false
            }
        }

        // Manejar recordatorio desde notificación
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        if (reminderId != -1L) {
            navController.navigate(
                R.id.nav_reminder_detail,
                Bundle().apply {
                    putLong("reminderId", reminderId)
                }
            )
        }
    }
}

