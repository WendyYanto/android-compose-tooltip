package com.dev.wen

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.dev.wen.ui.theme.ComposeToolTipTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeToolTipTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Column(
    ) {
        for (count in 1..100) {
            Item(count, name)
        }
    }
}

@Composable
private fun ToolTipAnchor(inverted: Boolean = false) {
    Canvas(
        modifier = Modifier
            .height(8.dp)
            .width(
                16.dp
            )
            .graphicsLayer {
                if (inverted) {
                    rotationX = 180f
                }
            }
    ) {
        val path = Path()
        path.moveTo(0f, 0f)
        path.lineTo(size.width / 2f, size.height)
        path.lineTo(size.width, 0f)
        path.close()

        drawPath(path, Color.Black)
    }
}

internal sealed class AnchorPosition {
    abstract val positionY: Int

    data class Bottom(override val positionY: Int) : AnchorPosition()
    data class Top(override val positionY: Int) : AnchorPosition()
}

@Composable
private fun Item(count: Int, name: String, isTopTooltip: Boolean = false) {
    val visiblePopUp = remember {
        mutableStateOf(false)
    }
    val anchorOffset = remember {
        mutableStateOf(IntOffset(0, 0))
    }
    val anchorSize = remember {
        mutableStateOf(IntSize(0, 0))
    }
    val popupSize = remember {
        mutableStateOf(IntSize(0, 0))
    }
    val density = LocalDensity.current.density
    val screenHeight = (LocalConfiguration.current.screenHeightDp * density).roundToInt()

    val popupPositionY = derivedStateOf {
        val onTopCoordsY = anchorOffset.value.y - popupSize.value.height
        val onDownCoordsY = anchorOffset.value.y + anchorSize.value.height

        val coordsY = if (isTopTooltip) {
            if (onTopCoordsY < 0) {
                AnchorPosition.Bottom(onDownCoordsY)
            } else {
                AnchorPosition.Top(onTopCoordsY)
            }
        } else {
            if (onDownCoordsY + popupSize.value.height > screenHeight) {
                AnchorPosition.Top(onTopCoordsY)
            } else {
                AnchorPosition.Bottom(onDownCoordsY)
            }
        }

        coordsY
    }

    val popupOffset = derivedStateOf {
        IntOffset(anchorOffset.value.x, popupPositionY.value.positionY)
    }

    if (visiblePopUp.value) {
        Popup(
            onDismissRequest = { visiblePopUp.value = false },
            offset = popupOffset.value
        ) {
            Column(modifier = Modifier
                .onSizeChanged {
                    popupSize.value = it
                }) {
                if (popupPositionY.value is AnchorPosition.Bottom) {
                    ToolTipAnchor(inverted = true)
                }
                Card(
                    modifier = Modifier
                        .background(color = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "JAMAMSAMSAMSKMASKMAKSMKAS")
                        Text(text = "JAMAMSAMSAMSKMASKMAKSMKAS")
                        Text(text = "JAMAMSAMSAMSKMASKMAKSMKAS")
                        Text(text = "JAMAMSAMSAMSKMASKMAKSMKAS")
                        Text(text = "JAMAMSAMSAMSKMASKMAKSMKAS")
                        Text(text = "JAMAMSAMSAMSKMASKMAKSMKAS")
                    }
                }
                if (popupPositionY.value is AnchorPosition.Top) {
                    ToolTipAnchor()
                }
            }

        }
    }

    Text(
        text = "Hello $count : $name!",
        modifier = Modifier
            .onGloballyPositioned {
                Log.v("WENDDD", "position changed : ${it.positionInWindow()}")
                Log.v("WENDDD", "position changed 2 : ${it.positionInRoot()}")
                anchorOffset.value =
                    IntOffset(it.positionInRoot().x.toInt(), it.positionInRoot().y.toInt())
            }
            .onSizeChanged {
                anchorSize.value = it
            }
            .clickable {
                visiblePopUp.value = true
            }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeToolTipTheme {
        Greeting("Android")
    }
}