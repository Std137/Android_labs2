package com.velestudio.labs2

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.velestudio.labs2.ui.theme.Labs2Theme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Labs2Theme {
                Body()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Body(context: Context = localContextProvider()) {
    var pressure by remember { mutableStateOf<Float?>(null) }
    var stateWeather by remember { mutableStateOf(false) }
    val sliderState = rememberSliderState (
        value = 750f,
        valueRange = 700f..800f)
    val lifecycleOwner = LocalLifecycleOwner.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    DisposableEffect(lifecycleOwner) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                pressure = event.values[0]
                pressure = pressure!! * 0.750062f
                stateWeather = (pressure!! > sliderState.value)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (pressureSensor != null) {
            sensorManager.registerListener(
                listener,
                pressureSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    )
    { innerPadding ->
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
            ){
                if (pressure != null) {
                    LineHeader()
                    WhetherImage(stateWeather)
                    LineForecast(stateWeather)
                    Slider(sliderState)
                    LinePressure(pressure!!)
                }
                else LineSensorError()
            }
        }
    }
}

@Composable
fun LineHeader() {
    Text(
        text = "Погода на ближайшее время",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    )
}

@Composable
fun LineSensorError() {
    Text(
        text = "Датчик давления не доступен",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    )
}

@Composable
fun WhetherImage(caseImage: Boolean) {
    if (caseImage)
        Image(
            painter =  painterResource(id = R.drawable.rain),
            contentDescription = ""
        )
    else
        Image(
            painter =  painterResource(id = R.drawable.sun),
            contentDescription = ""
    )
}


@Composable
fun LineForecast(caseMsg: Boolean) {
    Text(
        text = if (caseMsg)
            stringResource(id = R.string.rain_msg)
        else
            stringResource(id = R.string.sun_msg),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Slider(sliderState: SliderState) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(modifier = Modifier.padding(horizontal = 16.dp)){
        Text(text = "%.2f".format(sliderState.value))
        Slider(
            state = sliderState,
            interactionSource = interactionSource,
            thumb = { SliderDefaults.Thumb(interactionSource = interactionSource) },
            track = { SliderDefaults.Track(sliderState = sliderState)}
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun LinePressure(mmHg: Float) {
    Text(
        text = String.format("Текущее давление: \n %.1f mmHg", mmHg),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    )
}

@Composable
fun localContextProvider(): Context {
    return androidx.compose.ui.platform.LocalContext.current
}