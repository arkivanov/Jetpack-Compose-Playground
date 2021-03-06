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

@file:Suppress("unused")

package androidx.ui.core.samples


import androidx.compose.Composable
import androidx.ui.background
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text

import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.padding
import androidx.ui.material.Button
import androidx.ui.unit.dp


@Composable
fun ModifierUsageSample() {
    Text(
        "Hello, World!",
        Modifier.padding(16.dp) // Outer padding; outside background
            .background(color = Color.Green) // Solid element background color
            .padding(16.dp) // Inner padding; inside background, around text
    )
}


@Composable
fun ModifierFactorySample() {
    class FancyModifier(val level: Float) : Modifier.Element

    fun Modifier.fancy(level: Float) = this + FancyModifier(level)

    Row(Modifier.fancy(1f).padding(10.dp)) {
        // content
    }
}


@Composable
fun ModifierParameterSample() {
    @Composable
    fun PaddedColumn(modifier: Modifier = Modifier) {
        Column(modifier.padding(10.dp)) {
            // ...
        }
    }
}


@Composable
fun SubcomponentModifierSample() {
    @Composable
    fun ButtonBar(
        onOk: () -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier = Modifier,
        buttonModifier: Modifier = Modifier
    ) {
        Row(modifier) {
            Button(onCancel, buttonModifier) {
                Text("Cancel")
            }
            Button(onOk, buttonModifier) {
                Text("Ok")
            }
        }
    }
}
