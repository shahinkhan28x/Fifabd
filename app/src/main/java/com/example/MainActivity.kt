package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.network.IptvApi
import com.example.data.repository.StreamRepository
import com.example.ui.discovery.DiscoveryScreen
import com.example.ui.discovery.DiscoveryViewModel
import com.example.ui.player.PlayerScreen
import com.example.ui.theme.BDSportsTheme
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Serializable
object DiscoveryRoute

@Serializable
data class PlayerRoute(
    val url: String,
    val title: String,
    val userAgent: String? = null,
    val referrer: String? = null
)

class MainActivity : ComponentActivity() {

    private lateinit var repository: StreamRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Manual Dependency Injection
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(IptvApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(IptvApi::class.java)

        repository = StreamRepository(api)

        setContent {
            BDSportsTheme {
                AppNavigation(repository)
            }
        }
    }
}

@Composable
fun AppNavigation(repository: StreamRepository) {
    val navController = rememberNavController()
    val discoveryViewModel: DiscoveryViewModel = viewModel(
        factory = viewModelFactory { DiscoveryViewModel(repository) }
    )

    NavHost(navController = navController, startDestination = DiscoveryRoute) {
        composable<DiscoveryRoute> {
            DiscoveryScreen(
                viewModel = discoveryViewModel,
                onChannelSelected = { item ->
                    item.stream?.url?.let { url ->
                        navController.navigate(
                            PlayerRoute(
                                url = url,
                                title = item.channel.name,
                                userAgent = item.stream.userAgent,
                                referrer = item.stream.referrer
                            )
                        )
                    }
                }
            )
        }
        composable<PlayerRoute> { backStackEntry ->
            val route: PlayerRoute = backStackEntry.toRoute()
            PlayerScreen(
                url = route.url,
                title = route.title,
                userAgent = route.userAgent,
                referrer = route.referrer,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// Simple factory helper
fun <VM : androidx.lifecycle.ViewModel> viewModelFactory(initializer: () -> VM): androidx.lifecycle.ViewModelProvider.Factory {
    return object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return initializer() as T
        }
    }
}
