# Git Branch Comparator
A lightweight Java library for detecting files that have been independently modified in both a local Git branch (branchB) and a remote GitHub branch (branchA) since their last common ancestor (merge base). Designed to work without fetching or syncing remote branches, using only Git CLI and GitHub's HTTP API.

---
## Features

- Determines the **merge base** commit between the branches  
- Uses **Git CLI** for local history and file diffing  
- Uses **GitHub REST API** to gather remote commit/file changes  
- Filters and returns files **changed independently in both branches**  
- Excludes rolled-back changes (files that appear unchanged)

## Prerequisites
Before you begin ensure you have the following installed: 
- **Java 8** or higher
- **Maven** for building the project 
- **Git CLI** installed on your local machine
- GitHub **Personal Access Token** for accessing GitHub API

## Installation
Clone the repository: 
```bash
git clone https://github.com/radepejanvic/git-branch-comparator.git
cd git-branch-comparator
```
Build the JAR file: 
```bash 
mvn clean package
```

---

## Usage example

```java
BranchComparator comparator = new BranchComparator(
        new GitCommandExecutor("localRepoPath", new CommandUtils()),
        new GitHubApiClient(HttpClient.newHttpClient(), "repo", "owner", "accessToken")
);

List<String> modifiedFiles = comparator.compareModifiedFiles("branchB", "branchA");
```
| Parameter      | Description                                                      |
|----------------|------------------------------------------------------------------|
| `localRepoPath`| Path to the local Git repository where Git commands will run.    |
| `CommandUtils` | Utility class to execute Git commands in the local repository.   |
| `HttpClient`   | HTTP client for making requests to the GitHub API.               |
| `repo`         | Name of the GitHub repository.                                   |
| `owner`        | GitHub username or organization name that owns the repository.   |
| `accessToken`  | Personal access token for authenticating GitHub API requests.    |
| `branchA`      | The name of the remote branch |
| `branchB`      | The name of the local branch created from `branchA`. |

---

## Error Handling
- `GitCommandException`: Thrown if there's an error while executing Git commands (e.g., invalid branch names or issues with the Git CLI).
- `GitHubApiException`: Thrown if there's an issue with the GitHub API (e.g., invalid access token, API rate limits exceeded, or connection issues).

--- 

## Running tests
To run all unit tests defined in the library to verify it before usage. 
```bash
mvn test
```

