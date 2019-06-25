// Initial starting point
package main
import (
    "github.com/bwmarrin/discordgo"
    "flag"
    "fmt"
    "os"
    "os/signal"
    "syscall"
)

var (
    Token string
)

//command line parse
//program should be invoked with the flag -t and the token
func init() {
    flag.StringVar(&Token, "t", "", "Bot Token")
    flag.Parse()
}

func main() {
    discord, err := discordgo.New("Bot " + Token)
    if err != nil {
        fmt.Println("error creating session:", err)
        return
    }

    //register function below as a handler 
    discord.AddHandler(messageListen)

    err = discord.Open()
    if err != nil {
        fmt.Println("error opening websocket:", err)
        return
    }

    fmt.Println("Bot successfully booted!")
    sc := make(chan os.Signal, 1)
    signal.Notify(sc, syscall.SIGINT, syscall.SIGTERM, os.Interrupt, os.Kill)
    <-sc

    discord.Close()
}

//Message listener that responds to "ping" with "Pong!"
func messageListen(s *discordgo.Session, m *discordgo.MessageCreate) {
    fmt.Println("Message detected")
    if m.Author.ID == s.State.User.ID {
        fmt.Println("Author ID is me. Aborting.")
        return
    }

    if m.Content == "ping" {
        s.ChannelMessageSend(m.ChannelID, "Pong!")
    }

}
