cmake_minimum_required(VERSION 3.4.1)

# 添加第三方的库
# PYTHON_DIR
set(PYTHON_DIR ../../../../../python-for-android/libs/arm64-v8a)

# 包含头文件和库目录
include_directories(${PYTHON_DIR}/Include)
link_directories(${PYTHON_DIR})

# Log lib
find_library(log-lib log)

# 主库
add_library(pybridge SHARED pybridge.c)

# share libs
target_link_libraries(pybridge python3.7m ${log-lib})
