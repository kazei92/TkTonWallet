package com.tkton.wallet.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tkton.wallet.ui.theme.Grey500
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.tkton.wallet.ui.theme.*

@Composable
fun ClickableCard(onClick: () -> Unit = {},
                  content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .padding(standardPadding)
            .fillMaxWidth()
            .height(standartCardHeight),
        elevation = standardElevation
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.background,
            ),
            onClick = { onClick() },
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }

}


@Composable
fun SettingsCard(settingName: String,
                 settingValue: String,
                 onClick: () -> Unit = {},
                 content: @Composable () -> Unit = {}
) {
    ClickableCard(onClick = onClick, content = {
        content()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = settingName,
                style = MaterialTheme.typography.subtitle1,
            )
            Text(
                text = settingValue,
                style = MaterialTheme.typography.body1
            )
        }
    })
}

@Composable
fun TkDivider(modifier: Modifier = Modifier) {
    Divider(color = MaterialTheme.colors.background, thickness = 1.dp, modifier = modifier)
}

@Composable
fun TkCardItem(
    message: String,
    icon: @Composable () -> Unit,
    onIconClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(bigPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SelectionContainer() {
            Text(
                style = MaterialTheme.typography.body2,
                text = message,
            )
        }
        IconButton(
            onClick = { onIconClick() },
        ) {
            icon()
        }
    }
}



@Composable
fun BaseRow(
    modifier: Modifier = Modifier,
    color: Color,
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.height(68.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val typography = MaterialTheme.typography
        ColorBar(
            color = color,
            modifier = Modifier
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier) {
            Text(text = title, style = typography.body1)
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(text = subtitle, style = typography.subtitle1)
            }
        }
        Spacer(Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            icon()
        }
        Spacer(Modifier.width(16.dp))

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            IconButton(onClick = { onClick() }) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(24.dp)
                )
            }
        }
    }
    TkDivider()
}

@Composable
fun ColorBar(color: Color, modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .size(4.dp, 36.dp)
            .background(color = color)
    )
}

@Composable
fun DetailsRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    var editable by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            editable = true
            onClick()
            scope.launch {
                delay(1000)
                editable = false
            }
                  },
    ) {
        AnimatedVisibility(
            visible = editable,
            modifier = Modifier.zIndex(1f)
        ) {
            Text(text = "copied ")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ColorBar(
                color = Grey500,
                modifier = Modifier
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier) {
                Text(text = title, style = MaterialTheme.typography.body1)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(text = subtitle, style = MaterialTheme.typography.subtitle1)
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
}