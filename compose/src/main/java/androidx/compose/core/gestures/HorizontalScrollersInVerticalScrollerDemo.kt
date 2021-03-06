/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.ui.core.demos.gestures

import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.background
import androidx.ui.core.Constraints
import androidx.ui.core.ContentDrawScope
import androidx.ui.core.Direction
import androidx.ui.core.DrawModifier
import androidx.ui.core.Layout
import androidx.ui.core.Modifier
import androidx.ui.core.gesture.ScrollCallback
import androidx.ui.core.gesture.doubleTapGestureFilter
import androidx.ui.core.gesture.longPressGestureFilter
import androidx.ui.core.gesture.pressIndicatorGestureFilter
import androidx.ui.core.gesture.scrollGestureFilter
import androidx.ui.core.gesture.scrollorientationlocking.Orientation
import androidx.ui.core.gesture.tapGestureFilter
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.foundation.drawBorder

import androidx.ui.geometry.Offset
import androidx.ui.graphics.Color
import androidx.ui.graphics.drawscope.clipRect
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.preferredSize

import androidx.ui.unit.Dp
import androidx.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Demonstration for how multiple DragGestureDetectors interact.
 */
@Composable
fun HorizontalScrollersInVerticalScrollersDemo() {
    Column {
        Text("Demonstrates scroll orientation locking.")
        Text(
            "There is a column of rows, all of which are scrollable. Any one pointer can only " +
                    "contribute to dragging in one orientation at a time."
        )
        Scrollable(Orientation.Vertical) {
            RepeatingColumn(repetitions = 10) {
                Box(paddingTop = 8.dp, paddingBottom = 8.dp) {
                    // Inner composable that scrolls
                    Scrollable(Orientation.Horizontal) {
                        RepeatingRow(repetitions = 10) {
                            // Composable that indicates it is being pressed
                            Pressable(
                                width = 96.dp,
                                height = 96.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A very simple ScrollView like implementation that allows for vertical scrolling.
 */
@Composable
private fun Scrollable(orientation: Orientation, children: @Composable () -> Unit) {
    val maxOffset = 0f
    val offset = state { maxOffset }
    val minOffset = state { 0f }

    val scrollObserver = object : ScrollCallback {
        override fun onScroll(scrollDistance: Float): Float {
            val resultingOffset = offset.value + scrollDistance
            val toConsume =
                when {
                    resultingOffset > maxOffset -> {
                        maxOffset - offset.value
                    }
                    resultingOffset < minOffset.value -> {
                        minOffset.value - offset.value
                    }
                    else -> {
                        scrollDistance
                    }
                }
            offset.value = offset.value + toConsume
            return toConsume
        }
    }

    val canDrag: (Direction) -> Boolean = { direction ->
        when {
            direction == Direction.LEFT && offset.value > minOffset.value -> true
            direction == Direction.UP && offset.value > minOffset.value -> true
            direction == Direction.RIGHT && offset.value < maxOffset -> true
            direction == Direction.DOWN && offset.value < maxOffset -> true
            else -> false
        }
    }

    Layout(
        children = children,
        modifier = Modifier.scrollGestureFilter(scrollObserver, orientation, canDrag) +
                ClipModifier,
        measureBlock = { measurables, constraints ->
            val placeable =
                when (orientation) {
                    Orientation.Horizontal -> measurables.first().measure(
                        constraints.copy(
                            minWidth = 0,
                            maxWidth = Constraints.Infinity
                        )
                    )
                    Orientation.Vertical -> measurables.first().measure(
                        constraints.copy(
                            minHeight = 0,
                            maxHeight = Constraints.Infinity
                        )
                    )
                }

            minOffset.value =
                when (orientation) {
                    Orientation.Horizontal -> constraints.maxWidth.toFloat() - placeable.width
                    Orientation.Vertical -> constraints.maxHeight.toFloat() - placeable.height
                }

            val width =
                when (orientation) {
                    Orientation.Horizontal -> constraints.maxWidth
                    Orientation.Vertical -> placeable.width
                }

            val height =
                when (orientation) {
                    Orientation.Horizontal -> placeable.height
                    Orientation.Vertical -> constraints.maxHeight
                }

            layout(width, height) {
                when (orientation) {
                    Orientation.Horizontal -> placeable.place(offset.value.roundToInt(), 0)
                    Orientation.Vertical -> placeable.place(0, offset.value.roundToInt())
                }
            }
        })
}

private val ClipModifier = object : DrawModifier {
    override fun ContentDrawScope.draw() {
        clipRect {
            this@draw.drawContent()
        }
    }
}

/**
 * A very simple Button like implementation that visually indicates when it is being pressed.
 */
@Composable
private fun Pressable(
    width: Dp,
    height: Dp
) {

    val pressedColor = PressedColor
    val defaultColor = Red

    val color = state { defaultColor }
    val showPressed = state { false }

    val onPress: (Offset) -> Unit = {
        showPressed.value = true
    }

    val onRelease = {
        showPressed.value = false
    }

    val onTap: (Offset) -> Unit = {
        color.value = color.value.next()
    }

    val onDoubleTap: (Offset) -> Unit = {
        color.value = color.value.prev()
    }

    val onLongPress = { _: Offset ->
        color.value = defaultColor
        showPressed.value = false
    }

    val gestureDetectors =
        Modifier
            .pressIndicatorGestureFilter(onPress, onRelease, onRelease)
            .tapGestureFilter(onTap)
            .doubleTapGestureFilter(onDoubleTap)
            .longPressGestureFilter(onLongPress)

    val pressOverlay =
        if (showPressed.value) Modifier.background(color = pressedColor) else Modifier

    Box(
        gestureDetectors
            .preferredSize(width, height)
            .drawBorder(1.dp, Color.Black)
            .background(color = color.value)
            .plus(pressOverlay)
    )
}

/**
 * A simple composable that repeats its children as a vertical list of divided items [repetitions]
 * times.
 */
@Suppress("SameParameterValue")
@Composable
private fun RepeatingColumn(repetitions: Int, children: @Composable () -> Unit) {
    Column {
        for (i in 1..repetitions) {
            children()
        }
    }
}

/**
 * A simple composable that repeats its children as a vertical list of divided items [repetitions]
 * times.
 */
@Suppress("SameParameterValue")
@Composable
private fun RepeatingRow(repetitions: Int, children: @Composable () -> Unit) {
    Row {
        for (i in 1..repetitions) {
            children()
        }
    }
}