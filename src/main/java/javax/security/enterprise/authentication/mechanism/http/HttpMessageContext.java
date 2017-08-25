/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package javax.security.enterprise.authentication.mechanism.http;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.MessageInfo;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.CredentialValidationResult.Status;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <code>HttpMessageContext</code> contains all of the per-request state information and encapsulates the client request, 
 * server response, container handler for authentication callbacks, and the subject representing the caller.
 *
 */
public interface HttpMessageContext {

    /**
     * Checks if the currently requested resource is protected or not. A protected resource
     * is a resource (e.g. a Servlet, JSF page, JSP page etc) for which a constraint has been defined
     * in e.g. <code>web.xml</code>.
     * 
     * @return true if a protected resource was requested, false if a public resource was requested.
     */
    boolean isProtected();

    /**
     * Checks if the current call to an authentication mechanism is the result from the 
     * application calling {@link SecurityContext#authenticate(HttpServletRequest, HttpServletResponse, AuthenticationParameters)}
     * <p>
     * If SecurityContext#authenticate was not called, the authentication mechanism may have been invoked by the 
     * container at the start of a request.
     * 
     * @return true if SecurityContext#authenticate was called, false if not.
     */
    boolean isAuthenticationRequest();

    /**
     * Check if the runtime has been asked to register an authentication session duing the current request.
     * 
     * @return true if code has asked to register an authentication session, false otherwise.
     */
    boolean isRegisterSession();

    /**
     * Asks the runtime to register an authentication session. This will automatically remember the logged-in status
     * as long as the current HTTP session remains valid. Without this being asked, a {@link HttpAuthenticationMechanism} 
     * has to manually re-authenticate with the runtime at the start of each request.
     * 
     * @param callerName the caller name for which authentication should be be remembered
     * @param groups the groups for which authentication should be remembered.
     */
    void setRegisterSession(String callerName, Set<String> groups);

    /**
     * Convenience method to clean the subject associated with this context.
     * 
     * <p>
     * Cleaning this subject is done as defined by the Servlet Container Profile of JASPIC
     * (JSR 196) for the ServerAuthModule#cleanSubject method and the 
     * {@link HttpAuthenticationMechanism#cleanSubject(HttpServletRequest, HttpServletResponse, HttpMessageContext)} 
     * method defined by this specification.
     */
    void cleanClientSubject();

    /**
     * Returns the parameters that were provided with the SecurityContext#authenticate(AuthParameters) call.
     *  
     * @return the parameters that were provided with the SecurityContext#authenticate(AuthParameters) call, or a default instance. Never null.
     */
    AuthenticationParameters getAuthParameters();

    /**
     * Returns the low level JSR 196 handler that the runtime provided when creating this {@link HttpMessageContext},
     * and which this context uses to communicate the authentication details to the runtime.
     * 
     * <p>
     * <em>Note:</em> This is a low level object that most higher level code would not need to use directly.
     * 
     * @return the handler that the runtime provided to this context
     */
    CallbackHandler getHandler();

    /**
     * Returns the the low level JSR 196 message info instance for the current request.
     * 
     * <p>
     * <em>Note:</em> This is a low level object that most higher level code would not need to use directly.
     * 
     * @return the message info instance for the current request.
     */
    MessageInfo getMessageInfo();

    /**
     * Returns the subject for which authentication is to take place.
     * 
     * <p>
     * <em>Note:</em> This is a low level object that most higher level code would not need to use directly.
     * 
     * @return the subject for which authentication is to take place.
     */
    Subject getClientSubject();

    /**
     * Returns the request object associated with the current request.
     * 
     * @return the request object associated with the current request.
     */
    HttpServletRequest getRequest();
    
    /**
     * Sets the request object.
     * 
     * @param request the request object to be set
     * 
     */
    void setRequest(HttpServletRequest request);
    
    /**
     * Sets the request object.
     * 
     * @param request the request object to be set.
     * 
     * @return the HttpMessageContext instance on which this method was called, useful for
     * fluent style call call chains.
     */
    HttpMessageContext withRequest(HttpServletRequest request);

    /**
     * Returns the response object associated with the current request.
     * 
     * @return the response object associated with the current request.
     */
    HttpServletResponse getResponse();
    
    /**
     * Set the response object.
     * 
     * @param response the response object to be set.
     */
    void setResponse(HttpServletResponse response);
    
    /**
     * Sets the response status to SC_FOUND 302 (Found)
     * 
     * <p>
     * As a convenience this method returns SEND_CONTINUE, so this method can be used in
     * one fluent return statement from an {@link HttpAuthenticationMechanism}
     * 
     * @param location the location to redirect to
     * 
     * @return {@link AuthenticationStatus#SEND_CONTINUE}
     * 
     * @see HttpServletResponse#sendRedirect(String)
     */
    AuthenticationStatus redirect(String location);
    
