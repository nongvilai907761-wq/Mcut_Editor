cmake_minimum_required(VERSION 3.22.1)
project("video_engine_pro")

add_library(
        video_engine_pro
        SHARED
        native-lib.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        video_engine_pro
        ${log-lib}
        android
)
