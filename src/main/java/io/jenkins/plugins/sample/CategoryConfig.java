package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CategoryConfig extends AbstractDescribableImpl<CategoryConfig> {

    private final List<CategoryEntry> categories;

    @DataBoundConstructor
    public CategoryConfig(List<CategoryEntry> categories) {
        this.categories = categories != null ? new ArrayList<CategoryEntry>(categories) : Collections.<CategoryEntry>emptyList();
    }

    public List<CategoryEntry> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<CategoryConfig> {}

}
