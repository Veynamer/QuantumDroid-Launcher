/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.compose.components.preferences

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.launcher3.R
import com.saggitt.omega.compose.navigation.LocalNavController
import com.saggitt.omega.compose.navigation.subRoute
import com.saggitt.omega.preferences.BooleanPref
import com.saggitt.omega.preferences.ColorIntPref
import com.saggitt.omega.preferences.FloatPref
import com.saggitt.omega.preferences.IntSelectionPref
import com.saggitt.omega.preferences.IntentLauncherPref
import com.saggitt.omega.preferences.NavigationPref
import com.saggitt.omega.preferences.StringMultiSelectionPref
import com.saggitt.omega.preferences.StringPref
import com.saggitt.omega.preferences.StringSelectionPref
import com.saggitt.omega.preferences.StringSetPref
import com.saggitt.omega.preferences.StringTextPref
import com.saggitt.omega.util.addIf
import kotlinx.coroutines.launch

@Composable
fun BasePreference(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    summary: String? = null,
    isEnabled: Boolean = true,
    index: Int = 1,
    groupSize: Int = 1,
    startWidget: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
    bottomWidget: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val base = index.toFloat() / groupSize
    val rank = (index + 1f) / groupSize

    Column(
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (base == 0f) 16.dp else 6.dp,
                    topEnd = if (base == 0f) 16.dp else 6.dp,
                    bottomStart = if (rank == 1f) 16.dp else 6.dp,
                    bottomEnd = if (rank == 1f) 16.dp else 6.dp
                )
            )
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation((rank * 24).dp))
            .heightIn(min = 64.dp)
            .addIf(onClick != null) {
                clickable(enabled = isEnabled, onClick = onClick!!)
            }, verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            startWidget?.let {
                startWidget()
                Spacer(modifier = Modifier.requiredWidth(8.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .addIf(!isEnabled) {
                        alpha(0.3f)
                    }
            ) {
                Text(
                    text = stringResource(id = titleId),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp
                )
                if (summaryId != -1 || summary != null) {
                    Text(
                        text = summary ?: stringResource(id = summaryId),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                bottomWidget?.let {
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    bottomWidget()
                }
            }
            endWidget?.let {
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                endWidget()
            }
        }
    }
}

@Composable
fun StringPreference(
    modifier: Modifier = Modifier,
    pref: StringPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            pref.onClick?.invoke()
        }
    )
}

@Composable
fun NavigationPreference(
    modifier: Modifier = Modifier,
    pref: NavigationPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
) {
    val navController = LocalNavController.current
    val route = subRoute(pref.navRoute)
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            if (pref.navRoute != "") {
                Log.d("NavigationPreference", "Navigating to $route")
                navController.navigate(route)
            } else {
                pref.onClick?.invoke()
            }
        }
    )
}

@Composable
fun ColorIntPreference(
    modifier: Modifier = Modifier,
    pref: ColorIntPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
) {
    val navController = LocalNavController.current
    val route = subRoute(pref.navRoute)

    val currentColor by remember(pref) { mutableStateOf(pref.getValue()) }

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            if (pref.navRoute != "") {
                navController.navigate(route)
            }
        },
        endWidget = {
            Canvas(
                modifier = Modifier
                    .size(40.dp),
                onDraw = {
                    drawCircle(color = Color.Black, style = Stroke(width = 1.dp.toPx()))
                    drawCircle(color = Color(currentColor))
                }
            )
        }
    )
}


@Composable
fun SeekBarPreference(
    modifier: Modifier = Modifier,
    pref: FloatPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onValueChange: ((Float) -> Unit) = {},
) {
    var currentValue by remember(pref) { mutableStateOf(pref.getValue()) }

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        bottomWidget = {
            Row {
                Text(
                    text = pref.specialOutputs(currentValue),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.widthIn(min = 52.dp)
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                Slider(
                    modifier = Modifier
                        .requiredHeight(24.dp)
                        .weight(1f),
                    value = currentValue,
                    valueRange = pref.minValue..pref.maxValue,
                    onValueChange = { currentValue = it },
                    steps = pref.steps,
                    onValueChangeFinished = {
                        pref.setValue(currentValue)
                        onValueChange(currentValue)
                    },
                    enabled = isEnabled
                )
            }
        }
    )
}

@Composable
fun StringSetPreference(
    modifier: Modifier = Modifier,
    pref: StringSetPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
) {
    val navController = LocalNavController.current
    val route = subRoute(pref.navRoute)
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            if (pref.navRoute != "") {
                navController.navigate(route)
            }
        }
    )
}

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    pref: BooleanPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit) = {},
) {
    val (checked, check) = remember(pref) { mutableStateOf(pref.getValue()) }
    val coroutineScope = rememberCoroutineScope()

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            onCheckedChange(!checked)
            check(!checked)
            coroutineScope.launch { pref.set(!checked) }
        },
        endWidget = {
            Switch(
                modifier = Modifier
                    .height(24.dp),
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                    check(it)
                    coroutineScope.launch { pref.set(it) }
                },
                enabled = isEnabled,
            )
        }
    )
}

@Composable
fun IntSelectionPreference(
    modifier: Modifier = Modifier,
    pref: IntSelectionPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    val value = pref.get().collectAsState(initial = pref.defaultValue)
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.entries[value.value]?.let { stringResource(id = it) },
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun StringSelectionPreference(
    modifier: Modifier = Modifier,
    pref: StringSelectionPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.entries[pref.getValue()],
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun StringMultiSelectionPreference(
    modifier: Modifier = Modifier,
    pref: StringMultiSelectionPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.entries
            .filter { pref.getValue().contains(it.key) }
            .values.let {
                it.map { stringResource(id = it) }.joinToString(separator = ", ")
            },
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun StringTextPreference(
    modifier: Modifier = Modifier,
    pref: StringTextPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.getValue(),
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun PagePreference(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
    @DrawableRes iconId: Int = -1,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    route: String
) {
    val navController = LocalNavController.current
    val destination = subRoute(route)
    BasePreference(
        modifier = modifier,
        titleId = titleId,
        startWidget =
        if (iconId != -1) {
            {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = stringResource(id = titleId),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = { navController.navigate(destination) }
    )
}

@Composable
fun IntentLauncherPreference(
    modifier: Modifier = Modifier,
    pref: IntentLauncherPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    val summaryId = if (pref.getValue()) R.string.notification_dots_desc_on
    else R.string.notification_dots_desc_off

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}