package com.solofit.app.ui.foodlookup

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.data.remote.UsdaFood

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLookupScreen(
    onBack: () -> Unit,
    viewModel: FoodLookupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val nutrients = state.selectedFood?.let { extractNutrients(it.foodNutrients) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Nutrition Lookup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            SearchBar(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = viewModel::search,
                isSearching = state.isSearching
            )

            state.error?.let { error ->
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.isSearching) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (nutrients != null) {
                NutrientDetail(
                    name = state.selectedFood!!.description,
                    nutrients = nutrients,
                    onBack = viewModel::clearSelection
                )
            } else if (state.results.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Results",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(state.results, key = { it.fdcId }) { food ->
                        FoodResultCard(food, onClick = { viewModel.selectFood(food) })
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            } else if (state.query.isNotEmpty() && !state.isSearching) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Type a food name and tap search",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Search any food to see its\nfull nutritional profile",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("e.g. chicken breast") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Close, "Clear")
                    }
                }
            },
            enabled = !isSearching
        )
        IconButton(
            onClick = onSearch,
            enabled = query.trim().length >= 2 && !isSearching,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                Icons.Filled.Search,
                "Search",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun FoodResultCard(food: UsdaFood, onClick: () -> Unit) {
    val kcal = food.foodNutrients.find { it.nutrientId == 1008 }?.value
    val protein = food.foodNutrients.find { it.nutrientId == 1003 }?.value
    val carbs = food.foodNutrients.find { it.nutrientId == 1005 }?.value
    val fat = food.foodNutrients.find { it.nutrientId == 1004 }?.value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(food.description, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NutrientChip("${kcal?.formatNutrient() ?: "—"} kcal")
                NutrientChip("P ${protein?.formatNutrient() ?: "—"}g")
                NutrientChip("C ${carbs?.formatNutrient() ?: "—"}g")
                NutrientChip("F ${fat?.formatNutrient() ?: "—"}g")
            }
        }
    }
}

@Composable
private fun NutrientChip(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun NutrientDetail(
    name: String,
    nutrients: Map<String, String>,
    onBack: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Nutrients per 100g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
        }
        val entries = nutrients.entries.toList()
        items(entries.chunked(2)) { pair ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEach { (label, value) ->
                    NutrientCell(label, value, Modifier.weight(1f))
                }
                if (pair.size < 2) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        item {
            Spacer(Modifier.height(16.dp))
            Text(
                "Source: USDA FoodData Central",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NutrientCell(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
