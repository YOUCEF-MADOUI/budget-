package com.budgetplusplus.feature.security

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetplusplus.core.model.PinVerificationResult
import kotlinx.coroutines.delay

@Composable fun SecurityGate(vm:SecurityViewModel=hiltViewModel(),content: @Composable () -> Unit){
 val settings by vm.settings.collectAsStateWithLifecycle();val locked by vm.locked.collectAsStateWithLifecycle();val erased by vm.erased.collectAsStateWithLifecycle();val context=LocalContext.current;val activity=context.activity()
 DisposableEffect(Unit){val observer=object:DefaultLifecycleObserver{override fun onStop(owner:LifecycleOwner){vm.background()};override fun onStart(owner:LifecycleOwner){vm.foreground()}};ProcessLifecycleOwner.get().lifecycle.addObserver(observer);onDispose{ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)}}
 LaunchedEffect(settings.protectScreen,activity){activity?.window?.let{if(settings.protectScreen)it.addFlags(WindowManager.LayoutParams.FLAG_SECURE)else it.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)}}
 when{erased->ErasedScreen();settings.pinEnabled&&locked->LockScreen(settings.biometricEnabled,vm,activity as? FragmentActivity);else->content()}
}
@Composable private fun LockScreen(biometric:Boolean,vm:SecurityViewModel,activity:FragmentActivity?){val result by vm.result.collectAsStateWithLifecycle();var pin by remember{mutableStateOf("")};var warning by remember{mutableStateOf(false)};var confirmErase by remember{mutableStateOf(false)};var now by remember{mutableLongStateOf(System.currentTimeMillis())};val retryAt=when(val r=result){is PinVerificationResult.Blocked->r.retryAtEpochMillis;is PinVerificationResult.Invalid->r.retryAtEpochMillis;else->0};LaunchedEffect(retryAt){while(retryAt>System.currentTimeMillis()){now=System.currentTimeMillis();delay(1000)}};val blocked=retryAt>now
 Box(Modifier.fillMaxSize().padding(24.dp),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)){Text(stringResource(R.string.security_locked),style=MaterialTheme.typography.headlineMedium);Text(stringResource(R.string.security_locked_message));OutlinedTextField(pin,{if(it.length<=8&&it.all(Char::isDigit))pin=it},label={Text(stringResource(R.string.security_pin))},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),enabled=!blocked,singleLine=true);when(val r=result){is PinVerificationResult.Invalid->Text(if(blocked)stringResource(R.string.security_retry_seconds,((retryAt-now+999)/1000).coerceAtLeast(1))else stringResource(R.string.security_invalid_attempts,r.attempts),color=MaterialTheme.colorScheme.error);is PinVerificationResult.Blocked->Text(stringResource(R.string.security_retry_seconds,((retryAt-now+999)/1000).coerceAtLeast(1)),color=MaterialTheme.colorScheme.error);else->Unit};Button({vm.verify(pin);pin=""},enabled=pin.length in 4..8&&!blocked,modifier=Modifier.fillMaxWidth()){Text(stringResource(R.string.security_unlock))};if(biometric&&activity!=null)OutlinedButton({showBiometric(activity,vm)},Modifier.fillMaxWidth()){Text(stringResource(R.string.security_biometric))};TextButton({warning=true}){Text(stringResource(R.string.security_forgot))}}}
 if(warning)AlertDialog(onDismissRequest={warning=false},title={Text(stringResource(R.string.security_forgot_title))},text={Text(stringResource(R.string.security_forgot_warning))},confirmButton={TextButton({warning=false;confirmErase=true}){Text(stringResource(R.string.security_understand))}},dismissButton={TextButton({warning=false}){Text(stringResource(R.string.security_cancel))}})
 if(confirmErase)AlertDialog(onDismissRequest={confirmErase=false},title={Text(stringResource(R.string.security_erase_title))},text={Text(stringResource(R.string.security_erase_warning))},confirmButton={TextButton({confirmErase=false;vm.erase()}){Text(stringResource(R.string.security_erase_all),color=MaterialTheme.colorScheme.error)}},dismissButton={TextButton({confirmErase=false}){Text(stringResource(R.string.security_cancel))}})
}
private fun showBiometric(activity:FragmentActivity,vm:SecurityViewModel){val allowed=BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK;if(BiometricManager.from(activity).canAuthenticate(allowed)!=BiometricManager.BIOMETRIC_SUCCESS)return;val prompt=BiometricPrompt(activity,{command->activity.runOnUiThread(command)},object:BiometricPrompt.AuthenticationCallback(){override fun onAuthenticationSucceeded(result:BiometricPrompt.AuthenticationResult){vm.biometricSuccess()}});prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle(activity.getString(R.string.security_biometric_title)).setSubtitle(activity.getString(R.string.security_biometric_subtitle)).setAllowedAuthenticators(allowed).setNegativeButtonText(activity.getString(R.string.security_use_pin)).build())}
@Composable private fun ErasedScreen(){Box(Modifier.fillMaxSize().padding(24.dp),contentAlignment=Alignment.Center){Text(stringResource(R.string.security_erased_restart),style=MaterialTheme.typography.titleLarge)}}
private tailrec fun Context.activity():Activity?=when(this){is Activity->this;is ContextWrapper->baseContext.activity();else->null}
