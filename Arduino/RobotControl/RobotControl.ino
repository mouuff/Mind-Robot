#include <WiFi.h>
#include <WiFiUdp.h>
#include <Servo.h>

char ssid[] = "android";
char pass[] = "android42";
int status = WL_IDLE_STATUS;

int localPort = 12345;

char packetBuffer[255];
char lastCommand[255];

char ReplyBuffer[] = "pong";

boolean CameraMoving = false;

Servo ServoHorizontal, ServoVertical;
int ServoAngH = 90;
int ServoAngV = 90;

WiFiUDP Udp;


//set up the pins
int enableMD = 32;
int enableMG = 30;
int MD1 = 28;
int MD2 = 26;
int MG1 = 24;
int MG2 = 22;

void rmove(int dir){
  //1 forward
  //2 right
  //3 backward
  //4 left
  
  switch (dir){
    case (1):
      //avancer
      digitalWrite(MD1,LOW);
      digitalWrite(MD2,HIGH);
      
      digitalWrite(MG1,HIGH);
      digitalWrite(MG2,LOW);
      break;
    case (2):
      //droite
      
      digitalWrite(MD1,LOW);
      digitalWrite(MD2,HIGH);
      
      digitalWrite(MG1,LOW);
      digitalWrite(MG2,HIGH);
      
      break;
    
    case (3):
      //reculer
      digitalWrite(MD1,HIGH);
      digitalWrite(MD2,LOW);
      
      digitalWrite(MG1,LOW);
      digitalWrite(MG2,HIGH);
      break;
      
    case (4):
      //gauche
      digitalWrite(MD1,HIGH);
      digitalWrite(MD2,LOW);
      
      digitalWrite(MG1,HIGH);
      digitalWrite(MG2,LOW);
      break;
     
    default:
      digitalWrite(MD1,LOW);
      digitalWrite(MD2,LOW);
      
      digitalWrite(MG1,LOW);
      digitalWrite(MG2,LOW);
      break;
  }
}

void setup() {
  Serial.begin(9600);
  
  //ServoVertical.attach(0);
  ServoHorizontal.attach(11);
  
  
  pinMode(MD1,OUTPUT);
  pinMode(MD2,OUTPUT);
  pinMode(MG1,OUTPUT);
  pinMode(MD2,OUTPUT);
  pinMode(enableMD,OUTPUT);
  pinMode(enableMG,OUTPUT);
  
  digitalWrite(enableMD,HIGH);
  digitalWrite(enableMG,HIGH);
  
  
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present");
    while(true);
  }
  Serial.println(WiFi.firmwareVersion());
  while ( status != WL_CONNECTED) { 
    Serial.print("Attempting to connect to WPA SSID: ");
    Serial.println(ssid);
    status = WiFi.begin(ssid, pass);
    delay(10000);
  }
  
  Serial.println(WiFi.localIP());
  
  if (Udp.begin(localPort)){
    Serial.println("Now listenning on ");
    Serial.println(localPort);
  }
  else{
    Serial.println("Socket unavaiable");
  }
}

void loop() {
  int packetSize = Udp.parsePacket();
  if (packetSize){
    
    int len = Udp.read(packetBuffer, 255);
    if (len > 0) packetBuffer[len] = 0;
    
    if (packetBuffer[0] == 'C'){
      if (String(packetBuffer).equals("Cstop")){
        Serial.println("[*] Stopped camera");
        CameraMoving = false;
      }
      else{
        Serial.println("[*] Moving Camera");
        CameraMoving = true;
      }
      strcpy(lastCommand,packetBuffer);
    }
    
    if (packetBuffer[0] == 'M'){
      //motor command
      
      if (String(packetBuffer).equals("Mforward")){
        Serial.println("[*] engine forward");
        rmove(1);
      }
      else if (String(packetBuffer).equals("Mright")){
        Serial.println("[*] engine right");
        rmove(2);
      }
      else if (String(packetBuffer).equals("Mbackward")){
        Serial.println("[*] engine backward");
        rmove(3);
      }
      else if (String(packetBuffer).equals("Mleft")){
        Serial.println("[*] engine left");
        rmove(4);
      }
      else{
        rmove(0);
        Serial.println(packetBuffer);
      }
    }
    
    Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    Udp.write(ReplyBuffer);
    Udp.endPacket();
  }
  if (CameraMoving){
    if (String(packetBuffer).equals("Cup")){
        ServoAngV += 1;
      }
      else if (String(packetBuffer).equals("Cdown")){
        ServoAngV -= 1;
      }
      else if (String(packetBuffer).equals("Cright")){
        ServoAngH += 1;
      }
      else if (String(packetBuffer).equals("Cleft")){
        ServoAngH -= 1;
      }
      ServoHorizontal.write(ServoAngH);
      ServoHorizontal.write(ServoAngV);
      
      Serial.println(ServoAngH);
  }
}
