# hello-cb

## Introduction

This project contains tasks from https://engineering.beescloud.com/docs/engineering-cbci/latest/onboarding/practical-work-plugin

## Getting started

1. Create a new pipeline and use below script under script section 

```groovy
def categorySelected = ''

pipeline {
    agent any
    environment {
        GLOBAL_FILE = 'build_references.json' // check path
        TOTAL_BUILD_REFERENCES_TO_KEEP = 5
    }
    stages {
        stage('Hello') {
            steps {
                script {
                    getBuildReferences(env.TOTAL_BUILD_REFERENCES_TO_KEEP)
                    greet('cloudbees')

                    // Step 1: Alternative of existing step's config.jelly
                    def choicesMap = getCategoryChoicesMap()
                    def choices = choicesMap.collect { key, value -> key }.join('\n')
                    categorySelected = input(
                            message: 'Select a category:',
                            parameters: [choice(choices: choices, name: 'Categories')]
                    )
                    echo "You selected: ${categorySelected}"

                    // Step 2: Create onboarding step object, set selected category and execute the step
                    step([$class: 'OnboardingTaskBuilder', name: 'onboarding step 2', selectedCategory: choicesMap[categorySelected]])


                }
            }
        }
    }
    post {
        always {
            updateBuildReferences(currentBuild.number, categorySelected)
        }
    }
}


def getBuildReferences(noOfBuilds) {
    def currentReferences = []
    if (fileExists(env.GLOBAL_FILE)) {
        def jsonContent = readFile(env.GLOBAL_FILE)
        currentReferences = readJSON(text: jsonContent)
        while (currentReferences.size() > Integer.parseInt(noOfBuilds)) {
            currentReferences.remove(0)
        }
        echo "Last ${noOfBuilds} references:"
        currentReferences.each { reference ->
            echo "Reference: ${reference.url}"
        }
    } else {
        echo "No Existings build references to show"
    }
}

def updateBuildReferences(buildNumber, category) {
    echo "updating build reference: ${buildNumber} - ${category}"
    def currentReferences = []
    if (!fileExists(env.GLOBAL_FILE)) {
        writeFile(file: env.GLOBAL_FILE, text: '[]')
        echo "Created new file: ${env.GLOBAL_FILE}"
    }

    def jsonContent = readFile(env.GLOBAL_FILE)
    echo "Current references - ${jsonContent}"

    //def jsonSlurper = new groovy.json.JsonSlurper()
    //currentReferences = jsonSlurper.parseText(jsonContent)
    currentReferences = readJSON(text: jsonContent)

    def newReference = [
            buildNumber: buildNumber,
            url: env.BUILD_URL,
            category: category,
            timestamp: new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'")
    ]

    currentReferences.add(newReference)

    while (currentReferences.size() > Integer.parseInt(env.TOTAL_BUILD_REFERENCES_TO_KEEP)) {
        currentReferences.remove(0)
    }

    //def jsonOutput = JsonOutput.toJson(currentReferences)
    //writeFile(file: env.GLOBAL_FILE, text: JsonOutput.prettyPrint(jsonOutput))
    //echo "Updated build references: ${JsonOutput.prettyPrint(jsonOutput)}"

    def jsonOutput = writeJSON(returnText: true, json: currentReferences)
    writeFile(file: env.GLOBAL_FILE, text: jsonOutput)
    echo "Updated build references: ${jsonOutput}"
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
        echo "Onboarding configuration doesn't exist."
    }
    return choicesMap
}
```
2. Install pipeline-utility-steps
3. Approve method invocations from http://localhost:8080/jenkins/manage/scriptApproval/


```   
## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

