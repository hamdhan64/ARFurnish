package com.arfurnish.ui.screens

import com.arfurnish.ui.components.AppBottomNavigationBar
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.arfurnish.R

data class FurnitureItem(
    val modelKey: String,
    val title: String,
    val subtitle: String,
    val price: String,
    val category: String,
    val imageRes: Int
)

data class CategoryChip(
    val title: String,
    val icon: ImageVector,
    val tint: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val items = remember {
        listOf(
            FurnitureItem("A", "Modern Chair", "Comfortable living room chair", "Rs. 25,000", "Seating", R.drawable.tabel_chair_photoroom),
            FurnitureItem("B", "Water Holder", "Clean and compact water holder", "Rs. 44,000", "Storage", R.drawable.water_photoroom),
            FurnitureItem("C", "Fan", "Modern fan for home and office", "Rs. 72,000", "Living", R.drawable.fan_photoroom),
            FurnitureItem("D", "Outdoor Bench", "Weather-ready patio bench", "Rs. 38,000", "Outdoor", R.drawable.park_bench_photoroom),
            FurnitureItem("E", "Leather Sofa", "Premium 3-seater comfort", "Rs. 120,000", "Living", R.drawable.leather_sofa_photoroom),
            FurnitureItem("F", "Blue Sofa", "Small-space modern sofa", "Rs. 95,000", "Living", R.drawable.blue_sofa_photoroom_2),
            FurnitureItem("G", "Bookshelf", "Multi-level storage unit", "Rs. 56,000", "Storage", R.drawable.bookshelf_photoroom)
        )
    }

    val categories = remember {
        listOf(
            CategoryChip("All", Icons.Default.Star, Color(0xFF6D5DF6)),
            CategoryChip("Living", Icons.Default.Home, Color(0xFF0EA5E9)),
            CategoryChip("Seating", Icons.Default.Chair, Color(0xFF10B981)),
            CategoryChip("Tables", Icons.Default.TableRestaurant, Color(0xFFF59E0B)),
            CategoryChip("Storage", Icons.Default.Inventory2, Color(0xFF8B5CF6)),
            CategoryChip("Outdoor", Icons.Default.Favorite, Color(0xFFEF4444))
        )
    }

    val filteredItems = remember(query, selectedCategory, items) {
        val search = query.trim().lowercase()
        items.filter { item ->
            val categoryOk = selectedCategory == "All" || item.category == selectedCategory
            val searchOk = search.isBlank() ||
                item.title.lowercase().contains(search) ||
                item.subtitle.lowercase().contains(search) ||
                item.price.lowercase().contains(search) ||
                item.category.lowercase().contains(search)
            categoryOk && searchOk
        }
    }


    val displayedItems by remember(filteredItems) {
        derivedStateOf { filteredItems.take(3) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 94.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome back,",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ARFurnish",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    placeholder = { Text("Search furniture...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Visualize furniture in AR",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Browse the full catalog and preview each item in your own space.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = { navController.navigate("alphabet") },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text("Browse catalog")
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Image(
                            painter = painterResource(id = R.drawable.front1),
                            contentDescription = "Featured furniture",
                            modifier = Modifier
                                .size(90.dp)
                                .graphicsLayer { translationY = floatAnim }
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Explore Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { chip ->
                        val selected = selectedCategory == chip.title
                        val bgColor by animateColorAsState(
                            targetValue = if (selected) chip.tint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            label = "color"
                        )
                        val elevation by animateDpAsState(
                            targetValue = if (selected) 3.dp else 0.dp,
                            label = "elevation"
                        )

                        Surface(
                            color = bgColor,
                            shape = RoundedCornerShape(50),
                            shadowElevation = elevation,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedCategory = chip.title }
                                .animateContentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = chip.icon,
                                    contentDescription = chip.title,
                                    tint = chip.tint,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = chip.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${displayedItems.size} / ${filteredItems.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (filteredItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No matching items",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Try another keyword or change category.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = {
                                    query = ""
                                    selectedCategory = "All"
                                }
                            ) {
                                Text("Reset filters")
                            }
                        }
                    }
                }
            }

            items(
                items = displayedItems,
                key = { it.modelKey }
            ) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.category}  •  ${item.price}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { navController.navigate("ar/${item.modelKey}") },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Preview in AR")
                                }
                                Button(
                                    onClick = { navController.navigate("alphabet") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Open")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { navController.navigate("alphabet") },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 11.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Full Catalog")
                }
            }
        }

        AppBottomNavigationBar(
            navController = navController,
            currentRoute = "home",
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

