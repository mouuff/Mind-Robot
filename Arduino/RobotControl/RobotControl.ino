#include <WiFi.h>
#include <WiFiUdp.h>


char ssid[] = "Freebox-779353";
char pass[] = "acervandam-coutimini!4-undum-coactiv4";
int status = WL_IDLE_STATUS;

int localPort = 12345;

char packetBuffer[255];
char ReplyBuffer[] = "pong";


WiFiUDP Udp;


//set up the pins
int enableMD = 11;
int enableMG = 12;
int MD1 = 2;
int MD2 = 3;
int MG1 = 8;
int MG2 = 13;

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
      digitalWrite(MD1,HIGH);
      digitalWrite(MD2,LOW);
      
      digitalWrite(MG1,HIGH);
      digitalWrite(MG2,LOW);
      break;
    
    case (3):
      //reculer
      digitalWrite(MD1,LOW);
      digitalWrite(MD2,HIGH);
      
      digitalWrite(MG1,HIGH);
      digitalWrite(MG2,LOW);
      break;
      
    case (4):
      //gauche
      digitalWrite(MD1,LOW);
      digitalWrite(MD2,HIGH);
      
      digitalWrite(MG1,LOW);
      digitalWrite(MG2,HIGH);
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
    
    Serial.println(packetBuffer);

    Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    Udp.write(ReplyBuffer);
    Udp.endPacket();
  }
}
