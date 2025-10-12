Sudoku App
Welcome to the Sudoku App repository! This document provides all the necessary information to set up the project on your local machine for development and contribution. We welcome and appreciate all contributions.

Prerequisites
Before you begin, ensure you have the following software installed on your system.

Git: Required for version control. Download Git.

Android Studio: The latest stable version is highly recommended. It includes the Android SDK, build tools, and an integrated development environment (IDE). Download Android Studio.

Note: Android Studio comes with a built-in, recommended Java Development Kit (JDK). No separate JDK installation is necessary for most standard projects.

How to Contribute
We follow the standard GitHub "fork and pull" model for contributions. Follow these steps to get your development environment set up and submit your changes.

1. Fork the Repository
   First, create a personal copy (a "fork") of the main repository on your own GitHub account. You can do this by clicking the Fork button at the top-right corner of the main project's GitHub page.

2. Clone Your Fork
   Next, clone your forked repository to your local machine. Open your terminal or command prompt and run the following command, replacing YOUR_USERNAME with your GitHub username.

git clone [https://github.com/subrahmanyasv/Sudoku-App.git](https://github.com/YOUR_USERNAME/Sudoku-App.git)
cd Sudoku-App

3. Configure the Upstream Remote
   To keep your fork in sync with the main repository, you need to add it as a new remote, conventionally named upstream.

# Add the original repository as the 'upstream' remote
git remote add upstream [https://github.com/subrahmanyasv/Sudoku-App.git](https://github.com/subrahmanyasv/Sudoku-App.git)

# Verify that the remotes are set up correctly
git remote -v
# You should see 'origin' (your fork) and 'upstream' (the main repo)

4. Open and Build the Project in Android Studio
   Now you can open the project and ensure it builds correctly.

Launch Android Studio.

On the welcome screen, select "Open an Existing Project" (or go to File > Open).

Navigate to the directory where you cloned your fork and select the root folder.

Android Studio will automatically start a Gradle sync. This process downloads all necessary dependencies. Please wait for it to complete successfully. This might take several minutes on the first run.

5. Create a New Branch
   It is crucial to create a new branch for each feature or bug fix you work on. This keeps your changes organized and isolated from the main branch.

Before creating a new branch, make sure your local main branch is up-to-date with the upstream repository:

git checkout main
git pull upstream main

Now, create your new branch. Use a descriptive name, like feature/add-new-theme or fix/validation-logic-bug.

git checkout -b your-branch-name

6. Make Your Changes
   With the project open and on your new branch, you can now start developing! Write your code, fix bugs, or add new features.

Once your changes are complete, run the app on an emulator or a physical device to ensure everything works as expected and no new issues have been introduced.

7. Commit and Push Your Changes
   When you are ready, commit your changes with a clear and descriptive message.

# Stage your changes
git add .

# Commit your changes
git commit -m "feat: Implement a new dark theme"

# Push the branch to your forked repository ('origin')
git push origin your-branch-name

8. Submit a Pull Request
   The final step is to create a Pull Request (PR) to merge your changes into the main project.

Go to the main repository's page on GitHub (the upstream one, not your fork).

You will see a notification suggesting you create a pull request from your recently pushed branch. Click on it.

Give your pull request a clear title and a detailed description. Explain what you changed, why you changed it, and how it can be tested.

Click "Create Pull Request". Your PR will be reviewed by the project maintainers.

Common Troubleshooting
Gradle Sync Fails (JDK Configuration): If you see an error like Invalid Gradle JDK configuration, the simplest fix is to use the JDK embedded within Android Studio.

Go to File > Settings (or Android Studio > Preferences on macOS).

Navigate to Build, Execution, Deployment > Build Tools > Gradle.

Under Gradle JDK, select the option that includes jbr or Embedded JDK.

Click OK and sync the project again.