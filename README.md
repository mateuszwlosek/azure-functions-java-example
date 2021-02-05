# Azure Functions Java + Kubernetes commands example

Simple example of [Azure function](https://azure.microsoft.com/en-us/blog/introducing-azure-functions/) in Java with possibility to execute [Kubernetes](https://kubernetes.io/) commands on a cluster.

Example can be easily modified, the current version shows how to delete a requested pod. Can be later used in Azure logs triggers for example (when a pod is in a  broken state etc.)

### Guide 
Open the project in IntelliJ.
Download the [Azure Toolkit for IntelliJ Plugin](https://plugins.jetbrains.com/plugin/8053-azure-toolkit-for-intellij)  
You may need to upgrade your IntelliJ as some plugin functions are not available in the older versions.  
Log in to Azure (Azure CLI):  
![image](https://user-images.githubusercontent.com/15820051/107031298-3ec6fd00-67b2-11eb-93d3-2f4cc9782877.png)  
Double click on `Function App` and select `Create`.  
Setup settings for your needs.
When the function app is created, click on it and select `Deploy`  
![image](https://user-images.githubusercontent.com/15820051/107031737-f8be6900-67b2-11eb-9ddb-5b973d492439.png)  
In the app settings add the following values:
* AZURE_TOKEN  
Use `kubectl get secret` to fetch token. Get token for correct namespace with permissions (look for an account different than `default`, default may now be allowed to execute necessary actions)

* AZURE_CLUSTER_URL  
Go to `Kubernetes services`, select desired cluster, and in `Overview` copy `API server address` with `https` prefix.

and click `Run`.  
(Deploying may take some time)  
IntelliJ will display a link to your service.  
You can execute an example by:  
`curl 'https://url/api/HttpExample?podName=(podName)&namespaceName=(namespace)`  
Example:  
`curl 'https://test-function.azurewebsites.net/api/HttpExample?podName=demo-786588bdd7-bpgfd&namespaceName=demo-namespace`  
