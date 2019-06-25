## Getting Your Environment Setup

 1. Run `sudo apt-get install golang-go` to download the Golang CLI
 2. Create a folder that will hold all of your Golang code.
 3. Inside the folder create two folders: `src` and `bin`
   a. `src` contains your source files
   b. `bin` contains the executables of your program(s) 
 4. In your `.bashrc` file, add:
    ```
        export GOPATH=<absolute path to golang folder>
        export PATH=$PATH:$GOPATH/bin
    ```
 5. Either restart your terminal or type in source `.bashrc`
 6. Go to your golang projects folder and in your `src` folder git clone the project.
 7. In order to build the files there are two options. `go build` and `go install` \\
   a. `go build` builds your project in your current directory and creates the executable inside that directory. To remove it, run `go clean`\\
   b. `go install` in your current directory also builds the project but sends it to `GOPATH/bin` so that you can run the executable from anywhere \\
 

The project structure looks something like:
```
PATH TO GOLANG FOLDER/
    bin/
    src/
        discord_bot/
            README.md
            files and folders
            ...
```


## Tips And Resources

### Tips
 * All folders are called "packages" and all packages can be called through import "<package name>"
 * The paths of the packages start from `GOPATH/src`
 * Variables or functions that start with an upper case are public. Lowercase is private. Therefore if you create a printf function, when the file is imported, you cannot access it. Printf can be accessed outside.
 * `func init()` is the constructor and `func main()` is the entry point of the program.

### Resources
 * [List of standard libraries](https://golang.org/pkg/)
 * [Is Golang object oriented?](https://flaviocopes.com/golang-is-go-object-oriented/)
 * [Discord API wrapper](https://github.com/bwmarrin/discordgo)


Will add more later
