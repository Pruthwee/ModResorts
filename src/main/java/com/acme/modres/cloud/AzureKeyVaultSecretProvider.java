package com.acme.modres.cloud;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

/**
 * Retrieves application secrets from Azure Key Vault using Managed Identity or
 * DefaultAzureCredential. Environment variable fallback is kept only for local
 * development and migration safety; source code never contains secret values.
 */
public final class AzureKeyVaultSecretProvider {
  private static final String KEY_VAULT_URL_ENV = "AZURE_KEY_VAULT_URL";

  private AzureKeyVaultSecretProvider() {
  }

  public static String getSecret(String secretName, String localFallbackEnvName) {
    String vaultUrl = System.getenv(KEY_VAULT_URL_ENV);
    if (vaultUrl != null && !vaultUrl.trim().isEmpty()) {
      SecretClient secretClient = new SecretClientBuilder()
          .vaultUrl(vaultUrl)
          .credential(new DefaultAzureCredentialBuilder().build())
          .buildClient();
      return secretClient.getSecret(secretName).getValue();
    }
    return System.getenv(localFallbackEnvName);
  }
}
