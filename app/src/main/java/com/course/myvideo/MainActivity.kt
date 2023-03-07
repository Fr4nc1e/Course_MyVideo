package com.course.myvideo

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.course.myvideo.ui.theme.MyVideoTheme
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import java.io.File

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            permissions(),
            1,
        )
        setContent {
            MyVideoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val player = ExoPlayer.Builder(applicationContext).build()
                    var lifecycle by remember {
                        mutableStateOf(Lifecycle.Event.ON_CREATE)
                    }
                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            lifecycle = event
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)

                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }
                    val webUrl = remember {
                        mutableStateOf("http://www.ee.cityu.edu.hk/~lmpo/ee5415/videoWeb.mp4")
                    }

                    player.prepare()
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(onClick = {
                            val uri = Uri.parse(
                                "android.resource://" +
                                    packageName + "/" + R.raw.video,
                            )
                            player.setMediaItem(MediaItem.fromUri(uri))
                        }) {
                            Text(text = "Play Resource video")
                        }
                        Button(onClick = {
                            val file = File(
                                Environment.getExternalStorageDirectory(),
                                "videoSD.mp4",
                            )
                            val uri = Uri.fromFile(file)
                            player.setMediaItem(MediaItem.fromUri(uri))
                        }) {
                            Text(text = "Play SD CARD audio")
                        }
                        Button(onClick = {
                            val uri = Uri.parse(webUrl.value)
                            player.setMediaItem(MediaItem.fromUri(uri))
                        }) {
                            Text(text = "Play web audio")
                        }
                        TextField(
                            value = webUrl.value,
                            onValueChange = {
                                webUrl.value = it
                            },
                        )

                        AndroidView(
                            factory = { context ->
                                StyledPlayerView(context).also {
                                    it.player = player
                                }
                            },
                            update = {
                                when (lifecycle) {
                                    Lifecycle.Event.ON_PAUSE -> {
                                        it.onPause()
                                        it.player?.pause()
                                    }
                                    Lifecycle.Event.ON_RESUME -> {
                                        it.onResume()
                                    }
                                    else -> Unit
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1920f / 1080f)
                                .animateContentSize(),
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 1
        var storge_permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
        )

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        var storge_permissions_33 = arrayOf(
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_MEDIA_VIDEO,
        )
        fun permissions(): Array<String> {
            val p: Array<String>
            p = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                storge_permissions_33
            } else {
                storge_permissions
            }
            return p
        }
    }
}
