""" Sample workflow processor """
""" This is a sample application running on a compute node which receives activites from the sawdust agent, executes a computation, then publishes back the result"""

import socket
import threading

from ActivityHandler import ActivityHandler

activity_handler = ActivityHandler()

def handle_connection(conn, addr):
    """Handles communication wiht a connected client."""
    print(f"New connection from {addr}")

    while True:
        try:
            data = conn.recv(1024)
            if not data:
                break
            print(f"Received: {data.decode()}")
            conn.sendall(b"Message received!")
            activity_handler.handle_STARTING_activity(data)
        except ConnectionResetError:
            print(f"Client {addr} disconnected unexpectedly.")
            break
    
    conn.close()
    print(f"Connection with {addr} closed.")

def server():
    host = "127.0.0.1"
    port = 9001

    # create a socket
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((host, port))
        s.listen()
        print(f"Server listening on {host}:{port}")

        while True:
            conn, addr = s.accept() # accept new connection
            client_thread = threading.Thread(target=handle_connection, args=(conn, addr))
            client_thread.daemon = True
            client_thread.start()

if __name__ == "__main__":
    server()