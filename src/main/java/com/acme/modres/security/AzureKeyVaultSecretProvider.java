package com.acme.modres.security;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

public class AzureKeyVaultSecretProvider {
  private static final String KEY_VAULT_URL_ENV = "AZURE_KEY_VAULT_URL";

  public String getSecret(String secretName) {
    String vaultUrl = System.getenv(KEY_VAULT_URL_ENV);
    if (vaultUrl == null || vaultUrl.trim().isEmpty()) {
      return null;
    }

    SecretClient client = new SecretClientBuilder()
        .vaultUrl(vaultUrl)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

    return client.getSecret(secretName).getValue();
  }
}
