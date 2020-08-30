import cv2
import numpy as np
import datetime

from func_zmq import ZMessager

"""
提取图中的红色部分
"""

"""
import tensorflow.compat.v1 as tf
import tensorflow.compat.v1.keras.backend as K
graph = tf.get_default_graph()

# keras使用gpu代码
config = tf.ConfigProto()
config.gpu_options.allow_growth = True
sess = tf.Session(config=config)
K.set_session(sess)
"""

# 计算色差变化趋势
def catch(y):
    number_k = 0
    for i in range(len(y) - 1):
        number_k += (y[i + 1] - y[i]) / y[i]
    return np.average(number_k), y[-1]


# 参数选择
def config_nor_001(colors, distance, len_s, size_local, sec):
    """
    初始化参数配置函数
    colors:颜色  red:r, green:g, blue:b, gray:gr, black:bk, yellow:y, white: w, 其他:0
    distance: 检测距离/m  0-1:0，1-5:1, 5-15:2, 15以上:3
    len_s: 检测框的长度
    size_local: 说明数字显示位置
    sec: 图片检测间隔/s
    ：:return
    low_hsv1, 颜色检测上限
    high_hsv1,颜色检测上限
    len_s, 检测框的长度
    size_local， 说明数字显示位置
    areas,最小检测面积
    upper_catch,
    upper_add,
    down_catch,
    sec,
    cunter_number
    """

    """
    # 基于1920*1080上的视频数据处理
    """
    cunter_number = 5  # 计算色差变化率的趋势个数。包括b的K值个数的总数

    # 色差
    if colors == "r":
        low_hsv1 = np.array([0, 43, 46])  # 红色检测
        high_hsv1 = np.array([10, 255, 255])
    elif colors == "w":
        low_hsv1 = np.array([0, 0, 221])  # 白色边界
        high_hsv1 = np.array([180, 30, 255])
    elif colors == "g":
        low_hsv1 = np.array([35, 43, 46])  # 绿色边界
        high_hsv1 = np.array([77, 255, 255])
    elif colors == "b":
        low_hsv1 = np.array([100, 43, 46])  # 蓝色边界
        high_hsv1 = np.array([124, 255, 255])
    elif colors == "gr":
        low_hsv1 = np.array([0, 0, 46])  # 灰色边界
        high_hsv1 = np.array([180, 43, 220])
    elif colors == "y":
        low_hsv1 = np.array([26, 43, 46])  # 黄色边界
        high_hsv1 = np.array([34, 255, 255])
    elif colors == "bk":
        low_hsv1 = np.array([0, 0, 0])  # 黑色边界
        high_hsv1 = np.array([180, 255, 46])
    else:
        low_hsv1 = np.array([0, 43, 46])  # 默认是红色边界
        high_hsv1 = np.array([10, 255, 255])

    # 内部系数
    areas = 500  # 初始化数据
    upper_catch = 0.65
    upper_add = 0.2
    down_catch = -0.38

    if distance == "0":  # 近距离0-1米
        areas = 800  # 轮廓检测面积大小
        upper_catch = 0.36  # 初始化上升趋势比率
        upper_add = 0.2  # 色差上升容忍比率
        down_catch = -0.38  # 初始化色差下降趋势容忍比率

    elif distance == "1":
        areas = 500
        upper_catch = 0.65
        upper_add = 0.2
        down_catch = -0.38

    elif distance == "2":
        areas = 40
        upper_catch = 1
        upper_add = 0.2
        down_catch = -0.38

    len_s = 7
    size_local = (50, 550)
    sec = 3  # 图片检测间隔/s
    return low_hsv1, high_hsv1, len_s, size_local, areas, upper_catch, upper_add, down_catch, sec, cunter_number


