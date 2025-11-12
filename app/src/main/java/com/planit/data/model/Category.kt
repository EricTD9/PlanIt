package com.planit.data.model

enum class Category(val displayName: String, val colorRes: Int) {
    WORK("Trabajo", android.R.color.holo_red_dark),
    SCHOOL("Escuela", android.R.color.holo_blue_dark),
    PERSONAL("Personal", android.R.color.holo_green_dark)
}

