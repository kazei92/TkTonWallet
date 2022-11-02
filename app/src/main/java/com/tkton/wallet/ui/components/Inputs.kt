package com.tkton.wallet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.PopupProperties
import com.tkton.wallet.ui.theme.Shapes
import com.tkton.wallet.ui.theme.*

@Composable
fun TkActionButton(
    text: String,
    onClick: () -> Unit,
    showProgress : Boolean = false
) {
    Button(
        onClick = { onClick() },
        shape = Shapes.medium,
        modifier = Modifier.requiredSizeIn(
            minWidth = buttonMinWidth,
            minHeight = buttonMinHeight,
            maxHeight = buttonMaxHeight,
            maxWidth = buttonMaxWidth
        ),
    )
    {
        if (showProgress) {
            CircularProgressIndicator(color = MaterialTheme.colors.background, modifier = Modifier.size(circularSize))
        } else {
            Text(text, style = MaterialTheme.typography.button, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun TkInput(value: String,
            onValueChange: (String) -> Unit = {},
            placeholder: String = "",
            isNumberInput : Boolean = false
){
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.body1) },
        shape = Shapes.small,
        modifier = Modifier.widthIn(min=textInputWidth, max=textInputWidth),
        keyboardOptions = if (isNumberInput) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions(),
    )
}

@Composable
fun TkSearchableInput(value: String,
                      onValueChange: (String) -> Unit = {},
                      placeholder: String = "",
                      data: Array<String>
){
    var filtered : List<String> = emptyList()
    var textValue by remember { mutableStateOf(TextFieldValue("")) }
    var expanded by remember { mutableStateOf(false) }

    if (value.length > 2) {
        filtered = data.filter{ item -> item.startsWith(value) }
        if (filtered.isNotEmpty()) {
            expanded = !(filtered.first() == value)
        }

    }

    Column {
        TextField(
            value = textValue,
            onValueChange = {
                textValue = it
                onValueChange(textValue.text)
            },
            placeholder = { Text(placeholder, style = MaterialTheme.typography.body1) },
            shape = Shapes.small,
            modifier = Modifier.widthIn(min=textInputWidth, max=textInputWidth),
        )
        DropdownMenu(
            expanded = expanded,
            properties = PopupProperties(focusable = false),
            onDismissRequest = {}
        )
        {
            for (item in filtered) {
                DropdownMenuItem(onClick = {
                    textValue = TextFieldValue(text=item, selection = TextRange(item.length))
                    onValueChange(item)
                }
                ) {
                    Text(item)
                }
            }
        }
    }
}