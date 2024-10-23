package io.jenkins.plugins.sample;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class OnboardingTaskBuilder extends Builder implements SimpleBuildStep {

    private final String name;
    private String selectedCategory;
    @DataBoundConstructor
    public OnboardingTaskBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setSelectedCategory(String category) {
         this.selectedCategory = category;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        OnboardingSectionConfiguration onboardingSectionConfig = GlobalConfiguration.all()
                .get(OnboardingSectionConfiguration.class);
        if (Objects.nonNull(onboardingSectionConfig)) {
            Optional<CategoryEntry> categoryEntryOptional = onboardingSectionConfig.getCategoryConfig().getCategories()
                    .stream()
                    .filter(category -> category.getUuid().equals(selectedCategory)).findFirst();
            listener.getLogger().println("I'm Onboarding task build step" + (categoryEntryOptional
                    .map(categoryEntry -> "and I have selected " + categoryEntry.getName()).orElse("")));
        }
    }

    @Symbol("onboarding task")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingName());
            if (value.length() < 4)
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_tooShort());
            return FormValidation.ok();
        }

        public List<String> getCategories() {
            List<String> categoryNames = List.of();
            OnboardingSectionConfiguration onboardingSectionConfig = GlobalConfiguration.all().get(OnboardingSectionConfiguration.class);
            if (Objects.nonNull(onboardingSectionConfig)) {
                categoryNames = onboardingSectionConfig.getCategoryConfig().getCategories().stream()
                        .map(category -> category.getName()).collect(Collectors.toList());
            }
            return categoryNames; // Retrieve categories from global config
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Onboarding task step";
        }

        public ListBoxModel doFillSelectedCategoryItems() {
            ListBoxModel items = new ListBoxModel();
            OnboardingSectionConfiguration onboardingSectionConfig = GlobalConfiguration.all().get(OnboardingSectionConfiguration.class);
            System.out.println(onboardingSectionConfig == null);
            if (Objects.nonNull(onboardingSectionConfig)) {
                onboardingSectionConfig.getCategoryConfig().getCategories().forEach(
                        category -> items.add(category.getName(), category.getUuid())
                );
            }
            return items;
        }
    }

}


