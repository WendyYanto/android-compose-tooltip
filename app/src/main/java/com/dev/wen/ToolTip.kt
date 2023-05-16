package com.dev.wen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

private const val TOOLTIP_ANCHOR_OFFSET = 1f
private const val TOOLTIP_MARGIN_IN_DP = 10
private const val TOOLTIP_ELEVATION_IN_DP = 8
private const val TOOLTIP_ANCHOR_WIDTH_IN_DP = 16

private val TriangleShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width / 2f, size.height)
    lineTo(size.width, 0f)
}


/**
 * [ToolTip] is composable component for showing overlay card with anchor pointing at a component
 * It was built on top [Popup] component where [ToolTip] provides built-in logic to
 * auto-position and auto-scale based on anchor positions.
 *
 * @param isAnchorOnTop serves as flag to determine position of anchor.
 * Currently, only TOP and BOTTOM was supported.
 *
 * @param margin is used for spaces before showing overlay card.
 * This is to prevent ToolTip getting attached to edges of screen.
 *
 * @param popupProperties is to be passed to [Popup].
 * By default, [ToolTip] can be dismissed via back button.
 * This was achieved by setting focusable to true.
 *
 * @param anchorContent is where you place your content
 * that will be anchored by [ToolTip].
 */
@Composable
fun ToolTip(
    isAnchorOnTop: Boolean = false,
    margin: Dp = TOOLTIP_MARGIN_IN_DP.dp,
    popupProperties: PopupProperties = PopupProperties(focusable = true),
    toolTipContent: @Composable () -> Unit,
    anchorContent: @Composable () -> Unit
) {
    val density = LocalDensity.current.density

    var visiblePopUp by remember {
        mutableStateOf(false)
    }
    var anchorOffset by remember {
        mutableStateOf(IntOffset(0, 0))
    }
    var anchorSize by remember {
        mutableStateOf(IntSize(0, 0))
    }
    var popupSize by remember {
        mutableStateOf(IntSize(0, 0))
    }
    val marginInPx = with(LocalDensity.current) {
        margin.roundToPx()
    }

    val screenHeight = (LocalConfiguration.current.screenHeightDp * density).roundToInt()
    val screenWidth = (LocalConfiguration.current.screenWidthDp * density).roundToInt()

    val popupPositionY by remember {
        derivedStateOf {
            calculatePopupPositionY(
                anchorOffset = anchorOffset,
                anchorSize = anchorSize,
                popupSize = popupSize,
                margin = marginInPx,
                isAnchorOnTop = isAnchorOnTop,
                screenHeight = screenHeight
            )
        }
    }

    val popupPositionX by remember {
        derivedStateOf {
            calculatePopupPositionX(
                marginInPx = marginInPx,
                anchorSize = anchorSize,
                popupSize = popupSize
            )
        }
    }

    val popupOffset by remember {
        derivedStateOf {
            IntOffset(popupPositionX.position, popupPositionY.position)
        }
    }

    val tipPositionX by remember {
        derivedStateOf {
            calculateTipPositionX(
                anchorOffset = anchorOffset,
                anchorSize = anchorSize,
                popupSize = popupSize,
                popupPositionX = popupPositionX,
                screenWidth = screenWidth
            )
        }
    }

    Box {
        if (visiblePopUp) {
            Popup(
                onDismissRequest = { visiblePopUp = false },
                properties = popupProperties,
                offset = popupOffset
            ) {
                Column(modifier = Modifier
                    .onSizeChanged { popupSize = it }) {
                    if (popupPositionY is PopupPosition.Bottom) {
                        Tip(
                            tipOffset = tipPositionX,
                            inverted = true
                        )
                    }
                    Card(
                        modifier = Modifier
                            .background(color = Color.Transparent)
                            .padding(horizontal = margin),
                        elevation = TOOLTIP_ELEVATION_IN_DP.dp
                    ) {
                        toolTipContent()
                    }
                    if (popupPositionY is PopupPosition.Top) {
                        Tip(tipPositionX)
                    }
                }
            }
        }

        Box(modifier = Modifier
            .align(Alignment.TopStart)
            .onGloballyPositioned {
                anchorOffset =
                    IntOffset(it.positionInWindow().x.toInt(), it.positionInWindow().y.toInt())
            }
            .onSizeChanged { anchorSize = it }
            .clickable { visiblePopUp = true }) {

            anchorContent()
        }
    }
}


