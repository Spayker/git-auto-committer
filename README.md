[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/Spayker/git-auto-committer/blob/master/LICENSE)
[![Build Status](https://api.travis-ci.com/Spayker/git-auto-committer.svg?branch=main)](https://travis-ci.com/Spayker/git-auto-committer)

# jgac - PoC

Basically a try to automate typical and annoying processes while working with code in git environment. Can be run
after OS starts working for instance. Does not create PRs but commits and pushes local changes to origin repos.

## Project structure
Contains couple libraries including:
1) jackson-dataformat-yaml reads yml files and transforms data into appropriate model java class 
2) ScheduledExecutorService java api that helps in running of separate tasks

Additionally, the project uses ProcessBuilder java api approach to assemble git based commands and run them </br>
through installed git application.

## How to use
Has flexible options to be run:
1) yml config file - file can be left in same folder where app's jar is located. Example can be taken from resources folder </br>
2) with project path input parameter

Typical run command:
java -jar "jgac.yml" ~/projects

## Logging
Logs can be found in same folder where running jar is located. Configuration of logs is done through log4j.properties file stored in resources.

## Next steps
Implement PR creation logic.

## Links
Git token generation: https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token </br>
jackson-dataformat-yaml off.: https://github.com/FasterXML/jackson-dataformat-yaml </br>