package com.example.ui.discovery

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Category
import com.example.data.model.ChannelItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel,
    onChannelSelected: (ChannelItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Brush.verticalGradient(listOf(Color(0xFF0A0A0A), Color.Transparent)))) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "STREAMING NOW",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryAccent,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("BD")
                                withStyle(style = androidx.compose.ui.text.SpanStyle(color = PrimaryAccent)) {
                                    append("SPORTS")
                                }
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = (-1).sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                is DiscoveryUiState.Loading -> {
                    LoadingView()
                }
                is DiscoveryUiState.Success -> {
                    DiscoveryContent(
                        state = state,
                        onCategorySelected = viewModel::onCategorySelected,
                        onSearchChanged = viewModel::onSearchQueryChanged,
                        onChannelSelected = onChannelSelected
                    )
                }
                is DiscoveryUiState.Error -> {
                    ErrorView(message = state.message, onRetry = viewModel::loadDiscovery)
                }
            }
        }
    }
}

@Composable
fun DiscoveryContent(
    state: DiscoveryUiState.Success,
    onCategorySelected: (Category?) -> Unit,
    onSearchChanged: (String) -> Unit,
    onChannelSelected: (ChannelItem) -> Unit
) {
    Column {
        SearchBar(state.searchQuery, onSearchChanged)
        
        CategoryFilters(
            categories = state.categories,
            selectedCategory = state.selectedCategory,
            onCategorySelected = onCategorySelected
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero section could be implemented as a double-span item at the top
            val featured = state.items.firstOrNull()
            if (featured != null && state.searchQuery.isEmpty() && state.selectedCategory == null) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    HeroChannelCard(featured, onChannelSelected)
                }
            }

            items(state.items) { item ->
                ChannelCard(item, onChannelSelected)
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Search channels...", color = TextSecondary) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryAccent) },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfaceVariant,
            unfocusedContainerColor = SurfaceVariant,
            focusedIndicatorColor = PrimaryAccent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun CategoryFilters(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All Channels", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(20.dp),
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryAccent,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White.copy(alpha = 0.05f),
                    labelColor = TextSecondary
                )
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category.name ?: "Unknown", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                shape = RoundedCornerShape(20.dp),
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryAccent,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White.copy(alpha = 0.05f),
                    labelColor = TextSecondary
                )
            )
        }
    }
}

@Composable
fun HeroChannelCard(item: ChannelItem, onClick: (ChannelItem) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(32.dp))
            .clickable { onClick(item) }
    ) {
        AsyncImage(
            model = item.logo?.url,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )
        
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiveBadge()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("4K HDR", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = item.categories.firstOrNull()?.name?.uppercase() ?: "FEATURED",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.channel.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Surface(
            onClick = { onClick(item) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(48.dp),
            shape = CircleShape,
            color = Color.White,
            contentColor = Color.Black,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun ChannelCard(item: ChannelItem, onClick: (ChannelItem) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(20.dp))
            .clickable { onClick(item) }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = item.logo?.url,
                        contentDescription = null,
                        modifier = Modifier.padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Text(
                    text = item.country?.flag ?: "🌐",
                    fontSize = 18.sp
                )
            }
            
            Column {
                Text(
                    text = item.channel.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.categories.firstOrNull()?.name ?: "Unknown",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.4f),
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Progress bar mockup from design
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight()
                        .background(PrimaryAccent)
                )
            }
        }
    }
}

@Composable
fun LiveBadge(mini: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PrimaryAccent)
            .padding(horizontal = if (mini) 4.dp else 8.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (mini) 4.dp else 5.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = alpha))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "LIVE",
            color = Color.White,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            fontSize = if (mini) 8.sp else 10.sp
        )
    }
}

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimaryAccent)
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
