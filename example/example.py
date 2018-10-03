import socket
import sys
import threading
import time


class ChatServer(threading.Thread):

    def __init__(self, port):
        threading.Thread.__init__(self)

        self.clients = []
        self.port = port
        self.running = True
        self.server = None

    def add_client(self, host, port):
        localhost_addresses = ['localhost', '127.0.0.1']

        if host in localhost_addresses and port == self.port:
            return

        try:
            client_address = (host, port)

            client = socket.socket()
            client.connect(client_address)
            self.clients.append(client)

            print 'Connected with %s:%d.' % client_address

            thread = threading.Thread(target=self.handle_conversation,
                                      args=(client, client_address,))
            thread.daemon = True
            thread.start()

        except socket.error:
            print 'Could not connect with %s:%d.' % client_address

    def handle_conversation(self, client, address):
        while self.running:
            try:
                data = client.recv(1024)

                if not data:
                    break

                sys.stdout.write('\n[%s:%s] %s\n>>> ' %
                                 (address[0], address[1], data))
                sys.stdout.flush()

            except socket.error:
                break

        client.close()

        sys.stdout.write('\nClient %s:%s disconnected.\n>>> ' % address)
        sys.stdout.flush()

    def join(self, timeout=None):
        self.running = False

        # Since the server is still listening to accept a new connection, the
        # while-loop will not break when the running condition becomes False.
        # Therefore, by creating a new connection, the server can accept the
        # connection and eventually close down.
        socket.socket().connect(('', self.port))

        threading.Thread.join(self)

    def list_connected_clients(self):
        closed_clients = []

        print '%-4s\t%11s\t%s' % ('id:', 'IP Address', 'Port')

        for count, client in enumerate(self.clients):
            try:
                address = client.getpeername()
                print '%-4d\t%11s\t%d' % (count, address[0], address[1])
            except socket.error:
                closed_clients.append(client)

        for client in closed_clients:
            self.clients.remove(client)

    def run(self):
        self.server = socket.socket()
        self.server.bind(('', self.port))
        self.server.listen(10)

        print 'Chat server is listening on localhost, port %d.' % self.port

        while self.running:
            client, address = self.server.accept()

            if self.running:
                self.clients.append(client)

                sys.stdout.write('\nClient %s:%s joined.\n>>> ' % address)
                sys.stdout.flush()

                thread = threading.Thread(target=self.handle_conversation,
                                          args=(client, address,))
                thread.daemon = True
                thread.start()
            else:
                client.close()

        self.server.close()

    def send_message(self, client_id, message):
        for i in range(len(self.clients)):
            if i == client_id:
                self.clients[i].send(message)
                return

        print 'There is no recepient with the specified id.'

    def terminate(self, client_id):
        for i in range(len(self.clients)):
            if i == client_id:
                self.clients[i].close()
                return

        print 'There is no client connected with the specified id.'


def display_help_menu():
    with open('help-menu.txt', 'r') as help_menu:
        print help_menu.read()


def get_ip_address():
    try:
        connection = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        connection.connect(('8.8.8.8', 80))
        print connection.getsockname()[0]
        connection.close()
    except socket.error:
        print '127.0.0.1'


def main():
    if len(sys.argv) == 1:
        print 'Usage: %s <port>' % sys.argv[0]
        return

    try:
        port = int(sys.argv[1])
        if port <= 1024 or port > 66535:
            sys.stderr.write('%d is an invalid port number.\n' % port)
            return
    except ValueError:
        sys.stderr.write('"%s" is not a valid port number.\n' % sys.argv[1])
        return

    server = ChatServer(port)
    server.start()

    time.sleep(0.1)

    while server.running:
        response = raw_input('>>> ')

        if response.lower().startswith('connect'):
            response = response.split(' ')
            if len(response) == 3:
                try:
                    server.add_client(response[1], int(response[2]))
                except ValueError:
                    print '"%s" is not a valid port.' % response[2]
        elif response.lower() == 'exit':
            server.join()
        elif response.lower() == 'help':
            display_help_menu()
        elif response.lower() == 'list':
            server.list_connected_clients()
        elif response.lower() == 'myip':
            get_ip_address()
        elif response.lower() == 'myport':
            print port
        elif response.lower().startswith('send'):
            response = response.split(' ')
            if len(response) >= 3:
                try:
                    server.send_message(int(response[1]), ' '.join(response[2:]))
                except ValueError:
                    print '"%s" is not a valid client id.' % response[1]
        elif response.lower().startswith('terminate'):
            response = response.split(' ')
            if len(response) == 2:
                try:
                    server.terminate(int(response[1]))
                except ValueError:
                    print '"%s" is not a valid client id.' % response[2]
        else:
            print '"%s" is an invalid command.' % response


if __name__ == '__main__':
    main()
