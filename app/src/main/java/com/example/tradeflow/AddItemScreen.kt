package com.example.tradeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(onBackClick: () -> Unit = {}) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPurpose by remember { mutableStateOf("Select purpose") }
    var category by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val typeOptions = listOf("Barter", "Rent")
    val isPlaceholder = selectedPurpose == "Select purpose"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Add Items",
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_new_24),
                            contentDescription = "back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Greenish
                )
            )
        },
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
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
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
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
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
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
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
                        androidx.compose.foundation.layout.Column {
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("Location", fontSize = 14.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
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
                    }

                    // Type Column
                    Box(modifier = Modifier.weight(1f)) {
                        androidx.compose.foundation.layout.Column {
                            ExposedDropdownMenuBox(
                                expanded = isDropdownExpanded,
                                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedPurpose,
                                    onValueChange = {},
                                    readOnly = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
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
                                    ),
                                    shape = RoundedCornerShape(12.dp)
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
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = Greenish,
                        unfocusedIndicatorColor = Color.LightGray,
                        cursorColor = Greenish,
                        focusedLabelColor = Greenish,
                        unfocusedLabelColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = false
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE3F2FD))
                        .border(1.dp, Greenish, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.additem),
                        contentDescription = "Add Image",
                        modifier = Modifier.size(48.dp),
                        tint = Greenish
                    )
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
                    onClick = {  },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Greenish),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm", color = White, fontSize = 18.sp)
                }
            }
        }
    }
}