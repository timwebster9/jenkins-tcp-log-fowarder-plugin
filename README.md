# Jenkins TCP Log Forwarder Plugin

##### Generic plugin that streams Jenkins _console_ logs to a remote TCP endpoint.

Each line of the log will be prefixed with the job name and number, for example:

    my-java-job #2 - [my-java-job] Running shell script
    my-java-job #2 - + docker inspect -f . maven:3-alpine
    my-java-job #2 - .
    my-java-job #2 - 501919691f33-114b94e7 seems to be running inside container 501919691f33f0ca086c64f080ba0b62180a667231a69869d282b54768972263
    my-java-job #2 - [my-java-job] Running shell script
    my-java-job #2 - + mvn -B clean package

## Usage

### Configuration

* Go to Manage Jenkins -> Configure System
* Scroll down to 'Tcp Log Forwarder Plugin'
* Tick 'Enable'
* Fill in the fields for host and port

### Freestyle Projects

All that is required is that the plugin is enabled and configured.  Logs for all jobs will be streamed.

### Scripted Pipeline

     node {
         tcpForwardLog {

             checkout scm

             stage('Hello') {
                 echo 'Hello World'
             }
             stage('Goodbye') {
                 echo 'Goodbye World'
             }
         }
     }

### Declarative Pipeline

    pipeline {

        agent any

        options {
            tcpForwardLog()
        }
        stages {
            stage('Hello') {
                steps {
                    echo 'Hello World'
                }
            }
            stage('Goodbye') {
                steps {
                    echo 'Goodbye World'
                }
            }
        }
    }

## Development

### Building
To create the .hpi file, which can be installed via the Jenkins UI:

    mvn clean package

### Testing Locally
To start a local Jenkins instance with the plugin installed:

    mvn hpi:run

Jenkins can then be reached at http://localhost:8080/jenkins/

## CI
[![CircleCI](https://circleci.com/gh/timwebster9/jenkins-tcp-log-fowarder-plugin.svg?style=svg)](https://circleci.com/gh/timwebster9/jenkins-tcp-log-fowarder-plugin)
