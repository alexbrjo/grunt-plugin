grunt-plugin
============
Making Grunt a little easier

Goal
----
To get something like the following to run successfully
```
pipeline {
    agent any  
    tools { 
        grunt 'Grunt 4.0.0' 
        Node '7.0.0' 
    }
    stages { 
        stage('Build') { 
            steps { 
               grunt 'build'
            }
        }
        stage() {
           steps {
               grunt 'test'
           }
        }
    }
}
```

Use Cases
---------
* Global Grunt-cli is installed: uses
* No global Grunt-cli: WARN and download local

|Tested?| Global NodeJS | NodeJS installations | Expected |
|-------|---------------|----------------------|------------------------------|
|  No   |     Yes       |          0           |Falls back to global user installation|
|  No   |     Yes       |          1           |Uses global unless specified in config|
|  No   |      No       |          0           |ERROR cannot run without NodeJS installation|
|  No   |      No       |          1           |WARN if not specified in config, use installation|

Not Tested
----------
* Multiple Node installations
* Compatibility with any other plugins
* Running on other nodes
