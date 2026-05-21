FROM golang:1.26.2-alpine AS builder

WORKDIR /app
COPY go.mod ./
COPY go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o /exchange-adapter ./cmd/server

FROM alpine:3.22
WORKDIR /app
COPY --from=builder /exchange-adapter /usr/local/bin/exchange-adapter
ENTRYPOINT ["/usr/local/bin/exchange-adapter"]
