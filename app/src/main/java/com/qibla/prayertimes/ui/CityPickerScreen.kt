package com.qibla.prayertimes.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.qibla.prayertimes.MapPickerActivity
import com.qibla.prayertimes.R
import com.qibla.prayertimes.data.GeocodeResult
import com.qibla.prayertimes.data.GeocodingSearch
import com.qibla.prayertimes.data.LocationHelper
import com.qibla.prayertimes.model.City
import com.qibla.prayertimes.model.defaultCities
import com.qibla.prayertimes.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CityPickerScreen(
    selected: City,
    customCities: List<City>,
    onSelect: (City) -> Unit,
    onAddCity: (City) -> Unit,
    onRemoveCustom: (City) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val builtInCities = remember { defaultCities(context) }

    var query by remember { mutableStateOf("") }
    var onlineResults by remember { mutableStateOf<List<GeocodeResult>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    val geocoder = remember { GeocodingSearch() }

    var locating by remember { mutableStateOf(false) }
    var pendingNamePrompt by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var showManualDialog by remember { mutableStateOf(false) }

    // Launches the real (non-Compose) MapPickerActivity and receives the picked coordinates
    // back as an activity result — see MapPickerActivity.kt for why this is a plain Activity
    // rather than a Compose screen with an embedded WebView.
    val mapLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val lat = result.data?.getDoubleExtra(MapPickerActivity.EXTRA_PICKED_LAT, Double.NaN) ?: Double.NaN
            val lng = result.data?.getDoubleExtra(MapPickerActivity.EXTRA_PICKED_LNG, Double.NaN) ?: Double.NaN
            if (!lat.isNaN() && !lng.isNaN()) {
                pendingNamePrompt = lat to lng
            }
        }
    }

    val allLocalCities = remember(customCities) { customCities + builtInCities }
    val localMatches = remember(query, allLocalCities) {
        if (query.isBlank()) emptyList()
        else allLocalCities.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }

    LaunchedEffect(query) {
        if (query.trim().length < 2) {
            onlineResults = emptyList()
            searching = false
            return@LaunchedEffect
        }
        searching = true
        delay(600)
        onlineResults = geocoder.search(context, query.trim())
        searching = false
    }

    Column(modifier = Modifier.fillMaxSize().background(NightMid)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 24.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = AmberText)
            }
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.city_picker_title), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            // The four ways to add/choose a location.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MethodChip(
                    icon = Icons.Filled.MyLocation,
                    label = stringResource(R.string.city_current_location),
                    loading = locating,
                    modifier = Modifier.weight(1f)
                ) {
                    locating = true
                    scope.launch {
                        val city = LocationHelper(context).getCurrentCity()
                        locating = false
                        if (city != null) pendingNamePrompt = city.lat to city.lon
                    }
                }
                MethodChip(
                    icon = Icons.Filled.Map,
                    label = stringResource(R.string.city_choose_from_map),
                    modifier = Modifier.weight(1f)
                ) {
                    val intent = android.content.Intent(context, MapPickerActivity::class.java).apply {
                        putExtra(MapPickerActivity.EXTRA_INITIAL_LAT, selected.lat)
                        putExtra(MapPickerActivity.EXTRA_INITIAL_LNG, selected.lon)
                    }
                    mapLauncher.launch(intent)
                }
                MethodChip(
                    icon = Icons.Filled.EditLocationAlt,
                    label = stringResource(R.string.city_manual_entry),
                    modifier = Modifier.weight(1f)
                ) { showManualDialog = true }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.city_search_placeholder), color = AmberFaint, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = AmberMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AmberText,
                    unfocusedTextColor = AmberText,
                    focusedBorderColor = Brass,
                    unfocusedBorderColor = CardBorder,
                    cursorColor = Brass
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            if (query.isNotBlank()) {
                if (localMatches.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.city_local_results), color = AmberFaint, fontSize = 11.sp, modifier = Modifier.padding(vertical = 6.dp))
                    }
                    items(localMatches) { city ->
                        CityRow(
                            city = city,
                            isSelected = city.name == selected.name && city.lat == selected.lat,
                            onClick = { onSelect(city); onBack() },
                            onRemove = if (customCities.any { it.name == city.name && it.lat == city.lat }) {
                                { onRemoveCustom(city) }
                            } else null
                        )
                    }
                }
                item { Spacer(Modifier.height(10.dp)) }
                item {
                    Text(stringResource(R.string.city_online_search), color = AmberFaint, fontSize = 11.sp, modifier = Modifier.padding(vertical = 6.dp))
                }
                if (searching) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                            CircularProgressIndicator(color = Brass, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.city_searching), color = AmberMuted, fontSize = 12.sp)
                        }
                    }
                } else if (onlineResults.isEmpty() && query.trim().length >= 2) {
                    item {
                        Text(
                            stringResource(R.string.city_no_results),
                            color = AmberFaint,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                } else {
                    items(onlineResults) { result ->
                        OnlineResultRow(result = result) {
                            pendingNamePrompt = result.lat to result.lon
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            } else {
                if (customCities.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.city_my_cities), color = AmberFaint, fontSize = 11.sp, modifier = Modifier.padding(vertical = 6.dp))
                    }
                    items(customCities) { city ->
                        CityRow(
                            city = city,
                            isSelected = city.name == selected.name && city.lat == selected.lat,
                            onClick = { onSelect(city); onBack() },
                            onRemove = { onRemoveCustom(city) }
                        )
                    }
                    item { Spacer(Modifier.height(10.dp)) }
                }
                item {
                    Text(stringResource(R.string.city_default_cities), color = AmberFaint, fontSize = 11.sp, modifier = Modifier.padding(vertical = 6.dp))
                }
                items(builtInCities) { city ->
                    CityRow(
                        city = city,
                        isSelected = city.name == selected.name && city.lat == selected.lat,
                        onClick = { onSelect(city); onBack() },
                        onRemove = null
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    pendingNamePrompt?.let { (lat, lon) ->
        NamePromptDialog(
            onDismiss = { pendingNamePrompt = null },
            onConfirm = { name ->
                onAddCity(City(name, lat, lon))
                pendingNamePrompt = null
                onBack()
            }
        )
    }

    if (showManualDialog) {
        ManualEntryDialog(
            onDismiss = { showManualDialog = false },
            onConfirm = { name, lat, lon ->
                onAddCity(City(name, lat, lon))
                showManualDialog = false
                onBack()
            }
        )
    }
}

@Composable
private fun MethodChip(icon: ImageVector, label: String, loading: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loading) {
            CircularProgressIndicator(color = BrassLight, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
        } else {
            Icon(icon, contentDescription = null, tint = BrassLight, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = AmberMuted, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun CityRow(city: City, isSelected: Boolean, onClick: () -> Unit, onRemove: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color(0x4DC9A15C) else OverlayFaint)
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = if (isSelected) BrassLight else AmberFaint, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(city.name, color = if (isSelected) AmberText else AmberText.copy(alpha = 0.8f), fontSize = 14.sp)
        }
        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = RoseError.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun OnlineResultRow(result: GeocodeResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(OverlayFaint)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Public, contentDescription = null, tint = AmberFaint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(result.displayName, color = AmberText.copy(alpha = 0.85f), fontSize = 13.sp, maxLines = 2)
    }
}

@Composable
private fun NamePromptDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(NightSlate)
                .padding(18.dp)
        ) {
            Text(stringResource(R.string.city_name_prompt_title), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(stringResource(R.string.city_name_prompt_hint), color = AmberFaint) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AmberText,
                    unfocusedTextColor = AmberText,
                    focusedBorderColor = Brass,
                    unfocusedBorderColor = CardBorder,
                    cursorColor = Brass
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel_button), color = AmberMuted)
                }
                Spacer(Modifier.width(6.dp))
                Button(
                    onClick = { onConfirm(name.ifBlank { "?" }) },
                    colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = NightDeep)
                ) {
                    Text(stringResource(R.string.save_button), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ManualEntryDialog(onDismiss: () -> Unit, onConfirm: (name: String, lat: Double, lon: Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf("") }
    var lonText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val errorInvalid = stringResource(R.string.manual_error_invalid)
    val errorLatRange = stringResource(R.string.manual_error_lat_range)
    val errorLonRange = stringResource(R.string.manual_error_lon_range)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(NightSlate)
                .padding(18.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.city_manual_entry), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = AmberMuted)
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(stringResource(R.string.city_name_placeholder), color = AmberFaint) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AmberText, unfocusedTextColor = AmberText,
                    focusedBorderColor = Brass, unfocusedBorderColor = CardBorder, cursorColor = Brass
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = latText,
                    onValueChange = { latText = it; error = null },
                    label = { Text(stringResource(R.string.manual_lat_label), color = AmberFaint, fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AmberText, unfocusedTextColor = AmberText,
                        focusedBorderColor = Brass, unfocusedBorderColor = CardBorder, cursorColor = Brass
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = lonText,
                    onValueChange = { lonText = it; error = null },
                    label = { Text(stringResource(R.string.manual_lon_label), color = AmberFaint, fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AmberText, unfocusedTextColor = AmberText,
                        focusedBorderColor = Brass, unfocusedBorderColor = CardBorder, cursorColor = Brass
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            if (error != null) {
                Spacer(Modifier.height(6.dp))
                Text(error!!, color = RoseError, fontSize = 11.sp)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val lat = latText.trim().toDoubleOrNull()
                    val lon = lonText.trim().toDoubleOrNull()
                    when {
                        lat == null || lon == null -> error = errorInvalid
                        lat < -90.0 || lat > 90.0 -> error = errorLatRange
                        lon < -180.0 || lon > 180.0 -> error = errorLonRange
                        else -> onConfirm(name.ifBlank { "?" }, lat, lon)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = NightDeep),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_button), fontWeight = FontWeight.Bold)
            }
        }
    }
}
