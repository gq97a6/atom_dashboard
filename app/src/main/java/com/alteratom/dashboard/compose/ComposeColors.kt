package com.alteratom.dashboard.compose

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import com.alteratom.dashboard.Theme.Companion.colors

@Composable
fun EditTextColors() = TextFieldDefaults.outlinedTextFieldColors(
    textColor = colors.b,
    cursorColor = colors.b,
    focusedBorderColor = colors.a,
    focusedLabelColor = colors.a,
    unfocusedBorderColor = colors.b,
    unfocusedLabelColor = colors.b,
    disabledTextColor = colors.b,
    disabledBorderColor = colors.b,
    disabledLabelColor = colors.b,
    leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
    trailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
)

@Composable
fun SwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = colors.a,
    checkedTrackColor = colors.b,
    uncheckedThumbColor = colors.b,
    uncheckedTrackColor = colors.c,
)

@Composable
fun RadioButtonColors() = RadioButtonDefaults.colors(
    selectedColor = colors.a,
    unselectedColor = colors.c,
)

@Composable
fun CheckBoxColors() = CheckboxDefaults.colors(
    checkedColor = colors.b,
    uncheckedColor = colors.a,
    checkmarkColor = colors.background
)