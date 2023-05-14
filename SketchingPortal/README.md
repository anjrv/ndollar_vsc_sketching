# Canvas: the Sketching Portal

This Java Application represents a 3rd Party Software providing a drawing surface (CANVAS), used by a VS Code extenstion on its quest to allow users to draw over it. This canvas has a transparent background, allowing the user to see the codebase found underneath. It makes sure that the IDE is always focused. Also, this canvas filters the events encountered in this environment, and processes them accordingly:
* Mouse Events (such as clicks, selections, scroll) are getting replicated over VS Code (canvas becomes clickable-through)
* Touch Events (e.g. Pen / Finger events) are processed, triggering visual updates over the cavnas (moving UI elements around, adding/removing ink)
* Keyboard Events are not handled, as the IDE is the one taking care of them.

Additionally, this canvas also recognizes what was drawn by the user. The recognized Shapes/Gestures are:
- Line
- Arrow
- Box
- Circle
- Enclosure
- Build
- Run
- Debug
- Find
- Watch
- Clipboard
- Step Over
- Step Into
- Step Out
- Undo
- Redo
- Split Screen

The possibility of adding new shapes/gestures became easier with the new approach that's been used ($N).

## Installation and set-up

In order to install & modify this canvas:
* Find way to access git (e.g. via git cmd)
  - open Command Prompt
  - type in "git --version"
    - if an error appears ('git' is not recognized as an internal or external command, operable program or batch file), download and install the latest version of Git (https://git-scm.com/downloads), and proceed to the next step
    - if the git version is displayed (e.g. git version 2.27.0.windows.1), process to the next step
* Clone the repository
  - within Command Prompt, navigate to the destination folder 
    - cd C:\Users\usrName\Desktop\Sketching_Software, for example
  - get a clone of the repository
    - git clone https://github.com/siggigauti/Sketching-to-Command
    - authenticate with your GitHub Account
* Install IntelliJ IDEA
  - Go to https://www.jetbrains.com/idea/download/ and follow those steps on installing it.
* Set up the Java JDK Version 11
  - https://www.oracle.com/java/technologies/javase-jdk11-downloads.html should have a stable version
  - Open IntelliJ IDEA & set the JDK Version
    - Go to File > Project Structure > (Platform Settings) SKDs. 
    - Here, press the + button close to the top-left of the newly opened window.
    - Press “Add JDK...” and go to the location of JDK 11 that you just installed. 
      - If you didn’t modify the default location, it will be under C:\Program Files\java\jdk-11.0.8. 
    - Select jdk-11.0.8 folder 
    - Press “OK”
    - After this, press “Apply” and “OK” placed on the bottom-right of the window
   - Set Gradle
     - go to File > Setting... > Build, Extension, Deployment > Build Tools > Maven > Gradle 
     - here, make sure that the Gradle JVM is set to Project SDK 11
 * Import the Gradle project
   - locate the "build.gralde" file, found on the project manager view (left side of the IDE)
   - right click on it > Import Gradle Project
 - Update the code accordingly

## Usage

If you are running this manually for the first time, go on the right side of IntelliJ IDEA (there is a vertical menu), on the Gradle section, 
go under: SketchingPortal > Tasks > application > run. Double click it. After these steps are done, the project should be able to be run by pressing the green "Run" icon.

* If your pen is not allowing you to draw, then your settings might need a slight modification.
* In my case, I've done this steps:
* 1. Open the Pen Setting App (in my case "Lenovo Pen Settings")
* 2. Scoll down to a link entitled "Go to Windows Pen Control Panel" (or just look for a section that is similar).
*	   This one will open the "Pen & Windows Ink" section on the Windows Settings
* 3. Find the option "Let me use my pen as a mouse in some desktop apps" and deselect it.
*
* This worked on my case. It might solve your problem as well. :)
* If not, try googling your situation, and include details.

## Documentation

Initial documentation can be found on [Skemman](https://skemman.is/handle/1946/38531).
To be noted that the software was updated to use a new appraoch named [$N](https://depts.washington.edu/acelab/proj/dollar/ndollar.html), by applying some modifications accordingly. More details could be found [here](https://reiknistofnun-my.sharepoint.com/:w:/g/personal/siggigauti_hi_is/EXP_Zl3Z3gdCsXEPiR-dx4wBQJKCgB7JSh5SWmsPjfafFQ?e=i7pzsA).

## Contributing

  * Fork it! 
  * Create your feature branch: `git checkout -b my-new-feature` 
  * Commit your changes: `git commit -am 'Add some feature'` 
  * Push to the branch: `git push origin my-new-feature`
  * Submit a pull request!

## License

This software is distributed under the [New BSD License](https://en.wikipedia.org/wiki/BSD_licenses#3-clause_license_.28.22Revised_BSD_License.22.2C_.22New_BSD_License.22.2C_or_.22Modified_BSD_License.22.29) agreement.

## Known Limitations

Usually, the Recognition software works good. However, for certain shapes such as line or arrow, their features like the Tip and "Pointing Direction" are also cumputed. If these shapes were drawn in an un-intuitive way, the stated features might get wrongly computed (e.g. fixing an arrow with another arrow might result into a wrong Tip/Direction)
