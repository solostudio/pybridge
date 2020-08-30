import os
import datetime
import zmq

def singleton(cls, *args, **kwargs):
    instances = {}
    def _wrapper():
        if cls not in instances:
            instances[cls] = cls(*args, **kwargs)
        return instances[cls]
    return _wrapper

@singleton
class ZMessager(object):
    socket = None

    def connect(self, url="tcp://127.0.0.1:6666"):
        context = zmq.Context()
        self.socket = context.socket(zmq.REQ)
        self.socket.connect(url)

    def close(self):
        self.socket.close()

    def gen_msg(self, code, msg):
        return '%s' % {'code': code, 'msg': msg}

    def send_msg(self, code, msg):
        if not self.socket:
            self.connect()

        to_send = self.gen_msg(code, msg)
        self.socket.send_string(to_send)
        response = self.socket.recv()
        if response != b'true':
            print('Received: %s' % response)

        return to_send

if __name__ == '__main__':
    # debug
    ZMessager().connect(url="tcp://192.168.0.210:6666")
    ZMessager().send_msg(0, '这是测试消息')





