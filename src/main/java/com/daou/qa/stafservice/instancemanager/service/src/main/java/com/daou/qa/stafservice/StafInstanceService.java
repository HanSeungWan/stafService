package com.daou.qa.stafservice;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;
import com.ibm.staf.STAFUtil;
import com.ibm.staf.service.STAFCommandParseResult;
import com.ibm.staf.service.STAFCommandParser;
import com.ibm.staf.service.STAFServiceInterfaceLevel30;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by daou on 2016-06-20.
 */
public class StafInstanceService implements STAFServiceInterfaceLevel30 {

    private String fServiceName;
    private String fLineSep;
    private String fLocalMachineName;
    private String sHelpMsg = "";

    private STAFHandle fHandle;

    private final static SimpleDateFormat sTimestampFormat =
            new SimpleDateFormat("yyyyMMdd-HH:mm:ss");

    private static final int kDeviceInvalidSerialNumber = 7008;

    private STAFCommandParser fInstanceHandlerDeleteParser;
    private STAFCommandParser fInstanceHandlerCreateParser;

    public STAFResult init(InitInfo initInfo) {

        fServiceName = initInfo.name;
        try {
            this.fHandle = new STAFHandle("STAF/Service/" + initInfo.name);
        } catch (STAFException e) {
            return new STAFResult(STAFResult.STAFRegistrationError, e.toString());
        }


        //Delete 파서 정의
        this.fInstanceHandlerDeleteParser = new STAFCommandParser();

        fInstanceHandlerDeleteParser.addOption("DELETE", 1, STAFCommandParser.VALUEALLOWED);
        fInstanceHandlerDeleteParser.addOption("ENDPOINT", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerDeleteParser.addOption("USER", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerDeleteParser.addOption("PASSWORD", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerDeleteParser.addOption("TENANTNAME", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerDeleteParser.addOption("INSTANCENAME", 1, STAFCommandParser.VALUEREQUIRED);

        //Create 파서 정의
        this.fInstanceHandlerCreateParser = new STAFCommandParser();

        fInstanceHandlerCreateParser.addOption("CREATE", 1, STAFCommandParser.VALUEALLOWED);
        fInstanceHandlerCreateParser.addOption("ENDPOINT", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerCreateParser.addOption("USER", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerCreateParser.addOption("PASSWORD", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerCreateParser.addOption("TENANTNAME", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerCreateParser.addOption("INSTANCENAME", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerCreateParser.addOption("FLAVORID", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerCreateParser.addOption("KEYPAIRNAME", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerCreateParser.addOption("VOLUMENAME", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerCreateParser.addOption("SNAPSHOTVOLUMEID", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerCreateParser.addOption("PORTIPADDR", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerCreateParser.addOption("FLOATINGIPADDR", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerCreateParser.addOption("NETWORKID", 1, STAFCommandParser.VALUEREQUIRED);
        fInstanceHandlerCreateParser.addOption("SUBNETID", 1, STAFCommandParser.VALUEREQUIRED);


        // fLineSep 정의
        STAFResult res = new STAFResult();

        res = STAFUtil.resolveInitVar("{STAF/Config/Sep/Line}", fHandle);

        if (res.rc != STAFResult.Ok) return res;

        fLineSep = res.result;

        // LocalMachinename 정의
        res = STAFUtil.resolveInitVar("{STAF/Config/Machine}", fHandle);

        if (res.rc != STAFResult.Ok) return res;

        fLocalMachineName = res.result;

        // add help msg

        sHelpMsg = "***" + fServiceName + " Service Help***" +
                fLineSep + fLineSep +
                "서비스설명쓰는부분" +
                fLineSep +
                "HELP";

        //registerHelpData
        registerHelpData(
                kDeviceInvalidSerialNumber,
                "Invalid serial number",
                "A non-numeric value was spcified for serial number");

        return new STAFResult((STAFResult.Ok));

    }

    public STAFResult acceptRequest(RequestInfo requestInfo) {
        try {
            String action;
            int spaceIndex = requestInfo.request.indexOf(" ");

            if (spaceIndex != -1)
                action = requestInfo.request.substring(0, spaceIndex);
            else
                action = requestInfo.request;

            String actionLC = action.toLowerCase();


            //커맨드에 따라서 어떤동작을 할지 정의하는 부분
            if (actionLC.equals("delete")) {
                return handleDeleteInstance(requestInfo);
            } else if (actionLC.equals("create")) {
                return handleCreateInstance(requestInfo);
            } else if (actionLC.equals("help")) {
                return handleHelp(requestInfo);
            } else {
                return new STAFResult(
                        STAFResult.InvalidRequestString,
                        "'" + action +
                                "' is not a valid command request for the " +
                                fServiceName + " service" +
                                fLineSep + fLineSep + sHelpMsg
                );
            }
        } catch (Throwable t) {

            // Write the Java stack trace to the JVM log for the service

            System.out.println(
                    sTimestampFormat.format(Calendar.getInstance().getTime()) +
                            " ERROR: Exception on " + fServiceName + " service request:" +
                            fLineSep + fLineSep + requestInfo.request + fLineSep);

            t.printStackTrace();

            // And also return the Java stack trace in the result

            StringWriter sr = new StringWriter();
            t.printStackTrace(new PrintWriter(sr));

            if (t.getMessage() != null) {
                return new STAFResult(
                        STAFResult.JavaError,
                        t.getMessage() + fLineSep + sr.toString());
            } else {
                return new STAFResult(
                        STAFResult.JavaError, sr.toString());
            }
        }

    }

    private STAFResult handleHelp(STAFServiceInterfaceLevel30.RequestInfo info) {

        // Verify the requester has at least trust level 1

        STAFResult trustResult = STAFUtil.validateTrust(
                1, fServiceName, "HELP", fLocalMachineName, info);

        if (trustResult.rc != STAFResult.Ok) return trustResult;

        // Return help text for the service

        return new STAFResult(STAFResult.Ok, sHelpMsg);
    }

    private STAFResult handleDeleteInstance(STAFServiceInterfaceLevel30.RequestInfo command) {

        STAFResult trustResult = STAFUtil.validateTrust(
                2, fServiceName, "instance", fLocalMachineName, command);

        if (trustResult.rc != STAFResult.Ok) return trustResult;

        STAFCommandParseResult parsedRequest = fInstanceHandlerDeleteParser.parse((command.request));

        //command request 잘 들어왔는지 확인
        if (parsedRequest.rc != STAFResult.Ok) {
            return new STAFResult(STAFResult.InvalidRequestString, parsedRequest.errorBuffer);
        }

        //옵션벨류에 따른 파싱 구현하는 부분

        //endpoing옵션 파싱
        STAFResult res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("endpoint"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String endpoint = res.result;

        //user옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("user"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String user = res.result;

        //password옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("password"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String password = res.result;

        //tenantName옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("tenantName"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String tenantName = res.result;

        //instanceName옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("instancename"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String instanceName = res.result;

        OSBuilderInfo osBuilderInfo = new OSBuilderInfo();

        osBuilderInfo.setEndpoint(endpoint);
        osBuilderInfo.setUser(user);
        osBuilderInfo.setPassword(password);
        osBuilderInfo.setTenantName(tenantName);

        try {
            InstanceHandler instanceHandler = new InstanceHandler(osBuilderInfo);
            instanceHandler.deleteInstance(instanceName);
        } catch (InstanceHandlerException e) {
            String logMsg = "instancedelete service fail";
            int deleteinstancefailcode = 101;
            return new STAFResult(deleteinstancefailcode, logMsg);
        }

        String logMsg = "Instancedelete service success";

        return new STAFResult(STAFResult.Ok, logMsg);

    }

    private STAFResult handleCreateInstance(STAFServiceInterfaceLevel30.RequestInfo command) {
        STAFResult trustResult = STAFUtil.validateTrust(
                2, fServiceName, "instance", fLocalMachineName, command);

        if (trustResult.rc != STAFResult.Ok) return trustResult;

        STAFCommandParseResult parsedRequest = fInstanceHandlerCreateParser.parse((command.request));

        //command request 잘 들어왔는지 확인
        if (parsedRequest.rc != STAFResult.Ok) {
            return new STAFResult(STAFResult.InvalidRequestString, parsedRequest.errorBuffer);
        }

        //옵션벨류에 따른 파싱 구현하는 부분

        //endpoing옵션 파싱
        STAFResult res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("endpoint"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String endpoint = res.result;

        //user옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("user"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String user = res.result;

        //password옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("password"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String password = res.result;

        //tenantName옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("tenantName"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String tenantName = res.result;

        //instancename옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("instancename"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String instanceName = res.result;

        //flavorId옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("flavorid"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String flavorId = res.result;

        //ketPairName옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("keypairname"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String ketPairName = res.result;

        //volumeName옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("volumename"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String volumeName = res.result;

        //snapshotVolumeID옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("snapshotVolumeid"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String snapshotVolumeId = res.result;

        //portIpAddr옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("portipaddr"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String portIpAddr = res.result;

        //floatingIpAddr옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("floatingipaddr"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String floatingIpAddr = res.result;

        //networkId옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("networkid"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String networkId = res.result;

        //subnetId옵션 파싱
        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("subnetid"), fHandle, command.requestNumber
        );

        //파싱이 잘 되었나 검사
        if (res.rc != STAFResult.Ok) return res;

        String subnetId = res.result;

        OSBuilderInfo osBuilderInfo = new OSBuilderInfo();

        osBuilderInfo.setEndpoint(endpoint);
        osBuilderInfo.setUser(user);
        osBuilderInfo.setPassword(password);
        osBuilderInfo.setTenantName(tenantName);

        try {
            InstanceHandler instanceHandler = new InstanceHandler(osBuilderInfo);
            instanceHandler.createInstance(instanceName, flavorId, ketPairName, volumeName, snapshotVolumeId, portIpAddr, floatingIpAddr, networkId, subnetId);
        } catch (InstanceHandlerException e) {
            String logMsg = "instancecreate service fail";
            int createinstancefailcode = 102;
            return new STAFResult(createinstancefailcode, logMsg);
        }

        String logMsg = "Instancecreate service success";

        return new STAFResult(STAFResult.Ok, logMsg);


    }

    public STAFResult term() {

        try {
            // Un-register Help Data
            unregisterHelpData(kDeviceInvalidSerialNumber);

            this.fHandle.unRegister();
        } catch (STAFException e) {
            return new STAFResult(STAFResult.STAFRegistrationError, e.toString());
        }
        return new STAFResult(STAFResult.Ok);

    }


    private void registerHelpData(int errorNumber, String info,
                                  String description) {
        STAFResult res = fHandle.submit2(
                "local", "HELP", "REGISTER SERVICE " + fServiceName +
                        " ERROR " + errorNumber +
                        " INFO " + STAFUtil.wrapData(info) +
                        " DESCRIPTION " + STAFUtil.wrapData(description));
    }

    // Un-register error codes for this service with the HELP service

    private void unregisterHelpData(int errorNumber) {
        STAFResult res = fHandle.submit2(
                "local", "HELP", "UNREGISTER SERVICE " + fServiceName +
                        " ERROR " + errorNumber);
    }


}
