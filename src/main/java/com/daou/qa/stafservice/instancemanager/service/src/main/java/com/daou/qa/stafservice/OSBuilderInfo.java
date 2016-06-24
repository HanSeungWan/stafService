package com.daou.qa.stafservice;

/**
 * Created by byungshik on 2016. 6. 20.
 */
public class OSBuilderInfo {

    private String endpoint;    // openstack 서버
    private String user;    // openstack 접속 계정(ID)
    private String password;    // openstack 접속 패스워드
    private String tenantName;  // tenant name

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
}
