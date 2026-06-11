package com.example.data.repository

import com.example.data.model.ChannelItem
import com.example.data.network.IptvApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
class StreamRepository(private val api: IptvApi) {

    private var cachedItems: List<ChannelItem> = emptyList()

    suspend fun getDiscoveryItems(): List<ChannelItem> = withContext(Dispatchers.IO) {
        if (cachedItems.isNotEmpty()) return@withContext cachedItems

        try {
            val channelsDeferred = async { api.getChannels() }
            val streamsDeferred = async { api.getStreams() }
            val logosDeferred = async { api.getLogos() }
            val categoriesDeferred = async { api.getCategories() }
            val countriesDeferred = async { api.getCountries() }

            val channels = channelsDeferred.await().filter { !it.isNsfw }
            val streams = streamsDeferred.await()
                .filter { it.channel != null && it.url != null }
                .groupBy { it.channel!! }
            val logos = logosDeferred.await()
                .filter { it.channel != null && it.url != null }
                .associateBy { it.channel!! }
            val categories = categoriesDeferred.await()
                .filter { it.id != null && it.name != null }
                .associateBy { it.id!! }
            val countries = countriesDeferred.await()
                .filter { it.code != null && it.name != null }
                .associateBy { it.code!! }

            val joined = channels.mapNotNull { channel ->
                val channelStreams = streams[channel.id] ?: return@mapNotNull null
                // Prefer higher quality streams
                val bestStream = channelStreams.maxByOrNull { 
                    when (it.quality) {
                        "2160p" -> 100
                        "1080p" -> 50
                        "720p" -> 20
                        else -> 10
                    }
                }

                ChannelItem(
                    channel = channel,
                    stream = bestStream,
                    logo = logos[channel.id],
                    categories = channel.categories?.mapNotNull { categories[it] } ?: emptyList(),
                    country = countries[channel.country ?: ""]
                )
            }
            cachedItems = joined
            joined
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
