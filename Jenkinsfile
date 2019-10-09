pipeline {
  agent none
  stages {
    stage('test') {
      parallel {
        stage('test') {
          steps {
            sh 'echo "11"'
          }
        }
        stage('test1') {
          steps {
            sh 'echo "222"'
          }
        }
      }
    }
  }
}