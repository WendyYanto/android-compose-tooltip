package com.dev.wen

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
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

                BackHandler() {

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (count in 1..100) {
            Item(count, name)
        }
    }
}

private val TriangleShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width / 2f, size.height)
    lineTo(size.width, 0f)
}

@Composable
private fun ToolTipAnchor(anchorWidth: Int, inverted: Boolean = false) {
    val offset = with(LocalDensity.current) {
        4.dp.roundToPx()
    }
    Box(modifier = Modifier
        .zIndex(1f)
        .graphicsLayer {
            translationX = (anchorWidth - offset).toFloat()

        }) {
        Canvas(
            modifier = Modifier
                .height(8.dp)
                .width(16.dp)
                .align(Alignment.TopStart)
                .graphicsLayer {
                    shape = TriangleShape
                    translationY = if (inverted) {
                        1f
                    } else {
                        -1f
                    }
                    if (inverted) {
                        rotationX = 180f
                    }
                    shadowElevation = 10f
                }
        ) {
            val path = Path()
            path.moveTo(0f, 0f)
            path.lineTo(size.width / 2f, size.height)
            path.lineTo(size.width, 0f)

            path.close()

            drawPath(path, Color.White)
        }

        Canvas(
            modifier = Modifier
                .height(8.dp)
                .width(16.dp)
                .align(if (inverted) {
                    Alignment.BottomCenter
                } else {
                    Alignment.TopCenter
                })
                .graphicsLayer {
                    translationY = if (inverted) {
                        21f
                    } else {
                        -21f
                    }
                }
        ) {
            drawRect(Color.White)
        }
    }
}

internal sealed class AnchorPosition {

    abstract val position: Int

    data class Bottom(override val position: Int) : AnchorPosition()
    data class Top(override val position: Int) : AnchorPosition()
    data class Left(override val position: Int) : AnchorPosition()
}

@Composable
private fun Item(count: Int, name: String, isTopTooltip: Boolean = false) {
    val density = LocalDensity.current.density

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
    val offset = with(LocalDensity.current) {
        10.dp.roundToPx()
    }
    val screenHeight = (LocalConfiguration.current.screenHeightDp * density).roundToInt()
    val screenWidth = (LocalConfiguration.current.screenWidthDp * density).roundToInt()

    val popupPositionY = derivedStateOf {
        val onTopCoordinate = anchorOffset.value.y - popupSize.value.height - offset
        val onDownCoordinate = anchorOffset.value.y + anchorSize.value.height + offset

        val coordinate = if (isTopTooltip) {
            if (onTopCoordinate < 0) {
                AnchorPosition.Bottom(onDownCoordinate)
            } else {
                AnchorPosition.Top(onTopCoordinate)
            }
        } else {
            if (onDownCoordinate + popupSize.value.height > screenHeight) {
                AnchorPosition.Top(onTopCoordinate)
            } else {
                AnchorPosition.Bottom(onDownCoordinate)
            }
        }

        coordinate
    }

    val popupPositionX = derivedStateOf {
        val centerPosition =
            (anchorOffset.value.x + (anchorSize.value.width) / 2) - (popupSize.value.width / 2)

        AnchorPosition.Left(maxOf(0, centerPosition))
    }

    val popupOffset = derivedStateOf {
        IntOffset(popupPositionX.value.position, popupPositionY.value.position)
    }

    val anchorCenterX = derivedStateOf {
        val popupDiffOffset =
            minOf(screenWidth - (popupPositionX.value.position + popupSize.value.width), 0)
        val formattedPositionX = popupPositionX.value.position + popupDiffOffset
        val popupMostRightPosition = formattedPositionX + popupSize.value.width + offset

        val anchorPosition = (anchorOffset.value.x + (anchorSize.value.width) / 2)
        val anchorMostRightPosition = anchorOffset.value.x + anchorSize.value.width

        if (anchorMostRightPosition < popupMostRightPosition) {
            anchorPosition - formattedPositionX
        } else {
            (popupSize.value.width - offset) / 2
        }
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
                    ToolTipAnchor(
                        anchorWidth = anchorCenterX.value,
                        inverted = true
                    )
                }
                Card(
                    modifier = Modifier
                        .background(color = Color.Transparent)
                        .padding(horizontal = 10.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.White)
                            .padding(16.dp),
                    ) {
                        Text(text = "JAAJAakskakslaksajslajskjasjkaljskljaskljaskljklasjkajskjalksjklajsklajskljksajskajslkjl")
                    }
                }
                if (popupPositionY.value is AnchorPosition.Top) {
                    ToolTipAnchor(anchorCenterX.value)
                }
            }
        }
    }

    Text(
        text = "Hello $count : $name!",
        modifier = Modifier
            .onGloballyPositioned {
                Log.v(
                    "HI",
                    it
                        .positionInRoot()
                        .toString()
                )
                anchorOffset.value =
                    IntOffset(it.positionInRoot().x.toInt(), it.positionInRoot().y.toInt())
            }
            .onSizeChanged {
                Log.v("LENGTH", it.toString())
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