package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Pattern;

@Extension
public class OnboardingSectionConfiguration extends GlobalConfiguration {

    private String name;

    private String description;

    private String url;

    private String username;

    private String password;


    public OnboardingSectionConfiguration() {
        load();
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        save();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        save();
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        save();
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
//        if (validateName(name))
//            save();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FormValidation doCheckName(@QueryParameter String value) {
        if(Objects.isNull(value) || value.isBlank()) {
            return FormValidation.warning("Name is empty");
        } else {
            if (!validateName(value)) {
                return FormValidation.warning("Invalid name data");
            }
        }
        return FormValidation.ok("All good!");
    }

    private boolean validateName(String name) {
        Pattern pattern = Pattern.compile("^[A-Za-z\\- ]+$");
        if (!pattern.matcher(name).matches()) {
            return false;
        }
        return true;
    }

    public FormValidation doCheckUsername(@QueryParameter String value) {
        if(Objects.isNull(value) || value.isBlank()) {
            return FormValidation.warning("Username is empty");
        } else {
            Pattern pattern = Pattern.compile("^[A-Za-z]+$");
            if (!pattern.matcher(value).matches()) {
                return FormValidation.warning("Invalid Username Input");
            }
        }
        return FormValidation.ok("All good!");
    }

    public FormValidation doCheckPassword(@QueryParameter String value) {
        if(Objects.isNull(value) || value.isBlank()) {
            return FormValidation.warning("Password is empty");
        } else {
        }
        return FormValidation.ok("All good!");
    }
}

