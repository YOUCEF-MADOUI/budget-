package com.budgetplusplus.core.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.budgetplusplus.core.model.CropMode
import java.io.File

@Composable fun MediaPickerButton(onSelected:(String,CropMode)->Unit,modifier:Modifier=Modifier){val context=LocalContext.current;var pending by remember{mutableStateOf<Uri?>(null)};var cameraUri by remember{mutableStateOf<Uri?>(null)};val gallery=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->if(uri!=null)pending=uri};val camera=rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){ok->if(ok)pending=cameraUri};Row(modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton({gallery.launch("image/*")},Modifier.weight(1f)){Text(stringResource(R.string.media_gallery))};OutlinedButton({val file=File.createTempFile("capture-",".jpg",context.cacheDir);cameraUri=FileProvider.getUriForFile(context,"${context.packageName}.files",file);camera.launch(requireNotNull(cameraUri))},Modifier.weight(1f)){Text(stringResource(R.string.media_camera))}};pending?.let{uri->AlertDialog(onDismissRequest={pending=null},title={Text(stringResource(R.string.media_crop_title))},text={Text(stringResource(R.string.media_crop_message))},confirmButton={TextButton({pending=null;onSelected(uri.toString(),CropMode.SQUARE)}){Text(stringResource(R.string.media_crop_square))}},dismissButton={TextButton({pending=null;onSelected(uri.toString(),CropMode.ORIGINAL)}){Text(stringResource(R.string.media_keep_original))}})}}
