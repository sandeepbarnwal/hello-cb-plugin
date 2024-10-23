# hello-cb

## Introduction

This project contains tasks from https://engineering.beescloud.com/docs/engineering-cbci/latest/onboarding/practical-work-plugin

## Getting started

1. Create a new pipeline and use below script under script section 

```groovy
pipeline {
    agent any
    // parameters {
    //     string(name: 'SELECTED_CATEGORY', defaultValue: 'category1', description: 'A string parameter for categories')
    // }
    stages {
        stage('Hello') {
            steps {
                script {
                    // Call greet step if it exists
                    greet('cloudbees')

                    // Step 1: Alternative of existing step's config.jelly
                    def choices = getCategoryChoices()
                    def inputParams = input(
                        message: 'Select a category:',
                        parameters: [choice(choices: choices, name: 'Categories')]
                    )
                    echo "You selected: ${inputParams}"

                    // Step 2: Create onboarding step object, set selected category and execute the step
                }
            }
        }
    }
}

def getCategoryChoices() {
    def choices = []
    def onboardingConfig = jenkins.model.GlobalConfiguration.all().get(io.jenkins.plugins.sample.OnboardingSectionConfiguration.class)
    if (onboardingConfig) {
        def categoryNames = onboardingConfig.getCategoryConfig().getCategories().collect { it.name.toString() }
        choices.addAll(categoryNames)
    } else {
        echo "Onboarding configuration not found."
    }
    return choices
}
```   
## Issues

## Contributing

TODO review the default [CONTRIBUTING](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md) file and make sure it is appropriate for your plugin, if not then add your own one adapted from the base file

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

