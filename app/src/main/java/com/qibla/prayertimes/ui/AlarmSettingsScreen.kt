package com.qibla.prayertimes.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qibla.prayertimes.R
import com.qibla.prayertimes.alarm.AdhanPrayer
import com.qibla.prayertimes.alarm.AlarmPrefs
import com.qibla.prayertimes.alarm.AlarmScheduler
import com.qibla.prayertimes.alarm.REMINDER_OPTIONS
import com.qibla.prayertimes.data.WidgetDataStore
import com.qibla.prayertimes.ui.theme.*
import com.qibla.prayertimes.work.PrayerTimesWorker

@Composable
fun AlarmSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { AlarmPrefs(context) }
    var refreshTick by remember { mutableStateOf(0) }

    val soundPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        val prayerName = result.data?.getStringExtra(EXTRA_TARGET_PRAYER)
        if (uri != null && prayerName != null) {
            val prayer = AdhanPrayer.entries.firstOrNull { it.name == prayerName }
            if (prayer != null) {
                prefs.setSoundUri(prayer, uri)
                refreshTick++
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val prayer = pendingFilePickPrayer
        if (uri != null && prayer != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { /* some providers don't support persistable permissions */ }
            prefs.setSoundUri(prayer, uri)
            refreshTick++
        }
        pendingFilePickPrayer = null
    }

    fun rescheduleFromCache() {
        val store = WidgetDataStore(context)
        val snapshot = store.load()
        if (snapshot != null && store.isFreshToday()) {
            AlarmScheduler.scheduleToday(context, snapshot.timings)
        } else {
            PrayerTimesWorker.runOnce(context)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightMid)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 28.dp, bottom = 40.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = AmberText)
                }
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.alarms_title), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.alarms_subtitle),
                color = AmberMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    Spacer(Modifier.height(14.dp))
                    ExactAlarmPermissionBanner(context)
                }
            }

            Spacer(Modifier.height(18.dp))

            key(refreshTick) {
                AdhanPrayer.entries.forEach { prayer ->
                    PrayerAlarmRow(
                        prayer = prayer,
                        prefs = prefs,
                        onToggle = { enabled ->
                            prefs.setEnabled(prayer, enabled)
                            refreshTick++
                            rescheduleFromCache()
                        },
                        onPickDeviceSound = {
                            val title = context.getString(R.string.choose_sound_for, prayer.label(context))
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, title)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, prefs.soundUri(prayer))
                                putExtra(EXTRA_TARGET_PRAYER, prayer.name)
                            }
                            soundPickerLauncher.launch(intent)
                        },
                        onPickFile = {
                            pendingFilePickPrayer = prayer
                            filePickerLauncher.launch(arrayOf("audio/*"))
                        },
                        onPickReminder = { minutes ->
                            prefs.setReminderMinutes(prayer, minutes)
                            refreshTick++
                            rescheduleFromCache()
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

// Simple hand-off between the "choose file" click and the OpenDocument callback above.
private var pendingFilePickPrayer: AdhanPrayer? = null
private const val EXTRA_TARGET_PRAYER = "extra_target_prayer"

@Composable
private fun PrayerAlarmRow(
    prayer: AdhanPrayer,
    prefs: AlarmPrefs,
    onToggle: (Boolean) -> Unit,
    onPickDeviceSound: () -> Unit,
    onPickFile: () -> Unit,
    onPickReminder: (Int) -> Unit
) {
    val context = LocalContext.current
    var showSoundMenu by remember { mutableStateOf(false) }
    var showReminderMenu by remember { mutableStateOf(false) }
    val enabled = prefs.isEnabled(prayer)
    val reminderMinutes = prefs.reminderMinutes(prayer)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(prayer.label(context), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BrassLight,
                    checkedTrackColor = Brass.copy(alpha = 0.5f),
                    uncheckedThumbColor = AmberFaint,
                    uncheckedTrackColor = OverlayStrong
                )
            )
        }
        if (enabled) {
            Spacer(Modifier.height(10.dp))
            Box {
                OutlinedButton(
                    onClick = { showSoundMenu = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrassLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Brass.copy(alpha = 0.35f))
                ) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.choose_adhan_sound), fontSize = 12.sp)
                }
                DropdownMenu(expanded = showSoundMenu, onDismissRequest = { showSoundMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.device_ringtones)) },
                        onClick = { showSoundMenu = false; onPickDeviceSound() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.audio_file)) },
                        onClick = { showSoundMenu = false; onPickFile() }
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = AmberMuted, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.reminder_before_label), color = AmberMuted, fontSize = 12.sp)
            Spacer(Modifier.width(6.dp))
            Box {
                TextButton(onClick = { showReminderMenu = true }) {
                    Text(
                        if (reminderMinutes == 0) stringResource(R.string.reminder_off)
                        else stringResource(R.string.reminder_minutes_before, reminderMinutes),
                        color = BrassLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                DropdownMenu(expanded = showReminderMenu, onDismissRequest = { showReminderMenu = false }) {
                    REMINDER_OPTIONS.forEach { minutes ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (minutes == 0) stringResource(R.string.reminder_off)
                                    else stringResource(R.string.reminder_minutes_before, minutes)
                                )
                            },
                            onClick = { showReminderMenu = false; onPickReminder(minutes) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExactAlarmPermissionBanner(context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x33E5A3A3))
            .padding(12.dp)
    ) {
        Text(
            stringResource(R.string.exact_alarm_banner),
            color = AmberText,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }) {
            Text(stringResource(R.string.open_settings), color = BrassLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
