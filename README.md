# hello-cb

## Introduction

This project contains tasks from https://engineering.beescloud.com/docs/engineering-cbci/latest/onboarding/practical-work-plugin

## Getting started

1. Create a new pipeline and use below script under script section 

```groovy
pipeline {
    agent any
    stages {
        stage('Hello') {
            steps {
                script {
                    greet('cloudbees')

                    // Step 1: Alternative of existing step's config.jelly
                    def choicesMap = getCategoryChoicesMap()
                    def choices = choicesMap.collect { key, value -> key }.join('\n')
                    def inputParams = input(
                            message: 'Select a category:',
                            parameters: [choice(choices: choices, name: 'Categories')]
                    )
                    echo "You selected: ${inputParams}"

                    // Step 2: Create onboarding step object, set selected category and execute the step
                    step([$class: 'OnboardingTaskBuilder', name: 'onboarding step 2', selectedCategory: choicesMap[inputParams]])


                }
            }
        }
    }
}

def getCategoryChoicesMap() {
    def choices = []
    def choicesMap = [:]
    def onboardingConfig = jenkins.model.GlobalConfiguration.all().get(io.jenkins.plugins.sample.OnboardingSectionConfiguration.class)
    if (onboardingConfig) {
        //Check if possible to get this from step's descriptor
        def categories = onboardingConfig.getCategoryConfig().getCategories().collect {it}
        categories.each { category ->
            choicesMap[category.name] = category.uuid
        }
    } else {
        echo "Onboarding configuration not found."
    }
    return choicesMap
}

```   
## Issues

## Contributing

TODO review the default [CONTRIBUTING](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md) file and make sure it is appropriate for your plugin, if not then add your own one adapted from the base file

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

