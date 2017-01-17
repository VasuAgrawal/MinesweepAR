#include <Keypad.h>

const byte ROWS = 9; //four rows
const byte COLS = 9; //three columns
char keys[ROWS][COLS] = {
  {'A', 'J', 'S', 'b', 'k', 't', '2', '!', ')'},
  {'B', 'K', 'T', 'c', 'l', 'u', '3', '@', '-'},
  {'C', 'L', 'U', 'd', 'm', 'v', '4', '#', '_'},
  {'D', 'M', 'V', 'e', 'n', 'w', '5', '$', '='},
  {'E', 'N', 'W', 'f', 'o', 'x', '6', '%', '+'},
  {'F', 'O', 'X', 'g', 'p', 'y', '7', '^', ','},
  {'G', 'P', 'Y', 'h', 'q', 'z', '8', '&', '<'},
  {'H', 'Q', 'Z', 'i', 'r', '0', '9', '*', '.'},
  {'I', 'R', 'a', 'j', 's', '1', ' ', '(', '>'}
};

byte rowPins[ROWS] = {53, 52, 51, 50, 49, 48, 47, 46, 45}; //connect to the row pinouts of the keypad
byte colPins[COLS] = {22, 23, 24, 25, 26, 27 ,28, 29, 30}; //connect to the column pinouts of the keypad

Keypad keypad = Keypad( makeKeymap(keys), rowPins, colPins, ROWS, COLS );

void setup(){
  Serial.begin(115200);
}

// Return 1 if no key detected, 0 if detected.
int decode_key(char key, byte* row_num, byte* col_num) {
  if (key == NO_KEY) return 1;

  for (int row = 0; row < ROWS; ++row) {
    for (int col = 0; col < COLS; ++col) {
      if (keys[row][col] == key) {
        *row_num = row;
        *col_num = col;
        return 0;
      }
    }
  }
}

void loop(){
  char key = keypad.getKey();

  byte row_num;
  byte col_num;

  if (decode_key(key, &row_num, &col_num)) return;

  Serial.print(row_num);
  Serial.print(col_num);
}
