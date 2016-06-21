package com.daou.qa.stafservice;

import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.openstack.OSFactory;

/**
 * Created by byungshik on 2016. 6. 20..
 */
public class InstanceHandler {

    private final OSBuilderInfo osBuilderInfo;
    private final OSClient osClient;

    public InstanceHandler(OSBuilderInfo osBuilderInfo) throws InstanceHandlerException {
        this.osBuilderInfo = osBuilderInfo;
        this.osClient = osClientBuild(osBuilderInfo);
    }

    public void deleteInstance() {

    }

    public void createInstance() {

    }

    /**
     * OpenStack Client를 빌드하기 위한 OSBuilderInfo{@link OSBuilderInfo}를 참조하여 OSClient를 생성하고
     * 반환한다.
     * @param osBuilderInfo {@link OSBuilderInfo}
     * @return OpenStakClient 객체 {@link OSClient}
     * @throws InstanceHandlerException
     */
    private OSClient osClientBuild(OSBuilderInfo osBuilderInfo) throws InstanceHandlerException {
        try {
            OSClient osClient = OSFactory.builderV2()
                    .endpoint(osBuilderInfo.getEndpoint())
                    .credentials(osBuilderInfo.getUser(), osBuilderInfo.getPassword())
                    .tenantName(osBuilderInfo.getTenantName())
                    .authenticate();

            return osClient;
        } catch (AuthenticationException e) {
            throw new InstanceHandlerException(e, e.getMessage());
        }
    }
}
