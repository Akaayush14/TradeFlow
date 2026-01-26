package com.example.tradeflow.view

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tradeflow.ui.theme.TradeFlowTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.*

class LocationPickerActivity : ComponentActivity() {
    companion object {
        const val EXTRA_SELECTED_ADDRESS = "selected_address"
        const val EXTRA_INITIAL_ADDRESS = "initial_address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialAddress = intent.getStringExtra(EXTRA_INITIAL_ADDRESS)

        setContent {
            TradeFlowTheme {
                LocationPickerScreen(
                    initialAddress = initialAddress,
                    onLocationSelected = { address ->
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_SELECTED_ADDRESS, address)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    },
                    onBackClick = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    initialAddress: String?,
    onLocationSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // Default location (Kathmandu, Nepal)
    val defaultLocation = LatLng(27.7172, 85.3240)

    // Try to get initial location from address
    var initialLatLng = defaultLocation
    if (!initialAddress.isNullOrEmpty()) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName("$initialAddress, Nepal", 1)
            if (addresses != null && addresses.isNotEmpty()) {
                initialLatLng = LatLng(addresses[0].latitude, addresses[0].longitude)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var selectedLocation by remember { mutableStateOf(initialLatLng) }
    var selectedAddress by remember { mutableStateOf(initialAddress ?: "") }
    var isLoadingAddress by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 15f)
    }

    // Update selected location when camera moves
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            selectedLocation = cameraPositionState.position.target

            // Get address from coordinates
            isLoadingAddress = true
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(
                    selectedLocation.latitude,
                    selectedLocation.longitude,
                    1
                )

                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    // Get the most specific address component available
                    selectedAddress = when {
                        !address.thoroughfare.isNullOrEmpty() -> address.thoroughfare
                        !address.subLocality.isNullOrEmpty() -> address.subLocality
                        !address.locality.isNullOrEmpty() -> address.locality
                        !address.subAdminArea.isNullOrEmpty() -> address.subAdminArea
                        else -> "${selectedLocation.latitude}, ${selectedLocation.longitude}"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                selectedAddress = "${selectedLocation.latitude}, ${selectedLocation.longitude}"
            }
            isLoadingAddress = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Location") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onLocationSelected(selectedAddress) },
                        enabled = selectedAddress.isNotEmpty() && !isLoadingAddress
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm Location",
                            tint = if (selectedAddress.isNotEmpty() && !isLoadingAddress)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.Gray
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Google Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                )
            )

            // Center Pin
            Icon(
                painter = androidx.compose.ui.res.painterResource(
                    android.R.drawable.ic_menu_mylocation
                ),
                contentDescription = "Location Pin",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .offset(y = (-24).dp),
                tint = Color.Red
            )

            // Address Display Card
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Selected Location",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoadingAddress) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Getting address...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Text(
                            text = selectedAddress.ifEmpty { "Move map to select location" },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Lat: ${String.format("%.6f", selectedLocation.latitude)}, " +
                                "Lng: ${String.format("%.6f", selectedLocation.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Instruction Text
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Drag the map to choose your location",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}