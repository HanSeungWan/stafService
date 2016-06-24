package com.daou.qa.stafservice;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.compute.builder.BlockDeviceMappingBuilder;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.storage.block.Volume;
import org.openstack4j.openstack.OSFactory;
import org.vngx.jsch.ChannelExec;
import org.vngx.jsch.JSch;
import org.vngx.jsch.Session;
import org.vngx.jsch.UserInfo;
import org.vngx.jsch.exception.JSchException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Instance Handler
 * Created by byungshik on 2016. 6. 20..
 */
public class InstanceHandler {

    private static final Logger LOG = Logger.getLogger(InstanceHandler.class.getName());

    private OSClient osClient;

    public InstanceHandler(OSBuilderInfo osBuilderInfo) throws InstanceHandlerException {
        osClientBuild(osBuilderInfo);
    }

    public void deleteInstance(String instanceName) throws InstanceHandlerException {
        Server server;
        String instanceId;
        String volumeId;

        LOG.info("To be delete instance: " + instanceName);
        try {
            server = getServer(instanceName);
            instanceId = server.getId();
            LOG.info("Instance ID of " + instanceName + " : " + instanceId);
            volumeId = server.getOsExtendedVolumesAttached().get(0);
            LOG.info("Volume ID of " + instanceName + " : " + volumeId);
            LOG.info("Volume storage name : "
                             + osClient.blockStorage().volumes().get(volumeId).getName());
            LOG.info("Volume state of volume storage : "
                             + osClient.blockStorage().volumes().get(volumeId).getStatus());

            LOG.info(instanceId + "delete start.");
            osClient.compute().servers().delete(server.getId());
            osClient.compute().servers().waitForServerStatus(
                    server.getId(), Server.Status.DELETED, 30, TimeUnit.SECONDS);
            LOG.info(instanceId + "instance is deleted.");

            LOG.info("Volume storage delete start.");
            ActionResponse actionResponse = osClient.blockStorage().volumes().delete(volumeId);
            if (actionResponse.isSuccess()) {
                LOG.info(volumeId + "is deleted.");
            } else {
                LOG.info("Failed to delete volume storage. : " + actionResponse.getFault());
                throw new InstanceHandlerException();
            }

            LOG.info("Instance delete completed.");
        } catch (NullPointerException ne) {
            LOG.info(instanceName + " is not exist!!");
            ne.getMessage();
            throw new InstanceHandlerException(ne, ne.getMessage());
        } catch (IndexOutOfBoundsException ie) {
            LOG.info("Volume disk is not exist on the " + instanceName);
            throw new InstanceHandlerException(ie, ie.getMessage());
        }

    }

    // Instance 목록에서 instance 이름과 동일한 Server 객체를 찾아서 반환한다.
    private Server getServer(String instanceName) {
        List<? extends Server> servers = osClient.compute().servers().list();
        for (Server server : servers) {
            if (instanceName.equals(server.getName())) {
                return server;
            }
        }
        throw new NullPointerException("Server does not exist");
    }

