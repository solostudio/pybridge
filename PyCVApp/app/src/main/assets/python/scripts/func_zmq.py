import os
import datetime
import zmq


def start_zmq_test():
    context = zmq.Context()
    socket = context.socket(zmq.REP)
    socket.bind("tcp://*:8888")
    print("BYYD: Start ZMQ Server")

if __name__ == '__main__':
    # debug
    start_zmq_test()
