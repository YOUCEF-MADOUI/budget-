package com.budgetplusplus.feature.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.budgetplusplus.core.designsystem.components.*
import com.budgetplusplus.core.model.BackupPreview
import com.budgetplusplus.core.model.CsvDataset
import com.budgetplusplus.domain.backup.InvalidBackupException
import com.budgetplusplus.domain.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BackupState{data object Idle:BackupState;data object Working:BackupState;data class Preview(val value:BackupPreview,val uri:String):BackupState;data object Saved:BackupState;data object Exported:BackupState;data object Restored:BackupState;data class Error(val kind:ErrorKind):BackupState}
enum class ErrorKind{INVALID_OR_PASSWORD,IO,UNKNOWN}
@HiltViewModel class BackupViewModel @Inject constructor(private val repository:BackupRepository):ViewModel(){private val _state=MutableStateFlow<BackupState>(BackupState.Idle);val state=_state.asStateFlow()
 fun backup(uri:String,password:String)=run{_state.value=BackupState.Working;viewModelScope.launch{execute{repository.createBackup(uri,password.toCharArray());BackupState.Saved}}}
 fun preview(uri:String,password:String)=run{_state.value=BackupState.Working;viewModelScope.launch{execute{BackupState.Preview(repository.previewBackup(uri,password.toCharArray()),uri)}}}
 fun restore(uri:String,password:String)=run{_state.value=BackupState.Working;viewModelScope.launch{execute{repository.restoreBackup(uri,password.toCharArray());BackupState.Restored}}}
 fun export(uri:String,dataset:CsvDataset)=run{_state.value=BackupState.Working;viewModelScope.launch{execute{repository.exportCsv(uri,dataset);BackupState.Exported}}}
 fun idle(){_state.value=BackupState.Idle}
 private suspend fun execute(block:suspend()->BackupState){_state.value=try{block()}catch(_:InvalidBackupException){BackupState.Error(ErrorKind.INVALID_OR_PASSWORD)}catch(_:IOException){BackupState.Error(ErrorKind.IO)}catch(_:Exception){BackupState.Error(ErrorKind.UNKNOWN)}}
}
private enum class PasswordAction{BACKUP,PREVIEW,RESTORE}

