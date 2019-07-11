package io.github.cloudiator.monitoring.domain;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;
import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.monitoring.converter.MonitorToVisorMonitorConverter;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.persistance.MonitorModel;
import io.github.cloudiator.rest.converter.IpAddressConverter;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.util.Base64IdEncoder;
import io.github.cloudiator.util.IdEncoder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.cloudiator.messages.InstallationEntities.Tool;
import org.cloudiator.messaging.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.InstallationEntities;
import org.cloudiator.messages.InstallationEntities.Installation;
import org.cloudiator.messages.InstallationEntities.Installation.Builder;
import org.cloudiator.messages.Installation.InstallationRequest;
import org.cloudiator.messages.Installation.InstallationResponse;
import org.cloudiator.messages.Node.NodeQueryMessage;
import org.cloudiator.messages.Node.NodeQueryResponse;
import org.cloudiator.messages.NodeEntities;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.InstallationRequestService;
import org.cloudiator.messaging.services.NodeService;

import io.github.cloudiator.visor.rest.*;
import io.github.cloudiator.visor.rest.api.DefaultApi;


public class VisorMonitorHandler {


  private final InstallationRequestService installationRequestService;
  private final NodeService nodeService;
  private final MonitorToVisorMonitorConverter visorMonitorConverter = MonitorToVisorMonitorConverter.INSTANCE;
  private final NodeToNodeMessageConverter nodeMessageConverter = NodeToNodeMessageConverter.INSTANCE;

  private final String VisorPort = "31415";
  private static final Logger LOGGER = LoggerFactory.getLogger(VisorMonitorHandler.class);

  @Inject
  public VisorMonitorHandler(InstallationRequestService installationRequestService,
      NodeService nodeService) {
    this.installationRequestService = installationRequestService;
    this.nodeService = nodeService;
  }

  public boolean installEMSClient(String userId, Node node) {
    LOGGER.debug(
        " Starting EMSClientInstallationProcess on: " + node.name() + " IP: " + node.connectTo()
            .ip().toString());
    try {
      NodeEntities.Node target = nodeMessageConverter.apply(node);

      final Builder installationBuilder = Installation.newBuilder().setNode(target)
          .addTool(Tool.EMS_CLIENT);

      final InstallationRequest installationRequest = InstallationRequest.newBuilder()
          .setInstallation(installationBuilder.build())
          .setUserId(userId).build();

      final SettableFutureResponseCallback<InstallationResponse, InstallationResponse> futureResponseCallback = SettableFutureResponseCallback
          .create();

      installationRequestService
          .createInstallationRequestAsync(installationRequest, futureResponseCallback);

      futureResponseCallback.get();
    } catch (InterruptedException e) {
      LOGGER.debug("EMS install Exception catched: " + e);
      // throw new IllegalStateException("EMS Installation was interrupted during installation request.", e);
    } catch (ExecutionException e) {
      LOGGER.debug("EMS install ExecutionException catched: " + e);
      // throw new IllegalStateException("Error during EMSInstallation", e.getCause());
    }
    LOGGER.debug(
        "finished EMSClientInstallationProcess on: " + node.name() + " IP: " + node.connectTo()
            .ip().toString());
    return true;
  }

  public boolean installVisor(String userId, Node node) {
    LOGGER
        .debug(" Starting VisorInstallationProcess on: " + node.name() + " IP: " + node.connectTo()
            .ip().toString());
    try {
      NodeEntities.Node target = nodeMessageConverter.apply(node);

      final Builder installationBuilder = Installation.newBuilder().setNode(target)
          .addTool(InstallationEntities.Tool.VISOR);

      final InstallationRequest installationRequest = InstallationRequest.newBuilder()
          .setInstallation(installationBuilder.build())
          .setUserId(userId).build();

      final SettableFutureResponseCallback<InstallationResponse, InstallationResponse> futureResponseCallback = SettableFutureResponseCallback
          .create();

      installationRequestService
          .createInstallationRequestAsync(installationRequest, futureResponseCallback);

      futureResponseCallback.get();
    } catch (InterruptedException e) {
      //LOGGER.debug("Exception catched: " + e);
      throw new IllegalStateException(
          "VISOR Installation was interrupted during installation request.", e);
    } catch (ExecutionException e) {
      //LOGGER.debug("ExecutionException catched: " + e);
      throw new IllegalStateException("Error during VisorInstallation", e.getCause());
    }
    LOGGER.debug(
        "finished VisorInstallationProcess on: " + node.name() + " IP: " + node.connectTo().ip()
            .toString());
    return true;
  }

