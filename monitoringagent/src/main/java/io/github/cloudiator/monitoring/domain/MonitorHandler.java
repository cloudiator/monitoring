package io.github.cloudiator.monitoring.domain;

import com.google.inject.Inject;
import org.cloudiator.messages.InstallationEntities.Tool;
import org.cloudiator.messages.Node.NodeQueryMessage;
import org.cloudiator.messages.Node.NodeQueryResponse;
import org.cloudiator.messages.entities.IaasEntities.IpAddress;
import org.cloudiator.messaging.services.InstallationRequestService;
import org.cloudiator.messaging.services.NodeService;



public class MonitorHandler {

  private final InstallationRequestService installationRequestService;
  private final MonitorOrchestrationService monitorOrchestrationService;
  private final NodeService nodeService;

  @Inject
  public MonitorHandler(InstallationRequestService installationRequestService,
      MonitorOrchestrationService monitorOrchestrationService, NodeService nodeService) {
    this.installationRequestService = installationRequestService;
    this.monitorOrchestrationService = monitorOrchestrationService;
    this.nodeService = nodeService;
  }


  public boolean InstallTool(Tool tool, String nodeId) {

    switch (tool) {
      case VISOR:

        break;
      case AXE:
        break;
      case LANCE:
        break;
      case DOCKER:
        break;
      case KAIROSDB:
        break;
      case UNRECOGNIZED:
      default:
        throw new AssertionError("unkown Tool " + tool);
    }

    return false;
  }

  public IpAddress getIpAddressFromNodeId(String nodeId) {
    try {

      NodeQueryMessage request = NodeQueryMessage.newBuilder().setNodeId(nodeId).build();
      NodeQueryResponse response = nodeService.queryNodes(request);
      IpAddress ipAddress = response.getNodes(0).getIpAddresses(0);

      return ipAddress;
    } catch (Exception e) {
      throw new AssertionError("Problem with NodeIP");

    }
  }

}
