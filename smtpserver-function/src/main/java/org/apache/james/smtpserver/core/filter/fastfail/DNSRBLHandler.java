/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/



package org.apache.james.smtpserver.core.filter.fastfail;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.james.api.dnsservice.DNSService;
import org.apache.james.dsn.DSNStatus;
import org.apache.james.smtpserver.CommandHandler;
import org.apache.james.smtpserver.ConnectHandler;
import org.apache.james.smtpserver.SMTPSession;
import org.apache.james.smtpserver.junkscore.JunkScore;
import org.apache.mailet.MailAddress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

/**
  * Connect handler for DNSRBL processing
  */
public class DNSRBLHandler
    extends AbstractJunkHandler
    implements ConnectHandler, CommandHandler, Configurable, Serviceable {
    /**
     * The lists of rbl servers to be checked to limit spam
     */
    private String[] whitelist;
    private String[] blacklist;
    
    private DNSService dnsServer = null;
    
    private boolean getDetail = false;
    
    private String blocklistedDetail = null;
    
    public static final String RBL_BLOCKLISTED_MAIL_ATTRIBUTE_NAME = "org.apache.james.smtpserver.rbl.blocklisted";
    
    public static final String RBL_DETAIL_MAIL_ATTRIBUTE_NAME = "org.apache.james.smtpserver.rbl.detail";

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration handlerConfiguration) throws ConfigurationException {
        boolean validConfig = false;

        Configuration rblserverConfiguration = handlerConfiguration.getChild("rblservers", false);
        if ( rblserverConfiguration != null ) {
            ArrayList rblserverCollection = new ArrayList();
            Configuration[] children = rblserverConfiguration.getChildren("whitelist");
            if ( children != null ) {
                for ( int i = 0 ; i < children.length ; i++ ) {
                    String rblServerName = children[i].getValue();
                    rblserverCollection.add(rblServerName);
                    if (getLogger().isInfoEnabled()) {
                        getLogger().info("Adding RBL server to whitelist: " + rblServerName);
                    }
                }
                if (rblserverCollection != null && rblserverCollection.size() > 0) {
                    setWhitelist((String[]) rblserverCollection.toArray(new String[rblserverCollection.size()]));
                    rblserverCollection.clear();
                    validConfig = true;
                }
            }
            children = rblserverConfiguration.getChildren("blacklist");
            if ( children != null ) {
                for ( int i = 0 ; i < children.length ; i++ ) {
                    String rblServerName = children[i].getValue();
                    rblserverCollection.add(rblServerName);
                    if (getLogger().isInfoEnabled()) {
                        getLogger().info("Adding RBL server to blacklist: " + rblServerName);
                    }
                }
                if (rblserverCollection != null && rblserverCollection.size() > 0) {
                    setBlacklist((String[]) rblserverCollection.toArray(new String[rblserverCollection.size()]));
                    rblserverCollection.clear();
                    validConfig = true;
                }
            }
        }
        
        // Throw an ConfiigurationException on invalid config
        if (validConfig == false){
            throw new ConfigurationException("Please configure whitelist or blacklist");
        }
        
        Configuration configuration = handlerConfiguration.getChild("getDetail",false);
        if(configuration != null) {
           getDetail = configuration.getValueAsBoolean();
        }
        
        super.configure(handlerConfiguration);

    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(ServiceManager)
     */
    public void service(ServiceManager serviceMan) throws ServiceException {
        setDNSServer((DNSService) serviceMan.lookup(DNSService.ROLE));
    }
    
    /**
     * check if the remote Ip address is block listed
     *
     * @see org.apache.james.smtpserver.ConnectHandler#onConnect(SMTPSession)
    **/
    public void onConnect(SMTPSession session) {
        checkDNSRBL(session, session.getRemoteIPAddress());
    }
    
    /**
     * Set the whitelist array
     * 
     * @param whitelist The array which contains the whitelist
     */
    public void setWhitelist(String[] whitelist) {
        this.whitelist = whitelist;
    }
    
    /**
     * Set the blacklist array
     * 
     * @param blacklist The array which contains the blacklist
     */
    public void setBlacklist(String[] blacklist) {
        this.blacklist = blacklist;
    }
    
    /**
     * Set the DNSService
     * 
     * @param dnsServer The DNSService
     */
    public void setDNSServer(DNSService dnsServer) {
        this.dnsServer = dnsServer;
    }

    /**
     * Set for try to get a TXT record for the blocked record. 
     * 
     * @param getDetail Set to ture for enable
     */
    public void setGetDetail(boolean getDetail) {
        this.getDetail = getDetail;
    }

    /**
     *
     * This checks DNSRBL whitelists and blacklists.  If the remote IP is whitelisted
     * it will be permitted to send e-mail, otherwise if the remote IP is blacklisted,
     * the sender will only be permitted to send e-mail to postmaster (RFC 2821) or
     * abuse (RFC 2142), unless authenticated.
     */

    public void checkDNSRBL(SMTPSession session, String ipAddress) {
        
        /*
         * don't check against rbllists if the client is allowed to relay..
         * This whould make no sense.
         */
        if (session.isRelayingAllowed()) {
            getLogger().info("Ipaddress " + session.getRemoteIPAddress() + " is allowed to relay. Don't check it");
            return;
        }
        
        if (whitelist != null || blacklist != null) {
            StringBuffer sb = new StringBuffer();
            StringTokenizer st = new StringTokenizer(ipAddress, " .", false);
            while (st.hasMoreTokens()) {
                sb.insert(0, st.nextToken() + ".");
            }
            String reversedOctets = sb.toString();

            if (whitelist != null) {
                String[] rblList = whitelist;
                for (int i = 0 ; i < rblList.length ; i++) try {
                    dnsServer.getByName(reversedOctets + rblList[i]);
                    if (getLogger().isInfoEnabled()) {
                        getLogger().info("Connection from " + ipAddress + " whitelisted by " + rblList[i]);
                    }
                    
                    return;
                } catch (java.net.UnknownHostException uhe) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("IpAddress " + session.getRemoteIPAddress() + " not listed on " + rblList[i]);
                    }
                }
            }

            if (blacklist != null) {
                String[] rblList = blacklist;
                for (int i = 0 ; i < rblList.length ; i++) try {
                    dnsServer.getByName(reversedOctets + rblList[i]);
                    if (getLogger().isInfoEnabled()) {
                        getLogger().info("Connection from " + ipAddress + " restricted by " + rblList[i] + " to SMTP AUTH/postmaster/abuse.");
                    }
                    
                    // we should try to retrieve details
                    if (getDetail) {
                        Collection txt = dnsServer.findTXTRecords(reversedOctets + rblList[i]);
                        
                        // Check if we found a txt record
                        if (!txt.isEmpty()) {
                            // Set the detail
                            String blocklistedDetail = txt.iterator().next().toString();
                            
                            session.getConnectionState().put(RBL_DETAIL_MAIL_ATTRIBUTE_NAME, blocklistedDetail);
                        }
                    }
                    
                    session.getConnectionState().put(RBL_BLOCKLISTED_MAIL_ATTRIBUTE_NAME, "true");
                    return;
                } catch (java.net.UnknownHostException uhe) {
                    // if it is unknown, it isn't blocked
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("unknown host exception thrown:" + rblList[i]);
                    }
                }
            }
        }
    }

    /**
     * @see org.apache.james.smtpserver.CommandHandler#getImplCommands()
     */
    public Collection getImplCommands() {
        Collection commands = new ArrayList();
        commands.add("RCPT");
        return commands;
    }

    /**
     * @see org.apache.james.smtpserver.CommandHandler#onCommand(SMTPSession)
     */
    public void onCommand(SMTPSession session) {
        doProcessing(session);       
    }

    /**
     * @see org.apache.james.smtpserver.core.filter.fastfail.AbstractJunkHandler#check(org.apache.james.smtpserver.SMTPSession)
     */
    protected boolean check(SMTPSession session) {
        String blocklisted = (String) session.getConnectionState().get(RBL_BLOCKLISTED_MAIL_ATTRIBUTE_NAME);
        MailAddress recipientAddress = (MailAddress) session.getState().get(
                SMTPSession.CURRENT_RECIPIENT);

        return (blocklisted != null && // was found in the RBL
                !(session.isAuthRequired() && session.getUser() != null) && // Not (SMTP AUTH is enabled and not authenticated)
                !(recipientAddress.getUser().equalsIgnoreCase("postmaster") || recipientAddress.getUser().equalsIgnoreCase("abuse")));
    }

    /**
     * @see org.apache.james.smtpserver.core.filter.fastfail.AbstractJunkHandler#getJunkScore(org.apache.james.smtpserver.SMTPSession)
     */
    protected JunkScore getJunkScore(SMTPSession session) {
        return (JunkScore) session.getConnectionState().get(JunkScore.JUNK_SCORE_SESSION);
    }
    
    /**
     * @see org.apache.james.smtpserver.core.filter.fastfail.AbstractJunkHandler#getJunkHandlerData(org.apache.james.smtpserver.SMTPSession)
     */
    public JunkHandlerData getJunkHandlerData(SMTPSession session) {
        JunkHandlerData data = new JunkHandlerData();
        
        data.setJunkScoreLogString("Ipaddress " + session.getRemoteIPAddress() + " listed on RBL. Add junkScore: " + getScore());
        data.setRejectLogString("ipaddress " + session.getRemoteIPAddress() + " listed on RBL. Reject email");
    
        if (blocklistedDetail != null) {
            data.setRejectResponseString("530 "+ DSNStatus.getStatus(DSNStatus.PERMANENT,DSNStatus.SECURITY_AUTH) + " " + blocklistedDetail);
        } else {
            data.setRejectResponseString("530 "+ DSNStatus.getStatus(DSNStatus.PERMANENT,
                            DSNStatus.SECURITY_AUTH)  + " Rejected: unauthenticated e-mail from " + session.getRemoteIPAddress() 
                            + " is restricted.  Contact the postmaster for details.");
        }
        data.setScoreName("DNSRBLCheck");
        return data;
    }
}