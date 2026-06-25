package com.arfurnish.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.layout.ContentScale
import com.arfurnish.ui.components.AppBottomNavigationBar
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.arfurnish.R
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

private val CreamBg = Color(0xFFF9F5F0)
private val MutedBrownGrey = Color(0xFF7A726D)
private val ForegroundDark = Color(0xFF2C2826)
private val ForegroundMuted = Color(0xFF2C2826).copy(alpha = 0.15f)

private fun getProductDetails(name: String): Pair<String, String> {
    val category: String
    val subtitle: String
    
    when {
        name.contains("Chair", ignoreCase = true) || name.contains("Chairs", ignoreCase = true) || 
        name.contains("Sofa", ignoreCase = true) || name.contains("Ottoman", ignoreCase = true) || 
        name.contains("Bench", ignoreCase = true) -> {
            category = "Seating"
            subtitle = when {
                name.contains("Bar", ignoreCase = true) -> "Stylish counter-height stool"
                name.contains("Lounge", ignoreCase = true) -> "Comfortable mid-century lounge seat"
                name.contains("Arm Chair", ignoreCase = true) || name.contains("Armchair", ignoreCase = true) -> "Elegant accent armchair"
                name.contains("Leather", ignoreCase = true) -> "Premium leather seating"
                name.contains("L Shape", ignoreCase = true) -> "Spacious sectional sofa"
                name.contains("Skull", ignoreCase = true) -> "Artistic skull-shaped accent chair"
                name.contains("Sofa", ignoreCase = true) -> "Cozy cushioned sofa for lounging"
                name.contains("Ottoman", ignoreCase = true) -> "Soft upholstered footrest"
                name.contains("Bench", ignoreCase = true) -> "Durable outdoor park bench"
                else -> "Comfortable seating piece"
            }
        }
        name.contains("Table", ignoreCase = true) -> {
            category = "Tables"
            subtitle = when {
                name.contains("Coffee", ignoreCase = true) -> "Elegant centerpiece coffee table"
                name.contains("Gallinera", ignoreCase = true) -> "Rustic wooden dining table"
                else -> "Stylish utility table"
            }
        }
        name.contains("Cabinet", ignoreCase = true) || name.contains("Closet", ignoreCase = true) || 
        name.contains("Shelf", ignoreCase = true) || name.contains("Commode", ignoreCase = true) || 
        name.contains("Bookshelf", ignoreCase = true) || name.contains("Nightstand", ignoreCase = true) -> {
            category = "Storage"
            subtitle = when {
                name.contains("Cabinet", ignoreCase = true) -> "Spacious organizational cabinet"
                name.contains("Closet", ignoreCase = true) -> "Versatile clothing and display closet"
                name.contains("Shelf", ignoreCase = true) -> "Minimalist wall display shelf"
                name.contains("Commode", ignoreCase = true) -> "Classic wooden bedroom commode"
                name.contains("Bookshelf", ignoreCase = true) -> "Multi-level open bookshelf"
                name.contains("Nightstand", ignoreCase = true) -> "Compact bedside storage table"
                else -> "Functional storage unit"
            }
        }
        name.contains("Chandelier", ignoreCase = true) -> {
            category = "Lighting"
            subtitle = "Elegant ceiling hanging light fixture"
        }
        name.contains("Mirror", ignoreCase = true) || name.contains("Rug", ignoreCase = true) || 
        name.contains("Panels", ignoreCase = true) || name.contains("Style", ignoreCase = true) -> {
            category = "Decor"
            subtitle = when {
                name.contains("Mirror", ignoreCase = true) -> "Ornate decorative wall mirror"
                name.contains("Rug", ignoreCase = true) -> "Soft patterned floor covering"
                name.contains("Panels", ignoreCase = true) -> "Traditional decorative screen panels"
                else -> "Aesthetic home decor piece"
            }
        }
        name.contains("Door", ignoreCase = true) || name.contains("Wall", ignoreCase = true) -> {
            category = "Fittings"
            subtitle = when {
                name.contains("Door", ignoreCase = true) -> "High-quality wooden interior door"
                name.contains("Wall", ignoreCase = true) -> "Decorative background wall panel"
                else -> "Home architectural fitting"
            }
        }
        name.contains("Bathtub", ignoreCase = true) || name.contains("Basin", ignoreCase = true) || 
        name.contains("Toilet", ignoreCase = true) -> {
            category = "Bathroom"
            subtitle = when {
                name.contains("Bathtub", ignoreCase = true) -> "Luxury freestanding soak tub"
                name.contains("Basin", ignoreCase = true) -> "Modern ceramic washing basin"
                else -> "Essential bathroom hardware"
            }
        }
        name.contains("Bed", ignoreCase = true) -> {
            category = "Bedroom"
            subtitle = "Comfortable sleeping arrangement"
        }
        name.contains("Pool", ignoreCase = true) || name.contains("Outdoor", ignoreCase = true) -> {
            category = "Outdoor"
            subtitle = when {
                name.contains("Pool", ignoreCase = true) -> "Inflatable backyard family pool"
                else -> "Durable outdoor garden furniture"
            }
        }
        name.contains("Fan", ignoreCase = true) -> {
            category = "Living"
            subtitle = "Quiet overhead cooling fan"
        }
        else -> {
            category = "Living"
            subtitle = "Premium home furnishing item"
        }
    }
    
    return Pair(category, subtitle)
}

