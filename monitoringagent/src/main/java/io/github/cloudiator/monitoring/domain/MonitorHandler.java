package io.github.cloudiator.monitoring.domain;

import static org.reflections.util.ConfigurationBuilder.build;

import com.google.inject.Inject;
import io.github.cloudiator.rest.converter.IpAddressConverter;
import io.github.cloudiator.rest.converter.NodeConverter;
import io.github.cloudiator.rest.model.Node;
import io.github.cloudiator.util.Base64IdEncoder;
import io.github.cloudiator.util.IdEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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


public class MonitorHandler {

  private final InstallationRequestService installationRequestService;
  private final NodeService nodeService;
  private final IpAddressConverter ipConverter;
  private final NodeConverter nodeConverter;
  private final IdEncoder idEncoder = Base64IdEncoder.create();

  @Inject
  public MonitorHandler(InstallationRequestService installationRequestService,
      NodeService nodeService) {
    this.installationRequestService = installationRequestService;
    this.nodeService = nodeService;
    this.ipConverter = new IpAddressConverter();
    this.nodeConverter = new NodeConverter();
  }


  public boolean installVisor(String userId, String nodeId) {

    NodeEntities.Node target = getNodeById(nodeId, userId);
    if (target == null) {
      return false;
    }

    final Builder installationBuilder = Installation.newBuilder().setNode(target).addTool(
        InstallationEntities.Tool.VISOR);

    final InstallationRequest installationRequest = InstallationRequest.newBuilder()
        .setInstallation(installationBuilder.build())
        .setUserId(userId).build();

    final SettableFutureResponseCallback<InstallationResponse, InstallationResponse> futureResponseCallback = SettableFutureResponseCallback
        .create();

    installationRequestService
        .createInstallationRequestAsync(installationRequest, futureResponseCallback);

    try {
      futureResponseCallback.get();
    } catch (InterruptedException e) {
      throw new IllegalStateException(
          "VISOR Installation was interrupted during installation request.", e);
    } catch (ExecutionException e) {
      throw new IllegalStateException("Error during VisorInstallation", e.getCause());
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

  public NodeEntities.Node getNodeById(String nodeId, String userId) {

    final String decodedId = idEncoder.decode(nodeId);

    try {

      NodeQueryMessage request = NodeQueryMessage.newBuilder().setNodeId(decodedId)
          .setUserId(userId)
          .build();
      NodeQueryResponse response = nodeService.queryNodes(request);

      if (response.getNodesCount() > 1) {
        throw new AssertionError("More than one Node back ");
      } else if (response.getNodesCount() == 0) {
        return null;
      }

      System.out.println("NodeId:" + nodeId);
      System.out.println("Nodelist-size: " + response.getNodesList().size());
      System.out.println("Nodes: " + response.getNodesList().toString());
      NodeEntities.Node nodeEntity = response.getNodesList().get(0);

      return nodeEntity;

    } catch (Exception e) {
      throw new AssertionError("Problem by getting Node:" + e.getMessage());
    }
  }

}
