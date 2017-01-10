MinesweepAR
======

To build this code, please do the following.

1. Ensure you have OpenCV, g++, and CMake installed.
1. Download and build the
(apriltags)[https://github.com/VasuAgrawal/apriltags-cpp.git] library.
1. Install the libapriltags.a and header files to /usr/local/lib and
/usr/local/include respectively.
1. Run `./build_file` in this directory.
1. Run `./overlay`.

Roughly speaking, it should look something like this:

'''
$ git clone https://github.com/VasuAgrawal/apriltags-cpp.git
$ cd apriltags-cpp
$ mkdir build && cd build
$ cmake -DCMAKE_BUILD_TYPE=Release ..
$ make -j4
$ sudo cp libapriltags.a /usr/local/lib
$ sudo mkdir -p /usr/local/include/apriltags
$ sudo cp ../src/*.h /usr/local/include/apriltags
$ ./build_file
$ ./overlay
```
