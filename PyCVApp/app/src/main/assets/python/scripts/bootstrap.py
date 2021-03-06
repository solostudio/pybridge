"""
 This file is executed when the Python interpreter is started.
 Use this file to configure all your necessary python code.

"""

import json
import sys
import traceback

from func_video_inspect_001 import func_001, config_nor_001
from func_zmq import ZMessager

print("Starting 'bootstrap.py'")
print(sys.path)
ZMessager().send_msg(0, 'Python Env Start')


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


def start_func_001(args):
    """Simple function that greets someone."""

    # 参数
    video_path = args['link']

    # 参数定义
    # low_hsv1, high_hsv1, len_s, size_local, areas, upper_catch, upper_add, down_catch, sec, cunter_number \
    config_all = config_nor_001(colors="r", distance="2", len_s=7, size_local=(50, 650), sec=3)

    # 启动数据流分析
    print("BYYD: Start Scan %s" % video_path)
    res = func_001(video_path, config_all)
    return res

def test(args):
    """import"""
    msg = "PASS"

    # 参数
    video_path = args['link']
    p1 = args['p1']
    p2 = args['p2']

    try:
        # OpenCV
        import cv2
        print(cv2.getBuildInformation())
        video_path = video_path
        print("Start %s" % video_path)
        cap = cv2.VideoCapture(video_path)
        print("Connected %s" % video_path)
        print("BYYD: OpenCV Test PASS")

        # Test ZMQ
        import zmq
        context = zmq.Context()
        socket = context.socket(zmq.REP)
        socket.bind("tcp://*:8666")
        print("BYYD: ZMQ Test PASS")

        # np Test
        import numpy as np
        print(np.array([1, 2, 3]))
        print("BYYD: NUMPY Test PASS")

    except Exception as e:
        msg = '%s' % e
        print(msg)
        traceback.print_exc()

    print("BYYD: All Tests PASS")
    return 'Python Libs Tests (Numpy, OpenCV, ZeroMQ)： %s' % msg


routes = {
    'start_func_001': start_func_001,
    'test': test
}
