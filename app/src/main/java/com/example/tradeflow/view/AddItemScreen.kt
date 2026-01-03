package com.example.tradeflow.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

enum class AddItemMode { ADD, EDIT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    mode: AddItemMode = AddItemMode.ADD,
    initialProduct: ProductModel? = null,
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember {
        ProductViewModel(ProductRepoImpl())
    }

    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var price by remember { mutableStateOf(if (initialProduct != null) initialProduct.price.toString() else "") }
    var location by remember { mutableStateOf(initialProduct?.location ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var selectedPurpose by remember { mutableStateOf(initialProduct?.type ?: "Select purpose") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "") }
    var status by remember { mutableStateOf(initialProduct?.status ?: "Available") }
    var agreedToTerms by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

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
        status = "Available"
    }

    fun saveProduct() {
        if (!validateForm()) {
            errorMessage = "Please fill all fields and agree to terms"
            showErrorDialog = true
            return
        }

        if (ownerId.isEmpty() && mode == AddItemMode.ADD) {
            errorMessage = "Please login to add products"
            showErrorDialog = true
            return
        }

        isLoading = true

        val priceValue = try {
            price.toDouble()
        } catch (e: NumberFormatException) {
            errorMessage = "Please enter a valid price"
            showErrorDialog = true
            isLoading = false
            return
        }

        fun proceedToSave(finalImageUrl: String) {
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
                imageUrl = finalImageUrl
            )

            val callback: (Boolean, String) -> Unit = { success, message ->
                isLoading = false
                if (success) {
                    if (mode == AddItemMode.EDIT) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Item updated successfully")
                        }
                        onSaved()
                    } else {
                        showSuccessDialog = true
                        resetForm()
                        imageUri = null
                    }
                } else {
                    errorMessage = message
                    showErrorDialog = true
                }
            }

            if (mode == AddItemMode.ADD) {
                viewModel.addProduct(product, callback)
            } else {
                viewModel.updateProduct(product, callback)
            }
        }

        if (imageUri != null) {
            viewModel.uploadImage(context, imageUri!!) { url ->
                if (url != null) {
                    proceedToSave(url)
                } else {
                    isLoading = false
                    errorMessage = "Failed to upload image"
                    showErrorDialog = true
                }
            }
        } else {
            val currentUrl = initialProduct?.imageUrl ?: ""
            proceedToSave(currentUrl)
        }
    }

    Scaffold(
        topBar = {
            TradeFlowTopBar(
                title = {
                    Text(
                        text = if (mode == AddItemMode.ADD) "Add New Item" else "Edit Item",
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                },
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = White
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // NAME
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name", fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = Greenish,
                        unfocusedIndicatorColor = Color.LightGray,
                        cursorColor = Greenish,
                        focusedLabelColor = Greenish,
                        unfocusedLabelColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // PRICE
            item {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = Greenish,
                        unfocusedIndicatorColor = Color.LightGray,
                        cursorColor = Greenish,
                        focusedLabelColor = Greenish,
                        unfocusedLabelColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // CATEGORY
            item {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = Greenish,
                        unfocusedIndicatorColor = Color.LightGray,
                        cursorColor = Greenish,
                        focusedLabelColor = Greenish,
                        unfocusedLabelColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Location Column
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = Greenish,
                                unfocusedIndicatorColor = Color.LightGray,
                                cursorColor = Greenish,
                                focusedLabelColor = Greenish,
                                unfocusedLabelColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedPurpose,
                                onValueChange = {},
                                readOnly = true,
                                textStyle = TextStyle(fontSize = 14.sp),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                                },
                                modifier = Modifier.menuAnchor(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    disabledContainerColor = White,
                                    focusedIndicatorColor = Greenish,
                                    unfocusedIndicatorColor = Color.LightGray,
                                    disabledIndicatorColor = Color.LightGray,
                                    focusedTextColor = if (isPlaceholder) Color.Gray else Color.Black,
                                    unfocusedTextColor = if (isPlaceholder) Color.Gray else Color.Black,
                                    focusedLabelColor = Greenish,
                                    unfocusedLabelColor = Color.Gray
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false }
                            ) {
                                typeOptions.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
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
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    textStyle = TextStyle(fontSize = 14.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = Greenish,
                        unfocusedIndicatorColor = Color.LightGray,
                        cursorColor = Greenish,
                        focusedLabelColor = Greenish,
                        unfocusedLabelColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            item {
                Text(
                    "Add Image",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(1.dp, Greenish, RoundedCornerShape(12.dp))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val currentImage = imageUri ?: (if (mode == AddItemMode.EDIT && !initialProduct?.imageUrl.isNullOrEmpty()) initialProduct?.imageUrl else null)

                    if (currentImage != null) {
                        AsyncImage(
                            model = currentImage,
                            contentDescription = "Selected Image",
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
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = agreedToTerms,
                        onCheckedChange = { agreedToTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = Greenish)
                    )
                    Text(
                        buildAnnotatedString {
                            append("I've read and agree with the ")
                            withStyle(
                                style = SpanStyle(
                                    color = Greenish,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Terms and Conditions")
                            }
                            append(" and the ")
                            withStyle(
                                style = SpanStyle(
                                    color = Greenish,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Privacy Policy")
                            }
                            append(".")
                        },
                        fontSize = 12.sp
                    )
                }
            }
            item {
                Button(
                    onClick = { saveProduct() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Greenish)
                ) {
                    if (isLoading) {
                        Text("Saving...", color = White, fontSize = 18.sp)
                    } else {
                        Text("Confirm", color = White, fontSize = 18.sp)
                    }
                }
            }
        }
    }
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success", fontWeight = FontWeight.Bold) },
            text = { Text("Product added successfully!") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Greenish)
                ) {
                    Text("OK")
                }
            }
        )
    }
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Greenish)
                ) {
                    Text("OK")
                }
            }
        )
    }
}
