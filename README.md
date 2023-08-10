# PID-Tuner
This is an Android app for tuning `PID` parameters via Bluetooth. You just need a Bluetooth module with Arduino to get the value. The Arduino code is given below. Now, you don't need to connect Arduino to your PC every time for changing the value of `KP`, `KI`, and `KD`. This app requires a minimum of Android 8.0 (Oreo).

```c++
const int PACKET_SIZE = 8;
float kp, kd, ki;
void setup()
{
  Serial.begin(9600);
  delay(500);
}
void loop()
{
  if (Serial.available())
  {
    while (Serial.available())
      handleSerial(Serial.peek());
  }
}
void handleSerial(char c)
{
  switch (c)
  {
    case 'P':
      {
        if (Serial.available() < PACKET_SIZE)
          break;
        kp = Serial.parseFloat();
        if (Serial.read() == 13)
        {
          if (Serial.read() == 10)
          {
            //Use kp value from here
            Serial.print("KP: ");
            Serial.println(kp);
          }
          else
            clearSerialBuffer();
        }
        else
          clearSerialBuffer();
      } break;
    case 'D':
      {
        if (Serial.available() < PACKET_SIZE)
          break;
        kd = Serial.parseFloat();
        if (Serial.read() == 13)
        {
          if (Serial.read() == 10)
          {
            //Use kd value from here
            Serial.print("KD: ");
            Serial.println(kd);
          }
          else
            clearSerialBuffer();
        }
        else
          clearSerialBuffer();
      } break;
    case 'I':
      {
        if (Serial.available() < PACKET_SIZE)
          break;
        ki = Serial.parseFloat();
        if (Serial.read() == 13)
        {
          if (Serial.read() == 10)
          {
            //Use ki value from here
            Serial.print("KI: ");
            Serial.println(ki);
          }
          else
            clearSerialBuffer();
        }
        else
          clearSerialBuffer();
      } break;
    default:
      clearSerialBuffer();
      break;
  }
}
void clearSerialBuffer()
{
  while (Serial.available())
    Serial.read();
}
```

**A glimpse of the interface of the Android application.**

<div style="display: flex;">
  <img src=https://github.com/AtiqurRahmanAni/PID-Tuner/assets/56642339/7e2b28ab-c6b4-4278-b9b3-a89fd216d874 height=420px width=200px/>
  <img src=https://github.com/AtiqurRahmanAni/PID-Tuner/assets/56642339/fdd558d0-edb7-454a-9984-4e8fe739a41d width=420 height=200px/>
</div>
