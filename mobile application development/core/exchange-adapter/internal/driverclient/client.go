package driverclient

import (
	"fmt"
	"io"
	"os"
	"strings"
	"sync"
	"time"
)

const (
	readBufferSize = 32 * 1024
	ioTimeout      = 2 * time.Second
)

type Client struct {
	devicePath string
	mu         sync.Mutex
}

func New(devicePath string) *Client {
	return &Client{devicePath: devicePath}
}

func (c *Client) Execute(command string) (string, error) {
	c.mu.Lock()
	defer c.mu.Unlock()

	file, err := os.OpenFile(c.devicePath, os.O_RDWR, 0)
	if err != nil {
		return "", fmt.Errorf("open device: %w", err)
	}
	defer file.Close()

	if err := file.SetDeadline(time.Now().Add(ioTimeout)); err != nil {
		// Character devices like /dev/exchange may not support deadlines.
		if !strings.Contains(err.Error(), "does not support deadline") {
			return "", fmt.Errorf("set deadline: %w", err)
		}
	}

	if _, err := file.WriteString(command + "\n"); err != nil {
		return "", fmt.Errorf("write command: %w", err)
	}

	buffer := make([]byte, readBufferSize)
	n, err := file.Read(buffer)
	if err != nil && err != io.EOF {
		return "", fmt.Errorf("read response: %w", err)
	}

	return strings.TrimSpace(string(buffer[:n])), nil
}
