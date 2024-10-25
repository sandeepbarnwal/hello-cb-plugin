package io.jenkins.plugins.sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
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
import java.io.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class OnboardingTaskBuilder extends Builder implements SimpleBuildStep {

    private static final String CATEGORY_JOBS_FILE = "category_jobs.json";
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter())
            .setPrettyPrinting().create();
    private static final Object lock = new Object();

    public static class InstantAdapter extends TypeAdapter<Instant> {
        @Override
        public void write(JsonWriter out, Instant value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public Instant read(JsonReader in) throws IOException {
            return Instant.parse(in.nextString());
        }
    }

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

    public String getSelectedCategory() {
        return selectedCategory;
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
            listener.getLogger().println("I'm " + name + (categoryEntryOptional
                    .map(categoryEntry -> " and I have selected " + categoryEntry.getName()).orElse("")));
            categoryEntryOptional.ifPresent(categoryEntry -> saveJobNamePerCategory(run.getFullDisplayName(), categoryEntry.getName()));
        }
    }

    private void saveJobNamePerCategory(String jobName, String category) {
        File file = new File(CATEGORY_JOBS_FILE);
        synchronized (lock) {
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Map<String, List<JobRunEntry>> categoryJobsMap;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                Type type = new TypeToken<Map<String,List<JobRunEntry>>>(){}.getType();
                categoryJobsMap = gson.fromJson(reader, type);
                if (categoryJobsMap == null) {
                    categoryJobsMap = new HashMap<>();
                }
            } catch (IOException | JsonSyntaxException e) {
                categoryJobsMap = new HashMap<>();
            }

            categoryJobsMap.computeIfAbsent(category, k -> new ArrayList<>())
                    .add(new JobRunEntry(jobName, Instant.now()));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                gson.toJson(categoryJobsMap, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

public static class JobRunEntry {
    private final String jobName;
    private final Instant timestamp;

    public JobRunEntry(String jobName, Instant timestamp) {
        this.jobName = jobName;
        this.timestamp = timestamp;
    }

    public String getJobName() {
        return jobName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}

    //TODO: This approach could be slow if there too many jobs. Another way could be to maintain the last job run
    // separate in the same json file.
    public static String getLastJobRun() {
        synchronized (lock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(CATEGORY_JOBS_FILE))) {
                Type type = new TypeToken<Map<String,List<JobRunEntry>>>(){}.getType();
                Map<String, List<JobRunEntry>> categoryJobsMap = gson.fromJson(reader, type);

                if (categoryJobsMap == null || categoryJobsMap.isEmpty())
                    return "";

                JobRunEntry lastJobRun = null;
                for (List<JobRunEntry> jobRunEntries: categoryJobsMap.values()) {
                    for(JobRunEntry jre: jobRunEntries) {
                        if (lastJobRun == null || lastJobRun.timestamp.isBefore(jre.timestamp)) {
                            lastJobRun = jre;
                        }
                    }
                }
                return lastJobRun == null ? "" : lastJobRun.getJobName();
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    @Symbol("onboarding")
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
            return categoryNames;
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


