package com.daou.qa;

import com.ibm.staf.*;
import com.ibm.staf.service.STAFCommandParseResult;
import com.ibm.staf.service.STAFCommandParser;
import com.ibm.staf.service.STAFServiceInterfaceLevel30;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * STAF SERVICE DAOUMAIL
 * Created by intern on 2016-05-26.
 */
public class DaouMail implements STAFServiceInterfaceLevel30 {

    MailInfo mailInfo = new MailInfo();
    MailSender mailer = new MailSender();

    private String fServiceName;
    private String fLineSep;
    private String fLocalMachineName = "";
    private String sHelpMsg = "";

    private STAFHandle fHandle;

    private final static SimpleDateFormat sTimestampFormat =
            new SimpleDateFormat("yyyyMMdd-HH:mm:ss");

    // Define any error codes unique to this service

    private static final int kDeviceInvalidSerialNumber = 7007;

    private STAFCommandParser fSendParser;

    public STAFResult init(STAFServiceInterfaceLevel30.InitInfo initInfo) {
        try {
            fServiceName = initInfo.name;
            this.fHandle = new STAFHandle("STAF/Service/" + initInfo.name);
        } catch (STAFException e) {
            return new STAFResult(STAFResult.STAFRegistrationError, e.toString());
        }

        this.fSendParser = new STAFCommandParser();

        // SEND parser

        this.fSendParser.addOption("SEND", 1, STAFCommandParser.VALUEALLOWED);
        this.fSendParser.addOption("MAILSERVER", 1, STAFCommandParser.VALUEREQUIRED);
        this.fSendParser.addOption("FROM", 1, STAFCommandParser.VALUEREQUIRED);
        this.fSendParser.addOption("TO", 1, STAFCommandParser.VALUEREQUIRED);
        this.fSendParser.addOption("CONTENTS", 1, STAFCommandParser.VALUEREQUIRED);

        STAFResult res = new STAFResult();

        res = STAFUtil.resolveInitVar("{STAF/Config/Sep/Line}", fHandle);

        if (res.rc != STAFResult.Ok) return res;

        fLineSep = res.result;

        res = STAFUtil.resolveInitVar("{STAF/Config/Machine}", fHandle);

        if (res.rc != STAFResult.Ok) return res;

        fLocalMachineName = res.result;

        // add help msg

        sHelpMsg = "*** " + fServiceName + " Service Help ***" +
                fLineSep + fLineSep +
                "SEND MAILSERVER <hostname> SERVER <hostname> from <email> to <email> CONTENTS <eml contents>" +
                fLineSep +
                "HELP";

        // registerHelpData
        registerHelpData(
                kDeviceInvalidSerialNumber,
                "Invalid serial number",
                "A non-numeric value was specified for serial number");

        return new STAFResult(STAFResult.Ok);
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

            if (actionLC.equals("send")) {
                return handleSend(requestInfo);
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

    private STAFResult handleSend(STAFServiceInterfaceLevel30.RequestInfo var_1) {

        // print java
        System.out.println("Call HandleSend...!!!");

        STAFResult trustResult = STAFUtil.validateTrust(
                2, fServiceName, "SEND", fLocalMachineName, var_1);

        if (trustResult.rc != STAFResult.Ok) return trustResult;

        // Parse the request
        STAFCommandParseResult parsedRequest = fSendParser.parse(var_1.request);

        if (parsedRequest.rc != STAFResult.Ok) {
            return new STAFResult(STAFResult.InvalidRequestString,
                    parsedRequest.errorBuffer);
        }

        // Resolve any STAF variables in the to option's value

        // Resolve any STAF variables in the mail server option's value

        STAFResult res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("MAILSERVER"), fHandle, var_1.requestNumber
        );

        if (res.rc != STAFResult.Ok) return res;

        String mailServer = res.result;

        res = new STAFResult();

        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("TO"), fHandle, var_1.requestNumber
        );

        if (res.rc != STAFResult.Ok) return res;

        String to = res.result;

        // Resolve any STAF variables in the from option's value

        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("FROM"), fHandle, var_1.requestNumber
        );

        if (res.rc != STAFResult.Ok) return res;

        String from = res.result;

        // Resolve any STAF variables in the contents option's value

        res = STAFUtil.resolveRequestVar(
                parsedRequest.optionValue("CONTENTS"), fHandle, var_1.requestNumber
        );

        if (res.rc != STAFResult.Ok) return res;

        String contents = res.result;

        // Create temp eml file

        mailInfo.setContents(contents);
        mailInfo.setTo(to);
        mailInfo.setFrom(from);
        mailInfo.setMailServer(mailServer);

        // Send Mail

        SendMailResult result = mailer.send(mailInfo);

        String logMsg = result.result;

        fHandle.submit2(
                "local", "LOG", "LOG MACHINE LOGNAME " + fServiceName +
                        " LEVEL info MESSAGE " + STAFUtil.wrapData(logMsg));

        return new STAFResult(result.rc, logMsg);
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

    // Register error codes for this service with the HELP service

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