  public io.github.cloudiator.visor.rest.model.Monitor configureVisor(Node targetNode,
      DomainMonitorModel monitor) {
    LOGGER
        .debug("Starting VisorConfigurationProcess on: " + targetNode.connectTo().ip().toString());

    String metric = monitor.getMetric().split("[+++]", 3)[0];
    monitor.setMetric(metric);

    DefaultApi apiInstance = new DefaultApi();
    ApiClient apiClient = new ApiClient();
    String basepath = String.format("http://%s:%s", targetNode.connectTo().ip(), VisorPort);
    //String basepath = String.format("http://localhost:31415");
    apiClient.setBasePath(basepath);
    apiInstance.setApiClient(apiClient);

    Callable<Boolean> visorready = new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        LOGGER.debug(" - calling Visor - ");
        return (apiInstance.getMonitors() != null);
      }
    };

    Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
        .retryIfResult(Predicates.<Boolean>isNull())
        .retryIfExceptionOfType(ApiException.class)
        .retryIfRuntimeException()
        .withWaitStrategy(WaitStrategies.fixedWait(1000, TimeUnit.MILLISECONDS))
        .withStopStrategy(StopStrategies.stopAfterDelay(30, TimeUnit.SECONDS))
        .build();

    try {
      retryer.call(visorready);
    } catch (RetryException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    LOGGER.debug("- calling Visor successful - ");

    io.github.cloudiator.visor.rest.model.Monitor visorMonitor = visorMonitorConverter
        .apply(monitor);
    io.github.cloudiator.visor.rest.model.Monitor visorResponse = null;
    try {

      visorResponse = apiInstance.postMonitors(visorMonitor);

    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#postMonitors:" + e.getResponseBody());
      e.printStackTrace();
    }
    return visorResponse;
  }

  public boolean configureVisortest(Node targetNode, DomainMonitorModel monitor) {
    LOGGER.debug("Starting VisorConfigurationProcess on: localhost");

    DefaultApi apiInstance = new DefaultApi();
    ApiClient apiClient = new ApiClient();
    String basepath = String.format("http://localhost:31415");
    LOGGER.debug("Basepath: " + basepath.toString());
    apiClient.setBasePath(basepath);
    apiInstance.setApiClient(apiClient);
    LOGGER.debug("apiClient: " + apiClient.toString());

    Callable<Boolean> visorready = new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        LOGGER.debug(" - calling Visor - ");
        return (apiInstance.getMonitors() != null);
      }
    };

    Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
        .retryIfResult(Predicates.<Boolean>isNull())
        .retryIfExceptionOfType(ApiException.class)
        .retryIfRuntimeException()
        .withWaitStrategy(WaitStrategies.fixedWait(500, TimeUnit.MILLISECONDS))
        .withStopStrategy(StopStrategies.stopAfterDelay(10000, TimeUnit.MILLISECONDS))
        .build();

    try {
      retryer.call(visorready);
    } catch (RetryException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    LOGGER.debug("- calling Visor successful - ");
    io.github.cloudiator.visor.rest.model.Monitor visorMonitor = visorMonitorConverter
        .apply(monitor);

    try {
      LOGGER.debug("POSTing Monitor: ");

      io.github.cloudiator.visor.rest.model.Monitor visorResponse = apiInstance
          .postMonitors(visorMonitor);

    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#postMonitors:" + e.getResponseBody());
      e.printStackTrace();
    }
    return true;
  }

  public void deleteVisorMonitor(Node targetNode, DomainMonitorModel domainMonitor) {

    DefaultApi apiInstance = new DefaultApi();
    ApiClient apiClient = new ApiClient();
    String basepath = String.format("http://%s:%s", targetNode.connectTo().ip(), VisorPort);
    apiClient.setBasePath(basepath);
    apiInstance.setApiClient(apiClient);

    try {
      apiInstance.deleteMonitor(domainMonitor.getUuid());
    } catch (ApiException ae) {
      LOGGER.debug("ApiException occured: " + ae);
      throw new IllegalStateException("Error at deleting VisorMonitor: " + ae);
    }

  }

  public List<io.github.cloudiator.visor.rest.model.Monitor> getAllVisorMonitors(Node targetNode) {
    LOGGER.debug("GET ALLVISORMONITORS");
    List<io.github.cloudiator.visor.rest.model.Monitor> allMonitors = new ArrayList<>();
    String visorpath = String.format("http://%s:%s", targetNode.connectTo().ip(), VisorPort);
    String localpath = String.format("http://localhost:31415");
    ApiClient apiClient = new ApiClient().setBasePath(visorpath);
    DefaultApi api = new DefaultApi(apiClient);
    try {
      allMonitors.addAll(api.getMonitors());
      return allMonitors;
    } catch (ApiException e) {
      throw new IllegalStateException("Error while getMonitors: " + e.getMessage());

    }
  }


  public Node getNodeById(String userId, String nodeId) {
    try {

      NodeQueryMessage request = NodeQueryMessage.newBuilder().setNodeId(nodeId)
          .setUserId(userId)
          .build();
      NodeQueryResponse response = nodeService.queryNodes(request);

      if (response.getNodesCount() > 1) {
        throw new IllegalStateException("More than one Node back ");
      } else if (response.getNodesCount() == 0) {
        throw new IllegalStateException("Node not found");
      }
      NodeEntities.Node nodeEntity = response.getNodesList().get(0);

      return nodeMessageConverter.applyBack(nodeEntity);

    } catch (ResponseException re) {
      throw new AssertionError(re.getMessage());
    } catch (Exception e) {
      throw new AssertionError("Problem by getting Node:" + e.getMessage());
    }
  }

}
