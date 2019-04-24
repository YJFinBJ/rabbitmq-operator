package com.indeed.operators.rabbitmq.api;

import com.indeed.operators.rabbitmq.Constants;
import com.indeed.operators.rabbitmq.controller.SecretsController;
import com.indeed.operators.rabbitmq.model.rabbitmq.RabbitMQConnectionInfo;
import com.indeed.operators.rabbitmq.resources.RabbitMQSecrets;
import com.indeed.operators.rabbitmq.resources.RabbitMQServices;
import com.indeed.rabbitmq.admin.RabbitManagementApi;
import com.indeed.rabbitmq.admin.RabbitManagementApiFactory;
import io.fabric8.kubernetes.api.model.Secret;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RabbitManagementApiProvider {

    private final Map<RabbitMQConnectionInfo, RabbitManagementApi> rabbitApis;
    private final SecretsController secretsController;

    public RabbitManagementApiProvider(
            final SecretsController secretsController
    ) {
        rabbitApis = new HashMap<>();
        this.secretsController = secretsController;
    }

    public RabbitManagementApi getApi(final RabbitMQConnectionInfo connectionInfo) {
        if (rabbitApis.containsKey(connectionInfo)) {
            return rabbitApis.get(connectionInfo);
        }

        synchronized (rabbitApis) {
            if (rabbitApis.containsKey(connectionInfo)) {
                return rabbitApis.get(connectionInfo);
            }

            final Secret adminSecret = secretsController.get(RabbitMQSecrets.getUserSecretName(Constants.DEFAULT_USERNAME, connectionInfo.getClusterName()), connectionInfo.getNamespace());
            final RabbitManagementApi api = RabbitManagementApiFactory.newInstance(
                    buildApiUri(connectionInfo),
                    secretsController.decodeSecretPayload(adminSecret.getData().get(Constants.Secrets.USERNAME_KEY)),
                    secretsController.decodeSecretPayload(adminSecret.getData().get(Constants.Secrets.PASSWORD_KEY))
            );

            rabbitApis.put(connectionInfo, api);

            return api;
        }
    }

    private URI buildApiUri(final RabbitMQConnectionInfo connectionInfo) {
        final String serviceName = RabbitMQServices.getDiscoveryServiceName(connectionInfo.getClusterName());

        if (connectionInfo.getNodeName().isPresent()) {
            return URI.create(String.format("%s.%s:15672", connectionInfo.getNodeName().get(), serviceName));
        }

        return URI.create(serviceName);
    }
}
