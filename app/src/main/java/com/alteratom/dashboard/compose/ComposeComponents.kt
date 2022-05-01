package com.alteratom.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.CheckBoxColors
import com.alteratom.dashboard.compose.EditTextColors
import com.alteratom.dashboard.compose.RadioButtonColors
import com.alteratom.dashboard.compose.SwitchColors

@Composable
fun BoldStartText(a: String, b: String, fontSize: TextUnit = 15.sp, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(text = a, fontWeight = FontWeight.Bold, fontSize = fontSize, color = colors.a)
        Text(text = b, fontSize = fontSize, color = colors.b)
    }
}

@Composable
fun LabeledSwitch(
    label: @Composable (() -> Unit)? = null,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var checkedState by remember { mutableStateOf(checked) }

    Row(
        modifier = modifier.wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        label?.invoke()
        Text("OFF", fontSize = 12.sp, color = colors.b, modifier = Modifier.padding(start = 8.dp))
        Switch(
            checked = checkedState,
            onCheckedChange = {
                checkedState = it
                onCheckedChange?.invoke(checked)
            },
            enabled = enabled,
            colors = SwitchColors(),
            interactionSource = interactionSource,
            modifier = Modifier
                .wrapContentSize()
                .padding(0.dp)
        )
        Text("ON", fontSize = 12.sp, color = colors.b)
    }
}

@Composable
fun EditText(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small
) {
    var valueState by remember { mutableStateOf(value) }
    OutlinedTextField(
        enabled = enabled,
        readOnly = readOnly,
        value = valueState,
        onValueChange = {
            valueState = it
            onValueChange(it)
        },
        modifier = modifier
            .height(60.dp)
            .fillMaxWidth(),
        singleLine = singleLine,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = maxLines ?: 1,
        interactionSource = interactionSource,
        shape = shape,
        colors = EditTextColors()
    )
}

@Composable
fun RadioGroup(
    options: List<String> = listOf(),
    label: String = "",
    selected: Int,
    onClick: ((Int) -> Unit)?,
    modifier: Modifier = Modifier
) {
    var selectedState by remember { mutableStateOf(selected) }
    Column(modifier = modifier) {
        Text(text = label, fontSize = 15.sp, color = colors.color)
        options.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 15.dp)
            ) {
                RadioButton(
                    selected = index == selectedState,
                    onClick = {
                        selectedState = index
                        onClick?.invoke(index)
                    },
                    modifier = Modifier
                        .width(20.dp)
                        .height(25.dp),
                    colors = RadioButtonColors()
                )

                val annotatedText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = colors.color, fontSize = 15.sp))
                    { append(item) }
                }

                ClickableText(
                    text = annotatedText,
                    onClick = { selectedState = index },
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
    }
}

inline fun Modifier.nrClickable(crossinline onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

@Composable
fun LabeledCheckbox(
    label: @Composable (() -> Unit),
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var checkedState by remember { mutableStateOf(checked) }
    Row(
        modifier = modifier
            .nrClickable(onClick = { checkedState = !checkedState })
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            modifier = Modifier
                .width(20.dp)
                .height(20.dp),
            checked = checkedState,
            enabled = enabled,
            interactionSource = interactionSource,
            colors = CheckBoxColors(),
            onCheckedChange = {
                onCheckedChange(it)
                checkedState = it
            }
        )
        Spacer(modifier = Modifier.padding(5.dp))
        label()
    }
}