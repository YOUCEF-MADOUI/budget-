package com.budgetplusplus.domain.backup

import java.io.*
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class InvalidBackupException(message:String,cause:Throwable?=null):Exception(message,cause)
data class DecryptedBackup(val schemaVersion:Int,val createdAt:Long,val databaseBytes:ByteArray,val formatVersion:Int)

object BackupCrypto {
 private val MAGIC="BPPBACKUP".toByteArray(Charsets.US_ASCII);private const val CURRENT=3;private const val ITERATIONS=210_000;private const val LEGACY_ITERATIONS=120_000;private const val PAYLOAD=0x42505032
 fun encrypt(database:ByteArray,schemaVersion:Int,password:CharArray,output:OutputStream,formatVersion:Int=CURRENT,random:SecureRandom=SecureRandom()){
  require(password.isNotEmpty());require(formatVersion in 1..CURRENT);val salt=ByteArray(16).also(random::nextBytes);val iv=ByteArray(12).also(random::nextBytes);val iterations=if(formatVersion==1)LEGACY_ITERATIONS else ITERATIONS;val data=DataOutputStream(output);data.write(MAGIC);data.writeInt(formatVersion);data.writeInt(iterations);data.writeInt(salt.size);data.write(salt);data.writeInt(iv.size);data.write(iv);data.flush();val cipher=cipher(Cipher.ENCRYPT_MODE,password,salt,iv,iterations,formatVersion);CipherOutputStream(output,cipher).use{encrypted->val plain=DataOutputStream(encrypted);if(formatVersion==1){plain.writeInt(schemaVersion);plain.writeLong(database.size.toLong());plain.write(database)}else{plain.writeInt(PAYLOAD);plain.writeInt(schemaVersion);plain.writeLong(System.currentTimeMillis());val hash=MessageDigest.getInstance("SHA-256").digest(database);plain.writeInt(hash.size);plain.write(hash);plain.writeLong(database.size.toLong());plain.write(database)};plain.flush()}
 }
 fun decrypt(input:InputStream,password:CharArray,maxBytes:Long=512L*1024*1024):DecryptedBackup=try{
  val data=DataInputStream(BufferedInputStream(input));val magic=ByteArray(MAGIC.size);data.readFully(magic);if(!magic.contentEquals(MAGIC))throw InvalidBackupException("magic");val version=data.readInt();if(version !in 1..CURRENT)throw InvalidBackupException("version");val iterations=data.readInt();if(iterations !in 100_000..1_000_000)throw InvalidBackupException("iterations");val salt=ByteArray(data.readInt().takeIf{it in 16..64}?:throw InvalidBackupException("salt"));data.readFully(salt);val iv=ByteArray(data.readInt().takeIf{it in 12..32}?:throw InvalidBackupException("iv"));data.readFully(iv);val cipher=cipher(Cipher.DECRYPT_MODE,password,salt,iv,iterations,version);DataInputStream(CipherInputStream(data,cipher)).use{plain->if(version==1){val schema=plain.readInt();val length=plain.readLong();DecryptedBackup(schema,0,readBytes(plain,length,maxBytes),version)}else{if(plain.readInt()!=PAYLOAD)throw InvalidBackupException("payload");val schema=plain.readInt();val created=plain.readLong();val hash=ByteArray(plain.readInt().takeIf{it==32}?:throw InvalidBackupException("hash"));plain.readFully(hash);val length=plain.readLong();val bytes=readBytes(plain,length,maxBytes);if(!MessageDigest.isEqual(hash,MessageDigest.getInstance("SHA-256").digest(bytes)))throw InvalidBackupException("integrity");DecryptedBackup(schema,created,bytes,version)}}
 }catch(e:InvalidBackupException){throw e}catch(e:Exception){throw InvalidBackupException("decrypt",e)}
 private fun readBytes(input:DataInputStream,length:Long,max:Long):ByteArray{if(length<0||length>max||length>Int.MAX_VALUE)throw InvalidBackupException("size");return ByteArray(length.toInt()).also(input::readFully)}
 private fun cipher(mode:Int,password:CharArray,salt:ByteArray,iv:ByteArray,iterations:Int,version:Int):Cipher{val spec=PBEKeySpec(password,salt,iterations,256);val encoded=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded;spec.clearPassword();val cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(mode,SecretKeySpec(encoded,"AES"),GCMParameterSpec(128,iv));encoded.fill(0);cipher.updateAAD(ByteBuffer.allocate(MAGIC.size+4).put(MAGIC).putInt(version).array());return cipher}
}