@Composable fun BackupScreen(onBack:()->Unit,vm:BackupViewModel=hiltViewModel()){
 val state by vm.state.collectAsStateWithLifecycle();var passwordAction by remember{mutableStateOf<PasswordAction?>(null)};var selectedUri by remember{mutableStateOf<String?>(null)};var pendingDataset by remember{mutableStateOf<CsvDataset?>(null)}
 val backupFilename=stringResource(R.string.backup_filename,LocalDate.now().toString())
 val createBackup=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")){uri->if(uri!=null){selectedUri=uri.toString();passwordAction=PasswordAction.BACKUP}}
 val openBackup=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null){selectedUri=uri.toString();passwordAction=PasswordAction.PREVIEW}}
 val createCsv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){uri->val dataset=pendingDataset;if(uri!=null&&dataset!=null)vm.export(uri.toString(),dataset);pendingDataset=null}
 Scaffold(topBar={BudgetTopAppBar(stringResource(R.string.backup_title),onBackClick=onBack)}){p->Column(Modifier.padding(p).padding(20.dp).fillMaxSize(),verticalArrangement=Arrangement.spacedBy(16.dp)){
  Text(stringResource(R.string.backup_security),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
  Button({createBackup.launch(backupFilename)},Modifier.fillMaxWidth()){Text(stringResource(R.string.backup_create))}
  OutlinedButton({openBackup.launch(arrayOf("application/octet-stream","application/zip","*/*"))},Modifier.fillMaxWidth()){Text(stringResource(R.string.backup_restore))}
  HorizontalDivider();Text(stringResource(R.string.backup_csv_title),style=MaterialTheme.typography.titleMedium)
  CsvDataset.entries.forEach{dataset->val fileName=csvName(dataset);OutlinedButton({pendingDataset=dataset;createCsv.launch(fileName)},Modifier.fillMaxWidth()){Text(datasetName(dataset))}}
  when(val current=state){BackupState.Working->BudgetLoadingIndicator();BackupState.Saved->StatusCard(stringResource(R.string.backup_saved),vm::idle);BackupState.Exported->StatusCard(stringResource(R.string.backup_exported),vm::idle);BackupState.Restored->StatusCard(stringResource(R.string.backup_restored_restart),{});is BackupState.Error->StatusCard(errorText(current.kind),vm::idle);else->Unit}
 }}
 passwordAction?.let{action->PasswordDialog(title=stringResource(when(action){PasswordAction.BACKUP->R.string.backup_password_create;PasswordAction.PREVIEW->R.string.backup_password_open;PasswordAction.RESTORE->R.string.backup_password_confirm}),dismiss={passwordAction=null;selectedUri=null}){password->val uri=selectedUri;passwordAction=null;if(uri!=null)when(action){PasswordAction.BACKUP->vm.backup(uri,password);PasswordAction.PREVIEW->vm.preview(uri,password);PasswordAction.RESTORE->vm.restore(uri,password)}}}
 (state as? BackupState.Preview)?.let{preview->PreviewDialog(preview.value,{vm.idle()},{selectedUri=preview.uri;passwordAction=PasswordAction.RESTORE})}
}
@Composable private fun PasswordDialog(title:String,dismiss:()->Unit,confirm:(String)->Unit){var value by remember{mutableStateOf("")};var visible by remember{mutableStateOf(false)};AlertDialog(onDismissRequest=dismiss,title={Text(title)},text={OutlinedTextField(value,{value=it},label={Text(stringResource(R.string.backup_password))},visualTransformation=PasswordVisualTransformation(),singleLine=true,supportingText={Text(stringResource(R.string.backup_password_help))})},confirmButton={TextButton({confirm(value);value=""},enabled=value.length>=8){Text(stringResource(R.string.backup_continue))}},dismissButton={TextButton(dismiss){Text(stringResource(R.string.backup_cancel))}})}
@Composable private fun PreviewDialog(v:BackupPreview,dismiss:()->Unit,restore:()->Unit){BudgetConfirmationDialog(stringResource(R.string.backup_preview_title),stringResource(R.string.backup_preview_message,v.schemaVersion,v.accounts,v.transactions,v.categories,v.budgets),restore,dismiss,confirmText=stringResource(R.string.backup_replace),destructive=true)}
@Composable private fun StatusCard(text:String,dismiss:()->Unit){ElevatedCard(Modifier.fillMaxWidth()){Row(Modifier.padding(16.dp).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(text,Modifier.weight(1f));TextButton(dismiss){Text(stringResource(R.string.backup_ok))}}}}
@Composable private fun errorText(v:ErrorKind)=stringResource(when(v){ErrorKind.INVALID_OR_PASSWORD->R.string.backup_invalid;ErrorKind.IO->R.string.backup_io_error;ErrorKind.UNKNOWN->R.string.backup_unknown_error})
@Composable private fun datasetName(v:CsvDataset)=stringResource(when(v){CsvDataset.ACCOUNTS->R.string.backup_csv_accounts;CsvDataset.TRANSACTIONS->R.string.backup_csv_transactions;CsvDataset.CATEGORIES->R.string.backup_csv_categories;CsvDataset.BUDGETS->R.string.backup_csv_budgets})
@Composable private fun csvName(v:CsvDataset)=stringResource(R.string.backup_csv_filename, stringResource(when(v){CsvDataset.ACCOUNTS->R.string.backup_file_accounts;CsvDataset.TRANSACTIONS->R.string.backup_file_transactions;CsvDataset.CATEGORIES->R.string.backup_file_categories;CsvDataset.BUDGETS->R.string.backup_file_budgets}), LocalDate.now().toString())
