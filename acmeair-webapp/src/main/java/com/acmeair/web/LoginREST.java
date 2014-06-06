/*******************************************************************************
* Copyright (c) 2013 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.acmeair.web;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.acmeair.entities.CustomerSession;
import com.acmeair.service.*;


@Path("/login")
public class LoginREST {
	
	public static String SESSIONID_COOKIE_NAME = "sessionid";
	
	private CustomerService customerService = ServiceLocator.instance().getService(CustomerService.class);
	
	
	@POST
	@Consumes({"application/x-www-form-urlencoded"})
	@Produces("text/plain")
	public Response login(@FormParam("login") String login, @FormParam("password") String password) {
		try {
			boolean validCustomer = customerService.validateCustomer(login, password);
			
			if (!validCustomer) {
				return Response.status(Response.Status.FORBIDDEN).build();
			}
			
			CustomerSession session = customerService.createSession(login);
			// TODO:  Need to fix the security issues here - they are pretty gross likely
			NewCookie sessCookie = new NewCookie(SESSIONID_COOKIE_NAME, session.getId());
			// TODO: The mobile client app requires JSON in the response. 
			// To support the mobile client app, choose one of the following designs:
			// - Change this method to return JSON, and change the web app javascript to handle a JSON response.
			//   example:  return Response.ok("{\"status\":\"logged-in\"}").cookie(sessCookie).build();
			// - Or create another method which is identical to this one, except returns JSON response.
			//   Have the web app use the original method, and the mobile client app use the new one.
			return Response.ok("logged in").cookie(sessCookie).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@GET
	@Path("/logout")
	@Produces("text/plain")
	public Response logout(@QueryParam("login") String login, @CookieParam("sessionid") String sessionid) {
		try {
			customerService.invalidateSession(sessionid);
			// The following call will trigger query against all partitions, disable for now
//			customerService.invalidateAllUserSessions(login);
			
			// TODO:  Want to do this with setMaxAge to zero, but to do that I need to have the same path/domain as cookie
			// created in login.  Unfortunately, until we have a elastic ip and domain name its hard to do that for "localhost".
			// doing this will set the cookie to the empty string, but the browser will still send the cookie to future requests
			// and the server will need to detect the value is invalid vs actually forcing the browser to time out the cookie and
			// not send it to begin with
			NewCookie sessCookie = new NewCookie(SESSIONID_COOKIE_NAME, "");
			return Response.ok("logged out").cookie(sessCookie).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
