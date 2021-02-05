package com.github.mateuszwlosek.azurefunctions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class HttpTriggerFunction {

	private static final String AZURE_TOKEN_ENV_VARIABLE = "AZURE_TOKEN";
	private static final String AZURE_CLUSTER_URL_ENV_VARIABLE = "AZURE_CLUSTER_URL";
	private static final String QUERY_POD_NAME_PARAMETER = "podName";
	private static final String QUERY_NAMESPACE_NAME_PARAMETER = "namespaceName";
	private final static String TOKEN = System.getenv(AZURE_TOKEN_ENV_VARIABLE);
	private final static String CLUSTER_URL = System.getenv(AZURE_CLUSTER_URL_ENV_VARIABLE);
	private final static CoreV1Api api = buildAzureCoreV1Api();

	private static CoreV1Api buildAzureCoreV1Api() {
		final ApiClient apiClient = Config.fromToken(CLUSTER_URL, TOKEN, false);
		Configuration.setDefaultApiClient(apiClient);
		return new CoreV1Api(apiClient);
	}

	@FunctionName("HttpExample")
	public HttpResponseMessage run(
		@HttpTrigger(
			name = "req",
			methods = {HttpMethod.GET, HttpMethod.POST},
			authLevel = AuthorizationLevel.ANONYMOUS)
			HttpRequestMessage<Optional<String>> request,
		final ExecutionContext context) throws ApiException {

		final Logger log = context.getLogger();
		log.info("Java HTTP trigger processing an event...");

		final Map<String, String> queryParameters = request.getQueryParameters();
		final String podName = queryParameters.get(QUERY_POD_NAME_PARAMETER);
		final String namespaceName = queryParameters.get(QUERY_NAMESPACE_NAME_PARAMETER);

		if (!doesNamespaceExist(namespaceName)) {
			return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
				.body("Namespace: " + podName +" does not exist")
				.build();
		}

		if (!doesPodExist(podName, namespaceName)) {
			return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
				.body("Pod: " + podName +" does not exist")
				.build();
		}

		deletePod(log, podName, namespaceName);

		return request.createResponseBuilder(HttpStatus.OK)
			.body("Deleted pod: " + podName)
			.build();
	}

	private boolean doesNamespaceExist(final String namespaceName) throws ApiException {
		final V1NamespaceList namespaces = api.listNamespace(
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null);

		return namespaces.getItems()
			.stream()
			.filter(namespace -> Objects.nonNull(namespace.getMetadata()) && Objects.nonNull(namespace.getMetadata().getName()) && namespace.getMetadata().getName().equals(namespaceName))
			.filter(namespace -> Objects.nonNull(namespace.getMetadata().getName()) && namespace.getMetadata().getName().equals(namespaceName))
			.anyMatch(namespace -> namespace.getMetadata().getName().equals(namespaceName));
	}

	private boolean doesPodExist(final String podName, final String namespaceName) throws ApiException {
		final V1PodList pods = api.listNamespacedPod(
			namespaceName,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null);

		return pods.getItems()
			.stream()
			.filter(pod -> Objects.nonNull(pod.getMetadata()) && Objects.nonNull(pod.getMetadata().getName()) && pod.getMetadata().getName().equals(podName))
			.filter(pod -> Objects.nonNull(pod.getMetadata().getName()) && pod.getMetadata().getName().equals(podName))
			.anyMatch(pod -> pod.getMetadata().getName().equals(podName));
	}

	private void deletePod(final Logger log, final String podName, final String namespaceName) throws ApiException {
		log.info(String.format("Deleting pod: %s in namespace: %s", podName, namespaceName));
		api.deleteNamespacedPod(
			podName,
			namespaceName,
			null,
			null,
			null,
			false,
			null,
			null);
	}
}
