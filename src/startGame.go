package main

// Initial starting point
import (
	"fmt"
	"os"
	"os/signal"
	"syscall"

	"github.com/bwmarrin/discordgo"
)

var (
	token string
)

//command line parse
//program should be invoked with the flag -t and the token
func main() {

	// Get token from environment variable
	token, exists := os.LookupEnv("DISCORDTOKEN")
	if !exists {
		fmt.Println("please add your discord bot token")
		return
	}

	// Create bot
	discord, err := discordgo.New("Bot " + token)
	if err != nil {
		fmt.Println("error creating session:", err)
		return
	}

	// =============== ADD LISTENERS HERE ================
	discord.AddHandler(messageListen)

	// Connect to server specified when creating discord bot
	err = discord.Open()
	if err != nil {
		fmt.Println("error opening websocket:", err)
		return
	}

	handleSignal()

	// Bot is finished
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

// Handles system signals
func handleSignal() {
	sc := make(chan os.Signal, 1)
	signal.Notify(sc, syscall.SIGINT, syscall.SIGTERM, os.Interrupt, os.Kill)
	<-sc
}