data class CatalogProduct(
    val id: String,
    val name: String,
    val imageRes: Int,
    val price: String,
    val rating: String,
    val category: String,
    val subtitle: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlphabetScreen(navController: NavController) {
    val rawCatalog = listOf(
        Triple("B", "Bar Chair", R.drawable.bar_chair_round_01),
        Triple("C", "Bookshelf", R.drawable.bookshelf_photoroom),
        Triple("D", "Chair", R.drawable.greenchair_01),
        Triple("E", "Chairs", R.drawable.armchair_01),
        Triple("F", "Chairs 2", R.drawable.mid_century_lounge_chair),
        Triple("G", "Chandelier 01", R.drawable.chandelier_01),
        Triple("H", "Chandelier 03", R.drawable.chandelier_03),
        Triple("I", "Chinese Armchair", R.drawable.chinese_armchair),
        Triple("J", "Chinese Cabinet", R.drawable.chinese_cabinet),
        Triple("K", "Chinese Commode", R.drawable.chinese_commode),
        Triple("L", "Chinese Panels", R.drawable.chinese_screen_panels),
        Triple("M", "Chinese Style", R.drawable.chinese_style),
        Triple("N", "Closet", R.drawable.wooden_display_shelves_01),
        Triple("O", "Coffee Table", R.drawable.coffee_table_round_01),
        Triple("P", "Door 2", R.drawable.doo2),
        Triple("Q", "Door 1", R.drawable.door1),
        Triple("R", "Door 3", R.drawable.door3),
        Triple("S", "Fan", R.drawable.fan_photoroom),
        Triple("T", "Gallinera Table", R.drawable.gallinera_table),
        Triple("U", "Gothic Coffee Table", R.drawable.gothic_coffee_table),
        Triple("V", "Gothic Cabinet", R.drawable.gothicabinet_01),
        Triple("W", "Green Chair", R.drawable.greenchair_01),
        Triple("X", "Kitchen Cabinet", R.drawable.kitchen_cabinet),
        Triple("Y", "Leather Sofa", R.drawable.sofa_01_),
        Triple("Z", "L Shape Sofa", R.drawable.sofa_02),
        Triple("AA", "Lounge Chair", R.drawable.mid_century_lounge_chair),
        Triple("BB", "Modern Arm Chair", R.drawable.modern_arm_chair_01),
        Triple("CC", "Coffee Table 01", R.drawable.modern_coffee_table_01),
        Triple("EE", "Wooden Cabinet", R.drawable.modern_wooden_cabinet),
        Triple("FF", "Mirror", R.drawable.ornate_mirror_01),
        Triple("GG", "Ottoman", R.drawable.ottoman_01),
        Triple("HH", "Outdoor Set", R.drawable.outdoor_table_chair_set_01),
        Triple("II", "Bench", R.drawable.park_bench_photoroom),
        Triple("JJ", "Plaggy", R.drawable.plaggy),
        Triple("KK", "Pool", R.drawable.laggy_cc0_inflatable_pool_),
        Triple("LL", "Shelf", R.drawable.plaggy_cc0_shelf),
        Triple("MM", "Toilet Tissue Holder", R.drawable.plaggy_cc0_toilet_roll_holder_),
        Triple("NN", "Wash Basin", R.drawable.plaggy_cc0_wash_basin),
        Triple("OO", "Bathtub", R.drawable.quaternius_cc0_bathtub),
        Triple("PP", "Bunk Bed", R.drawable.quaternius_cc0_bunk_bed),
        Triple("QQ", "Nightstand", R.drawable.quaternius_cc0_nightstand),
        Triple("RR", "Wood Wall", R.drawable.quaternius_cc0_red_wood_wall),
        Triple("SS", "Shoji Wall", R.drawable.quaternius_cc0_shoji_wall),
        Triple("TT", "Rug", R.drawable.rug_photoroom),
        Triple("UU", "Rug 2", R.drawable.rug2),
        Triple("VV", "Skull Chair", R.drawable.skull_chair),
        Triple("WW", "Sofa", R.drawable.sofa_01_)
    )
    
    val allProducts = rawCatalog.mapIndexed { index, it -> 
        val details = getProductDetails(it.second)
        CatalogProduct(
            id = it.first, 
            name = it.second, 
            imageRes = it.third, 
            price = "\$${50 + (index * 7) % 200}.00",
            rating = "4.${(index % 6) + 4}/5",
            category = details.first,
            subtitle = details.second
        ) 
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchOpen by remember { mutableStateOf(false) }
    
    val products = remember(searchQuery) {
        if (searchQuery.isBlank()) allProducts
        else allProducts.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = if (products.isNotEmpty()) products.size / 2 else 0)
    val coroutineScope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf 0
            
            val viewportCenter = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            
            val closestItem = visibleItems.minByOrNull {
                val itemCenter = it.offset + (it.size / 2)
                (viewportCenter - itemCenter).absoluteValue
            }
            closestItem?.index ?: 0
        }
    }
    
    val selectedProduct = remember(products, centerIndex) {
        products.getOrNull(centerIndex)
    }

    Scaffold(
        containerColor = CreamBg,
        bottomBar = { AppBottomNavigationBar(navController, currentRoute = "alphabet") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding() + 4.dp,
                    bottom = paddingValues.calculateBottomPadding()
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button (left)
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { navController.popBackStack() }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ForegroundDark.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Back",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                            color = ForegroundDark.copy(alpha = 0.8f)
                        )
                    }
                    
                    // Search button (right-center)
                    val rotation by animateFloatAsState(targetValue = if (isSearchOpen) 90f else 0f, label = "rotate")
                    val bgAlpha by animateFloatAsState(targetValue = if (isSearchOpen) 1f else 0f, label = "bgAlpha")
                    
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                spotColor = Color.Black.copy(alpha = 0.18f),
                                ambientColor = Color.Black.copy(alpha = 0.05f)
                            )
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.lerp(Color.White, ForegroundDark, bgAlpha)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { isSearchOpen = !isSearchOpen },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSearchOpen) Icons.Filled.Close else Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = if (isSearchOpen) Color.White else ForegroundDark,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { rotationZ = rotation }
                        )
                    }
                }
                
                // Search Bar
                androidx.compose.animation.AnimatedVisibility(visible = isSearchOpen) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 16.dp),
                        placeholder = { Text("Search furniture...", color = ForegroundMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = ForegroundDark,
                            unfocusedBorderColor = ForegroundMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Category + brand block
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = (selectedProduct?.category ?: "LIVING ROOM").uppercase(),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 2.4.sp,
                    color = MutedBrownGrey
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Atelier Nord",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = ForegroundDark
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Carousel
                if (products.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        LazyRow(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            horizontalArrangement = Arrangement.spacedBy(40.dp),
                            contentPadding = PaddingValues(horizontal = 90.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
                        ) {
                            items(products.size) { index ->
                                val item = products[index]
                            
                            val scale by remember {
                                derivedStateOf {
                                    val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                                    if (info == null) 0.6f else {
                                        val center = listState.layoutInfo.viewportEndOffset / 2f
                                        val itemCenter = info.offset + (info.size / 2f)
                                        val distanceFromCenter = (center - itemCenter).absoluteValue
                                        val maxDistance = listState.layoutInfo.viewportEndOffset / 2f
                                        val fraction = 1f - (distanceFromCenter / maxDistance)
                                        0.6f + (2.2f * fraction.coerceIn(0f, 1f))
                                    }
                                }
                            }
                            
                                val alpha = if (index == centerIndex) 1f else 0.4f
                                
                                val animatedScale by animateFloatAsState(targetValue = scale, label = "scale")
                                val animatedAlpha by animateFloatAsState(targetValue = alpha, label = "alpha")

                            Box(
                                modifier = Modifier
                                    .width(220.dp)
                                    .fillMaxHeight(0.85f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (index == centerIndex) {
                                            // Click active item -> go to AR
                                            navController.navigate("ar/${item.id}")
                                        } else {
                                            // Click side item -> scroll to it
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(index)
                                            }
                                        }
                                    }
                                    .graphicsLayer {
                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                        this.alpha = animatedAlpha
                                        // Ensure the center item is drawn on top of the side items
                                        // instead of side items overlapping the center item
                                    }
                                    .zIndex(if (index == centerIndex) 1f else 0f),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = item.imageRes),
                                    contentDescription = item.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            } else {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No items found", color = Color.Gray)
                    }
                }
            
            // Bottom Section - Product Detail Card
            // Bottom Section - Product Detail Card
            var selectedSize by remember { mutableStateOf("60 cm") }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp, top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Product Title
                Box(
                    modifier = Modifier.height(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedProduct?.name?.replace(" ", "\n") ?: "",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontSize = 32.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Normal,
                        color = ForegroundDark,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 2
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subtitle
                Text(
                    text = selectedProduct?.subtitle ?: "Centerpiece for the lounge",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontSize = 14.sp,
                    color = MutedBrownGrey,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Size selector pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("60 cm", "80 cm").forEach { size ->
                        val isSelected = selectedSize == size
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) ForegroundDark else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else ForegroundMuted,
                                    shape = CircleShape
                                )
                                .clickable { selectedSize = size }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = size,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) CreamBg else ForegroundDark
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Footer row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Price
                    val priceStr = selectedProduct?.price ?: "$0.00"
                    val parts = priceStr.split(".")
                    val dollars = parts.getOrNull(0) ?: "$0"
                    val cents = if (parts.size > 1) "." + parts[1] else ".00"
                    
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = dollars,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = ForegroundDark
                        )
                        Text(
                            text = cents,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = MutedBrownGrey,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                    
                    // Center: Plus button
                    var isPressed by remember { mutableStateOf(false) }
                    val buttonScale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "buttonScale")
                    
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(buttonScale)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                spotColor = Color.Black.copy(alpha = 0.18f),
                                ambientColor = Color.Black.copy(alpha = 0.05f)
                            )
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { 
                                selectedProduct?.let { navController.navigate("ar/${it.id}") }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add",
                            tint = ForegroundDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Right: Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = selectedProduct?.rating ?: "4.6/5",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ForegroundDark
                        )
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = ForegroundDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

