
## Quick Start

```bash
./gradlew run
```

Website **http://localhost:8080**

## Features

Here's a list of features included in this project:

| Name                                               | Description                                                 |
| ----------------------------------------------------|------------------------------------------------------------- |
| [Routing](https://start.ktor.io/p/routing-default) | Allows to define structured routes and associated handlers. |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                                    | Description                                                          |
| -----------------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`                        | Run the tests                                                        |
| `./gradlew build`                       | Build everything                                                     |
| `./gradlew buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `./gradlew buildImage`                  | Build the docker image to use with the fat JAR                       |
| `./gradlew publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `./gradlew run`                         | Run the server                                                       |
| `./gradlew runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## File Tree

```
Library_App_2/
├── .DS_Store
├── .gitignore
├── GIT_CHEATSHEET.md
├── README.md
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── library_booklist.csv
├── settings.gradle.kts
└── src/
    ├── .DS_Store
    └── main/
        ├── .DS_Store
        ├── kotlin/
        │   ├── Application.kt
        │   ├── Authentication.kt
        │   ├── Database.kt
        │   ├── Query.kt
        │   ├── Routing.kt
        │   ├── Tables.kt
        │   ├── Templating.kt
        │   └── UserSession.kt
        └── resources/
            ├── application.yaml
            ├── logback.xml
            ├── psw4j.properties
            └── templates/
                ├── base.peb
                ├── book.peb
                ├── login.peb
                ├── profile.peb
                ├── register.peb
                ├── search.peb
                └── seeAllBooks.peb
```


## Programing Routine

### 1. Fetch everything from remote                                                                                                    
  git fetch origin                                                                                                                     
                                                                                                                                       
### 2. Update your local main                                                                                                          
  git checkout main                                                                                                                    
  git pull                                                                                                                             
                                                                                                                                       
### 3. Switch back to your branch and merge in latest main                                                                             
  git checkout Joe                                                                                                                     
  git merge main                                                                                                                       
                                                                                                                                       
  Or the shortcut (without switching branches):                                                                                        
                                                                                                                                       
  git fetch origin main:main                                                                                                           
  git merge main                                                                                                                       
                                                                                                                                       
  Key points:                                                                                                                          
                                                                                                                                       
  - git fetch downloads changes but doesn't modify your working code                                                                   
  - git pull = fetch + merge (only works for the branch you're currently on)                                                           
  - Merging main into your branch keeps you up to date and reduces merge conflicts later                                               


notes:
http://localhost:8082
JDBC URL: jdbc:h2:file:./data/library;AUTO_SERVER=TRUE;
User Name: (leave blank)
Password: (leave blank)

enter:
SELECT * FROM USERS;
run
to see user data in table

sets user to admin role
UPDATE USERS SET "role" = TRUE WHERE USERNAME = 'test';