export class WS {
  constructor(private wsAddress: string, private onReceive: (msg: any) => void, private onConnected: () => void) {
  }

  private connection?: WebSocket
  private stash: any[] = []

  connect = () => {
    const ws = new WebSocket(this.wsAddress)
    ws.onclose = () => {
      console.log('connection closed')
      this.reconnect()
    }
    ws.onopen = () => {
      console.log('connection opened')
      this.connection = ws
      this.stash.forEach(this.sendMessage)
      this.stash = []
      this.onConnected()
    }
    ws.onerror = console.error.bind(console)
    ws.onmessage = event => this.onReceive(JSON.parse(event.data))
  }

  private reconnect = (): void => {
    this.connection = undefined
    setTimeout(this.connect, 5000) // TODO implement with fib numbers
  }

  sendMessage = (msg: any) => {
    if (this.connection == undefined) return this.stash.push(msg)
    this.connection!.send(JSON.stringify(msg))
  }
}
