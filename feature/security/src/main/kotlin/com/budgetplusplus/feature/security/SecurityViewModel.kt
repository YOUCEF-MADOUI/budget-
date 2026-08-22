package com.budgetplusplus.feature.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetplusplus.core.model.*
import com.budgetplusplus.domain.repository.SecurityRepository
import com.budgetplusplus.domain.security.AppSecurityPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel class SecurityViewModel @Inject constructor(private val repository:SecurityRepository):ViewModel(){
 val settings=repository.observeSettings().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),AppSecuritySettings());private val _locked=MutableStateFlow(false);val locked=_locked.asStateFlow();private val _result=MutableStateFlow<PinVerificationResult?>(null);val result=_result.asStateFlow();private val _erased=MutableStateFlow(false);val erased=_erased.asStateFlow();private var loaded=false;private var backgroundAt:Long?=null
 init{viewModelScope.launch{settings.collect{if(!loaded){loaded=true;if(it.pinEnabled)_locked.value=true};if(!it.pinEnabled)_locked.value=false}}}
 fun background(now:Long=System.currentTimeMillis()){backgroundAt=now}
 fun foreground(now:Long=System.currentTimeMillis()){val start=backgroundAt;val value=settings.value;if(value.pinEnabled&&start!=null&&AppSecurityPolicy.shouldLockAfterBackground(now-start,value.lockDelaySeconds))_locked.value=true;backgroundAt=null}
 fun verify(pin:String)=viewModelScope.launch{val value=repository.verifyPin(pin.toCharArray());_result.value=value;if(value==PinVerificationResult.Success)_locked.value=false}
 fun biometricSuccess(){viewModelScope.launch{repository.recordBiometricSuccess();_result.value=null;_locked.value=false}}
 fun setPin(pin:String,done:()->Unit)=viewModelScope.launch{runCatching{repository.setPin(pin.toCharArray())}.onSuccess{done()}}
 fun biometric(value:Boolean)=viewModelScope.launch{repository.setBiometricEnabled(value)}
 fun delay(value:Long)=viewModelScope.launch{repository.setLockDelay(value)}
 fun protect(value:Boolean)=viewModelScope.launch{repository.setProtectScreen(value)}
 fun disable()=viewModelScope.launch{repository.disablePin()}
 fun erase()=viewModelScope.launch{repository.eraseAllDataAfterForgottenPin();_erased.value=true;_locked.value=false}
}
