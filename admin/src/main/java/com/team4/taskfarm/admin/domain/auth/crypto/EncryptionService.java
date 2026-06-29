package com.team4.taskfarm.admin.domain.auth.crypto;

public interface EncryptionService {
	
    String encrypt(String plaintext);

    String decrypt(String ciphertext);
}