def func_001(inputvideo, config_s):
    state = -1
    msg = 'unknown'

    low_hsv1, high_hsv1, len_s, size_local, areas, upper_catch, upper_add, down_catch, sec, cunter_number = config_s

    #print(cv2.getBuildInformation())

    print("Start %s" % inputvideo)
    cap = cv2.VideoCapture(inputvideo, cv2.CAP_FFMPEG)
    print("Connected %s" % inputvideo)

    # 记录初始时间状态
    nowtime = datetime.datetime.now()  # 获取当前时间
    print("Start status: %s, time: %s" % (cap.isOpened(), nowtime))
    if not cap.isOpened():
        msg = '打开视频失败'

    list_red = []
    list_b = []
    # global upper_catch, down_catch
    while cap.isOpened():
        #print("Start to get frame")
        ret, frame = cap.read()
        #print("got a frame")

        # 灰度图
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        ret, binary = cv2.threshold(gray, 0, 250, cv2.THRESH_BINARY | cv2.THRESH_TRIANGLE)

        # 高斯模糊
        guess = cv2.GaussianBlur(binary, (5, 5), 0)

        # 获取红色区域
        hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)

        result_get1 = cv2.inRange(hsv, lowerb=low_hsv1, upperb=high_hsv1)
        # result_get2 = cv2.inRange(hsv, lowerb=low_hsv2, upperb=high_hsv2)
        result_get = result_get1  # +result_get2

        # 进行腐蚀计算
        kernel_er = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (3, 3))
        kernel_di = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (1, 1))
        result_get_d = cv2.erode(result_get, kernel_er)
        result_get_d2 = cv2.erode(result_get_d, kernel_di)
        # result_get_d2 = cv2.erode(result_get, kernel2)

        now_times = datetime.datetime.now() - nowtime  # 时间差
        time_delate = int(now_times.total_seconds())  # 时间规整

        if (time_delate + 1) % sec == 0:  # 检测图片的间隔时间
            nowtime = datetime.datetime.now()  # 重置时间
            if len(list_red) < cunter_number:  # 红色个数的列表，在此，连续使用5组连续的红色颜色的变化
                list_red.append(np.sum(result_get_d2 == 255))  # 统计白色像素数量
                if len(list_red) >= 1 and np.sum(result_get_d2 == 255) < list_red[-1] / 2.5:
                    list_red.pop(0)  # 为了防止人为流动而检测到的突变状况
            else:
                list_red.append(np.sum(result_get_d2 == 255))  # 保持总数在5个数值
                list_red.pop(0)

        #  判断趋势，拟合函数，看趋势

        # result_get = get_red(frame)  # 获取红色区域
        all_red, rows_list_left, rows_list_right = [], [], []
        rows_list_top, rows_list_bottom = [], []
        red_number = []  # 红色区域的大小

        # 计算轮廓
        contours, hierarchy = cv2.findContours(result_get, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)
        counters = []
        contours_normal_counters = []
        for i in range(len(contours)):
            # print(cv2.contourArea(contours[i]))
            if cv2.contourArea(contours[i]) >= areas:  # 统计面积
                counters.append(contours[i])  # 计算初始有效面积

        # 画框：
        rect_left, rect_right = [], []
        if counters != []:
            for i in counters:
                # print(i)
                # print(i[:,-1])  # 列，行
                rec_1 = min(i[:, -1][:, 0])
                rec_2 = min(i[:, -1][:, 1])
                rect_left.append((rec_1, rec_2))  # 左上坐标
                rec_3 = max(i[:, -1][:, 0])
                rec_4 = max(i[:, -1][:, 1])
                rect_right.append((rec_3, rec_4))  # 右上坐标

            for i, j in zip(rect_left, rect_right):
                cv2.line(frame, (i[0], i[1]), (i[0] + len_s, i[1]), (20, 255, 0), 3, 88)
                cv2.line(frame, (i[0], i[1]), (i[0], i[1] + len_s), (20, 255, 0), 3, 88)
                cv2.line(frame, (j[0], j[1]), (j[0] - len_s, j[1]), (20, 255, 0), 3, 88)
                cv2.line(frame, (j[0], j[1]), (j[0], j[1] - len_s), (20, 255, 0), 3, 88)

        # 字体
        state = 0
        font = cv2.FONT_HERSHEY_SIMPLEX
        if len(list_red) == cunter_number:  # 满足连续五组数据进行B=k值的计算
            a = 0
            b, number_red = catch(list_red)  # 计算趋势

            # 自适应上升趋势的设定upper_catch
            if len(list_b) <= 24 * int(cunter_number / 2) * sec:  # 稳定n个周期
                list_b.append(b)
            else:
                list_b.pop(0)
            # print("B 列表", list(set(list_b)))

            # print()      # b自适应检索后确定
            # print(b, upper_catch)
            # print(type(upper_catch))
            if 10 > b > upper_catch:  # 呈上升趋势
                state = 1

                msg = "%s 检测到漏水.........." % datetime.datetime.now()
                print(msg)
                ZMessager().send_msg(1, msg)

                # cv2.putText(frame, "Water Leakage area detexted: Alarm reported", size_local, font, 0.5,
                #             (0, 0, 255), 1)
            elif b < down_catch:  # 呈下降趋势
                print("Water Leakage area Repaired")
            else:
                upper_catch = max(list_b) + upper_add  # 自适应阈值，根据稳定状态下的最大K值进行限定，并增加upper_add增加自适应的弹性值

                if max(list_b) < 0:  # 如果一直在处理，upper_catch处于下降状态，我们则会去一个绝对值，辅助适应值
                    upper_catch = abs(max(list_b) - upper_add)
                    down_catch = -1.5 * abs(max(list_b) - upper_add)
                # cv2.putText(frame, "Water Leakage area detextion...", size_local, font, 0.5, (0, 255, 20), 1)
                print("Water Leakage area detextion...")
        else:
            # cv2.putText(frame, "Water Leakage area detextion...", size_local, font, 0.5, (25, 255, 20), 1)
            print("Water Leakage area detextion...", str(datetime.datetime.now()))

    cap.release()
    print("BYYD CV2 Release")
    print("Water:", state)

    return ZMessager().send_msg(state, '%s' % msg)


if __name__ == '__main__':
    #
    video_path = "rtsp://192.168.0.151:554/stream2"
    #video_path = "rtmp://47.100.8.76:5656/live/demo"

    # 参数定义
    # low_hsv1, high_hsv1, len_s, size_local, areas, upper_catch, upper_add, down_catch, sec, cunter_number \
    config_all = config_nor_001(colors="r", distance="2", len_s=7, size_local=(50, 650), sec=3)

    # 启动数据流分析
    print("BYYD: Start Scan %s" % video_path)
    func_001(video_path, config_all)
