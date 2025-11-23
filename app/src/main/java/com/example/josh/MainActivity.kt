package com.example.josh

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.josh.network.ApiClient
import com.example.josh.network.ProductsApi
import com.example.josh.network.ProductsRepository
import com.example.josh.recorder.AudioRecorder
import com.example.josh.data.TaskRepository
import com.example.josh.ui.AppNavHost
import retrofit2.create

class MainActivity : ComponentActivity() {
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { perms ->
        // no-op; UI will request explicitly where needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA))

        // create api service
        val api = ApiClient.retrofit.create(ProductsApi::class.java)
        val productsRepo = ProductsRepository(api)

        setContent {
            val context = LocalContext.current
            val repo = remember { TaskRepository(context) }
            val recorder = remember { AudioRecorder(context) }

            Surface(color = MaterialTheme.colorScheme.background) {
                AppNavHost(repo = repo, recorder = recorder, productsRepository = productsRepo)
            }
        }
    }
}
