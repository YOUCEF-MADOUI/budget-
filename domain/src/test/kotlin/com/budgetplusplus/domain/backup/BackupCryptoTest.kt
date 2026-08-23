package com.budgetplusplus.domain.backup

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.*
import org.junit.Test

class BackupCryptoTest {
 private val database="SQLite format 3\u0000financial-data".toByteArray()
 @Test fun `round trips encrypted version two backup`(){val out=ByteArrayOutputStream();BackupCrypto.encrypt(database,6,"correct horse".toCharArray(),out);val decoded=BackupCrypto.decrypt(ByteArrayInputStream(out.toByteArray()),"correct horse".toCharArray());assertArrayEquals(database,decoded.databaseBytes);assertEquals(3,decoded.formatVersion);assertEquals(6,decoded.schemaVersion)}
 @Test fun `reads version two backup`(){val out=ByteArrayOutputStream();BackupCrypto.encrypt(database,6,"version-two".toCharArray(),out,formatVersion=2);assertEquals(2,BackupCrypto.decrypt(ByteArrayInputStream(out.toByteArray()),"version-two".toCharArray()).formatVersion)}
 @Test fun `reads legacy version one backup`(){val out=ByteArrayOutputStream();BackupCrypto.encrypt(database,3,"legacy-pass".toCharArray(),out,formatVersion=1);val decoded=BackupCrypto.decrypt(ByteArrayInputStream(out.toByteArray()),"legacy-pass".toCharArray());assertEquals(1,decoded.formatVersion);assertEquals(3,decoded.schemaVersion);assertArrayEquals(database,decoded.databaseBytes)}
 @Test(expected=InvalidBackupException::class) fun `rejects incorrect password`(){val out=ByteArrayOutputStream();BackupCrypto.encrypt(database,6,"right-pass".toCharArray(),out);BackupCrypto.decrypt(ByteArrayInputStream(out.toByteArray()),"wrong-pass".toCharArray())}
 @Test(expected=InvalidBackupException::class) fun `rejects modified ciphertext`(){val out=ByteArrayOutputStream();BackupCrypto.encrypt(database,6,"right-pass".toCharArray(),out);val bytes=out.toByteArray();bytes[bytes.lastIndex-2]=(bytes[bytes.lastIndex-2].toInt() xor 1).toByte();BackupCrypto.decrypt(ByteArrayInputStream(bytes),"right-pass".toCharArray())}
}
