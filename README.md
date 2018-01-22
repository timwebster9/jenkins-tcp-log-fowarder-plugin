# Jenkins TCP Log Forwarder Plugin

Streams Jenkins console log to a remote TCP endpoint.

## Usage

### Configuration

* Go to Manage Jenkins -> Configure System
* Scroll down to 'Tcp Log Forwarder Plugin'
* Tick 'Enable'
* Fill in the fields for host and port

### Freestyle Projects

All that is required is that the plugin is enabled and configured.

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
