# MinesweepAR

![Demo Board](Images/demo.jpg)

MinesweepAR is an augmented reality version of the classic game MinesweepAR,
played on your cell phone on a gigantic 12' x 12' physical board! Even better,
you can play together with your friends at the same time! An Android app is used
to overlay the game board over the camera feed, and a server manages the game
state for all connected GUIs and phones.

## Quickstart

1. Start webserver on computer via `Java Server/runServer.sh`
1. Build physical board
1. Connect board wires in a keyboard matrix circuit
1. Connect keyboard matrix to Arduino Mega running
   `board/single_click/single_click.ino`
1. Connect Arduino Mega to computer running `board/serial_read.py`
1. Start GUI at `GUI/gui.py`
1. Compile and load android app at `android-client`
1. Run Android App on phone(s)
1. Play the game!

## Components

### Physical Board

The game board is constructed out of 4'x8'x1/2" plywood and 2x4 lumber. Each
plywood sheet is cut in half to make 4'x4' sheets. These sheets are then 
strengthened by adding 2x4 lumber running **perpendicular to the grain of the 
plywood sheet**. In each of these tiles a 3x3 grid of 6" squares is cut out. 
These squares will becomes the slots for the buttons. Each of these tiles is 
then also tiled in a 3x3 grid, creating a 12'x12' board with 81 slots for 
buttons. The buttons are constructed from a combination of the square cutouts, 
extra plywood, foil tape, foam, and wire. The wire is then run along the
length of the board to a keyboard circuit.

### Keyboard Circuit

The circuit is a simple keyboard circuit; each of the 81 buttons is a single
switch in a 9x9 scanning keyboard matrix. A segment of a breadboard power rail
is used as each of the row / colums. Each of the buttons is then wired up to one
of the pairs of row / col (i.e. (0, 0), (0, 1), ... (9, 8), (9, 9)). A wire is
then drawn from each of the rails (both row and col) into an Arduino Mega.
Unfortunately, an Arduino Uno does not have enough pins to be able to read from
such a large circuit; 18 input pins are needed, which is a few more than the
Arduino Uno has.

Code for the Arduino can be found in `board/single_click/single_click.ino`. This
code uses the Keyboard library, and scans the keyboard to figure out which row /
col is pressed. Only a single keypress can be detected at once. The row / col is
then printed over serial to be processed by a python script running on a
computer, such as a laptop or a raspberry pi.

### Webserver

A webserver was written in Java to manage the game state. It can be started by
simply running `Java Server/runServer.sh`. It opens a server on port 8000. All
programs below will need to be configured to connect to the IP address of the
computer that the server is running on. The server manages game logic, board
inputs, and connected clients. The server is the only component of the system
which has any state; all other components (GUI, Android App, etc), are all
completely stateless, and base their displays and behavior on the most recent
game state message received from the webserver.

### Serial Reading

Note that all of these programs require `PySerial`, with `PyGame` optional for
the self tester. All programs run in Python 3.

There are a few programs that have been written to read and verify the board
state and functionality. The one necessary to run the actual game,
`board/serial_read.py`, reads the row / col serial message and uploads it to the
server. There's additionally a `board/self_test.py` file which reads row / col
and shows pressed buttons on a GUI. This is useful for debugging broken
connections, of which we had many. Finally, `board/fake_read.py` can be used to
generate single button press messages to send to the server without a board
connected, for testing purposes, i.e `./fake_read.py 0 1`.

### Debug GUI

To provide an audience view, as well as to visualize the entire game state, a
game GUI was written. This GUI also listens to the state from the server, so
ensure the correct IP address is entered. Clicking on tiles in the GUI will open
them, which is a desirable feature in the event that a certain button on the
board stops working. Right clicking will flag tiles. The GUI requires PyGame and
can be found at `GUI/gui.py`.

### Android App

The Android App uses C++ called via NDK to process the camera stream. The
received game state determines exactly which tile image is overlaid over which
April Tag. The android app and all related source can be found within the
`android-client` folder, and can be compiled with Android Studio. Ensure that
the correct server IP address is loaded into the app.
