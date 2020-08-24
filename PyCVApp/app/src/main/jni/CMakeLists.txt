cmake_minimum_required(VERSION 3.4.1)

# 添加第三方的库
# include
set(INC_DIR ../../../../../python-for-android/libs/arm64-v8a/Include)

# lib
set(LINK_DIR ../../../../../python-for-android/libs/arm64-v8a)

# 包含头文件和库目录
include_directories(${INC_DIR})
link_directories(${LINK_DIR})

find_library( log-lib log )

# 主库
add_library(pybridge SHARED pybridge.c)

# quoteapi
target_link_libraries(pybridge python3.7m ${log-lib})
