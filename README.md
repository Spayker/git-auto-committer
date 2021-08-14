[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/Spayker/git-auto-committer/blob/master/LICENSE)
[![Build Status](https://travis-ci.com/Spayker/git-auto-committer.svg?branch=main)](https://travis-ci.org/Spayker/git-auto-committer)

# jgac - PoC

Basically a try to automate typical and annoying processes while working with code in git environment. Can be run
after OS starts working for instance. Does not create PRs but commits and pushes local changes to origin repos.

## Project structure
Contains few libraries including:
1) jgit presents all git related commands that can be run from java app
2) jackson-dataformat-yaml reads yml files and transforms data into appropriate model java class 
3) ScheduledExecutorService java api that helps in running of separate tasks

Additionally, the project is divided on two different approaches: </br>
a) based on jgit usage (more-less stable and checked in practice) </br>
b) based on ProcessBuilder java api **(not yet ready)** </br>

## How to use
Has flexible options to be run:
1) yml config file - file can be left in same folder where app's jar is located. Example can be taken from resources folder </br>
2) with set of input parameters: git token, project folders 

Typical run command:
java -jar "jgac.yml" ~/projects ghp_qbBFsR2iGbAWHe3redHyExz9dwpMp21qvgy

## Logging
Logs can be found in same folder where running jar is located. Configuration of logs is done through log4j.properties file stored in resources.

## Found issues
During first implementation stage jgit library was used to link app with git side. The library works fine in general,
but it has issues with committing of missed files/folders. Application can detect missed or removed resources, but
it can't commit them before pushing.

## Next steps
Since auto committer is going to become one of few main components for next, more global project, all found issues
have to be solved.

## Links
Git token generation: https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token </br>
Jgit off.: https://www.eclipse.org/jgit/ </br>
jackson-dataformat-yaml off.: https://github.com/FasterXML/jackson-dataformat-yaml </br>