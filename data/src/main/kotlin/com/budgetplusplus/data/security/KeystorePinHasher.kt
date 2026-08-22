package com.budgetplusplus.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

class KeystorePinHasher {
 private val alias="budgetplusplus.pin.hmac.v1"
 fun hash(pin:CharArray,salt:ByteArray):ByteArray{val bytes=ByteArray(pin.size){pin[it].code.toByte()};return try{Mac.getInstance("HmacSHA256").run{init(key());update(salt);doFinal(bytes)}}finally{bytes.fill(0);pin.fill('\u0000')}}
 fun deleteKey(){val store=KeyStore.getInstance("AndroidKeyStore").apply{load(null)};if(store.containsAlias(alias))store.deleteEntry(alias)}
 private fun key():SecretKey{val store=KeyStore.getInstance("AndroidKeyStore").apply{load(null)};(store.getKey(alias,null)as?SecretKey)?.let{return it};return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256,"AndroidKeyStore").run{init(KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY).setDigests(KeyProperties.DIGEST_SHA256).build());generateKey()}}
}