    public void createInstance(String instanceName, String flavorId, String keyPairName,
                               String volumeName, String snapshotVolumeID, String portIpAddr,
                               String floatingIpAddr, String networkId, String subnetId)
            throws InstanceHandlerException {
        LOG.info("Start creating the instance");

        LOG.info("Creating new volume.");
        Volume volume = osClient.blockStorage().volumes().create(
                Builders.volume().name(volumeName).snapshot(snapshotVolumeID).build());
        LOG.info("New volume is created. : " + volume.getId());

        BlockDeviceMappingBuilder blockDeviceMappingBuilder = Builders.blockDeviceMapping()
                .uuid(volume.getId())
                .deviceName("/dev/vda")
                .bootIndex(0);

        Port port;
        try {
            port = getPort(portIpAddr);
            osClient.networking().port().delete(port.getId());
        } catch (NullPointerException ne) {
            LOG.warning("Port is not exist. : " + ne.getMessage());
        }

        LOG.info("Creating new port. : " + portIpAddr);
        try {
            port = Builders.port()
                    .networkId(networkId)
                    .fixedIp(portIpAddr, subnetId)
                    .deviceOwner("nova")
                    .build();
            port = osClient.networking().port().create(port);
        } catch (ClientResponseException cre) {
            LOG.warning("Failed to create port " + portIpAddr + " : " + cre.getStatusCode());
            throw new InstanceHandlerException(cre, cre.getMessage());
        }
        LOG.info("New port is created.");

        LOG.info("Creating new server. : " + instanceName);
        ServerCreate serverCreate = Builders.server()
                .name(instanceName)
                .flavor(flavorId)
                .blockDevice(blockDeviceMappingBuilder.build())
                .addNetworkPort(port.getId())
                .keypairName(keyPairName)
                .build();

        Server server = osClient.compute().servers().bootAndWaitActive(serverCreate, 60000);
        LOG.info("New server is created. : " + server.getId());

        LOG.info("Waiting for the server to boot.");
        osClient.compute().servers().waitForServerStatus(
                server.getId(), Server.Status.ACTIVE, 60, TimeUnit.SECONDS);
        LOG.info("Server is booted.");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOG.info("Assigning floating IP address. : " + floatingIpAddr);
        osClient.compute().floatingIps().addFloatingIP(server, floatingIpAddr);

        LOG.info("MTU set to 1500.");
        try {
            executeCommand(floatingIpAddr, "ifconfig eth0 mtu 1500 up");
        } catch (JSchException e) {
            LOG.warning("Failed to execute remote command on the " + instanceName + ". : "
                                + e.getMessage());
        }


        LOG.info("Instance creating was complete");
    }

    private Port getPort(String fixedIp) {
        List<? extends Port> ports = osClient.networking().port().list();
        for (Port port : ports) {
            if (fixedIp.equals(port.getFixedIps().iterator().next().getIpAddress())) {
                return port;
            }
        }
        throw new NullPointerException("Port(" + fixedIp + ") does not exist");
    }

    private static void executeCommand(String host, String cmd) throws JSchException {
        final String USERNAME = "root";
        final String PASSWORD = "thwnekfrehflxkd!";
        final String HOST = host;
        final int PORT = 22;
        List<String> result = new ArrayList<>();

        try {
            JSch jsch = JSch.getInstance();

            UserInfo userInfo = new SshUserInfo(PASSWORD);
            Session session = jsch.createSession(USERNAME, HOST, PORT);
            session.setUserInfo(userInfo);
            int retryCount = 0;
            while (retryCount < 5) {
                try {
                    session.connect(PASSWORD.getBytes());
                    break;
                } catch (JSchException e) {
                    e.printStackTrace();
                    LOG.info("Connection Failed. I'll retry.");
                    retryCount++;
                    Thread.sleep(10000);
                }
            }

            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");

            InputStream in = channelExec.getInputStream();

            channelExec.setCommand(cmd);
            channelExec.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }

            int exitStatus = channelExec.getExitStatus();

            channelExec.disconnect();
            session.disconnect();

            if (exitStatus < 0) {
                LOG.info("Success: execute command.");
            } else if (exitStatus > 0) {
                LOG.info("Error:");
            } else {
                LOG.info("Success: execute command.");
            }

        } catch (JSchException | IOException | InterruptedException e) {
            e.printStackTrace();
            throw new JSchException("Failed to execute command.");
        }
    }

    /**
     * OpenStack Client를 빌드하기 위한 OSBuilderInfo{@link OSBuilderInfo}를 참조하여 OSClient를 생성하고
     * 반환한다.
     * @param osBuilderInfo {@link OSBuilderInfo}
     * @throws InstanceHandlerException
     */
    private void osClientBuild(OSBuilderInfo osBuilderInfo) throws InstanceHandlerException {
        try {
            osClient = OSFactory.builderV2()
                    .endpoint(osBuilderInfo.getEndpoint())
                    .credentials(osBuilderInfo.getUser(), osBuilderInfo.getPassword())
                    .tenantName(osBuilderInfo.getTenantName())
                    .authenticate();
        } catch (AuthenticationException e) {
            throw new InstanceHandlerException(e, e.getMessage());
        }
    }
}
