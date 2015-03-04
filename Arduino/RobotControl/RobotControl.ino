#include <WiFi.h>
#include <WiFiUdp.h>
#include <Servo.h>
#include <String.h>

char ssid[] = "android";
char pass[] = "android42";
int status = WL_IDLE_STATUS;

int localPort = 12345;

char packetBuffer[255];

char ReplyBuffer[] = "pong";


Servo ServoHorizontal, ServoVertical;
int ServoAngH = 90;
int ServoAngV = 90;

WiFiUDP Udp;
WiFiServer server(80);

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
    //all this thing can be modified
    //depends how you connected the engine
    case (1):
      //avancer
      digitalWrite(MD1,HIGH);
      digitalWrite(MD2,LOW);
      
      digitalWrite(MG1,HIGH);
      digitalWrite(MG2,LOW);
      break;
    case (2):
      //droite
      
      digitalWrite(MD1,HIGH);
      digitalWrite(MD2,LOW);
      
      digitalWrite(MG1,LOW);
      digitalWrite(MG2,HIGH);
      
      break;
    
    case (3):
      //reculer
      digitalWrite(MD1,LOW);
      digitalWrite(MD2,HIGH);
      
      digitalWrite(MG1,LOW);
      digitalWrite(MG2,HIGH);
      break;
      
    case (4):
      //gauche
      digitalWrite(MD1,LOW);
      digitalWrite(MD2,HIGH);
      
      digitalWrite(MG1,HIGH);
      digitalWrite(MG2,LOW);
      break;
    
    case (5):
      //droite tout droit
      digitalWrite(MD1,LOW);
      digitalWrite(MD2,LOW);
      
      digitalWrite(MG1,HIGH);
      digitalWrite(MG2,LOW);
      break;
      
    case (6):
      //gauche tout droit
      digitalWrite(MD1,HIGH);
      digitalWrite(MD2,LOW);
      
      digitalWrite(MG1,LOW);
      digitalWrite(MG2,LOW);
      break;
    
    case (7):
      //gauche arriere
      digitalWrite(MD1,LOW);
      digitalWrite(MD2,HIGH);
      
      digitalWrite(MG1,LOW);
      digitalWrite(MG2,LOW);
      break;
    
    case (8):
      //droite arriere
      digitalWrite(MD1,LOW);
      digitalWrite(MD2,LOW);
      
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
  
  ServoVertical.attach(15);
  ServoHorizontal.attach(14);
  //servos must be powered by 3.3v on arduino
  //otherwise it could crash the wifi shield
  
  ServoHorizontal.write(ServoAngH);
  ServoVertical.write(ServoAngV);
  
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
  
 
  
  server.begin();
    
  if (Udp.begin(localPort)){
    Serial.println("Now listenning on ");
    Serial.println(localPort);
  }
  else{
    Serial.println("Socket unavaiable");
  }
}

void loop() {
  WiFiClient client = server.available();
  if (client) {
    Serial.println("new client");
    
    
    // an http request ends with a blank line
    boolean currentLineIsBlank = true;
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        Serial.write(c);
        
        
        if (c == '\n' && currentLineIsBlank) {
          // send a standard http response header
          client.println("HTTP/1.1 200 OK");
          client.println("Content-Type: text/html");
          client.println();
          client.println("<!DOCTYPE HTML>");
          client.println("<html>");
          client.println("<body bgcolor=\"#000000\">");

          client.println("<font color=\"red\">Reception des capteurs:");
          
          client.print(String(analogRead(8)));
          
          client.println("</font>");
          
          client.println("</html>");
          
          client.stop();
          Serial.write("Stopped client");
        }
        
        if (c == '\n') {
          // you're starting a new line
          currentLineIsBlank = true;
        } 
        else if (c != '\r') {
          // you've gotten a character on the current line
          currentLineIsBlank = false;
        }
      }
    }
  }
  
  
  int packetSize = Udp.parsePacket();
  if (packetSize){
    
    int len = Udp.read(packetBuffer, 255);
    if (len > 0) packetBuffer[len] = 0;
    
    if (packetBuffer[0] == 'C'){
      String buff = String(packetBuffer);
      int Hang = buff.substring(buff.indexOf(":")+1).toInt();
      int Vang = buff.substring(1, buff.indexOf(":")).toInt();
      
      ServoHorizontal.write(Hang);
      ServoVertical.write(Vang);
      
      Serial.println(Vang);
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
      else if (String(packetBuffer).equals("Mforward-left")){
        Serial.println("[*] engine left");
        rmove(5);
      }
      else if (String(packetBuffer).equals("Mforward-right")){
        Serial.println("[*] engine left");
        rmove(6);
      }
      else if (String(packetBuffer).equals("Mbackward-left")){
        Serial.println("[*] engine left");
        rmove(7);
      }
      else if (String(packetBuffer).equals("Mbackward-right")){
        Serial.println("[*] engine left");
        rmove(8);
      }
      else{
        //CStop
        rmove(0);
        Serial.println(packetBuffer);
      }
    }
    
    Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    Udp.write(ReplyBuffer);
    Udp.endPacket();
  }
}
