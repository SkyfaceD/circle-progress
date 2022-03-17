package org.skyfaced.circlechart.util

data class Rainbow(
    val animate: Boolean = false,
    val animationRotation: Rotation = Rotation.Clockwise,
    val animationDurationMillis: Int = DefaultDurationMillis,
) {
    companion object {
        const val DefaultDurationMillis = 1500
    }
}