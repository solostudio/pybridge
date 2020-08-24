"""
 This file is executed when the Python interpreter is started.
 Use this file to configure all your necessary python code.

"""

import json
import sys
import traceback

import zmq
import numpy as np
import cv2


print("Starting 'bootstrap.py'")
print(sys.path)


def router(args):
    """
    Defines the router function that routes by function name.

    :param args: JSON arguments
    :return: JSON response
    """
    values = json.loads(args)

    try:
        function = routes[values.get('function')]

        status = 'ok'
        res = function(values)
    except KeyError:
        status = 'fail'
        res = None

    return json.dumps({
        'status': status,
        'result': res,
    })


def greet(args):
    """Simple function that greets someone."""
    return 'This is %s' % args['name']


def add(args):
    """Simple function to add two numbers."""
    return args['a'] + args['b']


def mul(args):
    """Simple function to multiply two numbers."""
    return args['a'] * args['b']


def test_import(args):
    """import"""
    msg = "OK"

    try:
        # Test ZMQ
        context = zmq.Context()
        socket = context.socket(zmq.REP)
        socket.bind("tcp://*:6666")
        print("BYYD: Pass ZMQ, Start ZMQ Server")

        # np
        print(np.array([1,2,3]))
        print("Pass Numpy Test")
        
        # OpenCV
        import cv2
        video_path = "rtsp://192.168.0.151:554/stream2"
        print("Start %s" % video_path)
        cap = cv2.VideoCapture(video_path)
        print("Connected %s" % video_path)
        print("BYYD: Pass OpenCV, Open RTSP Stream")

        print("Pass All Test")
    except Exception as e:
        msg = '%s'  % e
        print(msg)
        traceback.print_exc()

    return 'Import Status %s' % msg

routes = {
    'greet': greet,
    'add': add,
    'mul': mul,
    'test_import': test_import,
}
