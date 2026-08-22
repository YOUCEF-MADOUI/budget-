package com.budgetplusplus.feature.security

import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetplusplus.core.designsystem.components.BudgetTopAppBar

@Composable fun SecuritySettingsScreen(onBack:()->Unit,vm:SecurityViewModel=hiltViewModel()){val settings by vm.settings.collectAsStateWithLifecycle();var pinDialog by remember{mutableStateOf(false)};var disableDialog by remember{mutableStateOf(false)};val context=LocalContext.current;val authenticators=BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK;val biometricAvailable=BiometricManager.from(context).canAuthenticate(authenticators)==BiometricManager.BIOMETRIC_SUCCESS
 Scaffold(topBar={BudgetTopAppBar(stringResource(R.string.security_settings_title),onBackClick=onBack)}){p->Column(Modifier.padding(p).padding(20.dp).fillMaxSize(),verticalArrangement=Arrangement.spacedBy(16.dp)){Text(stringResource(R.string.security_pin_section),style=MaterialTheme.typography.titleMedium);if(settings.pinEnabled){Button({pinDialog=true},Modifier.fillMaxWidth()){Text(stringResource(R.string.security_change_pin))};OutlinedButton({disableDialog=true},Modifier.fillMaxWidth()){Text(stringResource(R.string.security_disable_pin))}}else Button({pinDialog=true},Modifier.fillMaxWidth()){Text(stringResource(R.string.security_enable_pin))}
  SettingSwitch(stringResource(R.string.security_biometric_setting),stringResource(if(biometricAvailable)R.string.security_biometric_available else R.string.security_biometric_unavailable),settings.biometricEnabled,{vm.biometric(it)},settings.pinEnabled&&biometricAvailable)
  Text(stringResource(R.string.security_delay),style=MaterialTheme.typography.titleMedium);LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){items(listOf(0L,30L,60L,120L,300L)){value->FilterChip(settings.lockDelaySeconds==value,{vm.delay(value)},{Text(delayLabel(value))})}}
  SettingSwitch(stringResource(R.string.security_protect_screen),stringResource(R.string.security_protect_screen_message),settings.protectScreen,vm::protect,true)
  Text(stringResource(R.string.security_storage_info),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
 }}
 if(pinDialog)PinSetupDialog({pinDialog=false}){pin->vm.setPin(pin){pinDialog=false}}
 if(disableDialog)AlertDialog(onDismissRequest={disableDialog=false},title={Text(stringResource(R.string.security_disable_title))},text={Text(stringResource(R.string.security_disable_warning))},confirmButton={TextButton({disableDialog=false;vm.disable()}){Text(stringResource(R.string.security_disable_pin))}},dismissButton={TextButton({disableDialog=false}){Text(stringResource(R.string.security_cancel))}})
}
@Composable private fun SettingSwitch(title:String,message:String,checked:Boolean,change:(Boolean)->Unit,enabled:Boolean){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(title);Text(message,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Switch(checked,change,enabled=enabled)}}
@Composable private fun PinSetupDialog(dismiss:()->Unit,save:(String)->Unit){var pin by remember{mutableStateOf("")};var confirm by remember{mutableStateOf("")};val valid=pin.length in 4..8&&pin.all(Char::isDigit)&&pin==confirm;AlertDialog(onDismissRequest=dismiss,title={Text(stringResource(R.string.security_set_pin))},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(pin,{if(it.length<=8&&it.all(Char::isDigit))pin=it},label={Text(stringResource(R.string.security_new_pin))},visualTransformation=PasswordVisualTransformation());OutlinedTextField(confirm,{if(it.length<=8&&it.all(Char::isDigit))confirm=it},label={Text(stringResource(R.string.security_confirm_pin))},visualTransformation=PasswordVisualTransformation());Text(stringResource(R.string.security_pin_help),style=MaterialTheme.typography.bodySmall)}},confirmButton={TextButton({save(pin);pin="";confirm=""},enabled=valid){Text(stringResource(R.string.security_save))}},dismissButton={TextButton(dismiss){Text(stringResource(R.string.security_cancel))}})}
@Composable private fun delayLabel(value:Long)=stringResource(when(value){0L->R.string.security_delay_immediate;30L->R.string.security_delay_30;60L->R.string.security_delay_60;120L->R.string.security_delay_120;else->R.string.security_delay_300})
