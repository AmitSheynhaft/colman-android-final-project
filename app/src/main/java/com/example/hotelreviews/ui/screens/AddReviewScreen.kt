package com.example.hotelreviews.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hotelreviews.ui.theme.PrimaryBlue
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: (String, String, Int, String, String, String, Double, Int) -> Unit
) {
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }

    var hotelName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    // API Data
    var placeId by remember { mutableStateOf("") }
    var apiRating by remember { mutableDoubleStateOf(0.0) }
    var apiReviewCount by remember { mutableIntStateOf(0) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Review", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = PrimaryBlue
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Google Places Search UI
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Search Hotel (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            if (query.length > 2) {
                                val request = FindAutocompletePredictionsRequest.builder()
                                    .setQuery(query)
                                    .build()
                                placesClient.findAutocompletePredictions(request)
                                    .addOnSuccessListener { response ->
                                        predictions = response.autocompletePredictions
                                    }
                            } else {
                                predictions = emptyList()
                            }
                        },
                        placeholder = { Text("Search Google Places") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (predictions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            predictions.forEach { prediction ->
                                Text(
                                    text = prediction.getFullText(null).toString(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val placeFields = listOf(
                                                Place.Field.ID,
                                                Place.Field.NAME,
                                                Place.Field.ADDRESS_COMPONENTS,
                                                Place.Field.RATING,
                                                Place.Field.USER_RATINGS_TOTAL
                                            )
                                            val fetchPlaceRequest = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
                                            placesClient.fetchPlace(fetchPlaceRequest)
                                                .addOnSuccessListener { response ->
                                                    val place = response.place
                                                    hotelName = place.name ?: ""
                                                    placeId = place.id ?: ""
                                                    apiRating = place.rating ?: 0.0
                                                    apiReviewCount = place.userRatingsTotal ?: 0
                                                    
                                                    // Extract city from address components
                                                    city = place.addressComponents?.asList()
                                                        ?.find { it.types.contains("locality") }?.name ?: ""
                                                    
                                                    predictions = emptyList()
                                                    searchQuery = ""
                                                }
                                        }
                                        .padding(12.dp),
                                    fontSize = 14.sp
                                )
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE8EFFF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Note: Mock API. Replace with your key.",
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFF2B4CBF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = hotelName,
                onValueChange = { hotelName = it },
                label = { Text("Hotel Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (placeId.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Google Places Data:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Rating: $apiRating ⭐ ($apiReviewCount reviews)", fontSize = 12.sp)
                        Text("Place ID: $placeId", fontSize = 10.sp, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Your Rating", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                repeat(5) { index ->
                    IconButton(onClick = { rating = index + 1 }) {
                        Icon(
                            imageVector = if (index < rating) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = null,
                            tint = if (index < rating) Color(0xFFFFB400) else Color.LightGray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    onSaveClick(hotelName, city, rating, description, imageUrl, placeId, apiRating, apiReviewCount) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && hotelName.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Review")
                }
            }
        }
    }
}
