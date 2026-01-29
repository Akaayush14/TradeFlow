package com.example.tradeflow.view

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import com.example.tradeflow.repository.UserRepoImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.Icons

enum class AddItemMode { ADD, EDIT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAddItemScreen(
    mode: AddItemMode = AddItemMode.ADD,
    initialProduct: ProductModel? = null,
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    // Form fields
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var price by remember { mutableStateOf(if (initialProduct != null) initialProduct.price.toString() else "") }
    var location by remember { mutableStateOf(initialProduct?.location ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var selectedPurpose by remember { mutableStateOf(initialProduct?.type ?: "Select purpose") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "") }
    var status by remember { mutableStateOf(initialProduct?.status ?: "Pending") }
    var agreedToTerms by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Image URIs
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUri2 by remember { mutableStateOf<Uri?>(null) }
    var imageUri3 by remember { mutableStateOf<Uri?>(null) }
    var imageUri4 by remember { mutableStateOf<Uri?>(null) }
    var activeImageIndex by remember { mutableStateOf(0) }

    // UI state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val ownerId = currentUser?.uid ?: ""
    val typeOptions = listOf("Barter", "Rent", "Both")
    val isPlaceholder = selectedPurpose == "Select purpose"
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Log.d("TF_IMAGE_SELECT", "Selected image uri=$uri for index=$activeImageIndex")
            when (activeImageIndex) {
                0 -> imageUri = uri
                1 -> imageUri2 = uri
                2 -> imageUri3 = uri
                3 -> imageUri4 = uri
            }
        } else {
            Log.e("TF_IMAGE_SELECT", "No URI returned for index=$activeImageIndex")
        }
    }
    // Location picker launcher
    val locationPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedAddress = result.data?.getStringExtra(
                LocationPickerActivity.EXTRA_SELECTED_ADDRESS
            )
            if (!selectedAddress.isNullOrEmpty()) {
                location = selectedAddress
                Log.d("TF_LOCATION_PICKER", "Selected address: $selectedAddress")
            }
        }
    }

    // Load initial product data in EDIT mode
    LaunchedEffect(initialProduct?.productId, mode) {
        if (mode == AddItemMode.EDIT && initialProduct != null) {
            name = initialProduct.name
            price = initialProduct.price.toString()
            location = initialProduct.location
            description = initialProduct.description
            selectedPurpose = initialProduct.type
            category = initialProduct.category
            status = initialProduct.status
        }
    }

    fun validateForm(): Boolean {
        return name.isNotBlank() &&
                price.isNotBlank() &&
                category.isNotBlank() &&
                location.isNotBlank() &&
                description.isNotBlank() &&
                selectedPurpose != "Select purpose" &&
                agreedToTerms
    }

    fun resetForm() {
        name = ""
        price = ""
        location = ""
        description = ""
        selectedPurpose = "Select purpose"
        category = ""
        agreedToTerms = false
        status = "Pending"
        imageUri = null
        imageUri2 = null
        imageUri3 = null
        imageUri4 = null
    }

    fun saveProduct() {
        Log.d("TF_SAVE_FLOW", "Save initiated mode=$mode")

        if (!validateForm()) {
            errorMessage = "Please fill all fields and agree to terms"
            showErrorDialog = true
            Log.e("TF_SAVE_FLOW", "Validation failed: $errorMessage")
            return
        }

        if (ownerId.isEmpty() && mode == AddItemMode.ADD) {
            errorMessage = "Please login to add products"
            showErrorDialog = true
            Log.e("TF_SAVE_FLOW", "OwnerId empty for ADD")
            return
        }

        isLoading = true

        val priceValue = try {
            price.toDouble()
        } catch (e: NumberFormatException) {
            errorMessage = "Please enter a valid price"
            showErrorDialog = true
            isLoading = false
            Log.e("TF_SAVE_FLOW", "Invalid price input error=${e.message}")
            return
        }

        // Define proceedToSave FIRST so it's in scope for uploadNextImage
        fun proceedToSave(
            mainUrl: String,
            subUrl2: String = "",
            subUrl3: String = "",
            subUrl4: String = ""
        ) {
            val product = ProductModel(
                productId = if (mode == AddItemMode.EDIT) initialProduct?.productId ?: "" else "",
                name = name.trim(),
                price = priceValue,
                category = category.trim(),
                location = location.trim(),
                description = description.trim(),
                type = selectedPurpose,
                ownerId = if (mode == AddItemMode.EDIT) initialProduct?.ownerId ?: ownerId else ownerId,
                status = status,
                imageUrl = mainUrl,
                imageUrl2 = subUrl2,
                imageUrl3 = subUrl3,
                imageUrl4 = subUrl4,
                isListed = false // Always false for new/updated items until admin approves
            )

            val callback: (Boolean, String) -> Unit = { success, message ->
                isLoading = false
                Log.d("TF_FIRESTORE_SAVE", "Save callback success=$success message=$message productId=${product.productId}")

                if (success) {
                    if (mode == AddItemMode.EDIT) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Item updated successfully")
                        }
                        onSaved()
                    } else {
                        // Calculate and award points for new items
                        val pointsToAward = (priceValue * 0.72).toLong()
                        if (pointsToAward > 0 && ownerId.isNotEmpty()) {
                            userViewModel.updateUserPoints(ownerId, pointsToAward) { pointsSuccess, pointsMessage ->
                                Log.d("TF_POINTS", "Points update success=$pointsSuccess message=$pointsMessage")
                            }
                        }
                        showSuccessDialog = true
                        resetForm()
                    }
                } else {
                    errorMessage = message
                    showErrorDialog = true
                }
            }

            if (mode == AddItemMode.ADD) {
                Log.d("TF_FIRESTORE_SAVE", "Calling addProduct with product name=${product.name}")
                viewModel.addProduct(product, callback)
            } else {
                Log.d("TF_FIRESTORE_SAVE", "Calling updateProduct with productId=${product.productId}")
                viewModel.updateProduct(product, callback)
            }
        }

        // Upload all images to Cloudinary first, then save to Firebase
        fun uploadAllImagesAndSave() {
            val imagesToUpload = mutableListOf<Pair<Int, Uri>>()

            imageUri?.let { imagesToUpload.add(0 to it) }
            imageUri2?.let { imagesToUpload.add(1 to it) }
            imageUri3?.let { imagesToUpload.add(2 to it) }
            imageUri4?.let { imagesToUpload.add(3 to it) }

            // In ADD mode, require at least one image
            if (mode == AddItemMode.ADD && imagesToUpload.isEmpty()) {
                isLoading = false
                errorMessage = "Please select at least one image"
                showErrorDialog = true
                return
            }

            // If no new images in EDIT mode, use existing URLs
            if (imagesToUpload.isEmpty()) {
                proceedToSave(
                    mainUrl = initialProduct?.imageUrl ?: "",
                    subUrl2 = initialProduct?.imageUrl2 ?: "",
                    subUrl3 = initialProduct?.imageUrl3 ?: "",
                    subUrl4 = initialProduct?.imageUrl4 ?: ""
                )
                return
            }

            // Upload images sequentially to avoid race conditions
            val uploadedUrls = mutableMapOf<Int, String>()
            var currentUploadIndex = 0

            fun uploadNextImage() {
                if (currentUploadIndex >= imagesToUpload.size) {
                    // All uploads complete, proceed to save
                    proceedToSave(
                        mainUrl = uploadedUrls[0] ?: initialProduct?.imageUrl ?: "",
                        subUrl2 = uploadedUrls[1] ?: initialProduct?.imageUrl2 ?: "",
                        subUrl3 = uploadedUrls[2] ?: initialProduct?.imageUrl3 ?: "",
                        subUrl4 = uploadedUrls[3] ?: initialProduct?.imageUrl4 ?: ""
                    )
                    return
                }

                val (index, uri) = imagesToUpload[currentUploadIndex]
                val imageLabel = if (index == 0) "main" else "sub $index"

                Log.d("TF_IMAGE_UPLOAD", "Uploading $imageLabel image uri=$uri")

                viewModel.uploadImage(context, uri) { url ->
                    if (url == null) {
                        isLoading = false
                        errorMessage = "Failed to upload $imageLabel image. Please check your connection and try again."
                        showErrorDialog = true
                        Log.e("TF_IMAGE_UPLOAD", "Upload failed for $imageLabel image")
                        return@uploadImage
                    }

                    Log.d("TF_IMAGE_UPLOAD", "Upload success for $imageLabel image url=$url")
                    uploadedUrls[index] = url
                    currentUploadIndex++
                    uploadNextImage()
                }
            }

            // Start uploading the first image
            uploadNextImage()
        }

        // Start the upload and save process
        uploadAllImagesAndSave()
    }

    Scaffold(
        topBar = {
            TradeFlowTopBar(
                title = {
                    Text(
                        text = if (mode == AddItemMode.ADD) "Add New Item" else "Edit Item",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // NAME
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text("Item Name", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // PRICE
            item {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = {
                        Text("Price", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // CATEGORY
            item {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = {
                        Text("Category", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // LOCATION AND PURPOSE
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location with Map Picker
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = {
                                Text("Location", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            modifier = Modifier.fillMaxSize(),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        // Open location picker
                                        val intent = Intent(context, LocationPickerActivity::class.java).apply {
                                            putExtra(LocationPickerActivity.EXTRA_INITIAL_ADDRESS, location)
                                        }
                                        locationPickerLauncher.launch(intent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Pick Location",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    // Purpose Dropdown -
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            OutlinedTextField(
                                value = selectedPurpose,
                                onValueChange = {},
                                readOnly = true,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = if (isPlaceholder)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                ),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxSize(),  // Fill parent
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = if (isPlaceholder)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = if (isPlaceholder)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                typeOptions.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                selectionOption,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            selectedPurpose = selectionOption
                                            isDropdownExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // DESCRIPTION
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = {
                        Text("Description", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // IMAGES SECTION
            item {
                Text(
                    "Add Images",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Main Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            activeImageIndex = 0
                            launcher.launch("image/*")
                        }
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val currentImage = imageUri ?: (if (mode == AddItemMode.EDIT && !initialProduct?.imageUrl.isNullOrEmpty())
                        initialProduct?.imageUrl else null)

                    if (currentImage != null) {
                        AsyncImage(
                            model = currentImage,
                            contentDescription = "Main Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.placeholderimage),
                            error = painterResource(R.drawable.placeholderimage)
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.placeholderimage),
                            contentDescription = "Placeholder Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Main Image Label
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Main",
                            color = MaterialTheme.colorScheme.surface,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sub Images Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val subImages = listOf(
                        Triple(imageUri2, initialProduct?.imageUrl2, 1),
                        Triple(imageUri3, initialProduct?.imageUrl3, 2),
                        Triple(imageUri4, initialProduct?.imageUrl4, 3)
                    )

                    subImages.forEach { (uri, existingUrl, index) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    activeImageIndex = index
                                    launcher.launch("image/*")
                                }
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            val currentSubImage = uri ?: (if (mode == AddItemMode.EDIT && !existingUrl.isNullOrEmpty())
                                existingUrl else null)

                            if (currentSubImage != null) {
                                AsyncImage(
                                    model = currentSubImage,
                                    contentDescription = "Sub Image $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(R.drawable.placeholderimage),
                                    error = painterResource(R.drawable.placeholderimage)
                                )
                            } else {
                                Image(
                                    painter = painterResource(R.drawable.placeholderimage),
                                    contentDescription = "Placeholder",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            // TERMS AND CONDITIONS
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = agreedToTerms,
                        onCheckedChange = { agreedToTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Text(
                        buildAnnotatedString {
                            append("I've read and agree with the ")
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Terms and Conditions")
                            }
                            append(" and the ")
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Privacy Policy")
                            }
                            append(".")
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // CONFIRM BUTTON
            item {
                Button(
                    onClick = { saveProduct() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        Text("Saving...", fontSize = 18.sp)
                    } else {
                        Text("Confirm", fontSize = 18.sp)
                    }
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    "Success",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Product added successfully!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "OK",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    "Error",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "OK",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}