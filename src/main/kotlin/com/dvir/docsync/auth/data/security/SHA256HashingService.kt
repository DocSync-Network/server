package com.dvir.docsync.auth.data.security

import com.dvir.docsync.auth.domain.security.HashingService
import com.dvir.docsync.auth.domain.security.SaltedHash
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom

class SHA256HashingService: HashingService {
    override fun generateSaltedHash(value: String, saltLength: Int): SaltedHash {
        val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength)
        val saltAsHex = Hex.encodeHexString(salt)
        val hash = DigestUtils.sha256Hex("$saltAsHex$value")

        return SaltedHash(
            hash = hash,
            salt = saltAsHex
        )
    }

    override fun verify(value: String, saltedHash: SaltedHash): Boolean {
        val concatenated = saltedHash.salt + value
        val hashToVerify = DigestUtils.sha256Hex(concatenated)

        return DigestUtils.sha256Hex(saltedHash.salt + value) == saltedHash.hash
    }
}