    /**
     * Forwards to another resource (servlet, JSP file, or HTML file) on the server.
     * 
     * <p>
     * As a convenience this method returns SEND_CONTINUE, so this method can be used in
     * one fluent return statement from an {@link HttpAuthenticationMechanism}
     * 
     * @param path a String specifying the pathname to the resource.
     * 
     * @return {@link AuthenticationStatus#SEND_CONTINUE}
     * 
     * @see RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    AuthenticationStatus forward(String path);

    /**
     * Sets the response status to 401 (unauthorized).
     * <p>
     * As a convenience this method returns SEND_FAILURE, so this method can be used in
     * one fluent return statement from an {@link HttpAuthenticationMechanism}
     * 
     * @return {@link AuthenticationStatus#SEND_FAILURE}
     */
    AuthenticationStatus responseUnauthorized();

    /**
     * Sets the response status to 404 (not found).
     * <p>
     * As a convenience this method returns SEND_FAILURE, so this method can be used in
     * one fluent return statement from an {@link HttpAuthenticationMechanism}
     * 
     * @return {@link AuthenticationStatus#SEND_FAILURE}
     */
    AuthenticationStatus responseNotFound();

    /**
     * Asks the container to register the given caller name and groups in order to make
     * them available to the application for use with {@link SecurityContext#isCallerInRole(String)} etc.
     *
     * <p>
     * Note that after this call returned, the authenticated identity will not be immediately active. This
     * will only take place (should no errors occur) after the authentication mechanism
     * in which this call takes place returns control back to the container (runtime).
     * 
     * <p>
     * As a convenience this method returns SUCCESS, so this method can be used in
     * one fluent return statement from an {@link HttpAuthenticationMechanism}
     * 
     * @param callername the caller name that will become the caller principal
     * @param groups the groups associated with the caller principal
     * @return {@link AuthenticationStatus#SUCCESS}
     *
     */
    AuthenticationStatus notifyContainerAboutLogin(String callername, Set<String> groups);
    
    /**
     * Asks the container to register the given caller principal and groups in order to make
     * them available to the application for use with {@link SecurityContext#isCallerInRole(String)} etc.
     * 
     * <p>
     * Note that this call may result in the container establishing two caller principals to
     * represent the caller's identity -- the Principal provided here as the principal parameter,
     * and a second principal used as the container's representation of the caller identity.
     * A second principal is added only if the container uses a different Principal type to
     * represent the caller. If the types are the same, only one Principal is added.
     * 
     * <p>
     * If a second principal is added, the value returned by {@link Principal#getName()}
     * will be the same for both principals.
     * 
     * <p>
     * When two principals are added, the container's caller principal is returned from
     * {@link SecurityContext#getCallerPrincipal()}, and the principal supplied here
     * as a parameter can be retrieved using {@link SecurityContext#getPrincipalsByType(Class)}.
     * When only one is added, it is returned by {@link SecurityContext#getCallerPrincipal()}.
     *
     * <p>
     * Note that after this call returned, the authenticated identity will not be immediately active. This
     * will only take place (should no errors occur) after the authentication mechanism
     * in which this call takes place returns control back to the container (runtime).
     * 
     * <p>
     * As a convenience this method returns SUCCESS, so this method can be used in
     * one fluent return statement from an {@link HttpAuthenticationMechanism}
     * 
     * @param principal the Principal that will become the caller principal
     * @param groups the groups associated with the caller principal
     * @return {@link AuthenticationStatus#SUCCESS}
     *
     */
    AuthenticationStatus notifyContainerAboutLogin(Principal principal, Set<String> groups);
    
    /**
     * Convenience method intended to pass the <code>CredentialValidationResult</code> result of an 
     * identity store directly on to the container.
     * 
     * <p>
     * If the outcome from the given {@link CredentialValidationResult#getStatus()} equals
     * {@link Status#VALID}, the {@link CallerPrincipal} and groups are obtained from the
     * <code>CredentialValidationResult</code> and passed into 
     * {@link HttpMessageContext#notifyContainerAboutLogin(Principal, Set)}.
     * 
     * <p>
     * If the outcome from the given {@link CredentialValidationResult#getStatus()} is not 
     * equal to {@link Status#VALID} a failure result is returned.
     * 
     * @param result a CredentialValidationResult which is inspected for its status and from which the principal and groups 
     * are taken.
     * 
     * @return {@link AuthenticationStatus#SUCCESS} if {@link CredentialValidationResult#getStatus()} 
     * equals {@link Status#VALID} otherwise {@link AuthenticationStatus#SEND_FAILURE}
     *
     */
    AuthenticationStatus notifyContainerAboutLogin(CredentialValidationResult result);

    /**
     * Instructs the container to "do nothing".
     * 
     * <p>
     * When intending to do nothing, a JSR 375 authentication mechanism has to indicate this
     * explicitly via its return value.
     * 
     * <p>
     * As a convenience this method returns NOT_DONE, so this method can be used in
     * one fluent return statement from an {@link HttpAuthenticationMechanism}
     * 
     * @return {@link AuthenticationStatus#NOT_DONE}
     */
    AuthenticationStatus doNothing();
    
    /**
     * Gets the Principal set by a call to notifyContainerAboutLogin().
     * 
     * @return The caller principal
     */
    Principal getCallerPrincipal();

    /**
     * Gets the groups set by a call to notifyContainerAboutLogin().
     * 
     * @return The groups
     */
    Set<String> getGroups();

}
