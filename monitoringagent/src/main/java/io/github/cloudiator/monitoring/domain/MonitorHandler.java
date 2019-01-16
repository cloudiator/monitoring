package io.github.cloudiator.monitoring.domain;

import static org.reflections.util.ConfigurationBuilder.build;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.converter.MonitorToVisorMonitorConverter;
import io.github.cloudiator.rest.converter.IpAddressConverter;
import io.github.cloudiator.rest.converter.NodeConverter;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.Node;
import io.github.cloudiator.util.Base64IdEncoder;
import io.github.cloudiator.util.IdEncoder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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
import io.github.cloudiator.rest.model.IpAddress;

import io.github.cloudiator.visor.rest.*;
import io.github.cloudiator.visor.rest.auth.*;
import io.github.cloudiator.visor.rest.model.*;
import io.github.cloudiator.visor.rest.api.DefaultApi;


public class MonitorHandler {


  private final InstallationRequestService installationRequestService;
  private final NodeService nodeService;
  private final IpAddressConverter ipConverter;
  private final NodeConverter nodeConverter;
  private final IdEncoder idEncoder = Base64IdEncoder.create();
  private final MonitorToVisorMonitorConverter visorMonitorConverter = new MonitorToVisorMonitorConverter();

  private final String VisorPort = "31415";
  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorHandler.class);

  @Inject
  public MonitorHandler(InstallationRequestService installationRequestService,
      NodeService nodeService) {
    this.installationRequestService = installationRequestService;
    this.nodeService = nodeService;
    this.ipConverter = new IpAddressConverter();
    this.nodeConverter = new NodeConverter();
  }


  public boolean installVisor(String userId, Node node) {
    LOGGER.debug(" Starting VisorInstallationProcess on: " + node.getNodeId());
    try {
      NodeEntities.Node target = nodeConverter.apply(node);

      final Builder installationBuilder = Installation.newBuilder().setNode(target).addTool(
          InstallationEntities.Tool.VISOR);

      final InstallationRequest installationRequest = InstallationRequest.newBuilder()
          .setInstallation(installationBuilder.build())
          .setUserId(userId).build();

      final SettableFutureResponseCallback<InstallationResponse, InstallationResponse> futureResponseCallback = SettableFutureResponseCallback
          .create();

      installationRequestService
          .createInstallationRequestAsync(installationRequest, futureResponseCallback);

      futureResponseCallback.get();
    } catch (InterruptedException e) {
      throw new IllegalStateException(
          "VISOR Installation was interrupted during installation request.", e);
    } catch (ExecutionException e) {
      throw new IllegalStateException("Error during VisorInstallation", e.getCause());
    }
    LOGGER.debug("finished VisorInstallationProcess on: " + node.getNodeId());
    return true;
  }

  public boolean configureVisor(String userId, MonitoringTarget target, Node targetNode,
      Monitor monitor) {
    LOGGER.debug("Starting VisorConfigurationProcess on: " + targetNode.getNodeId());

    DefaultApi apiInstance = new DefaultApi();
    //io.github.cloudiator.visor.rest.model.Monitor visorMonitor = new io.github.cloudiator.visor.rest.model.Monitor(); // Monitor |
    io.github.cloudiator.visor.rest.model.Monitor visorMonitor = visorMonitorConverter
        .apply(monitor);
    try {
      io.github.cloudiator.visor.rest.model.Monitor visorResponse = apiInstance
          .postMonitors(visorMonitor);
      System.out.println(visorResponse);
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#postMonitors");
      e.printStackTrace();
    }

    return true;
  }

  public IpAddress getIpAddressFromNodeId(String nodeId) {
    try {

      NodeQueryMessage request = NodeQueryMessage.newBuilder().setNodeId(nodeId).build();
      NodeQueryResponse response = nodeService.queryNodes(request);
      IpAddress ipAddress = ipConverter.applyBack(response.getNodesOrBuilder(0).getIpAddresses(0));

      return ipAddress;
    } catch (Exception e) {
      throw new AssertionError("Problem with NodeIP");
    }
  }

  public Node getNodeById(String nodeId, String userId) {
    LOGGER.debug(" Starting getNodeById ");
    final String decodedId = idEncoder.decode(nodeId);
    try {

      NodeQueryMessage request = NodeQueryMessage.newBuilder().setNodeId(decodedId)
          .setUserId(userId)
          .build();
      NodeQueryResponse response = nodeService.queryNodes(request);

      if (response.getNodesCount() > 1) {
        throw new IllegalStateException("More than one Node back ");
      } else if (response.getNodesCount() == 0) {
        throw new IllegalStateException("Node not found");
      }

      NodeEntities.Node nodeEntity = response.getNodesList().get(0);
      LOGGER.debug("found NodeEntity: " + nodeEntity.getId());

      return nodeConverter.applyBack(nodeEntity);

    } catch (Exception e) {
      throw new AssertionError("Problem by getting Node:" + e.getMessage());
    }
  }

}
