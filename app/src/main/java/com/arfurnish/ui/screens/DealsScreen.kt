package com.arfurnish.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.arfurnish.R
import com.arfurnish.ui.components.AppBottomNavigationBar

private data class DealBundleUi(
    val id: String,
    val title: String,
    val price: String,
    val imageUrl: String,
    val itemKeys: List<String>,
    val itemNames: List<String>
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DealsScreen(navController: NavController) {
    val colorScheme = MaterialTheme.colorScheme
    val deals = remember {
        listOf(
            DealBundleUi("living_essentials", "Living Essentials Bundle", "Rs. 210,000",
                "https://images.unsplash.com/photo-1540518614846-7eded433c457?w=600&auto=format&fit=crop",
                listOf("Y", "O", "C"), listOf("Leather Sofa", "Coffee Table", "Bookshelf")),
            DealBundleUi("dining_combo", "Dining Combo Set", "Rs. 109,000",
                "https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?w=600&auto=format&fit=crop",
                listOf("D", "E", "CCC"), listOf("Chair", "Chairs Set", "Table Chair Set")),
            DealBundleUi("outdoor_corner", "Outdoor Corner Bundle", "Rs. 120,000",
                "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=600&auto=format&fit=crop",
                listOf("II", "W"), listOf("Park Bench", "Green Chair")),
            DealBundleUi("bedroom_comfort", "Bedroom Comfort Bundle", "Rs. 340,000",
                "https://images.unsplash.com/photo-1616594039964-ae9021a400a0?w=600&auto=format&fit=crop",
                listOf("PP", "QQ", "N"), listOf("Bunk Bed", "Nightstand", "Closet")),
            DealBundleUi("home_office", "Home Office Setup", "Rs. 85,000",
                "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=600&auto=format&fit=crop",
                listOf("BB", "EE", "JJJ"), listOf("Arm Chair", "Wooden Cabinet", "Whiteboard"))
        )
    }

    val bgGradient = Brush.verticalGradient(
        listOf(
            colorScheme.background,
            colorScheme.surfaceVariant.copy(alpha = 0.45f),
            colorScheme.background
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 94.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surface)
                            .border(1.dp, colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colorScheme.onSurface)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text("Deals", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = colorScheme.onSurface)
                }
            }
            item { HeaderCard(colorScheme) }
            itemsIndexed(deals, key = { _, it -> it.id }) { index, deal ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(420, delayMillis = index * 90)) +
                            slideInVertically(tween(420, delayMillis = index * 90)) { it / 5 }
                ) {
                    DealCard(deal, colorScheme) {
                        val encoded = deal.itemKeys.joinToString(",") { Uri.encode(it) }
                        navController.navigate("deals/ar/$encoded")
                    }
                }
            }
        }
        
        AppBottomNavigationBar(
            navController = navController,
            currentRoute = "deals",
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun HeaderCard(colorScheme: ColorScheme) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth().shadow(16.dp, RoundedCornerShape(24.dp), clip = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(colorScheme.primary, colorScheme.secondary)))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = colorScheme.onPrimary, modifier = Modifier.size(26.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Bundle Deals", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onPrimary)
                    Text(
                        "Pick one deal to instantly place the full set in AR.",
                        fontSize = 13.sp, color = colorScheme.onPrimary.copy(alpha = 0.9f), lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DealCard(deal: DealBundleUi, colorScheme: ColorScheme, onPlaceInAr: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(18.dp, RoundedCornerShape(24.dp), clip = false),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = deal.imageUrl,
                    contentDescription = deal.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Dark overlay gradient for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 100f
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(deal.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(deal.price, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.primaryContainer)
                }
            }

            Text(
                "INCLUDED ITEMS",
                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant, letterSpacing = 1.2.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                deal.itemNames.forEach { name ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text(name, fontSize = 14.sp, color = colorScheme.onSurface)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(14.dp))
                        Text(
                            "Bundle of ${deal.itemKeys.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onPrimaryContainer
                        )
                    }
                }

                Button(
                    onClick = onPlaceInAr,
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(colorScheme.primary, colorScheme.secondary)))
                ) {
                    Text("Place in AR", color = colorScheme.onPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

