package com.dev.wen

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.dev.wen.ui.theme.ComposeToolTipTheme

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
    Log.v("YANRTO", LocalConfiguration.current.screenHeightDp.toString())
    Column {

        for (count in 1..100) {
            Item(count, name)
        }
    }
}

@Composable
private fun Item(count: Int, name: String) {
    val visiblePopUp = remember {
        mutableStateOf(false)
    }
    val anchorOffset = remember {
        mutableStateOf(IntOffset(0, 0))
    }
    val popupSize = remember {
        mutableStateOf(IntSize(0, 0))
    }
    val popupOffset = derivedStateOf {
        IntOffset(anchorOffset.value.x, anchorOffset.value.y - popupSize.value.height)
    }

    if (visiblePopUp.value) {
        Log.v("YEAD", "visible")
        Popup(offset = popupOffset.value) {
            Box(modifier = Modifier
                .background(color = Color.White)
                .onSizeChanged {
                    popupSize.value = it
                }
                .padding(4.dp)
            ) {
                Text(text = "JAMAMSAMSAMSKMASKMAKSMKAS")
            }
        }
    }

    val width = LocalView.current.measuredWidth.dp
    Text(
        text = "Hello $count : $name!",
        modifier = Modifier
            .onGloballyPositioned {
                Log.v("WENDDD", "position changed : ${it.positionInWindow()}")
                Log.v("WENDDD", "position changed 2 : ${it.positionInRoot()}")

                anchorOffset.value =
                    IntOffset(it.positionInRoot().x.toInt(), it.positionInRoot().y.toInt())
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