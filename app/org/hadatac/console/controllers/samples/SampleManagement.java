package org.hadatac.console.controllers.samples;

import java.util.List;
import java.net.URLDecoder;

import org.hadatac.entity.pojo.Study;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.samples.*;
import play.mvc.Result;
import play.mvc.Controller;

public class SampleManagement extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String std_uri) {

	std_uri = URLDecoder.decode(std_uri);
    	Study std = Study.find(std_uri);
    	
        return ok(sampleManagement.render(std));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String std_uri) {
        return index(std_uri);
    }
}