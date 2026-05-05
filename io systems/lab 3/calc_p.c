#include <stdio.h>
void main(){
    int hh = 0b00010100;
    int lh = 0b00000000;
    int ht = 0b00011010;
    int lt = 0b00000011;

    int p = 0b00110001;

    int data[5] = {hh, lh, ht, lt, p}; 
    
    int humidity = data[0];
    int temperature = data[2];

    printf("Humidity: %d%%\n", humidity);
    printf("Temperature: %d°C\n", temperature);
}