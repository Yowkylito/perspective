package com.perspective.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Everything is square.
 *
 * The wordmark is a thin rectangular rule with hard corners — that single
 * decision sets the whole shape language. Rounded corners would put the UI and
 * the logo in different voices, and a pill button in particular is the shape
 * most associated with the sportswear apps this is meant not to resemble.
 *
 * Buttons, cards, sheets, fields: all 0dp. If something needs to feel softer,
 * give it more space around it, not a radius.
 */
private val Square = RoundedCornerShape(0.dp)

val PerspectiveShapes = Shapes(
    extraSmall = Square,
    small = Square,
    medium = Square,
    large = Square,
    extraLarge = Square,
)