private fun calculatePopupPositionY(
    anchorOffset: IntOffset,
    anchorSize: IntSize,
    popupSize: IntSize,
    margin: Int,
    isAnchorOnTop: Boolean,
    screenHeight: Int
): PopupPosition {
    // anchorTopPosition value when anchor is placed on top
    val anchorTopPosition = anchorOffset.y - anchorSize.height - popupSize.height - margin
    val anchorBottomPosition = anchorOffset.y + anchorSize.height + margin

    val popupMostBottomPosition = anchorBottomPosition + popupSize.height

    val popupToBottomOffset = anchorSize.height + margin
    val popupToTopOffset = -(popupSize.height + margin)

    return if (isAnchorOnTop) {
        if (anchorTopPosition < 0) {
            PopupPosition.Bottom(popupToBottomOffset)
        } else {
            PopupPosition.Top(popupToTopOffset)
        }
    } else {
        if (popupMostBottomPosition > screenHeight) {
            PopupPosition.Top(popupToTopOffset)
        } else {
            PopupPosition.Bottom(popupToBottomOffset)
        }
    }
}

private fun calculatePopupPositionX(
    marginInPx: Int,
    anchorSize: IntSize,
    popupSize: IntSize,
): PopupPosition {
    val purePopupSizeWidth = (popupSize.width - 2 * marginInPx)
    val widthDiff = (anchorSize.width - purePopupSizeWidth) / 2

    return PopupPosition.Left(widthDiff - marginInPx)
}

private fun calculateTipPositionX(
    anchorOffset: IntOffset,
    anchorSize: IntSize,
    popupSize: IntSize,
    popupPositionX: PopupPosition,
    screenWidth: Int
): Int {
    val popupRightPosition = anchorOffset.x + popupPositionX.position + popupSize.width
    // if popup right position exceeds screenWidth then popupRightOffset will become negative
    // popupLeftPosition will be moved to the left by this popupRightOffset
    val popupLeftOffset = minOf(screenWidth - popupRightPosition, 0)
    val popupLeftPosition = anchorOffset.x + popupPositionX.position + popupLeftOffset

    val diff = maxOf(anchorOffset.x - maxOf(0, popupLeftPosition), 0)
    return diff + anchorSize.width / 2 - maxOf(popupPositionX.position, 0)
}

@Composable
private fun Tip(
    tipOffset: Int,
    inverted: Boolean = false,
    width: Dp = TOOLTIP_ANCHOR_WIDTH_IN_DP.dp
) {
    val widthOffset = with(LocalDensity.current) {
        (width / 2).roundToPx()
    }

    val height = width / 2

    Box(modifier = Modifier
        .zIndex(1f)
        .graphicsLayer {
            translationX = (tipOffset - widthOffset).toFloat()
        }) {

        TipShape(
            modifier = Modifier.align(Alignment.TopStart),
            height = height,
            width = width,
            inverted = inverted
        )

        TipShadowCover(
            modifier = Modifier.align(
                if (inverted) {
                    Alignment.BottomCenter
                } else {
                    Alignment.TopCenter
                }
            ),
            height = height,
            width = width,
            inverted = inverted
        )
    }
}

@Composable
private fun TipShape(
    modifier: Modifier = Modifier,
    height: Dp,
    width: Dp,
    inverted: Boolean
) {
    Canvas(
        modifier = modifier
            .height(height)
            .width(width)
            .graphicsLayer {
                shape = TriangleShape
                translationY = if (inverted) {
                    TOOLTIP_ANCHOR_OFFSET
                } else {
                    -TOOLTIP_ANCHOR_OFFSET
                }
                if (inverted) {
                    rotationX = 180f
                }
                shadowElevation = 8f
            }
    ) {
        val path = Path()
        path.moveTo(0f, 0f)
        path.lineTo(size.width / 2f, size.height)
        path.lineTo(size.width, 0f)

        path.close()

        drawPath(path, Color.White)
    }
}

@Composable
private fun TipShadowCover(
    modifier: Modifier = Modifier,
    height: Dp,
    width: Dp,
    inverted: Boolean
) {
    val heightInPx = with(LocalDensity.current) {
        height.roundToPx()
    }

    Canvas(
        modifier = modifier
            .height(height)
            .width(width)
            .graphicsLayer {
                translationY = if (inverted) {
                    heightInPx + TOOLTIP_ANCHOR_OFFSET
                } else {
                    -(heightInPx + TOOLTIP_ANCHOR_OFFSET)
                }
            }
    ) {
        drawRect(Color.White)
    }
}

internal sealed class PopupPosition {

    abstract val position: Int

    data class Bottom(override val position: Int) : PopupPosition()
    data class Top(override val position: Int) : PopupPosition()
    data class Left(override val position: Int) : PopupPosition()
}

@Composable
fun ToolTipContent() {
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(16.dp),
    ) {
        Text(text = "AAAAAAAAAAAAAAAAAA")
    }
}