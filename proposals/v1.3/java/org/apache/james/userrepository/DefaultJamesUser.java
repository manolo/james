/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.james.userrepository;

import java.io.Serializable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.james.services.User;
import org.apache.james.services.JamesUser;

/**
 * Implementation of User Interface.
 *
 * @author Charles Benett <charles@benett1.demon.co.uk>
 *
 * Last changed by: $Author: charlesb $ on $Date: 2001/05/16 14:00:35 $
 * $Revision: 1.1 $
 */

public class DefaultJamesUser 
        extends DefaultUser
        implements JamesUser, Initializable {

    private boolean forwarding;
    private String forwardingDestination;
    private boolean aliasing;
    private String alias;

    public DefaultJamesUser(String name, String pass) {
	super(name, pass);
    }

    /**
     * Call initialize when creating a new instance.
     */
    public void initialize() {
	forwarding = false;
	forwardingDestination = "";
	aliasing = false;
	alias = "";
    }

    public boolean setPassword(String pass) {
	return setPass(pass);
    }

    public void setForwarding(boolean forward) {
	forwarding = forward;
    }

    public boolean getForwarding() {
	return forwarding;
    }

    
    public boolean setForwardingDestination(String address) {
	/* Some verification would be good */
	forwardingDestination = address;
	return true;
    }

    public String getForwardingDestination() {
	return forwardingDestination;
    }

    public void setAliasing(boolean alias) {
        aliasing = alias;
    }

    public boolean getAliasing() {
	return aliasing;
    }

    public boolean setAlias(String address) {
	/* Some verification would be good */
	alias = address;
	return true;
    }

    public String getAlias() {
	return alias;
    }
}
