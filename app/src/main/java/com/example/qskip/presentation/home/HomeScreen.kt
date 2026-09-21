package com.example.qskip.presentation.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.qskip.R
import com.example.qskip.domain.model.Flyer
import com.example.qskip.domain.model.Product
import com.example.qskip.presentation.components.*
import com.example.qskip.utils.toCurrency
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToScanner: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToAdminPortal: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (!uiState.isUserLoggedIn) {
        onLogout() // Trigger navigation side-effect back to login if unauthenticated
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.app_banner),
                        contentDescription = "Qskip Logo",
                        modifier = Modifier.height(32.dp),
                        contentScale = ContentScale.Fit
                    )
                },
                actions = {
                    if (uiState.isAdmin) {
                        TextButton(onClick = onNavigateToAdminPortal) {
                            Text("Admin Portal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            
            // Header / Greeting
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Hello there!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ready to skip the queue?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Active Banner Flyers Auto-Scrolling Carousel
            if (uiState.activeFlyers.isNotEmpty()) {
                FlyersCarousel(flyers = uiState.activeFlyers)
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Customer Fashion Categories Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val categories = listOf("Men", "Women", "Kids", "Bags", "Other")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categories.forEach { catName ->
                        val isSelected = uiState.selectedCategory == catName
                        CategoryCardItem(
                            title = catName,
                            icon = {
                                when (catName) {
                                    "Men" -> MenCategoryIcon(size = 28.dp)
                                    "Women" -> WomenCategoryIcon(size = 28.dp)
                                    "Kids" -> KidCategoryIcon(size = 28.dp)
                                    "Bags" -> BagCategoryIcon(size = 28.dp)
                                    else -> OtherCategoryIcon(size = 28.dp)
                                }
                            },
                            isSelected = isSelected,
                            onClick = { viewModel.selectCategory(catName) }
                        )
                    }
                }

                if (uiState.selectedCategory != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    InputChip(
                        selected = true,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("Filter: ${uiState.selectedCategory}") },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Clear Filter", modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main CTA: Scanner Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = onNavigateToScanner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        QrScannerIcon(
                            size = 28.dp,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "SCAN PRODUCT",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shopping Budget Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBudget() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.budget > 0 && uiState.cartTotal > uiState.budget) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MoneyIcon(
                                    size = 22.dp,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Shopping Budget",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(
                                onClick = onNavigateToBudget,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(if (uiState.budget > 0) "Manage" else "Set Budget", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (uiState.budget > 0) {
                            val diff = uiState.budget - uiState.cartTotal
                            val progress = (uiState.cartTotal / uiState.budget).toFloat().coerceIn(0.0f, 1.0f)
                            val isExceeded = diff < 0

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Spent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Rs. ${uiState.cartTotal.toCurrency()}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (isExceeded) "Exceeded By" else "Remaining",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Rs. ${(if (isExceeded) -diff else diff).toCurrency()}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Usage Progress Bar
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        } else {
                            Text(
                                text = "No budget set for this shopping session.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order History Shortcut
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToOrders,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Orders & Exit QR Pass", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Filtered Products List
            val displayedProducts = if (uiState.selectedCategory == null) {
                uiState.recommendedProducts
            } else {
                uiState.recommendedProducts.filter { p ->
                    val categoryName = p.categoryId.trim()
                    when (uiState.selectedCategory) {
                        "Men" -> categoryName.equals("Men", ignoreCase = true) || categoryName.equals("T-Shirts", ignoreCase = true) || categoryName.equals("Jeans", ignoreCase = true) || categoryName.equals("Shirts", ignoreCase = true)
                        "Women" -> categoryName.equals("Women", ignoreCase = true) || categoryName.equals("Dresses", ignoreCase = true) || categoryName.equals("Skirts", ignoreCase = true)
                        "Kids" -> categoryName.equals("Kids", ignoreCase = true) || categoryName.equals("Children", ignoreCase = true)
                        "Bags" -> categoryName.equals("Bags", ignoreCase = true) || categoryName.equals("Handbags", ignoreCase = true) || categoryName.equals("Backpacks", ignoreCase = true)
                        else -> !categoryName.equals("Men", ignoreCase = true) && !categoryName.equals("Women", ignoreCase = true) && !categoryName.equals("Kids", ignoreCase = true) && !categoryName.equals("Bags", ignoreCase = true)
                    }
                }
            }

            Column {
                Text(
                    text = if (uiState.selectedCategory != null) "${uiState.selectedCategory} Products" else "Recommended for you",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading && displayedProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (displayedProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No products found in this category.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(displayedProducts, key = { it.productId }) { product ->
                            HomeProductCard(
                                product = product,
                                onClick = { onNavigateToProduct(product.productId) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlyersCarousel(
    flyers: List<Flyer>,
    modifier: Modifier = Modifier
) {
    if (flyers.isEmpty()) return

    val itemCount = flyers.size
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { itemCount }
    )

    // Controlled auto-scroll loop keyed on (flyers, itemCount)
    LaunchedEffect(flyers, itemCount) {
        if (itemCount > 1) {
            while (true) {
                delay(4000L) // Auto-advance every 4 seconds
                if (!pagerState.isScrollInProgress) {
                    val targetPage = (pagerState.currentPage + 1) % itemCount
                    try {
                        pagerState.animateScrollToPage(
                            page = targetPage,
                            animationSpec = tween(
                                durationMillis = 700,
                                easing = FastOutSlowInEasing
                            )
                        )
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(20.dp))
        ) { page ->
            val flyer = flyers[page]

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (flyer.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = flyer.imageUrl,
                        contentDescription = flyer.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )
                }

                // Flyer Text Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (flyer.title.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "SPECIAL OFFER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = flyer.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (flyer.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = flyer.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Bottom-Right Pagination Dots Indicator (Matching Reference Image)
        if (itemCount > 1) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.45f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(itemCount) { index ->
                        val isSelected = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 18.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCardItem(
    title: String,
    icon: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = containerColor,
            border = BorderStroke(1.5.dp, borderColor),
            modifier = Modifier.size(58.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                icon()
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun HomeProductCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            val imageUrl = product.imageUrls.firstOrNull()
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image")
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${product.size} • ${product.color}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Rs. ${product.price.toCurrency()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
